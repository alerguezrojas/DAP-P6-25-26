package chat.mediator;

import chat.user.ChatUser;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ConcreteChatMediator implements ChatMediator {

    private Map<String, ChatUser> users = new HashMap<>();
    private Map<String, List<ChatUser>> groups = new HashMap<>();
    private Map<String, Boolean> onlineStatus = new HashMap<>();
    private List<String> log = new ArrayList<>();

    // =======================
    // Métodos privados de log
    // =======================
    private void logEvent(String msg) {
        String ts = LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toString();
        String line = "[" + ts + "] " + msg;
        log.add(line);
        System.out.println(line);
    }

    // =======================
    // Implementación interfaz
    // =======================

    @Override
    public void registerUser(ChatUser user) {
        users.put(user.getName(), user);
        onlineStatus.put(user.getName(), false);
        logEvent("Registrado usuario: " + user.getName());
    }

    @Override
    public void createGroup(String groupName, List<ChatUser> members) {
        groups.put(groupName, members);
        logEvent("Grupo creado: " + groupName + " con miembros: " +
                members.stream().map(ChatUser::getName).toList());
    }

    @Override
    public void sendPrivateMessage(String from, String to, String message) {
        ChatUser receiver = users.get(to);
        if (receiver != null) {
            receiver.receiveMessage(from, message);
            logEvent("PRIVADO " + from + " → " + to + ": " + message);
        } else {
            logEvent("ERROR: intento de mensaje privado a usuario inexistente: " + to);
        }
    }

    @Override
    public void sendGroupMessage(String from, String groupName, String message) {
        List<ChatUser> members = groups.get(groupName);
        if (members == null) {
            logEvent("ERROR: intento de mensaje a grupo inexistente: " + groupName);
            return;
        }

        for (ChatUser u : members) {
            if (!u.getName().equals(from)) {
                u.receiveMessage(from + " (grupo " + groupName + ")", message);
            }
        }
        logEvent("GRUPO " + from + " → [" + groupName + "]: " + message);
    }

    @Override
    public List<String> getConnectedUserNames() {
        return new ArrayList<>(users.keySet());
    }

    @Override
    public List<String> getGroupNames() {
        return new ArrayList<>(groups.keySet());
    }

    @Override
    public void setUserOnline(String userName, boolean online) {
        if (!onlineStatus.containsKey(userName)) return;

        boolean prev = onlineStatus.get(userName);
        if (prev == online) return; // sin cambios

        onlineStatus.put(userName, online);
        String estado = online ? "ONLINE" : "OFFLINE";

        logEvent("Estado usuario " + userName + ": " + estado);

        // Notificar al resto de usuarios como mensaje del "Sistema"
        String msg = userName + " está ahora " + estado.toLowerCase();
        for (ChatUser u : users.values()) {
            if (!u.getName().equals(userName)) {
                u.receiveMessage("Sistema", msg);
            }
        }
    }

    @Override
    public boolean isUserOnline(String userName) {
        return onlineStatus.getOrDefault(userName, false);
    }

    @Override
    public Map<String, Boolean> getUserStatus() {
        return new HashMap<>(onlineStatus);
    }

    @Override
    public List<String> getLog() {
        return new ArrayList<>(log);
    }
}
