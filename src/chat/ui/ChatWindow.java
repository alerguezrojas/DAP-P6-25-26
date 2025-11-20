package chat.ui;

import chat.mediator.ChatMediator;
import chat.user.ConcreteChatUser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ChatWindow extends JFrame {

    private final ConcreteChatUser user;
    private final ChatMediator mediator;

    // UI principal
    private JPanel panelChat;        // contenedor de burbujas
    private JScrollPane scrollPane;
    private JTextField fieldMessage;

    private JButton btnDestino;
    private JLabel lblDestino;
    private JLabel lblEstado;

    private String destinoSeleccionado = null;
    private boolean hasUnread = false;
    private String baseTitle;

    public ChatWindow(ConcreteChatUser user) {
        this.user = user;
        this.mediator = user.getMediator();

        user.setUI(this);

        baseTitle = "Chat - " + user.getName();
        setTitle(baseTitle);
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ==========================
        // PANEL NORTE (estado y destino)
        // ==========================
        JPanel panelNorth = new JPanel(new GridLayout(3, 1));
        lblEstado = new JLabel("Estado: ONLINE");
        lblEstado.setForeground(new Color(0, 128, 0));
        lblDestino = new JLabel("Destino actual: — Ninguno —");
        btnDestino = new JButton("Seleccionar destino");

        btnDestino.addActionListener(e -> seleccionarDestino());

        panelNorth.add(lblEstado);
        panelNorth.add(lblDestino);
        panelNorth.add(btnDestino);
        add(panelNorth, BorderLayout.NORTH);

        // ==========================
        // PANEL CENTRAL (burbujas)
        // ==========================
        panelChat = new JPanel();
        panelChat.setLayout(new BoxLayout(panelChat, BoxLayout.Y_AXIS));
        panelChat.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(panelChat);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        // ==========================
        // PANEL SUR (input mensaje)
        // ==========================
        JPanel panelSouth = new JPanel(new BorderLayout());
        fieldMessage = new JTextField();
        JButton btnEnviar = new JButton("Enviar");

        btnEnviar.addActionListener(e -> enviarMensaje());

        panelSouth.add(fieldMessage, BorderLayout.CENTER);
        panelSouth.add(btnEnviar, BorderLayout.EAST);
        add(panelSouth, BorderLayout.SOUTH);

        // ==========================
        // LISTENERS DE VENTANA
        // ==========================
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                mediator.setUserOnline(user.getName(), true);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                mediator.setUserOnline(user.getName(), false);
                // nada más, se cierra
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {
                limpiarNotificaciones();
            }
        });

        // Cargar historial
        loadHistoryFromFile();

        // Marcar estado inicial online
        mediator.setUserOnline(user.getName(), true);

        setVisible(true);
    }

    // ==========================
    // SELECCIÓN DE DESTINO
    // ==========================
    private void seleccionarDestino() {
        List<String> opciones = new ArrayList<>();

        // Agregar usuarios excepto yo mismo
        for (String u : mediator.getConnectedUserNames()) {
            if (!u.equals(user.getName())) {
                opciones.add(u);
            }
        }

        // Agregar grupos
        opciones.addAll(mediator.getGroupNames());

        if (opciones.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay usuarios ni grupos disponibles como destino.",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

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

    // ==========================
    // ENVÍO DE MENSAJE
    // ==========================
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

        // ¿Es un grupo o un usuario?
        if (mediator.getGroupNames().contains(destinoSeleccionado)) {
            mediator.sendGroupMessage(user.getName(), destinoSeleccionado, msg);
            addMessageBubble("Yo → Grupo " + destinoSeleccionado, msg, true);
        } else {
            mediator.sendPrivateMessage(user.getName(), destinoSeleccionado, msg);
            addMessageBubble("Yo → " + destinoSeleccionado, msg, true);
        }

        fieldMessage.setText("");
    }

    // ==========================
    // RECEPCIÓN DE MENSAJES
    // ==========================
    public void showIncomingMessage(String from, String msg) {
        addMessageBubble(from, msg, true);

        // Notificación visual si la ventana no está enfocada
        if (!isFocused()) {
            hasUnread = true;
            setTitle(baseTitle + " (Nuevo mensaje)");
            getRootPane().setBorder(new LineBorder(Color.ORANGE, 3));
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private void limpiarNotificaciones() {
        if (hasUnread) {
            hasUnread = false;
            setTitle(baseTitle);
            getRootPane().setBorder(null);
        }
    }

    // ==========================
    // BURBUJAS DE CHAT (estilo WhatsApp)
    // ==========================
    private void addMessageBubble(String from, String msg, boolean saveHistory) {
        JPanel bubble = new JPanel(new BorderLayout());
        bubble.setBorder(new EmptyBorder(5, 10, 5, 10));

        // Texto con HTML para permitir saltos de línea si hace falta
        JLabel label = new JLabel("<html><b>" + escapeHtml(from) + ":</b> " +
                escapeHtml(msg) + "</html>");

        label.setOpaque(true);
        label.setBorder(new EmptyBorder(5, 10, 5, 10));

        boolean isOwnMessage = from.startsWith("Yo") || from.startsWith(user.getName());

        if (isOwnMessage) {
            label.setBackground(new Color(220, 248, 198)); // verde claro
        } else if (from.equalsIgnoreCase("Sistema")) {
            label.setBackground(new Color(230, 230, 230)); // gris claro para sistema
        } else {
            label.setBackground(new Color(240, 240, 240)); // gris para otros
        }

        JPanel line = new JPanel();
        line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
        line.setOpaque(false);

        if (isOwnMessage) {
            line.add(Box.createHorizontalGlue());
            line.add(label);
        } else {
            line.add(label);
            line.add(Box.createHorizontalGlue());
        }

        panelChat.add(line);
        panelChat.revalidate();
        panelChat.repaint();

        // Scroll al final
        SwingUtilities.invokeLater(() ->
                scrollPane.getVerticalScrollBar().setValue(
                        scrollPane.getVerticalScrollBar().getMaximum()
                )
        );

        if (saveHistory) {
            appendHistoryToFile(from, msg);
        }
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    // ==========================
    // HISTORIAL (persistente)
    // ==========================
    private File getHistoryFile() {
        return new File("history_" + user.getName() + ".txt");
    }

    private void appendHistoryToFile(String from, String msg) {
        File f = getHistoryFile();
        try (FileWriter fw = new FileWriter(f, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println(from + "\t" + msg);
        } catch (IOException e) {
            System.err.println("Error escribiendo historial de " + user.getName() + ": " + e.getMessage());
        }
    }

    private void loadHistoryFromFile() {
        File f = getHistoryFile();
        if (!f.exists()) return;

        try (FileReader fr = new FileReader(f);
             BufferedReader br = new BufferedReader(fr)) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t", 2);
                if (parts.length == 2) {
                    String from = parts[0];
                    String msg = parts[1];
                    // Importante: NO guardar de nuevo en historial (saveHistory = false)
                    addMessageBubble(from, msg, false);
                }
            }

        } catch (IOException e) {
            System.err.println("Error leyendo historial de " + user.getName() + ": " + e.getMessage());
        }
    }
}
