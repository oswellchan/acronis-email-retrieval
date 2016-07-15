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
    private static Storage mem = Storage.getInstance();

    private static SecretKey generateKey(String pw) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(pw.toCharArray(), salt.getBytes(), 65536, 128);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        return secret;
    }

    public static String encrypt(String pw, byte[] plaintext, String msgId) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidParameterSpecException,
            UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        SecretKey secret = generateKey(pw);
        System.out.println(secret.getAlgorithm());
        System.out.println(secret.getEncoded().length);
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();
        byte[] ciphertext = cipher.doFinal(plaintext);

        //Save IV for decryption
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        mem.updateIdToIVMap(msgId, iv);

        return Base64.getEncoder().encodeToString(ciphertext);
    }

    public static String decrypt(String pw, byte[] ciphertext, String msgId) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidParameterSpecException,
            UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        byte[] val = Base64.getDecoder().decode(ciphertext);

        SecretKey secret = generateKey(pw);
        byte[] iv = mem.getIV(msgId);
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        byte[] plaintext = cipher.doFinal(val);

        return Base64.getEncoder().encodeToString(plaintext);
    }
}
