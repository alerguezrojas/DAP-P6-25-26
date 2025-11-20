package chat.mediator;

import chat.user.ChatUser;
import java.util.List;

public interface ChatMediator {
    void registerUser(ChatUser user);
    void sendPrivateMessage(String from, String to, String message);

    // NUEVO: soporte de grupos
    void sendGroupMessage(String from, String groupName, String message);
    void createGroup(String groupName, List<ChatUser> members);

    // NUEVO: utilidades para la interfaz gráfica futura
    List<String> getConnectedUserNames();
    List<String> getGroupNames();
}
