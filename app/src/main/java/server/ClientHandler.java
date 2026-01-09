package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ClientHandler implements Runnable {
    private final Socket socket;

    private final DataOutputStream outputStream;
    private final DataInputStream inputStream;

    private ClientType clientType = null;
    private volatile boolean isRunning = false;
    private ControlSession controlSession = null;

    private final Object hostPendingLock = new Object();

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.inputStream = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        System.out.println("thread is running");
        this.isRunning = true;
        try {
            this.clientType = identification();
            switch (this.clientType) {
                case HOST:
                    hostHandler();
                    break;
                case VIEWER:
                    viewerHandler();
                    break;
                case NULL:
                    break;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        finally  {
            if (this.clientType != ClientType.VIEWER)
            closeEverything();
        }
    }

    private ClientType identification() throws IOException {
        String identification = this.inputStream.readUTF().toUpperCase();

        if (!identification.equals(ClientType.HOST.name()) && !identification.equals(ClientType.VIEWER.name())) {
            sendMessage("identification has been failed :(");
            return ClientType.NULL;
        }
        sendMessage("enter desired session code");
        return identification.equals(ClientType.HOST.name()) ? ClientType.HOST : ClientType.VIEWER;
    }

    
    private void hostHandler() throws IOException, InterruptedException {
        String sessionKey = recieveSessionKeyFromHost();

        RelayServer.addAWaitingHost(sessionKey, this);
        hostPendingConnection();
        sendMessage("viewer has been found");

        ControlSession controlSession = RelayServer.getControlSessionByKey(sessionKey);
        ClientHandler viewerHandler = controlSession.getViewerHandler();

        // transfering data between the viewer to the host
        Thread viewerToHost = new Thread(() -> {
            try {
                boolean keepRunning = true;
                while (keepRunning) {
                    String viewerInput = viewerHandler.recieveMessage();
                    keepRunning = !viewerInput.equals("EXIT");
                    sendMessage(viewerInput);
                }

            } catch (IOException e) { System.out.println(e.getMessage()); }
        }); 
        viewerToHost.start();

        // transfering data between host to the viewer
        boolean keepRunning = true;
        while (keepRunning) {
            String hostInput = recieveMessage();
            keepRunning = !hostInput.equals("EXIT");
            viewerHandler.sendMessage(hostInput);;
        }
    }
    
    private void hostPendingConnection() throws InterruptedException {
        synchronized (this.hostPendingLock) {
            while (this.isRunning) {
                this.hostPendingLock.wait();
            }
            System.out.println("Viewer has been found");
        }
    }
    
    private void viewerHandler() throws IOException {
        String sessionKey = recieveSessionKeyFromViewer();
        ClientHandler hostHandler = RelayServer.removeAWaitingHost(sessionKey);

        this.controlSession = new ControlSession(this, hostHandler);
        RelayServer.addAnActiveSession(sessionKey, this.controlSession);

        // notify host for an upcoming connection
        synchronized (hostHandler.hostPendingLock) { 
            hostHandler.isRunning = false;
            hostHandler.hostPendingLock.notify();
        }

    }

    
    private String recieveSessionKeyFromHost() throws IOException {
        while (true) {
            String sessionKey = this.inputStream.readUTF();
            if (!RelayServer.isKeyInActiveSessions(sessionKey) && !RelayServer.isKeyInWaitingHosts(sessionKey)) {
                return sessionKey;
            }
            sendMessage("Key is already in use, try using a different key");
        }
    }
    
    private String recieveSessionKeyFromViewer() throws IOException {
        while (true) {
            String sessionKey = this.inputStream.readUTF();

            if (RelayServer.isKeyInWaitingHosts(sessionKey)) {
                return sessionKey;
            }
            sendMessage("No waiting host recognized by this key was found");
        }
    }

    private void sendMessage(String msg) throws IOException {
        this.outputStream.writeUTF(msg);
    }

    private String recieveMessage() throws IOException {
        return this.inputStream.readUTF();
    }
    
    private void closeEverything() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
        System.err.println("Error while closing: " + e.getMessage());
        }
    }

    public Socket getSocket() {
        return this.socket;
    }

    public DataInputStream getDataInputStream() {
        return this.inputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return this.outputStream;
    }

    public ClientType getClientType() {
        return this.clientType;
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}


enum ClientType {
    HOST,
    VIEWER,
    NULL;
}
