package resources;

import java.io.IOException;

public interface ServerProtocolSender {
    public void sendPacket(Packet packet) throws IOException;
    public void sendErrStringPacket(String errMsg) throws IOException;
    public void sendSuccessPacket(String successMsg) throws IOException;
}
