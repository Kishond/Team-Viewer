package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import server.Packet.PacketType;

public class ServerProtocol {
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    public ServerProtocol(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = new DataInputStream(inputStream);
        this.outputStream = new DataOutputStream(outputStream);
    }

    public Packet recievePacket() throws IOException {
        int payloadLength = this.inputStream.readInt();
        byte byteSign = this.inputStream.readByte();
        byte[] payload = new byte[payloadLength];

        this.inputStream.readFully(payload);
        return new Packet(PacketType.getPacketType(byteSign), payload);
    }

    public void sendPacket(Packet packet) throws IOException {
        this.outputStream.writeInt((byte) packet.getPayload().length);
        this.outputStream.writeByte(packet.getPacketType().getByteSign());
        this.outputStream.write(packet.getPayload());

        this.outputStream.flush();
    }

    public String getPacketStringPayload() throws IOException {
        Packet packet = recievePacket();
        if (packet.getPacketType() != Packet.PacketType.SESSION_KEY) {
            throw new IllegalArgumentException("packet type is not a string valid");
        }
        return new String(packet.getPayload(), StandardCharsets.UTF_8);
    }

    public void sendErrStringPacket(String errMsg) throws IOException {
        Packet packet = new Packet(Packet.PacketType.ERR_MESSAGE, errMsg.getBytes(StandardCharsets.UTF_8));

        sendPacket(packet);
    }

    public DataInputStream getInputStream() {
        return this.inputStream;
    }

    public DataOutputStream getOutputStream() {
        return this.outputStream;
    }
}

