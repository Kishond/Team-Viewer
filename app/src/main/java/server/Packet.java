package server;

public class Packet {
    enum PacketType {
        HOST((byte)0),
        VIEWER((byte)1),
        SESSION_KEY((byte)2),
        IMAGE((byte)3),
        COMMAND((byte)4),
        ERR_MESSAGE((byte)5),
        QUIT((byte)6);

        private final byte byteSign;

        private PacketType(byte byteSign) {
            this.byteSign = byteSign;
        }

        public byte getByteSign() {
            return this.byteSign;
        }

        public static PacketType getPacketType(byte b) {
            for (PacketType packetType : PacketType.values()) {
                if (packetType.getByteSign() == b) {
                    return packetType;
                }
            }
            throw new IllegalArgumentException("No packet type with this byte");
        }
    }

    private PacketType packetType;
    private byte[] payload;

    public Packet(PacketType packetType, byte[] payLoad) {
        this.packetType = packetType;
        this.payload = payLoad;
    }

    public PacketType getPacketType() {
        return this.packetType;
    }

    public byte[] getPayload() {
        return this.payload;
    }
}