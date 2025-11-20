package chat.user;

import chat.mediator.ChatMediator;

public class ConcreteChatUser implements ChatUser {

    private String name;
    private ChatMediator mediator;

    public ConcreteChatUser(String name, ChatMediator mediator) {
        this.name = name;
        this.mediator = mediator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void receiveMessage(String from, String message) {
        System.out.println("[" + name + "] Mensaje de " + from + ": " + message);
    }

    public void sendPrivateMessage(String toUser, String msg) {
        mediator.sendPrivateMessage(name, toUser, msg);
    }

    public void sendGroupMessage(String groupName, String msg) {
        mediator.sendGroupMessage(name, groupName, msg);
    }

    public ChatMediator getMediator() {
        return mediator;
    }
}
