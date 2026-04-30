package host;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import resources.*;
import resources.Packet.PacketType;

public class HostManager implements RegisterableToHost, HandleDissconnection {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 5000;

    private final ServerProtocol serverProtocol;
    private final Socket hostSocket;
    
    private final RobotController robotController;
    private HostUI hostUI;
    
    private NetworkSender networkSender;
    private NetworkReciever networkReciever;

    private Thread networkSenderThread;
    private Thread networkRecieverThread;
    private Thread registrationThread;

    private boolean isRegistered = false;

    public HostManager(String serverIP, int serverPort) throws IOException {
        this.hostSocket = new Socket(serverIP, serverPort);
        
        DataOutputStream outputStream = new DataOutputStream(this.hostSocket.getOutputStream());
        DataInputStream inputStream = new DataInputStream(this.hostSocket.getInputStream());

        this.serverProtocol = new ServerProtocol(inputStream, outputStream);
        
        // Initialize the sender and the controller
        this.networkSender = new NetworkSender(this.serverProtocol, this);
        this.robotController = new RobotController(this.networkSender);
    }

    private void startHost() {
        this.hostUI = new HostUI(this);
        
        this.networkReciever = new NetworkReciever(this.serverProtocol, this.robotController, this);

        this.networkSenderThread = new Thread(this.networkSender);
        this.networkRecieverThread = new Thread(this.networkReciever);

        this.hostUI.showUI();
    }

    @Override
    public void registerHost(Supplier<String> sessionKeySupplier, HostRegistrationCallback callback) {
        this.registrationThread = new Thread(() -> {
            try {
                // 1. Identification
                serverProtocol.sendPacket(new Packet(PacketType.HOST));
                serverProtocol.recievePacket();

                boolean keyAccepted = false;
                String finalKey = "";

                while (!keyAccepted) {
                    finalKey = sessionKeySupplier.get();
                    if (finalKey == null) {
                        return; 
                    }

                    serverProtocol.sendPacket(new Packet(PacketType.SESSION_KEY, finalKey.getBytes()));
                    Packet response = serverProtocol.recievePacket();
                    String serverMsg = new String(response.getPayload());

                    if (serverMsg.contains("session Key is in use")) {
                        callback.onRegistrationError("Key already in use. Try another.");
                    } else {
                        keyAccepted = true;
                    }
                }

                this.isRegistered = true;
                callback.onRegistrationSuccess(finalKey);

                Packet viewerFoundPacket = serverProtocol.recievePacket(); 

                serverProtocol.sendPacket(new Packet(PacketType.SUCCESS));
                if (viewerFoundPacket.getPacketType() != PacketType.SUCCESS) {
                    handleConnectionLost();
                    return;
                }
                
                // the timeout is for avoiding a race between recieving success packets
                try {
                    TimeUnit.SECONDS.sleep(5); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                this.networkSender.setCryptoKey(finalKey);
                this.networkReciever.setCryptoKey(finalKey);

                this.networkSenderThread.start();
                this.networkRecieverThread.start();
                
                this.robotController.startLiveCapture();

                callback.onConnection();
            } catch (IOException e) {
                callback.onRegistrationError(e.getMessage());
            }
        });
        this.registrationThread.start();
    }

    @Override
    public void handleConnectionLost() {
        cleanup("Connection to server lost");
    }

    @Override
    public void handleQuitRequest() {
        cleanup("Session ended by user");
    }

    private void cleanup(String reason) {
        System.out.println("Cleaning up: " + reason);
        this.networkSender.queueDisconnect();
        try {
            this.networkReciever.setRunning(false);
            while (this.networkSender.isRunning()) {}
            // Expanded if statements for readability
            if (hostSocket != null) {
                if (!hostSocket.isClosed()) {
                    hostSocket.close();
                }
            }

            if (networkSenderThread != null) {
                networkSenderThread.interrupt();
            }

            if (networkRecieverThread != null) {
                networkRecieverThread.interrupt();
            }

            if (registrationThread != null) {
                registrationThread.interrupt();
            }
            
            if (robotController != null) {
                robotController.stopLiveCapture();
            }

            if (this.hostUI != null) {
                this.hostUI.onSessionClosed(reason);
            }

            this.isRegistered = false;
        } catch (IOException e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            HostManager hostManager = new HostManager(SERVER_IP, SERVER_PORT);
            hostManager.startHost();
        } catch (IOException e) {
            System.err.println("Could not connect to Relay Server: " + e.getMessage());
        }
    }
}

interface HandleDissconnection {
    public void handleConnectionLost();
    public void handleQuitRequest();
}

interface RegisterableToHost {
    void registerHost(Supplier<String> sessionKeySupplier, HostRegistrationCallback callback);
}