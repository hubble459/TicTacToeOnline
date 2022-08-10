import game.Menu;
import nl.saxion.app.SaxionApp;

public class Main implements Runnable {
    public static void main(String[] args) {
        SaxionApp.start(new Main());
    }

    @Override
    public void run() {
        SaxionApp.turnBorderOff();

        Menu.start();
    }
}
