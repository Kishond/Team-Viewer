package viewer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import resources.*;
import resources.Packet.PacketType;

public class ViewerManager implements ConnectableToHost {
    private static final String SERVER_IP = "127.0.0.1"; 
    private static final int SERVER_PORT = 5000; 
    
    private final ServerProtocol serverProtocol;
    private final Socket viewerSocket;

    private ViewerUI viewerUI;
    private NetworkReciever networkReciever;
    private NetworkSender networkSender;
    
    private boolean hasConnectedTohost = false;

    public ViewerManager(String serverIP, int serverPort) throws IOException {
        this.viewerSocket = new Socket(serverIP, serverPort);
        DataOutputStream outputStream = new DataOutputStream(this.viewerSocket.getOutputStream());
        DataInputStream inputStream = new DataInputStream(this.viewerSocket.getInputStream());

        this.serverProtocol = new ServerProtocol(inputStream, outputStream);
    }

    public void startViewer() {
        this.networkSender = new NetworkSender(this.serverProtocol);

        SwingUtilities.invokeLater(() -> {
                this.viewerUI = new ViewerUI(this, this.networkSender);
                this.viewerUI.setVisible(true);
            });

        this.networkReciever = new NetworkReciever(this.serverProtocol, this.viewerUI);
    }

    public void connectToHost(Supplier<String> sesionCode) throws IOException {
        Packet packet = new Packet(Packet.PacketType.VIEWER);
        
        this.serverProtocol.sendPacket(packet);
        Packet recievedPacket = serverProtocol.recievePacket();
        if (recievedPacket.getPacketType() != PacketType.SUCCESS) {
            throw new RuntimeException("Server Error");
        }
        
        serverProtocol.sendPacket(new Packet(PacketType.SESSION_KEY, sesionCode.get().getBytes()));
        recievedPacket = serverProtocol.recievePacket();

        while (recievedPacket.getPacketType() == PacketType.CONNECTION_FAILED) {
            serverProtocol.sendPacket(new Packet(PacketType.SESSION_KEY, sesionCode.get().getBytes()));
            recievedPacket = serverProtocol.recievePacket();
        }

        if (recievedPacket.getPacketType() != PacketType.SUCCESS) {
            throw new RuntimeException(recievedPacket.getPacketType().name());
        }
        this.hasConnectedTohost = true;
    }

    public boolean hasConnectedTohost() {
        return this.hasConnectedTohost;
    }
    public static void main(String[] args) {
        try {
            ViewerManager viewerManager = new ViewerManager(SERVER_IP, SERVER_PORT);
            viewerManager.startViewer();
            
        } catch (IOException e) {e.getMessage();}

    }
}

interface ConnectableToHost {
    // on call connect to the host
    public void connectToHost(Supplier<String> sesionCode) throws IOException;

    // has host been connected
    public boolean hasConnectedTohost();
}


