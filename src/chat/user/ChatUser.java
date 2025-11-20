package chat.user;

public interface ChatUser {
    String getName();
    void receiveMessage(String from, String message);

    // Indicador “está escribiendo…”
    void receiveTypingNotification(String from);
}
