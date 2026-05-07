package host;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import resources.*;
import resources.Packet.PacketType;

public class NetworkReciever implements Runnable {
    private final ServerProtocol serverProtocol;
    
    // Configurable static path for incoming files on the Host
    public static String DOWNLOAD_DIR = "downloads";
    private final RemoteActionListener robotController;
    private final HandleDissconnection hostManager;

    private boolean isRunning;
    private String cryptoKey;

    public NetworkReciever(ServerProtocol serverProtocol, RemoteActionListener robotController, HandleDissconnection hostManager) {
        this.serverProtocol = serverProtocol;
        this.robotController = robotController;
        this.hostManager = hostManager;
        
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
                Packet packet = serverProtocol.recievePacket();
                
                if (packet != null && cryptoKey != null && packet.getPayload() != null && packet.getPayload().length > 0) {
                    byte[] decryptedPayload = resources.CryptoUtils.decrypt(packet.getPayload(), cryptoKey);
                    packet = new Packet(packet.getPacketType(), decryptedPayload);
                }

                PacketType type = packet.getPacketType();

                switch (type) {
                    case MOUSE_MOVE:
                        robotController.onMouseMove(packet);
                        break;
                    case MOUSE_BUTTON:
                        robotController.onMouseButton(packet);
                        break;
                    case MOUSE_WHEEL:
                        robotController.onMouseWheel(packet);
                        break;
                    case KEY_PRESS:
                        robotController.onKeyPress(packet);
                        break;
                    case QUIT:
                        this.isRunning = false;
                        hostManager.handleQuitRequest();
                        break;
                    case FILE:
                        String fileName = ServerProtocol.getFileNameFromPacket(packet);
                        byte[] fileData = ServerProtocol.getFileDataFromPacket(packet);
                        try {
                            java.nio.file.Path dirPath = Paths.get(DOWNLOAD_DIR);
                            if (!Files.exists(dirPath)) {
                                Files.createDirectories(dirPath);
                            }
                            Files.write(dirPath.resolve(fileName), fileData);
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            if (isRunning) {
                hostManager.handleConnectionLost();
            }
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
}