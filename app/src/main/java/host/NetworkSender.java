package host;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import resources.*;

public class NetworkSender implements HostActionsListener, Runnable {
    private final ServerProtocolSender serverProtocol;
    private final HandleDissconnection hostManager;
    
    private final BlockingQueue<Packet> packetQueue;
    private boolean isRunning;

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
                    this.hostManager.handleQuitRequest();
                    isRunning = false;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            hostManager.handleConnectionLost();
        }
    }

    @Override
    public void sendImage(byte[] imageData) {
        // RobotController calls this to queue a new screenshot
        packetQueue.offer(new Packet(Packet.PacketType.IMAGE, imageData));
    }

    @Override
    public void sendDisconnect() {
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
    void sendImage(byte[] imageData);

    /**
     * Notifies the server and viewer that the host is disconnecting.
     */
    void sendDisconnect();
}