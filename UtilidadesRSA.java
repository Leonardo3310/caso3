
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.Mac;



public class UtilidadesRSA {


    public byte[] firmar(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    public boolean verificarFirma(byte[] data, byte[] signature, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(signature);
    }

    public String firmarString(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    public boolean verificarFirmaString(String data, String signature, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data.getBytes());
        return sig.verify(Base64.getDecoder().decode(signature));
    }

    public byte[] calcularHMac(Key llaveCifrado, String mensaje) throws NoSuchAlgorithmException, InvalidKeyException{
        Mac mac = Mac.getInstance("HmacSHA256");

        mac.init(llaveCifrado);

        byte [] hmac = mac.doFinal(mensaje.getBytes());

        return hmac;


    }

    
}
