package chat.ui;

import chat.mediator.ChatMediator;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LogWindow extends JFrame {

    private ChatMediator mediator;
    private JTextArea areaLog;
    private Timer timer;

    public LogWindow(ChatMediator mediator) {
        this.mediator = mediator;

        setTitle("Log del Mediador");
        setSize(500, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Consolas", Font.PLAIN, 12));

        add(new JScrollPane(areaLog), BorderLayout.CENTER);

        // Timer para refrescar el contenido cada segundo
        timer = new Timer(1000, e -> refreshLog());
        timer.start();

        setVisible(true);
    }

    private void refreshLog() {
        List<String> log = mediator.getLog();
        StringBuilder sb = new StringBuilder();
        for (String line : log) {
            sb.append(line).append("\n");
        }
        areaLog.setText(sb.toString());
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }
}
