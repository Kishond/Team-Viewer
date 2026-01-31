package viewer;

import resources.Packet;
import resources.Packet.PacketType;
import resources.ServerProtocolReciever;

public class NetworkReciever implements Runnable {
    private ServerProtocolReciever serverProtocol;
    private RemoteUpdateListener UI;
    private HandleDissconnection viewerManager;

    public NetworkReciever(ServerProtocolReciever serverProtocol, RemoteUpdateListener UI, HandleDissconnection viewerManager) {
        this.serverProtocol = serverProtocol;
        this.UI = UI;
        this.viewerManager = viewerManager;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Packet incomingPacket = serverProtocol.recievePacket();

                if (incomingPacket == null) break;

                if (incomingPacket.getPacketType() == PacketType.IMAGE) {
                    UI.onImageRecieved(incomingPacket.getPayload());
                } 
                
                else if (incomingPacket.getPacketType() == PacketType.QUIT) {
                    viewerManager.handleQuitRequest();
                    break; 
                }
            }
        } catch (Exception e) {
            // Any interruption or socket closure lands here
            this.viewerManager.handleConnectionLost();
        } finally {
            // Final cleanup message or logging
            System.out.println("NetworkReceiver has terminated.");
        }
    }
}