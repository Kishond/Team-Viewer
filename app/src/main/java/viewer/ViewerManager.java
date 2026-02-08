package viewer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Supplier;

import resources.*;
import resources.Packet.PacketType;

public class ViewerManager implements ConnectableToHost, HandleDissconnection {

    private static final String SERVER_IP = "127.0.0.1"; 
    private static final int SERVER_PORT = 5000; 
    
    private final ServerProtocol serverProtocol;
    private final Socket viewerSocket;

    private RemoteUpdateListener viewerUI;
    private NetworkReciever networkReciever;
    private NetworkSender networkSender;

    private Thread networkSenderThread;
    private Thread networkRecieverThread;
    private Thread connectionThread; 

    private boolean hasConnectedTohost = false;

    public ViewerManager(String serverIP, int serverPort) throws IOException {
        this.viewerSocket = new Socket(serverIP, serverPort);
        DataOutputStream outputStream = new DataOutputStream(this.viewerSocket.getOutputStream());
        DataInputStream inputStream = new DataInputStream(this.viewerSocket.getInputStream());

        this.serverProtocol = new ServerProtocol(inputStream, outputStream);
    }

    private void startViewer() {
        this.networkSender = new NetworkSender(this.serverProtocol, this);
        this.viewerUI = new ViewerUI(this.networkSender, this);
        this.networkReciever = new NetworkReciever(this.serverProtocol, this.viewerUI, this);
        
        this.networkSenderThread = new Thread(networkSender);
        this.networkRecieverThread = new Thread(networkReciever);

        this.viewerUI.showUI();
    }

    @Override
    public void connectToHost(Supplier<String> sesionCode, ConnectionCallback callback) {
        this.connectionThread = new Thread(() -> {
            try {
                Packet idPacket = new Packet(Packet.PacketType.VIEWER);
                this.serverProtocol.sendPacket(idPacket);
                
                Packet promptPacket = serverProtocol.recievePacket(); 
                if (promptPacket.getPacketType() == PacketType.ERR_MESSAGE) {
                    throw new RuntimeException("Identification has been failed");
                }
                
                boolean isAuthorized = false;
                
                // 3. Loop until connection 
                while (!isAuthorized) {
                    String key = sesionCode.get();
                    if (key == null) {
                        return; // User cancelled the dialog
                    }

                    this.serverProtocol.sendPacket(new Packet(PacketType.SESSION_KEY, key.getBytes()));
                    
                    Packet response = serverProtocol.recievePacket();
                    
                    if (response.getPacketType() == PacketType.SUCCESS) {
                        isAuthorized = true;
                    } else {
                        // Notify UI to show the error message received from server
                        String serverMessage = new String(response.getPayload());
                        callback.onConnectionError(serverMessage);
                    }
                }

                // 4. Connection successful
                this.hasConnectedTohost = true;

                this.networkSenderThread.start();
                this.networkRecieverThread.start();

                callback.onConnectionSuccess();

            } catch (IOException e) {
                callback.onConnectionError(e.getMessage());
            } catch (Exception e) {
                callback.onConnectionError(e.getMessage());
            }
        });

        this.connectionThread.start();
    }

    public boolean hasConnectedTohost() {
        return this.hasConnectedTohost;
    }

    public static void main(String[] args) {
        try {
            ViewerManager viewerManager = new ViewerManager(SERVER_IP, SERVER_PORT);
            viewerManager.startViewer();
            
        } catch (IOException e) {
            System.err.println("Could not connect to server: " + e.getMessage());
        }
    }

    @Override
    public void handleConnectionLost() {
        cleanup("Connection has been lost");
    }

    @Override
    public void handleQuitRequest() {
        cleanup("By quit request");
    }

    // Stop program helper method
    private void cleanup(String reason) {
        System.out.println(reason);
        try {
            // Closing sockets
            if (viewerSocket != null && !viewerSocket.isClosed()) {
                viewerSocket.close();
            }
            
            // Interrupting the networking threads if necessary 
            if (networkSenderThread != null) {
                networkSenderThread.interrupt();
            } 
            if (networkRecieverThread != null) {
                networkRecieverThread.interrupt();
            }
            if (connectionThread != null) {
                connectionThread.interrupt();
            }

            // Close the UI
            if (this.viewerUI != null) {
                this.viewerUI.onSessionClosed(reason);
            }

            this.hasConnectedTohost = false;
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}

interface ConnectableToHost {
    // on call connect to the host asynchronously
    public void connectToHost(Supplier<String> sesionCode, ConnectionCallback callback);

    // has host been connected
    public boolean hasConnectedTohost();
}

interface HandleDissconnection {
    public void handleConnectionLost();
    public void handleQuitRequest();
}

