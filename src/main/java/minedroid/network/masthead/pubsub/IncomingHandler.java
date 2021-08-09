package minedroid.network.masthead.pubsub;

public interface IncomingHandler {

    /**
     * @return The channel which this message is received from.
     */
    String getChannel();

    /**
     * Handle this message accordingly based on the data within the message.
     *
     * @param data The data within the message.
     */
    void handle(String data);

}
