import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;

public class SocketProcess implements Runnable {
    public static final String UNKNOWN_USER = "UNKNOWN";
    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private final ArrayList<SocketProcess> clients;
    private final Queue<SocketProcess> waiting;
    private String username;
    private Battle battle;

    public SocketProcess(Socket socket, ArrayList<SocketProcess> clients, Queue<SocketProcess> waiting) throws IOException {
        this.socket = socket;
        this.writer = new PrintWriter(socket.getOutputStream());
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.clients = clients;
        this.waiting = waiting;
        this.username = UNKNOWN_USER;
    }

    public String getUsername() {
        return username;
    }

    public void send(Command command, String message) {
        writer.println(command + " " + message);
        writer.flush();
    }

    public void broadcast(Command command, String payload, boolean includeSelf) {
        synchronized (clients) {
            for (SocketProcess client : clients) {
                if (includeSelf || client != this) {
                    client.send(command, payload);
                }
            }
        }
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                String message = reader.readLine();
                if (!handleMessage(message)) {
                    break;
                }
            } catch (IOException e) {
                break;
            }
        }

        disconnect();
    }

    /**
     * @param message message
     * @return false if disconnect
     */
    private boolean handleMessage(String message) {
        if (message.isEmpty()) {
            send(Command.UNKNOWN, "Unknown command");
        } else {
            String[] split = message.split(" ", 2);
            Command command = Command.fromString(split[0]);

            if (command == null) {
                send(Command.UNKNOWN, "Unknown command");
            } else {
                String payload = "";
                if (split.length > 1) {
                    payload = split[1];
                }

                if (username.equals(UNKNOWN_USER) && command != Command.LOGIN) {
                    send(Command.NOT_LOGGED_IN, "Log in first!");
                } else {
                    System.out.println(username + ": " + command + " " + payload);

                    switch (command) {
                        case LOGIN:
                            login(payload);
                            break;
                        case PLAY:
                            waitInQueue();
                            break;
                        case MOVE:
                            if (checkBattle(payload)) {
                                battle.move(this, payload);
                            }
                            break;
                        case CHECK:
                            if (checkBattle(payload)) {
                                battle.check(this, payload);
                            }
                            break;
                        case TALK:
                            if (battle != null) {
                                if (!payload.isEmpty()) {
                                    battle.talk(this, payload);
                                } else {
                                    send(Command.UNKNOWN, "Missing <message>");
                                }
                            } else {
                                send(Command.NOT_IN_A_BATTLE, "Join a game first");
                            }
                            break;
                        case QUIT:
                            return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean checkBattle(String payload) {
        if (battle == null) {
            send(Command.NOT_IN_A_BATTLE, "Join a game first");
            return false;
        } else if (payload.isEmpty()) {
            send(Command.UNKNOWN, "No payload given");
            return false;
        }
        return true;
    }

    private void waitInQueue() {
        battle = null;

        SocketProcess player1 = waiting.poll();
        if (player1 != null) {
            System.out.println("Started battle between " + player1.username + " and " + username);
            battle = new Battle(player1, this);
            player1.setBattle(battle);
        } else {
            send(Command.WAITING_FOR_PLAYER, "Waiting for other player");
            waiting.offer(this);
        }
    }

    public void setBattle(Battle battle) {
        this.battle = battle;
    }

    private void login(String username) {
        if (username.matches("\\w{3,14}")) {
            if (clientFromUsername(username) == null) {
                this.username = username;
                clients.add(this);
                send(Command.LOGGED_IN, "Logged in as " + username);
            } else {
                send(Command.ALREADY_LOGGED_IN, "Username already taken");
            }
        } else {
            send(Command.INVALID_FORMAT, "Username should be between 3 and 14 characters");
        }
    }

    private SocketProcess clientFromUsername(String username) {
        for (SocketProcess client : clients) {
            if (client.username.equals(username)) {
                return client;
            }
        }
        return null;
    }

    public void disconnect() {
        System.out.println(username + " disconnected");
        if (!socket.isClosed()) {
            broadcast(Command.LEFT, username + " disconnected", true);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            broadcast(Command.LEFT, username + " disconnected", false);
        }

        clients.remove(this);
        waiting.remove(this);
    }
}
