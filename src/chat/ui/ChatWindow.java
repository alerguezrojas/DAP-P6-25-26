package chat.ui;

import chat.user.ConcreteChatUser;

import javax.swing.*;
import java.awt.*;

public class ChatWindow extends JFrame {

    private ConcreteChatUser user;
    private ChatController controller;

    private JTextArea areaChat;
    private JTextField fieldMessage;
    private JComboBox<String> comboUsuarios;
    private JComboBox<String> comboGrupos;

    public ChatWindow(ConcreteChatUser user) {
        this.user = user;
        this.controller = new ChatController(user);
        user.setUI(this);

        setTitle("Chat - Usuario: " + user.getName());
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ==== PANEL SUPERIOR ====
        JPanel panelNorth = new JPanel(new GridLayout(2, 2));

        comboUsuarios = new JComboBox<>();
        comboGrupos = new JComboBox<>();

        refreshLists();

        panelNorth.add(new JLabel("Usuarios:"));
        panelNorth.add(comboUsuarios);
        panelNorth.add(new JLabel("Grupos:"));
        panelNorth.add(comboGrupos);

        add(panelNorth, BorderLayout.NORTH);

        // ==== PANEL CENTRAL ====
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setFont(new Font("Arial", Font.PLAIN, 14));
        add(new JScrollPane(areaChat), BorderLayout.CENTER);

        // ==== PANEL INFERIOR ====
        JPanel panelSouth = new JPanel(new BorderLayout());
        fieldMessage = new JTextField();
        JButton btnSend = new JButton("Enviar");

        btnSend.addActionListener(e -> sendMessage());

        panelSouth.add(fieldMessage, BorderLayout.CENTER);
        panelSouth.add(btnSend, BorderLayout.EAST);
        add(panelSouth, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void sendMessage() {
        String msg = fieldMessage.getText().trim();
        if (msg.isEmpty()) return;

        String selectedUser = (String) comboUsuarios.getSelectedItem();
        String selectedGroup = (String) comboGrupos.getSelectedItem();

        if (selectedUser != null) {
            controller.sendPrivate(selectedUser, msg);
            showIncomingMessage("Yo → " + selectedUser, msg);
        }

        if (selectedGroup != null) {
            controller.sendGroup(selectedGroup, msg);
            showIncomingMessage("Yo → Grupo " + selectedGroup, msg);
        }

        fieldMessage.setText("");
    }

    public void showIncomingMessage(String from, String msg) {
        areaChat.append(from + ": " + msg + "\n");
    }

    public void refreshLists() {
        comboUsuarios.removeAllItems();
        comboGrupos.removeAllItems();

        controller.getMediator().getConnectedUserNames()
                .forEach(comboUsuarios::addItem);

        controller.getMediator().getGroupNames()
                .forEach(comboGrupos::addItem);
    }
}
