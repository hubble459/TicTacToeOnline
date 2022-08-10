package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerUtil {
    private static Socket socket;
    private static PrintWriter writer;
    private static int opponent = 1;

    public static boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    public static void setOpponent(int opponent) {
        ServerUtil.opponent = opponent;
    }

    public static int getOpponent() {
        return opponent;
    }

    public static void disconnect() {
        ServerUtil.send(Command.QUIT);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void inputReader(OnMessageReceived listener) throws IOException {
        assert socket != null;
        new Thread(new RunnableReader(socket, listener)).start();
    }

    public static void init(Socket socket) {
        ServerUtil.socket = socket;
        try {
            ServerUtil.writer = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void send(Command command) {
        send(command, "");
    }

    public static void send(Command command, String payload) {
        assert writer != null;
        writer.println(command + " " + payload);
        writer.flush();
    }

    private static class RunnableReader implements Runnable {
        private final Socket socket;
        private final BufferedReader reader;
        private final OnMessageReceived listener;

        public RunnableReader(Socket socket, OnMessageReceived listener) throws IOException {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.listener = listener;
        }

        @Override
        public void run() {
            while (!socket.isClosed()) {
                try {
                    String message = reader.readLine();
                    if (message == null) {
                        break;
                    } else {
                        if (message.isEmpty()) {
                            System.out.println("Empty message?");
                        } else {
                            String[] split = message.split(" ", 2);
                            Command command = Command.fromString(split[0]);
                            if (command != null) {
                                String payload = "";
                                if (split.length > 1) {
                                    payload = split[1];
                                }

                                listener.onMessage(command, payload);
                            } else {
                                System.out.println("Unknown command");
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    break;
                }
            }
            listener.onDisconnect();
        }
    }

    public interface OnMessageReceived {
        void onMessage(Command command, String payload);

        void onDisconnect();
    }
}
