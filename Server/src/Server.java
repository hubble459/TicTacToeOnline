import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class Server {
    public static void main(String[] args) throws IOException {
        int port = 1337;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception ignored) {
        }
        Server.start(port);
    }

    public static void start(int port) throws IOException {
        ServerSocket server = new ServerSocket(port);
        ArrayList<SocketProcess> clients = new ArrayList<>();
        Queue<SocketProcess> waiting = new ArrayDeque<>();

        while (!server.isClosed()) {
            Socket client = server.accept();

            SocketProcess process = new SocketProcess(client, clients, waiting);
            System.out.println("Client connected (" + clients.size() + ")");
            process.send(Command.INFO, "Welcome to the TTT server");

            new Thread(process).start();
        }
    }
}
