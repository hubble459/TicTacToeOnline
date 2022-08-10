public enum Command {
    LOGIN("CONN"), // CONN <username> (to login)
    PLAY("PLAY"), // WAIT (to wait for other player)
    TALK("TALK"), // TALK <message>
    MOVE("MOVE"), // MOVE <position>
    CHECK("CHECK"), // CHECK <position>
    QUIT("QUIT"), // QUIT (to disconnect)
    TIMED_OUT("DSCT"), // (disconnected message)
    INFO("INFO"), // (info message)
    WAITING_FOR_PLAYER("WAIT"),
    UNKNOWN("400"),
    ALREADY_LOGGED_IN("401"),
    INVALID_FORMAT("402"),
    NOT_LOGGED_IN("403"),
    NOT_IN_A_BATTLE("404"),
    LOGGED_IN("200"),
    QUITED("201"),
    JOINED("202"),
    LEFT("203");

    private final String command;

    Command(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command;
    }

    public static Command fromString(String command) {
        for (Command value : values()) {
            if (value.command.equals(command)) {
                return value;
            }
        }
        return null;
    }
}
