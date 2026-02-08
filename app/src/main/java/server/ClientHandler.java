package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import resources.*;
import resources.Packet.PacketType;


public class ClientHandler implements Runnable {
    private final Socket socket;

    private ServerProtocol serverProtocol;

    private ClientType clientType = null;
    private volatile boolean isRunning = false;
    private ControlSession controlSession = null;

    private final Object hostPendingLock = new Object();

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.serverProtocol = new ServerProtocol(this.socket.getInputStream(), this.socket.getOutputStream());
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
        Packet identificationPacket = this.serverProtocol.recievePacket();
        Packet.PacketType packetType = identificationPacket.getPacketType();

        if (packetType != Packet.PacketType.HOST && packetType != Packet.PacketType.VIEWER) {
            this.serverProtocol.sendErrStringPacket("identification has been failed :(");
            return ClientType.NULL;
        }
        this.serverProtocol.sendSuccessPacket("enter desired session code");
        return packetType == PacketType.HOST ? ClientType.HOST : ClientType.VIEWER;
    }

    
    private void hostHandler() throws IOException, InterruptedException {
        String sessionKey = recieveSessionKeyFromHost();

        RelayServer.addAWaitingHost(sessionKey, this);
        System.out.println("Host has been added to waiting list");

        hostPendingConnection();

        ControlSession controlSession = RelayServer.getControlSessionByKey(sessionKey);
        ClientHandler viewerHandler = controlSession.getViewerHandler();

        // transfering data between the viewer to the host
        Thread viewerToHost = new Thread(() -> {
            try {
                boolean keepRunning = true;
                while (keepRunning) {
                    Packet viewerPacket = viewerHandler.recievePacket();
                    sendPacket(viewerPacket);;
                    keepRunning = viewerPacket.getPacketType() != Packet.PacketType.QUIT;
                }

            } catch (IOException e) { System.out.println(e.getMessage()); }
        }); 
        viewerToHost.start();

        // transfering data between host to the viewer
        boolean keepRunning = true;
        while (keepRunning) {
            Packet hostPacket = this.serverProtocol.recievePacket();
            viewerHandler.sendPacket(hostPacket);
            keepRunning = hostPacket.getPacketType() != Packet.PacketType.QUIT;
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
        
        hostHandler.serverProtocol.sendSuccessPacket("connected to viewer!"); 
        hostHandler.serverProtocol.recievePacket();

        this.serverProtocol.sendSuccessPacket("Connected to host!"); 

        synchronized (hostHandler.hostPendingLock) { 
            hostHandler.isRunning = false;
            hostHandler.hostPendingLock.notify();
        }
    }

    private String recieveSessionKeyFromHost() throws IOException {
        while (true) {
            String sessionKey = this.serverProtocol.getPacketStringPayload();
            if (!RelayServer.isKeyInActiveSessions(sessionKey) && !RelayServer.isKeyInWaitingHosts(sessionKey)) {
                this.serverProtocol.sendSuccessPacket("key found");
                return sessionKey;
            }
            this.serverProtocol.sendErrStringPacket("session Key is in use");
        }
    }
    
    private String recieveSessionKeyFromViewer() throws IOException {
        while (true) {
            String sessionKey = this.serverProtocol.getPacketStringPayload();

            if (RelayServer.isKeyInWaitingHosts(sessionKey)) {
                
                return sessionKey;
            }
            this.serverProtocol.sendErrStringPacket("No waiting host recognized by this key was found");
        }
    }

    private void sendPacket(Packet packet) throws IOException {
        this.serverProtocol.sendPacket(packet);
    }

    private Packet recievePacket() throws IOException {
        return this.serverProtocol.recievePacket();
    }

    private void closeEverything() {
        try {
            if (getInputStream() != null) getInputStream().close();
            if (getOutputStream() != null) getOutputStream().close();
            if (socket != null) socket.close();
        } catch (IOException e) {
        System.err.println("Error while closing: " + e.getMessage());
        }
    }

    public Socket getSocket() {
        return this.socket;
    }

    public DataInputStream getInputStream() {
        return this.serverProtocol.getInputStream();
    }

    public DataOutputStream getOutputStream() {
        return this.serverProtocol.getOutputStream();
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
