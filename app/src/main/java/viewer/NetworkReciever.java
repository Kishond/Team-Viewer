package viewer;

import resources.CryptoUtils;
import resources.Packet;
import resources.Packet.PacketType;
import resources.ServerProtocolReciever;

public class NetworkReciever implements Runnable {
    private ServerProtocolReciever serverProtocol;
    private RemoteUpdateListener UI;
    private HandleDissconnection viewerManager;
    private String cryptoKey;

    public NetworkReciever(ServerProtocolReciever serverProtocol, RemoteUpdateListener UI, HandleDissconnection viewerManager) {
        this.serverProtocol = serverProtocol;
        this.UI = UI;
        this.viewerManager = viewerManager;
    }

    public void setCryptoKey(String key) {
        this.cryptoKey = key;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Packet incomingPacket = serverProtocol.recievePacket();

                if (incomingPacket == null) break;
                
                if (cryptoKey != null && incomingPacket.getPayload() != null && incomingPacket.getPayload().length > 0) {
                    byte[] decryptedPayload = CryptoUtils.decrypt(incomingPacket.getPayload(), cryptoKey);
                    incomingPacket = new Packet(incomingPacket.getPacketType(), decryptedPayload);
                }

                if (incomingPacket.getPacketType() == PacketType.IMAGE) {
                    UI.onImageRecieved(incomingPacket.getPayload());
                } 
                
                else if (incomingPacket.getPacketType() == PacketType.QUIT) {
                    viewerManager.handleQuitRequest();
                    break; 
                }
            }
        } catch (Exception e) {
            this.viewerManager.handleConnectionLost();
        } finally {
            System.out.println("NetworkReceiver been shutted off");
        }
    }
}