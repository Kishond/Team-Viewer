package resources;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import resources.Packet.PacketType;

public class ServerProtocol implements ServerProtocolReciever, ServerProtocolSender {
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    // --- DATA HOLDERS for extraction ---
    public static class ButtonData {
        public final int button;
        public final boolean isPressed;
        public ButtonData(int b, boolean p) {
             this.button = b; this.isPressed = p; 
            }
    }

    public static class KeyData {
        public final int keyCode;
        public final boolean isPressed;
        public KeyData(int k, boolean p) { 
            this.keyCode = k; this.isPressed = p; 
        }
    }

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
        this.outputStream.writeInt(packet.getPayload().length);
        this.outputStream.writeByte(packet.getPacketType().getByteSign());
        this.outputStream.write(packet.getPayload());

        this.outputStream.flush();
    }

    public String getPacketStringPayload() throws IOException {
    Packet packet = recievePacket();
    if (packet.getPacketType() != Packet.PacketType.SESSION_KEY) {
        throw new IllegalArgumentException("Expected SESSION_KEY but got: " + packet.getPacketType());
    }
    return new String(packet.getPayload(), StandardCharsets.UTF_8);
}

    public void sendErrStringPacket(String errMsg) throws IOException {
        Packet packet = new Packet(Packet.PacketType.ERR_MESSAGE, errMsg.getBytes(StandardCharsets.UTF_8));
        sendPacket(packet);
    }

    public void sendSuccessPacket(String successMsg) throws IOException {
        Packet packet = new Packet(Packet.PacketType.SUCCESS, successMsg.getBytes(StandardCharsets.UTF_8));
        sendPacket(packet);
    }

    public DataInputStream getInputStream() {
        return this.inputStream;
    }

    public DataOutputStream getOutputStream() {
        return this.outputStream;
    }

    // host side

    public static Packet createCordsPacket(int x, int y) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(x);
        buffer.putInt(y);
        return new Packet(PacketType.MOUSE_MOVE, buffer.array());
    }

    public static Packet createMouseButtonPacket(int button, boolean isPressed) {
        ByteBuffer buffer = ByteBuffer.allocate(5); 
        buffer.putInt(button);
        buffer.put((byte) (isPressed ? 1 : 0));
        return new Packet(PacketType.MOUSE_BUTTON, buffer.array());
    }

    public static Packet createMouseWheelPacket(int rotation) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(rotation);
        return new Packet(PacketType.MOUSE_WHEEL, buffer.array());
    }

    public static Packet createKeyPressPacket(int keyCode, boolean isPressed) {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.putInt(keyCode);
        buffer.put((byte) (isPressed ? 1 : 0));
        return new Packet(PacketType.KEY_PRESS, buffer.array());
    }

    public static Packet createQuitPacket() {
        return new Packet(PacketType.QUIT, new byte[0]);
    }

    // viewer side

    public static int[] getCordsFromPacket(Packet packet) {
        ByteBuffer buffer = ByteBuffer.wrap(packet.getPayload());
        return new int[]{buffer.getInt(), buffer.getInt()};
    }

    public static ButtonData getButtonFromPacket(Packet packet) {
        ByteBuffer buffer = ByteBuffer.wrap(packet.getPayload());
        int button = buffer.getInt();
        boolean isPressed = buffer.get() == 1;
        return new ButtonData(button, isPressed);
    }

    public static int getWheelRotationFromPacket(Packet packet) {
        ByteBuffer buffer = ByteBuffer.wrap(packet.getPayload());
        return buffer.getInt();
    }

    public static KeyData getKeysFromPacket(Packet packet) {
        ByteBuffer buffer = ByteBuffer.wrap(packet.getPayload());
        int keyCode = buffer.getInt();
        boolean isPressed = buffer.get() == 1;
        return new KeyData(keyCode, isPressed);
    }

    public static Packet createFilePacket(String fileName, byte[] fileData) {
        byte[] nameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + nameBytes.length + fileData.length);
        buffer.putInt(nameBytes.length);
        buffer.put(nameBytes);
        buffer.put(fileData);
        return new Packet(PacketType.FILE, buffer.array());
    }

    public static String getFileNameFromPacket(Packet packet) {
        ByteBuffer buffer = ByteBuffer.wrap(packet.getPayload());
        int nameLen = buffer.getInt();
        byte[] nameBytes = new byte[nameLen];
        buffer.get(nameBytes);
        return new String(nameBytes, StandardCharsets.UTF_8);
    }

    public static byte[] getFileDataFromPacket(Packet packet) {
        ByteBuffer buffer = ByteBuffer.wrap(packet.getPayload());
        int nameLen = buffer.getInt();
        buffer.position(4 + nameLen);
        byte[] fileData = new byte[buffer.remaining()];
        buffer.get(fileData);
        return fileData;
    }
}