package game;

import nl.saxion.app.SaxionApp;
import util.Command;
import util.ServerUtil;

import java.awt.*;
import java.util.function.Consumer;

public class TicTacToe {
    private final static Color HOVER_COLOR_1 = new Color(0, 255, 255, 43);
    private final static Color HOVER_COLOR_2 = new Color(255, 0, 255, 43);
    private final Object lock = new Object();
    private final Object onlineLock = new Object();
    private MultiplayerTTT parent;

    private boolean hovering = true;
    private boolean isOnline;
    private int boxWidth;
    private int boxHeight;
    private int round;
    private int winner;
    private int player = 1;
    private int position = 4;
    private int[] field;

    public void setParent(MultiplayerTTT parent) {
        this.parent = parent;
    }

    public void start() {
        isOnline = ServerUtil.isConnected();

        /*
         * 0 1 2
         * 3 4 5
         * 6 7 8
         */
        field = new int[9];
        round = 0;

        boxWidth = SaxionApp.getWidth() / 3;
        boxHeight = SaxionApp.getHeight() / 3;

        int lineWidth = 10;

        int x = boxWidth;
        int y = boxHeight;
        SaxionApp.setFill(SaxionApp.SAXION_PINK);
        for (int i = 0; i < 4; i++) {
            SaxionApp.drawRectangle(x, 0, lineWidth, SaxionApp.getHeight());
            SaxionApp.drawRectangle(0, y, SaxionApp.getWidth(), lineWidth);
            x += x;
            y += y;
        }

        hover(position);
        while (round < 9) {
            if (onlineOpponentTurn()) {
                synchronized (onlineLock) {
                    try {
                        onlineLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                char move;
                do {
                    do {
                        move = Character.toLowerCase(SaxionApp.readChar());
                        position = move(move);
                        if (isOnline) {
                            ServerUtil.send(Command.MOVE, String.valueOf(position));
                        }
                    } while (move != ' ');

                    SaxionApp.removeLastDraw();
                    hovering = false;
                } while (!check(position));

                if (isOnline) {
                    ServerUtil.send(Command.CHECK, String.valueOf(position));
                }
            }

            winner = gameover();
            if (winner != 0) {
                break;
            } else {
                next();
            }
        }

        System.out.println(winner);

        if (winner == 0) {
            SaxionApp.showMessage("It's a tie!", Color.GRAY);
        } else if (winner == 1) {
            SaxionApp.showMessage("Player 1 wins!", Color.BLUE);
        } else {
            SaxionApp.showMessage("Player 2 wins!", Color.RED);
        }

        if (parent != null) {
            parent.playAgain();
        }
    }

    private synchronized void next() {
        position = 4;
        hover(position);
    }

    private synchronized int gameover() {
        // Debug print
        System.out.println("Field:");
        for (int i = 0; i < 9; i += 3) {
            System.out.printf("[%d] [%d] [%d]\n", field[i], field[i + 1], field[i + 2]);
        }

        // Horizontal & Vertical
        int hor = 0;
        for (int i = 0; i < 3; i++) {
            if (field[hor] != 0 && field[hor] == field[hor + 1] && field[hor + 1] == field[hor + 2]) {
                // eg. 3 4 5
                return field[hor];
            } else if (field[i] != 0 && field[i] == field[i + 3] && field[i + 3] == field[i + 6]) {
                // eg. 0 3 6
                return field[i];
            }
            hor += 3;
        }

        // Diagonal
        if (field[0] != 0 && field[0] == field[4] && field[4] == field[8]) {
            return field[0];
        } else if (field[2] != 0 && field[2] == field[4] && field[4] == field[6]) {
            return field[2];
        }

        return 0;
    }

    public boolean check(int pos) {
        if (field[pos] == 0) {
            if (hovering) {
                SaxionApp.removeLastDraw();
                hovering = false;
            }

            int xMargin = (int) (boxWidth * .14);
            int yMargin = (int) (boxHeight * .125);
            field[pos] = player;
            String p = player == 1 ? "X" : "O";
            SaxionApp.setBorderColor(player == 1 ? Color.BLUE : Color.RED);
            onPos(pos, xy -> SaxionApp.drawText(p, xy[0] + xMargin, xy[1] + yMargin, Math.min(boxWidth, boxHeight)));

            player = ++round % 2 + 1;

            if (isOnline) {
                synchronized (onlineLock) {
                    onlineLock.notify();
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public int move(char move) {
        switch (move) {
            case 'w':
                position -= 3;
                break;
            case 'a':
                position -= 1;
                break;
            case 's':
                position += 3;
                break;
            case 'd':
                position += 1;
                break;
        }
        if (position < 0) {
            position = 9 + position;
        } else if (position >= 9) {
            position -= 9;
        }

        hover(position);

        return position;
    }

    public void hover(int pos) {
        if (hovering) SaxionApp.removeLastDraw();
        hovering = true;
        SaxionApp.setFill(player == 1 ? HOVER_COLOR_1 : HOVER_COLOR_2);
        onPos(pos, xy -> SaxionApp.drawRectangle(xy[0], xy[1], boxWidth, boxHeight));
    }

    private void onPos(int pos, Consumer<int[]> xy) {
        int x = 0;
        int y = 0;
        for (int i = 0; i < 9; i++) {
            if (i == pos) {
                xy.accept(new int[]{x, y});
                break;
            }
            x += boxWidth;
            if (i != 0 && (i + 1) % 3 == 0) {
                x = 0;
                y += boxHeight;
            }
        }
    }

    private boolean onlineOpponentTurn() {
        return isOnline && ServerUtil.getOpponent() == player;
    }

    public void startAsync() {
        new Thread(this::start).start();
    }
}
