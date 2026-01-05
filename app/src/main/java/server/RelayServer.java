package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class RelayServer {
    private final int LISTENING_PORT = 5000;
    private static Map<String, ClientHandler> waitingHosts;
    private static Map<String, ControlSession> activeSessions;
    

    public static void main(String[] args) {
    }

    private static void startServer(int listeningPort) {
        try (ServerSocket serverSocket = new ServerSocket(listeningPort)) {
            System.out.println("The server is litening on port: " + listeningPort);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("client has been connected from IP: " + socket.getInetAddress().getHostAddress());
            }
        }
        catch (IOException e) {
        System.out.println("somthing went wrong");
        }
    } 

    public static boolean isKeyInActiveSessions(String keySession) {
        for (String key : activeSessions.keySet()) {
            if (key == keySession) {
                return false;
            }
        }
        return true;
    }

    public static boolean isKeyInWaitingHosts(String keySession) {
        for (String key : waitingHosts.keySet()) {
            if (key == keySession) {
                return false;
            }
        }
        return true;
    }

    public static void addAWaitingHost(String key, ClientHandler clientHandler) {
        if (!waitingHosts.containsKey(key)) {
            waitingHosts.put(key, clientHandler);
        } else {
            throw new RuntimeException("waiting host already exists");
        }
    }
}
