package chat.ui;

import chat.user.ConcreteChatUser;
import chat.mediator.ChatMediator;

public class ChatController {

    private ConcreteChatUser user;

    public ChatController(ConcreteChatUser user) {
        this.user = user;
    }

    public void sendPrivate(String toUser, String msg) {
        user.sendPrivateMessage(toUser, msg);
    }

    public void sendGroup(String group, String msg) {
        user.sendGroupMessage(group, msg);
    }

    public ChatMediator getMediator() {
        return user.getMediator();
    }
}
