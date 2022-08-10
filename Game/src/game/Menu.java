package game;

import saxion.SaxionMenu;

public class Menu {
    public static void start() {
        SaxionMenu menu = new SaxionMenu("Single-player", "Multiplayer", "Exit");
        menu.show();

        boolean exit = false;
        switch (menu.getSelected()) {
            case 0:
                new TicTacToe().start();
                break;
            case 1:
                new MultiplayerTTT();
                break;
            case 2:
                exit = true;
        }

        if (!exit) {
            start();
        }
    }
}
