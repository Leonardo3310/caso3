import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Server extends Thread{

    private BigInteger llavePrivada;
    public BigInteger llavePublica;
    private BigInteger numeroP;
    private Integer numeroG;

    public Server() throws NoSuchAlgorithmException{
        // generacion de llaves privadas y publicas

        KeyGenerator keygenerator = KeyGenerator.getInstance("AES"); 
        SecretKey key = keygenerator.generateKey();
    }


    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static byte[] calculateSHA512(String hexString) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            return digest.digest(hexStringToByteArray(hexString));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

     public static byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        return iv;
    }


    public static byte[] generateReto(){
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

    @Override
    public void run() {
        

        try {
            int port = 1234; // Puerto en el que escucha el servidor
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Servidor iniciado en el puerto " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Aceptar conexión del cliente
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                
                

                String inputLine = in.readLine();
                String[] partido = inputLine.split(",");

                if(partido[0].equals("SECURE INIT")){
                    byte[] reto = generateReto();
                    BigInteger retoInt = new BigInteger(reto);

                    // sacamos el RETO y lo ciframos con nuestra llave privada 
                }
                
                serverSocket.close();


            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}
