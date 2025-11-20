package chat;

import chat.mediator.ChatMediator;
import chat.mediator.ConcreteChatMediator;
import chat.user.ConcreteChatUser;
import chat.ui.ChatWindow;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {

        ChatMediator mediator = new ConcreteChatMediator();

        ConcreteChatUser u1 = new ConcreteChatUser("Alejandro", mediator);
        ConcreteChatUser u2 = new ConcreteChatUser("Javier", mediator);
        ConcreteChatUser u3 = new ConcreteChatUser("Aitor", mediator);

        mediator.registerUser(u1);
        mediator.registerUser(u2);
        mediator.registerUser(u3);

        mediator.createGroup("GrupoULL", Arrays.asList(u1, u2, u3));

        new ChatWindow(u1);
        new ChatWindow(u2);
        new ChatWindow(u3);
    }
}
