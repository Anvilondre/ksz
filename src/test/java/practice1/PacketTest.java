package practice1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class PacketTest {

    @ParameterizedTest
    @CsvSource({
            "91, 654, 0, 1, heyok",
            "1, 253, 1232, 7, taraot",
            "0, 0000, 754, 2, ?",
            "1, 19876, 1249, 61, 1 "

    })
    void shouldEncodePackage(byte client, long packet, int cType, int bUserID, String text)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException,
            InvalidKeyException
    {
        Message message = new Message(cType, bUserID, text.getBytes(StandardCharsets.UTF_8));
        Packet inputPacket = new Packet(client, packet, message);
        byte[] encodedInput = Packet.encodePackage(inputPacket);

        Packet decoded = Packet.decodePackage(encodedInput);

        assertEquals(client, decoded.getClient());
        
        assertEquals(packet, decoded.getPacketID());
        
        assertEquals(cType, decoded.getMessage().getCType());
        
        assertEquals(bUserID, decoded.getMessage().getBUserID());
        
        assertEquals(text, new String(decoded.getMessage().getMsg(), StandardCharsets.UTF_8));
    }
}