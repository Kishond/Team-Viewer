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

    public NetworkSender(ServerProtocolSender serverProtocol, HandleDissconnection hostManager) {
        this.serverProtocol = serverProtocol;
        this.hostManager = hostManager;
        
        this.packetQueue = new LinkedBlockingQueue<>();
        this.isRunning = false;
    }

    @Override
    public void run() {
        this.isRunning = true;
        try {
            while (isRunning) {
                // Blocks until a packet is offered via the HostActionsListener methods
                Packet packet = packetQueue.take();
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
        // Drop any unsent old frames to prevent latency buildup
        packetQueue.removeIf(packet -> packet.getPacketType() == Packet.PacketType.IMAGE);
        // RobotController calls this to queue a new screenshot
        packetQueue.offer(new Packet(Packet.PacketType.IMAGE, imageData));
    }

    @Override
    public void queueDisconnect() {
        // Queues a QUIT packet to gracefully close the session
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