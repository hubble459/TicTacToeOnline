public class Battle {
    private final SocketProcess player1;
    private final SocketProcess player2;

    public Battle(SocketProcess player1, SocketProcess player2) {
        this.player1 = player1;
        this.player2 = player2;

        player1.send(Command.JOINED, "2 " + player2.getUsername());
        player2.send(Command.JOINED, "1 " + player1.getUsername());
    }

    public void move(SocketProcess me, String move) {
        int pos = Integer.parseInt(move);
        if (player1 == me) {
            player2.send(Command.MOVE, String.valueOf(pos));
        } else {
            player1.send(Command.MOVE, String.valueOf(pos));
        }
    }

    public void check(SocketProcess me, String move) {
        int pos = Integer.parseInt(move);
        if (player1 == me) {
            player2.send(Command.CHECK, String.valueOf(pos));
        } else {
            player1.send(Command.CHECK, String.valueOf(pos));
        }
    }


    public void talk(SocketProcess me, String message) {
        if (player1 == me) {
            player2.send(Command.TALK, message);
        } else {
            player1.send(Command.TALK, message);
        }
    }
}
