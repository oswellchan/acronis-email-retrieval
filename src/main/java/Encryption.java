import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;

import java.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
    private static String salt = "salt";

    private static SecretKey generateKey(String pw) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(pw.toCharArray(), salt.getBytes(), 65536, 128);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        return secret;
    }

    public static byte[] encrypt(String email, byte[] plaintext, String msgId) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidParameterSpecException,
            UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        SecretKey secret = generateKey(email);
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();
        byte[] ciphertext = cipher.doFinal(plaintext);

        //Save IV for decryption
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        Storage.getInstance(email).updateIdToIVMap(msgId, iv);

        return Base64.getEncoder().encode(ciphertext);
    }

    public static byte[] decrypt(String email, byte[] ciphertext, String msgId) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidParameterSpecException,
            UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        byte[] val = Base64.getDecoder().decode(ciphertext);

        SecretKey secret = generateKey(email);
        byte[] iv = Storage.getInstance(email).getIV(msgId);
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        byte[] plaintext = cipher.doFinal(val);

        return Base64.getEncoder().encode(plaintext);
    }
}
