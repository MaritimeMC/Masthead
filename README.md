# Masthead

**Masthead** is a standalone Java orchestration service that manages the lifecycle of containerised Minecraft game servers at runtime — automatically provisioning, monitoring, and decommissioning instances in response to player demand. It was written to replace the manual overhead of server administration for a multi-server Minecraft network and operates with no human intervention once running.

The design pattern is architecturally analogous to a primitive container scheduler: it mirrors the core concern of systems like Kubernetes or Nomad — maintaining a declared steady-state of running workloads across a pool of compute resources — built from first principles for a specific domain.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Masthead JVM                         │
│                                                             │
│  ┌─────────────────┐    ┌──────────────────────────────┐   │
│  │ ServerGroupMgr  │───▶│    MinecraftServerManager    │   │
│  │  (group config) │    │  (lifecycle state machine)   │   │
│  └─────────────────┘    └──────────┬───────────────────┘   │
│                                    │                        │
│          ┌─────────────────────────┼──────────────────┐    │
│          ▼                         ▼                   ▼    │
│  ┌───────────────┐   ┌─────────────────────┐  ┌──────────┐ │
│  │ServerGroup    │   │ PterodactylController│  │ Redis    │ │
│  │Monitor(s)     │   │  (panel API client)  │  │ pub/sub  │ │
│  │ (per group)   │   └────────┬────────────┘  └──────────┘ │
│  └───────┬───────┘            │                             │
│          │              ┌─────▼──────┐                      │
│          │              │ WebSocket  │ ◀── real-time panel  │
│          │              │ Listener   │     status events    │
│          │              └────────────┘                      │
│          ▼                                                   │
│  ┌───────────────┐   ┌──────────────┐                       │
│  │ ThreadPool    │   │  MongoDB     │                       │
│  │ (ASYNC_POOL)  │   │  (persistence│                       │
│  └───────────────┘   └──────────────┘                       │
└─────────────────────────────────────────────────────────────┘
              │
              ▼  Pterodactyl Panel REST API
    ┌─────────────────────────────┐
    │  Container infrastructure   │
    │  (Docker-backed game servers│
    │   created/deleted on demand)│
    └─────────────────────────────┘
```

On startup, Masthead runs a reconciliation pass (`removeLoneServers`) that compares the database against the panel, deleting orphaned containers and purging stale records. It then loads all known servers into an in-memory cache, attaches a WebSocket listener per server for real-time utilisation events, and starts a `ServerGroupMonitor` for every defined group. Each monitor runs on a `Timer` that fires every 10 seconds and re-evaluates whether the group's server count satisfies its configured thresholds.

---

## Scientifically Relevant Features

**Feedback-driven autoscaling via threshold logic.** `ServerGroupMonitor.doCreations()` implements a demand-responsive provisioning algorithm that maintains minimum idle-server buffers and triggers creation when capacity utilisation crosses a defined threshold (75% in the present implementation). This is structurally equivalent to adaptive sampling strategies used in computational chemistry — for instance, adaptive replica-exchange molecular dynamics, where the number of active replicas is adjusted based on the current sampling density of configuration space. Both problems share the underlying structure: a monitored quantity (player load / sampling coverage) drives decisions about allocating additional compute resources.

**State machine modelling with persistence.** Each server transitions through a defined set of states — `CREATING → STARTING → IDLE → RUNNING → STOPPING → DEAD` — with every transition written atomically to MongoDB. In cheminformatics pipelines, compound processing workflows are often modelled as state machines (e.g., a molecule progressing through screening, docking, and ADMET filtering stages), with persistent state enabling recovery from partial failure. Masthead applies the same principle: on restart, the reconciliation pass re-derives system state from the database rather than assuming a clean slate.

**Event-driven architecture with decoupled listeners.** `WebSocketListener` subscribes to Pterodactyl's real-time panel WebSocket and raises internal `ServerStatusChangeEvent` objects through a custom `ListenerManager`. This event bus pattern separates status detection from response logic — a pattern common in laboratory automation and instrument control software (e.g., LIMS event triggers), where hardware state changes should propagate to processing logic without tight coupling.

**Redis as a shared state medium for distributed services.** Masthead publishes player-count updates and server-ready events onto Redis channels using the `masthead:*` namespace, consumed by the proxy layer. This is functionally identical to message-broker patterns in large-scale cheminformatics platforms (e.g., ChEMBL's data update pipelines), where services communicate state changes asynchronously rather than polling shared databases. The Redis `JedisPool` is configured with a keepalive `ping` timer (`RedisDatabase.ping()`) to detect and handle broken connections without crashing the service.

**Thread pool execution for non-blocking I/O.** `MinecraftServerManager.createServer()` and `deleteServer()` both accept an `async` flag; when set, work is submitted to `ThreadPool.ASYNC_POOL` (a cached thread pool via `Executors.newCachedThreadPool()`). This ensures that panel API calls — which involve multiple sequential HTTP requests to Pterodactyl — do not block the main orchestration loop. This pattern directly maps to practices in data engineering pipelines where slow I/O operations (external API calls, database writes) are offloaded to worker threads to preserve throughput on the coordination thread.

---

## Project-Specific Features

**Pterodactyl panel integration via Pterodactyl4J.** `PterodactylController` wraps the Pterodactyl application and client APIs to programmatically create game server containers with full environment configuration. A single `createServer()` call sets the Docker image (derived from the `SupportedMinecraftVersion` enum), allocates RAM, disk, and I/O limits, injects environment variables (`MINECRAFT_VERSION`, `SERVER_NAME`, `GROUP_REPO_NAME`, `AC`, `MAX_PLAYERS`), and sets `startOnCompletion(true)` — starting the container immediately upon panel-side installation. A `ProcessTimer` logs wall-clock duration for each creation and deletion, providing lightweight profiling without an external tracing framework.

**Credential validation at startup.** Before any server operations, `PterodactylController.ensureCredentials()` tests both the application and client API keys independently. If either fails a `LoginException`, the process exits with a fatal log rather than proceeding into a partially authenticated state. This fail-fast design prevents silent misconfigurations from causing partial lifecycle operations.

**Garbage collection of lone servers.** `removeLoneServers()` performs a bidirectional consistency check: MongoDB records without a matching panel entry are deleted from the database; panel entries without a matching database record (and not prefixed with `MHIGNORE`) are deleted from the panel. This prevents resource leaks from servers that were created but not cleanly registered, or registered but externally deleted.

**Disposable-server semantics.** Groups flagged `isDisposable()` follow a different lifecycle: servers are created fresh on demand and deleted when the utilisation ratio drops below the scaling threshold, rather than being kept permanently registered. `cleanDisposableServers()` purges these at startup, ensuring no stale disposable containers carry over across restarts.

**Collision-free server naming.** `generateServerName()` appends a 5-digit random numeric suffix to the group name, retrying until a name not already present in MongoDB is found. While simple, this is a deterministic uniqueness guarantee without requiring a central counter or distributed lock.

**Concurrent cache safety with explicit locking.** The server cache (`Set<MinecraftServer> serverCache`) is protected by a `synchronized (SERVER_CACHE_LOCK)` block wherever it is read or written from multiple code paths. This is a deliberate and correct approach given that cache mutations can originate from both the orchestration timer and from async `ThreadPool` tasks triggered by WebSocket events.

**GitHub Actions CI dispatch.** The core dependency is versioned and its release triggers a `repository_dispatch` event to downstream repositories via a custom GitHub Actions workflow, ensuring dependent plugins always receive bumped dependency versions without manual coordination.

---

## Technology Stack

- **Language:** Java 11+
- **Build:** Gradle
- **Panel API:** Pterodactyl4J (application + client)
- **Database:** MongoDB (server/group persistence), Redis (cross-service pub/sub, player count)
- **Concurrency:** `java.util.concurrent` — `Executors.newCachedThreadPool()`, `java.util.Timer`
- **Serialisation:** Gson
- **Utilities:** Lombok (`@RequiredArgsConstructor`, `@SneakyThrows`, `@Getter`, `@Setter`)
