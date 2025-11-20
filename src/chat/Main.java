package chat;

import chat.mediator.ChatMediator;
import chat.mediator.ConcreteChatMediator;
import chat.user.ConcreteChatUser;

public class Main {
    public static void main(String[] args) {

        ChatMediator mediator = new ConcreteChatMediator();

        ConcreteChatUser u1 = new ConcreteChatUser("Alejandro", mediator);
        ConcreteChatUser u2 = new ConcreteChatUser("Javier", mediator);

        mediator.registerUser(u1);
        mediator.registerUser(u2);

        u1.sendPrivateMessage("Javier", "Hola Javier, soy Alejandro.");
        u2.sendPrivateMessage("Alejandro", "Buenas Ale, todo bien.");
    }
}
