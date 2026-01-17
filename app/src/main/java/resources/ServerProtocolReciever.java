package resources;

import java.io.IOException;

public interface ServerProtocolReciever {
    public Packet recievePacket() throws IOException;
    public String getPacketStringPayload() throws IOException;
}
