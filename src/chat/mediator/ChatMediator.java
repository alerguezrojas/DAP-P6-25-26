package chat.mediator;

import chat.user.ChatUser;

import java.util.List;
import java.util.Map;

public interface ChatMediator {

    // Gestión de usuarios y grupos
    void registerUser(ChatUser user);
    void createGroup(String groupName, List<ChatUser> members);

    // Mensajes
    void sendPrivateMessage(String from, String to, String message);
    void sendGroupMessage(String from, String groupName, String message);

    // Información para la UI
    List<String> getConnectedUserNames();
    List<String> getGroupNames();

    // Estados ONLINE/OFFLINE
    void setUserOnline(String userName, boolean online);
    boolean isUserOnline(String userName);
    Map<String, Boolean> getUserStatus();

    // Log global
    List<String> getLog();

    // Notificación de “está escribiendo…”
    void notifyTyping(String from, String toOrGroup);
}
