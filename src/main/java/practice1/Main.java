package practice1;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Main {

    public static void main(String[] args) throws NoSuchPaddingException, IllegalBlockSizeException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        Message sent_msg = new Message(1, 1, "My name is Roman!".getBytes(StandardCharsets.UTF_8));
        Packet input = new Packet((byte) 1, 42, sent_msg);

        byte[] encodedInput = Packet.encodePackage(input);
        Packet packet = Packet.decodePackage(encodedInput);
        String received_msg = new String(packet.message.getMsg(), StandardCharsets.UTF_8);
        System.out.println(received_msg);
    }
}
