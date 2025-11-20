package chat.user;

import chat.mediator.ChatMediator;
import chat.ui.ChatWindow;

public class ConcreteChatUser implements ChatUser {

    private String name;
    private ChatMediator mediator;
    private ChatWindow ui;  // referencia a la ventana gráfica

    public ConcreteChatUser(String name, ChatMediator mediator) {
        this.name = name;
        this.mediator = mediator;
    }

    public void setUI(ChatWindow ui) {
        this.ui = ui;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void receiveMessage(String from, String message) {
        if (ui != null) {
            ui.showIncomingMessage(from, message);
        } else {
            System.out.println("[" + name + "] Mensaje de " + from + ": " + message);
        }
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
