import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class UtilidadesCifrado {
    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // Para AES de 256 bits
        return keyGen.generateKey();
    }
}
