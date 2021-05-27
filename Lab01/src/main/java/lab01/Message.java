package lab01;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class Message {
    private final int cType;
    private final int bUserID;
    private byte[] msg;
    private final byte[] SECRET_KEY = "INeed16CharsHere".getBytes(StandardCharsets.UTF_8);
    private final String ALGORITHM = "AES";
    private final String ALGORITHM_INSTANCE_PATH = "AES/ECB/PKCS5Padding";

    public static final int INFO_LENGTH = 8;

    public Message(int cType, int bUserID, byte[] message) {
        this.cType = cType;
        this.bUserID = bUserID;
        this.msg = message;
    }

    public int getCType() {
        return cType;
    }

    public int getBUserID() {
        return bUserID;
    }

    public byte[] getMsg() {
        return msg;
    }

    public int fullMessageLength(){
        return INFO_LENGTH + msg.length;
    }

    public byte[] bytesForPacket(){
        return ByteBuffer.allocate(fullMessageLength())
                .putInt(cType)
                .putInt(bUserID)
                .put(msg)
                .array();
    }

    public void encode()throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(ALGORITHM_INSTANCE_PATH);
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY, ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        msg = cipher.doFinal(msg);
    }

    public void decode()throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(ALGORITHM_INSTANCE_PATH);
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY, ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        msg = cipher.doFinal(msg);
    }
}
