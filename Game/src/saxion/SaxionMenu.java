package saxion;

import nl.saxion.app.SaxionApp;

import java.awt.*;
import java.util.ArrayList;

public class SaxionMenu {
    private final String[] options;
    private int selected;

    public SaxionMenu(String... options) {
        this.options = options;
    }

    public SaxionMenu(ArrayList<String> options) {
        this(options.toArray(new String[0]));
    }

    public void show() {
        show(false);
    }

    public int getSelected() {
        return selected;
    }

    public String getSelectedValue() {
        return options[selected];
    }

    public void show(boolean instructions) {
        SaxionApp.clear();

        if (instructions) {
            SaxionApp.printLine("W & S to move selection");
            SaxionApp.printLine("SPACE to select");
        }

        select(selected);

        char c;
        do {
            c = Character.toLowerCase(SaxionApp.readChar());

            if (c == 'w') {
                selected--;
            } else if (c == 's') {
                selected++;
            }

            if (selected < 0) {
                selected = options.length + selected;
            } else if (selected >= options.length) {
                selected -= options.length;
            }

            for (int i = 0; i < options.length; i++) {
                SaxionApp.removeLastPrint();
            }

            select(selected);
        } while (c != ' ');

        SaxionApp.clear();
    }

    private void select(int selected) {
        for (int i = 0; i < options.length; i++) {
            String sel;
            Color col = Color.WHITE;
            if (i == selected) {
                col = Color.CYAN;
                sel = "> ";
            } else {
                sel = "  ";
            }
            SaxionApp.printLine(sel + options[i], col);
        }
    }
}
