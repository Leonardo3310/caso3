import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;

public class Server extends Thread{

    private PrivateKey llavePrivada;
    public PublicKey llavePublica;
    private BigInteger numeroP;
    private Integer numeroG;


    // Aca inicializamos la clase para firmar y verificar los datos que nos envien
    private UtilidadesRSA cipherRSA;

    public Server() throws NoSuchAlgorithmException{

        // Generamos el par de llaves con el algoritmo RSA
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        // Inicializamos el generador de las llaves
		generator.initialize(1024);
        //generamos el par de llaves
		KeyPair keyPair = generator.generateKeyPair();
		llavePrivada = keyPair.getPrivate();
        llavePublica =keyPair.getPublic();

        cipherRSA = new UtilidadesRSA();
    }



    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public byte[] calculateSHA512(String hexString) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            return digest.digest(hexStringToByteArray(hexString));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

     public byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        return iv;
    }


    public byte[] generateReto(){
        SecureRandom random = new SecureRandom();
        byte[] reto = new byte[16];
        random.nextBytes(reto);
        return reto;
    }


    public void generatePG(){
        String numeroString = "009b626f2511b600d65ab765bf0512523b1652f91ac616b04f67d110dfeb1f3474e2de30031ba08ff13addee5ca004093860cd0f704da25a3431adde8a9c4c827a9121d25f61748d924418433b264fd1dac5fe11f9b66a53c2215a38fbd696ac0f04af7674d929a1965abd0bfd0f78aef59c7898e82276aff7ef7292a11313a9ff";
        byte[] bytes = hexStringToByteArray(numeroString);

        byte[] digestWithSHA512 = calculateSHA512(numeroString);

        byte[] encryptionKey = Arrays.copyOfRange(digestWithSHA512, 0, 32); // Primeros 256 bits
        byte[] hmacKey = Arrays.copyOfRange(digestWithSHA512, 32, 64);      // Últimos 256 bits


        byte[] iv = generateIV();

        
        numeroG = 2;
        numeroP =  new BigInteger(1,bytes);
    }

    public PublicKey getPublicKey(){
        return llavePublica;
    }
    
    public void run() {
        try {
            int port = 1234; // Puerto en el que escucha el servidor
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Servidor iniciado en el puerto " + port);
            while (true) {

                Socket clientSocket = serverSocket.accept(); // Aceptar conexión del cliente
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                // PASO 2 : INICIALIZACION Y RECIBO DEL RETO
                // Aca tomamos el String que mando el cliente el cual inciializa la conexion con el servidor como SECURE INIT
                String inicializacion = (String) in.readObject();

                System.out.println(inicializacion);

                // Si el string esta incorrecto es decir que la inicializacion esta mal por lo cual enviamos una excepcion
                if(! (inicializacion.equals("SECURE INIT"))){
                    System.out.println(inicializacion.equals("SECURE INIT"));
                    throw new Exception("Inicializacion mal hecha");
                }

                // Cuando ya inicializamos la conexion recibimos el reto que es un array de 16 bytes aleatorios
                byte[] reto =(byte[]) in.readObject();

                // Ciframos el reto que acabamos de recibir con nuestra llave privada
                byte[] retoCifrado = cipherRSA.firmar(reto, llavePrivada);

                // validamos que el cifrado se hizo de forma completa
                if(retoCifrado ==null){
                    throw new Exception("Cifrado RSA PASO 2 es null");

                }

                // Enviamos el reto cifrado al cliente
                out.writeObject(retoCifrado);

                

                
                





                
                

                
                
                


            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
        

        
    }
}
