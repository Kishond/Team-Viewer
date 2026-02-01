package host;

import java.io.IOException;
import resources.*;
import resources.Packet.PacketType;

public class NetworkReciever implements Runnable {
    private final ServerProtocol serverProtocol;
    private final RemoteActionListener robotController;
    private final HandleDissconnection hostManager;

    private boolean isRunning;

    public NetworkReciever(ServerProtocol serverProtocol, RemoteActionListener robotController, HandleDissconnection hostManager) {
        this.serverProtocol = serverProtocol;
        this.robotController = robotController;
        this.hostManager = hostManager;
        
        this.isRunning = false;
    }

    @Override
    public void run() {
        this.isRunning = true;
        try {
            while (isRunning) {
                Packet packet = serverProtocol.recievePacket();

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