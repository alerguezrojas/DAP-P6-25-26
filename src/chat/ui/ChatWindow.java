package chat.ui;

import chat.mediator.ChatMediator;
import chat.user.ConcreteChatUser;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChatWindow extends JFrame {

    private ConcreteChatUser user;
    private ChatMediator mediator;

    private JTextArea areaChat;
    private JTextField fieldMessage;

    private JButton btnDestino;
    private JLabel lblDestino;

    private String destinoSeleccionado = null;   // destino (usuario o grupo)

    public ChatWindow(ConcreteChatUser user) {
        this.user = user;
        this.mediator = user.getMediator();
        user.setUI(this);

        setTitle("Chat - Usuario: " + user.getName());
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ======== PANEL NORTE ========
        JPanel panelNorth = new JPanel(new GridLayout(2, 1));

        btnDestino = new JButton("Seleccionar destino");
        lblDestino = new JLabel("Destino actual: — Ninguno —");

        btnDestino.addActionListener(e -> seleccionarDestino());

        panelNorth.add(btnDestino);
        panelNorth.add(lblDestino);
        add(panelNorth, BorderLayout.NORTH);

        // ======== ÁREA DE CHAT ========
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setFont(new Font("Arial", Font.PLAIN, 14));
        add(new JScrollPane(areaChat), BorderLayout.CENTER);

        // ======== PANEL DE ENVÍO ========
        JPanel panelSouth = new JPanel(new BorderLayout());
        fieldMessage = new JTextField();
        JButton btnEnviar = new JButton("Enviar");

        btnEnviar.addActionListener(e -> enviarMensaje());

        panelSouth.add(fieldMessage, BorderLayout.CENTER);
        panelSouth.add(btnEnviar, BorderLayout.EAST);

        add(panelSouth, BorderLayout.SOUTH);

        setVisible(true);
    }

    // ======================================
    // SELECCIÓN DE DESTINO (usuarios y grupos)
    // ======================================
    private void seleccionarDestino() {

        List<String> opciones = new ArrayList<>();

        // Agregar usuarios excepto yo mismo
        for (String u : mediator.getConnectedUserNames()) {
            if (!u.equals(user.getName()))
                opciones.add(u);
        }

        // Agregar grupos
        opciones.addAll(mediator.getGroupNames());

        String[] arrOpciones = opciones.toArray(new String[0]);

        String seleccionado = (String) JOptionPane.showInputDialog(
                this,
                "Selecciona un usuario o grupo:",
                "Elegir destino",
                JOptionPane.PLAIN_MESSAGE,
                null,
                arrOpciones,
                null
        );

        if (seleccionado != null) {
            destinoSeleccionado = seleccionado;
            lblDestino.setText("Destino actual: " + destinoSeleccionado);
        }
    }


    // ============================
    // ENVÍO DE MENSAJE
    // ============================
    private void enviarMensaje() {
        if (destinoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Debes seleccionar un destino primero.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String msg = fieldMessage.getText().trim();
        if (msg.isEmpty()) return;

        // es grupo o es usuario?
        if (mediator.getGroupNames().contains(destinoSeleccionado)) {
            mediator.sendGroupMessage(user.getName(), destinoSeleccionado, msg);
            showIncomingMessage("Yo → Grupo " + destinoSeleccionado, msg);

        } else { // es usuario
            mediator.sendPrivateMessage(user.getName(), destinoSeleccionado, msg);
            showIncomingMessage("Yo → " + destinoSeleccionado, msg);
        }

        fieldMessage.setText("");
    }

    public void showIncomingMessage(String from, String msg) {
        areaChat.append(from + ": " + msg + "\n");
    }
}
