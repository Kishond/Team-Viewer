package host;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import resources.*;

public class NetworkSender implements HostActionsListener, Runnable {
    private final ServerProtocolSender serverProtocol;
    private final HandleDissconnection hostManager;
    
    private final BlockingQueue<Packet> packetQueue;
    private volatile boolean isRunning;
    private String cryptoKey;

    public NetworkSender(ServerProtocolSender serverProtocol, HandleDissconnection hostManager) {
        this.serverProtocol = serverProtocol;
        this.hostManager = hostManager;
        
        this.packetQueue = new LinkedBlockingQueue<>();
        this.isRunning = false;
    }

    public void setCryptoKey(String key) {
        this.cryptoKey = key;
    }

    @Override
    public void run() {
        this.isRunning = true;
        try {
            while (isRunning) {
                // blocking method until packet is ready
                Packet packet = packetQueue.take();
                
                if (cryptoKey != null && packet.getPayload() != null && packet.getPayload().length > 0) {
                    byte[] encrypted = CryptoUtils.encrypt(packet.getPayload(), cryptoKey);
                    packet = new Packet(packet.getPacketType(), encrypted);
                }
                
                serverProtocol.sendPacket(packet);
                if (packet.getPacketType() == Packet.PacketType.QUIT) {
                    System.out.println("quit");
                    serverProtocol.sendPacket(new Packet(Packet.PacketType.QUIT));
                    System.out.println("sent");
                    isRunning = false;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            if (isRunning) {
                hostManager.handleConnectionLost();
            }
        }
    }

    @Override
    public void queueImage(byte[] imageData) {
        // droping unsent old frames to remove unnecssarry traffic
        packetQueue.removeIf(packet -> packet.getPacketType() == Packet.PacketType.IMAGE);
        packetQueue.offer(new Packet(Packet.PacketType.IMAGE, imageData));
    }

    @Override
    public void queueDisconnect() {
        // queues a QUIT packet to close the session
        packetQueue.offer(new Packet(Packet.PacketType.QUIT));
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}

interface HostActionsListener {
    /**
     * Sends the captured screen image to the viewer.
     */
    void queueImage(byte[] imageData);

    /**
     * Notifies the server and viewer that the host is disconnecting.
     */
    void queueDisconnect();
}