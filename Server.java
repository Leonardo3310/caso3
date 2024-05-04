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

import java.util.Random;

import java.lang.Math.*;

public class Server extends Thread{

    //llave privada del servidor
    private PrivateKey llavePrivada;

    // llave publica del servidor
    public PublicKey llavePublica;

    // numero P para calcular Diffie Hellman
    private BigInteger numeroP;

    // numero G especifico para calcular Diffie Hellman
    private Integer numeroG;

    // numero X privado para calcular Diffie Hellman
    private Integer numeroX;


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


    // Funcion que hace pasar de un string de hexadecimales a una cadena de bytes
    // Esto debido a que el numero P esta en hexadecimal
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

    // Funcion para generar el numero P y G para el algoritmo de diffie hellman
    public void generatePandG(){
        
        // Numero P en formato String
        String numeroPString = "009b626f2511b600d65ab765bf0512523b1652f91ac616b04f67d110dfeb1f3474e2de30031ba08ff13addee5ca004093860cd0f704da25a3431adde8a9c4c827a9121d25f61748d924418433b264fd1dac5fe11f9b66a53c2215a38fbd696ac0f04af7674d929a1965abd0bfd0f78aef59c7898e82276aff7ef7292a11313a9ff";
        
        // Pasamos el numero P a bytes por medio de la funcion "hexStringToByteArray"
        byte[] bytes =hexStringToByteArray(numeroPString);

        // numero G en este caso es igual a 2
        numeroG = 2;
       
        // Asignamos como Big Integer el numero P
        numeroP =  new BigInteger(bytes);

        Random random = new Random();

        numeroX = random.nextInt(15);
    }



    public PublicKey getPublicKey(){
        return llavePublica;
    }
    
    public void run() {
        try {
            int port = 1234; // Puerto en el que escucha el servidor
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Servidor iniciado en el puerto " + port);
            Boolean bandera = true;

            while (bandera) {
                Socket clientSocket = serverSocket.accept(); // Aceptar conexión del cliente
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                // PASO 2 : INICIALIZACION Y RECIBO DEL RETO
                // Aca tomamos el String que mando el cliente el cual inciializa la conexion con el servidor como SECURE INIT
                String inicializacion = (String) in.readObject();

        

                // Si el string esta incorrecto es decir que la inicializacion esta mal por lo cual enviamos una excepcion
                if(! (inicializacion.equals("SECURE INIT"))){
                    
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

                // recibimos si el reto esta correcto
                String confirmacion = (String)in.readObject();

                // verificamos la respuesta para poder continuar
                if(confirmacion.equals("ERROR")){
                    throw new Exception("Reto no verificado, mal hecho");
                }

                // llamamos a la funcion de generar P  y G 
                // Guarda esos numero en variables globales para que los usemos
                generatePandG();

                // Generar el numero G elevado a la X 
                Integer numeroGElevadoAX = (int) Math.pow((double)numeroG, (double)numeroX);

                // calculamos el vector de inicializacion
                byte[] vectorInicializacion = generateIV();

                // Concatenamos los diferentes numeros en un string para enviarlos al Cliente
                String numerosConcatenados = numeroG + "" + numeroP + numeroGElevadoAX ;
            

                System.out.println(numerosConcatenados);

                // Hacemos el cifrado de el numero G, numero P y el numero G elevado a la x como String
                String numerosCifrados = cipherRSA.firmarString(numerosConcatenados, llavePrivada);


                out.writeObject(numeroG);
                out.writeObject(numeroP);
                out.writeObject(numeroGElevadoAX);
                out.writeObject(vectorInicializacion);
                out.writeObject(numerosCifrados);


            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
        

        
    }
}
