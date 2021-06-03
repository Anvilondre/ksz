package practice1;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Packet {
    private static final byte MAGIC_BYTE = 0x13;
    private final byte client;
    private final long packetID;
    Message message;

    public Packet(byte client, long packetID, Message message) {
        this.client = client;
        this.packetID = packetID;
        this.message = message;
    }



    public byte getClient() {
        return client;
    }

    public long getPacketID() {
        return packetID;
    }

    public Message getMessage() {
        return message;
    }


    public static Packet decodePackage(byte[] bytes) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);

        if(bb.get() != MAGIC_BYTE){
            throw new IllegalArgumentException("Magic byte");
        }

        byte client = bb.get();
        System.out.println("Client: " + client);
        long packet = bb.getLong();
        System.out.println("Packet id: " + packet);
        int messageLength = bb.getInt();
        System.out.println("Length: " + messageLength);
        short crc16Head = bb.getShort();
        System.out.println("CRC16 Head: " + crc16Head);

        byte[] head = ByteBuffer.allocate(14)
                .order(ByteOrder.BIG_ENDIAN)
                .put(MAGIC_BYTE)
                .put(client)
                .putLong(packet)
                .putInt(messageLength)
                .array();

        if(CRC16.crc16(head) != crc16Head){
            throw new IllegalArgumentException("CRC16 Head");
        }

        int cType = bb.getInt();
        int bUserID = bb.getInt();


        byte[] checkMessage = Arrays.copyOfRange(bytes, 16, 16 + messageLength);
        short crc16Message = bb.getShort(16 + messageLength);
        if(CRC16.crc16(checkMessage) != crc16Message){
            throw new IllegalArgumentException("CRC16 Message");
        }

        byte[] text = Arrays.copyOfRange(bytes, 24, 24 + messageLength - Message.INFO_LENGTH);
        Message message = new Message(cType, bUserID, text);
        message.decode();
        return new Packet(client, packet, message);
    }

    public static byte[] encodePackage(Packet packet) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Message message = packet.getMessage();
        message.encode();
        byte[] head = ByteBuffer.allocate(14)
                .order(ByteOrder.BIG_ENDIAN)
                .put(MAGIC_BYTE)
                .put(packet.getClient())
                .putLong(packet.getPacketID())
                .putInt(message.fullMessageLength())
                .array();

        return ByteBuffer.allocate(16 + message.fullMessageLength() + 2)
                .order(ByteOrder.BIG_ENDIAN)
                .put(head)
                .putShort(CRC16.crc16(head))
                .put(message.bytesForPacket())
                .putShort(CRC16.crc16(message.bytesForPacket()))
                .array();
    }

    @Override
    public String toString() {
        return "{" +
                "client: " + client +
                "; packet: " + packetID +
                "; message: " + message.toString() +
                '}';
    }
}
