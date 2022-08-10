package game;

import nl.saxion.app.SaxionApp;
import util.Command;
import util.ServerUtil;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;

public class MultiplayerTTT {
    private boolean loggedIn;

    private final Object lock = new Object();
    private TicTacToe ttt;
    private Socket socket;
    private String player;
    private String player1;
    private String player2;

    public MultiplayerTTT() {
        connect();

        if (socket != null) {
            connected();
        }

        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void connected() {
        SaxionApp.printLine("Connected!", Color.GREEN);
        ServerUtil.init(socket);
        try {
            ServerUtil.inputReader(new ServerUtil.OnMessageReceived() {
                @Override
                public void onMessage(Command command, String payload) {
                    MultiplayerTTT.this.onMessage(command, payload);
                }

                @Override
                public void onDisconnect() {
//                    SaxionApp.showMessage("Disconnected from server", Color.RED);

                    synchronized (lock) {
                        lock.notify();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void login() {
        SaxionApp.print("Username: ");
        player = SaxionApp.readString();
        ServerUtil.send(Command.LOGIN, player);
    }

    private void play() {
        ttt = null;
        SaxionApp.printLine("Waiting for opponent...");
        ServerUtil.send(Command.PLAY);
    }

    private void connect() {
        SaxionApp.printLine("Connecting to server...");
        try {
            socket = new Socket("localhost", 1338);
        } catch (IOException e) {
            SaxionApp.printLine("Connection failed", Color.RED);
            SaxionApp.printLine("Try again? (Y/n)");
            char confirm = Character.toLowerCase(SaxionApp.readChar());
            if (confirm != 'n') {
                connect();
            }
        }
    }

    private void onMessage(Command command, String payload) {
        if (command == Command.INFO) {
            login();
        } else if (command == Command.LOGGED_IN) {
            loggedIn = true;
            SaxionApp.printLine("Logged in as " + player);
            play();
        } else if (!loggedIn) {
            login();
        } else if (command == Command.JOINED) {
            startGame(payload);
        } else if (command == Command.MOVE) {
            ttt.hover(Integer.parseInt(payload));
        } else if (command == Command.CHECK) {
            ttt.check(Integer.parseInt(payload));
        } else if (command == Command.LEFT) {
            String username = payload.split(" ", 2)[0];
            if (ttt != null) {
                if (username.equals(player1) || username.equals(player2)) {
                    SaxionApp.showMessage("Opponent left, you win by default!", Color.GREEN);
                    Menu.start();
                }
            }
        } else if (command == Command.QUITED) {
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    private void startGame(String payload) {
        String[] split = payload.split(" ");
        if (split[0].equals("1")) {
            player1 = split[1];
            player2 = player;

            ServerUtil.setOpponent(1);
        } else {
            player1 = player;
            player2 = split[1];

            ServerUtil.setOpponent(2);
        }
        if (player.equals(player1)) {
            SaxionApp.printLine("Playing against " + player2);
            SaxionApp.printLine("You are player 1");
        } else {
            SaxionApp.printLine("Playing against " + player1);
            SaxionApp.printLine("You are player 2");
        }
        SaxionApp.pause();
        SaxionApp.clear();

        ttt = new TicTacToe();
        ttt.setParent(this);
        ttt.startAsync();
        SaxionApp.sleep(1D);
        System.out.println("TTT Started");
    }

    public void playAgain() {
        ttt = null;

        SaxionApp.clear();
        SaxionApp.printLine("Play again? (Y/n)");
        int c = Character.toLowerCase(SaxionApp.readChar());
        if (c != 'n') {
            play();
        } else {
            SaxionApp.printLine("Thanks for playing");
            SaxionApp.sleep(2D);

            ServerUtil.disconnect();
        }
    }
}
