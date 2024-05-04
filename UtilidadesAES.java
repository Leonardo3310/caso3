import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

public class UtilidadesAES {
    public String encrypt(String plainText, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt(String cipherText, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decodedValue = Base64.getDecoder().decode(cipherText);
        byte[] decrypted = cipher.doFinal(decodedValue);
        return new String(decrypted);
    }
}
