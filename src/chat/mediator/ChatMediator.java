package chat.mediator;

import chat.user.ChatUser;

public interface ChatMediator {
    void registerUser(ChatUser user);
    void sendPrivateMessage(String from, String to, String message);
}
