package viewer;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import resources.*;

public class NetworkSender implements ViewerUIListener, Runnable {
    private final ServerProtocolSender serverProtocol;
    private final HandleDissconnection viewerManager;
    
    private final BlockingQueue<Packet> packetQueue;

    private boolean isRunning;
    private String cryptoKey;

    public NetworkSender(ServerProtocolSender serverProtocol, HandleDissconnection viewerManager) {
        this.serverProtocol = serverProtocol;
        this.viewerManager = viewerManager;

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
                Packet packet = packetQueue.take();
                
                if (cryptoKey != null && packet.getPayload() != null && packet.getPayload().length > 0) {
                    byte[] encrypted = CryptoUtils.encrypt(packet.getPayload(), cryptoKey);
                    packet = new Packet(packet.getPacketType(), encrypted);
                }

                serverProtocol.sendPacket(packet);
                
                // if recieved QUIT packet, stop and notify viewer manager
                if (packet.getPacketType() == Packet.PacketType.QUIT) {
                    this.viewerManager.handleQuitRequest();
                    isRunning = false;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            viewerManager.handleConnectionLost();
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public void sendMouseMove(int x, int y) {
        packetQueue.offer(ServerProtocol.createCordsPacket(x, y));
    }

    @Override
    public void sendMouseButton(int button, boolean isPressed) {
        packetQueue.offer(ServerProtocol.createMouseButtonPacket(button, isPressed));
    }

    @Override
    public void sendMouseWheel(int rotation) {
        packetQueue.offer(ServerProtocol.createMouseWheelPacket(rotation));
    }

    @Override
    public void sendKeyPress(int keyCode, boolean isPressed) {
        packetQueue.offer(ServerProtocol.createKeyPressPacket(keyCode, isPressed));
    }

    @Override
    public void sendDisconnect() {
        packetQueue.offer(ServerProtocol.createQuitPacket());
    }
    
}

interface ViewerUIListener {
    // Mouse Events
    void sendMouseMove(int x, int y);
    void sendMouseButton(int button, boolean isPressed); // 1=Left, 2=Middle, 3=Right
    void sendMouseWheel(int rotation);

    // Keyboard Events
    void sendKeyPress(int keyCode, boolean isPressed);

    // Connection Events
    void sendDisconnect();
}
