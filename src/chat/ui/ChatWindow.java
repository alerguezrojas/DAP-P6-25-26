package chat.ui;

import chat.mediator.ChatMediator;
import chat.user.ConcreteChatUser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Indicador “está escribiendo…”
    private JLabel lblTyping;
    private Timer typingTimer;

    // Conversaciones independientes por destino (usuario o grupo)
    private static class MessageRecord {
        String convKey;   // clave de conversación (destino)
        String from;
        String msg;
        String time;
        MessageRecord(String convKey, String from, String msg, String time) {
            this.convKey = convKey;
            this.from = from;
            this.msg = msg;
            this.time = time;
        }
    }

    private Map<String, List<MessageRecord>> conversaciones = new HashMap<>();

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
        // PANEL NORTE (estado, destino, typing)
        // ==========================
        JPanel panelNorth = new JPanel(new GridLayout(4, 1));
        lblEstado = new JLabel("Estado: ONLINE");
        lblEstado.setForeground(new Color(0, 128, 0));
        lblDestino = new JLabel("Destino actual: — Ninguno —");
        btnDestino = new JButton("Seleccionar destino");
        btnDestino.addActionListener(e -> seleccionarDestino());

        lblTyping = new JLabel("");
        lblTyping.setFont(new Font("Arial", Font.ITALIC, 12));
        lblTyping.setForeground(Color.GRAY);

        panelNorth.add(lblEstado);
        panelNorth.add(lblDestino);
        panelNorth.add(btnDestino);
        panelNorth.add(lblTyping);
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

        // Enviar notificación de “escribiendo…”
        fieldMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                enviarNotificacionEscribiendo();
            }
        });

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
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {
                limpiarNotificaciones();
            }
        });

        // Timer para limpiar indicador typing
        typingTimer = new Timer(1200, e -> lblTyping.setText(""));
        typingTimer.setRepeats(false);

        // Cargar historial (por conversación)
        loadHistoryFromFile();

        // Marcar estado inicial online
        mediator.setUserOnline(user.getName(), true);

        setVisible(true);
    }

    // ==========================
    // INDICADOR “ESTÁ ESCRIBIENDO…”
    // ==========================
    private void enviarNotificacionEscribiendo() {
        if (destinoSeleccionado == null) return;
        mediator.notifyTyping(user.getName(), destinoSeleccionado);
    }

    public void showTypingIndicator(String from) {
        lblTyping.setText(from + " está escribiendo...");
        typingTimer.restart();
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
            // Recargar conversación solo de este destino
            mostrarConversacion(destinoSeleccionado);
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

        String hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        // ¿Es un grupo o un usuario?
        if (mediator.getGroupNames().contains(destinoSeleccionado)) {
            mediator.sendGroupMessage(user.getName(), destinoSeleccionado, msg);
            addMessageToConversation(destinoSeleccionado,
                    "Yo → Grupo " + destinoSeleccionado, msg, hora, true);
        } else {
            mediator.sendPrivateMessage(user.getName(), destinoSeleccionado, msg);
            addMessageToConversation(destinoSeleccionado,
                    "Yo → " + destinoSeleccionado, msg, hora, true);
        }

        fieldMessage.setText("");
    }

    // ==========================
    // RECEPCIÓN DE MENSAJES
    // ==========================
    public void showIncomingMessage(String from, String msg) {
        // Determinar convKey (destino de la conversación) según si es grupo o privado
        String convKey;
        String displayFrom = from;

        int idx = from.indexOf("(grupo ");
        if (idx != -1 && from.endsWith(")")) {
            int start = idx + "(grupo ".length();
            int end = from.lastIndexOf(')');
            String groupName = from.substring(start, end).trim();
            convKey = groupName; // la conversación se agrupa por nombre de grupo
        } else {
            convKey = from; // privado: conversación con ese usuario
        }

        String hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        addMessageToConversation(convKey, displayFrom, msg, hora, true);

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
    // GESTIÓN DE CONVERSACIONES
    // ==========================
    private void addMessageToConversation(String convKey, String from, String msg,
                                          String time, boolean saveHistory) {
        List<MessageRecord> lista = conversaciones
                .computeIfAbsent(convKey, k -> new ArrayList<>());

        lista.add(new MessageRecord(convKey, from, msg, time));

        // Si estamos viendo esa conversación, pintamos inmediatamente
        if (convKey.equals(destinoSeleccionado)) {
            renderMessageBubble(from, msg, time);
        }

        if (saveHistory) {
            appendHistoryToFile(convKey, from, time, msg);
        }
    }

    private void mostrarConversacion(String convKey) {
        panelChat.removeAll();

        List<MessageRecord> lista = conversaciones.get(convKey);
        if (lista != null) {
            for (MessageRecord rec : lista) {
                renderMessageBubble(rec.from, rec.msg, rec.time);
            }
        }

        panelChat.revalidate();
        panelChat.repaint();

        SwingUtilities.invokeLater(() ->
                scrollPane.getVerticalScrollBar().setValue(
                        scrollPane.getVerticalScrollBar().getMaximum()
                )
        );
    }

    // ==========================
    // BURBUJAS DE CHAT + HORA
    // ==========================
    private void renderMessageBubble(String from, String msg, String hora) {
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new EmptyBorder(5, 10, 5, 10));
        bubble.setOpaque(false);

        JLabel labelMsg = new JLabel("<html><b>" + escapeHtml(from) + ":</b><br>" +
                escapeHtml(msg) + "</html>");
        labelMsg.setOpaque(true);
        labelMsg.setBorder(new EmptyBorder(5, 10, 5, 10));

        boolean isOwnMessage = from.startsWith("Yo") || from.startsWith(user.getName());

        if (isOwnMessage) {
            labelMsg.setBackground(new Color(220, 248, 198)); // verde claro
        } else if (from.equalsIgnoreCase("Sistema")) {
            labelMsg.setBackground(new Color(230, 230, 230)); // gris claro sistema
        } else {
            labelMsg.setBackground(new Color(240, 240, 240)); // gris otros
        }

        JLabel labelHora = new JLabel("[" + hora + "]");
        labelHora.setFont(new Font("Arial", Font.ITALIC, 10));
        labelHora.setForeground(Color.GRAY);

        JPanel line = new JPanel();
        line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
        line.setOpaque(false);

        JPanel cont = new JPanel();
        cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));
        cont.setOpaque(false);
        cont.add(labelMsg);
        cont.add(Box.createVerticalStrut(2));
        cont.add(labelHora);

        if (isOwnMessage) {
            line.add(Box.createHorizontalGlue());
            line.add(cont);
        } else {
            line.add(cont);
            line.add(Box.createHorizontalGlue());
        }

        panelChat.add(line);
        panelChat.revalidate();
        panelChat.repaint();

        SwingUtilities.invokeLater(() ->
                scrollPane.getVerticalScrollBar().setValue(
                        scrollPane.getVerticalScrollBar().getMaximum()
                )
        );
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    // ==========================
    // HISTORIAL (persistente por conversación)
    // Formato línea: convKey \t from \t time \t msg
    // ==========================
    private File getHistoryFile() {
        return new File("history_" + user.getName() + ".txt");
    }

    private void appendHistoryToFile(String convKey, String from, String time, String msg) {
        File f = getHistoryFile();
        try (FileWriter fw = new FileWriter(f, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println(convKey + "\t" + from + "\t" + time + "\t" + msg);
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
                String[] parts = line.split("\t", 4);
                if (parts.length == 4) {
                    String convKey = parts[0];
                    String from = parts[1];
                    String time = parts[2];
                    String msg = parts[3];
                    // Cargamos en memoria pero no volvemos a escribir en fichero
                    addMessageToConversation(convKey, from, msg, time, false);
                }
            }

        } catch (IOException e) {
            System.err.println("Error leyendo historial de " + user.getName() + ": " + e.getMessage());
        }
    }
}
