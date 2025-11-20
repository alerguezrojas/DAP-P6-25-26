package chat.mediator;

import chat.user.ChatUser;

import java.util.HashMap;
import java.util.Map;

public class ConcreteChatMediator implements ChatMediator {

    private Map<String, ChatUser> users = new HashMap<>();

    @Override
    public void registerUser(ChatUser user) {
        users.put(user.getName(), user);
        System.out.println("[MEDIATOR] Registrado usuario: " + user.getName());
    }

    @Override
    public void sendPrivateMessage(String from, String to, String message) {
        ChatUser receiver = users.get(to);
        if (receiver != null) {
            receiver.receiveMessage(from, message);
        } else {
            System.out.println("[MEDIATOR] Usuario destino no encontrado: " + to);
        }
    }
}
