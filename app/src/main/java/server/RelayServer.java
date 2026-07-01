package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class RelayServer {
    private static final int LISTENING_PORT = 5000;

    private static Map<String, ClientHandler> waitingHosts;
    private static Map<String, ControlSession> activeSessions;

    private static DatabaseManager databaseManager;

    public static void main(String[] args) {
        System.out.println("server has started");
        startServer(LISTENING_PORT);
    }

    private static void startServer(int listeningPort) {
        waitingHosts = new HashMap<String, ClientHandler>();
        activeSessions = new HashMap<String, ControlSession>();

        databaseManager = new DatabaseManager();
        try (ServerSocket serverSocket = new ServerSocket(listeningPort)) {
            System.out.println("The server is litening on port: " + listeningPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("client has been connected from IP: " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        }
        catch (IOException e) {
        System.out.println("stop the current server broda or switch port");
        }
    } 

    public static void logConnection(String sessionKey, String hostIp) {
        System.out.println("[DB] Session created: " + sessionKey + " (Host: " + hostIp + ")");
        databaseManager.logConnection(sessionKey, hostIp);
    }

    public static void logViewerJoining(String sessionKey, String viewerIp) {
        System.out.println("[DB] Session activated: " + sessionKey + " (Viewer: " + viewerIp + ")");
        databaseManager.logViewerJoining(sessionKey, viewerIp);
    }

    public static void closeSession(String sessionKey) {
        System.out.println("[DB] Session closed: " + sessionKey);
        databaseManager.closeSession(sessionKey);
    }


    public static boolean isKeyInWaitingHosts(String keySession) {
        return waitingHosts.containsKey(keySession);
    }
    
    public static void addAWaitingHost(String key, ClientHandler clientHandler) {
        if (!isKeyInWaitingHosts(key)) {
            waitingHosts.put(key, clientHandler);
        } else {
            throw new RuntimeException("waiting host already exists");
        }
    }

    public static boolean isKeyInActiveSessions(String keySession) {
        return activeSessions.containsKey(keySession);
    }
    
    public static void addAnActiveSession(String key, ControlSession controlSession) {
        if (!isKeyInActiveSessions(key)) {
            activeSessions.put(key, controlSession);
        } else {
            throw new RuntimeException("active session key already exists");
        }
    }

    public static ClientHandler removeAWaitingHost(String key) {
        if (isKeyInWaitingHosts(key)) {
            return waitingHosts.remove(key);
        } else {
            throw new RuntimeException("no such a waiting host");
        }
    }

    public static ControlSession getControlSessionByKey(String keySession) {
        if (isKeyInActiveSessions(keySession)) {
            return activeSessions.get(keySession);
        } else {
            throw new RuntimeException("key is not in active sessions");
        }
    }
}
