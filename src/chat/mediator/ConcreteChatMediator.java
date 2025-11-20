package chat.mediator;

import chat.user.ChatUser;

import java.util.*;

public class ConcreteChatMediator implements ChatMediator {

    private Map<String, ChatUser> users = new HashMap<>();
    private Map<String, List<ChatUser>> groups = new HashMap<>();

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

    @Override
    public void sendGroupMessage(String from, String groupName, String message) {
        List<ChatUser> members = groups.get(groupName);
        if (members == null) {
            System.out.println("[MEDIATOR] Grupo no encontrado: " + groupName);
            return;
        }

        for (ChatUser u : members) {
            if (!u.getName().equals(from)) {
                u.receiveMessage(from + " (grupo " + groupName + ")", message);
            }
        }
    }

    @Override
    public void createGroup(String groupName, List<ChatUser> members) {
        groups.put(groupName, members);
        System.out.println("[MEDIATOR] Grupo creado: " + groupName);
    }

    @Override
    public List<String> getConnectedUserNames() {
        return new ArrayList<>(users.keySet());
    }

    @Override
    public List<String> getGroupNames() {
        return new ArrayList<>(groups.keySet());
    }
}
