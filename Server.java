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
import java.util.Base64;
import java.util.Random;

import javax.crypto.spec.SecretKeySpec;

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
    private BigInteger numeroX;


    // Aca inicializamos la clase para firmar y verificar los datos que nos envien
    private UtilidadesRSA cipherRSA;


    // Aca inicializamos la clase para el cifrado asimetrico
    private UtilidadesAES cipherAES;


    private int port;

    public Server(int port) throws NoSuchAlgorithmException{

        // Generamos el par de llaves con el algoritmo RSA
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        // Inicializamos el generador de las llaves
		generator.initialize(1024);
        //generamos el par de llaves
		KeyPair keyPair = generator.generateKeyPair();
        // Sacamos la llave privada
		llavePrivada = keyPair.getPrivate();
        // Sacamos la llave Publica
        llavePublica =keyPair.getPublic();
        // Asignamos el cipher como utilidades para el cifrado Asimetrico
        cipherRSA = new UtilidadesRSA();
        // Asignamos el cipher como utilidades para el cifrado simetrico
        cipherAES = new UtilidadesAES();

        this.port = port;
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

    public byte[] calculateSHA512(byte[] messageInString) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            return digest.digest(messageInString);
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


    public void generateKeys(BigInteger llaveMaestra){
        
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

        // Usamos Random para crear el numero x
        Random random = new Random();

        // añadimos el limite al que puede estar
        Integer limite = numeroP.subtract(new BigInteger("1")).intValue();

        // Generar un número aleatorio dentro del rango [0, limiteInt)
        int numeroXInt = random.nextInt(limite);

        // Convertir el número aleatorio generado a un BigInteger
        numeroX = BigInteger.valueOf(numeroXInt);

        
    }

    



    public PublicKey getPublicKey(){
        return llavePublica;
    }
    
    public void run() {
        try {
           
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Servidor iniciado en el puerto " + port);

           
            Socket clientSocket = serverSocket.accept(); // Aceptar conexión del cliente
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

            // PASO 2 : INICIALIZACION Y RECIBO DEL RETO
                // Aca tomamos el String que mando el cliente el cual inciializa la conexion con el servidor como SECURE INIT
                String inicializacion = (String) in.readObject();

        

                // Si el string esta incorrecto es decir que la inicializacion esta mal por lo cual enviamos una excepcion
                if(! (inicializacion.equals("SECURE INIT"))){
                    serverSocket.close();
                    clientSocket.close();
                    
                    throw new Exception("Inicializacion mal hecha");
                }

                // Cuando ya inicializamos la conexion recibimos el reto que es un array de 16 bytes aleatorios
                byte[] reto =(byte[]) in.readObject();

                // Ciframos el reto que acabamos de recibir con nuestra llave privada
                byte[] retoCifrado = cipherRSA.firmar(reto, llavePrivada);

                // validamos que el cifrado se hizo de forma completa
                if(retoCifrado ==null){
                    serverSocket.close();
                    clientSocket.close();
                    throw new Exception("Cifrado RSA PASO 2 es null");

                }

                // Enviamos el reto cifrado al cliente
                out.writeObject(retoCifrado);

                // recibimos si el reto esta correcto
                String confirmacion = (String)in.readObject();

                // verificamos la respuesta para poder continuar
                if(confirmacion.equals("ERROR")){
                    serverSocket.close();
                    clientSocket.close();
                    throw new Exception("Reto no verificado, mal hecho");
                }
                

                // llamamos a la funcion de generar P  y G
                // Guarda esos numero en variables globales para que los usemos
                generatePandG();

                // Generar el numero G elevado a la X 
                BigInteger numeroGElevadoAX = BigInteger.valueOf(numeroG).pow(numeroX.intValue()).mod(numeroP);

                // calculamos el vector de inicializacion
                byte[] vectorInicializacion = generateIV();

                // Concatenamos los diferentes numeros en un string para enviarlos al Cliente
                String numerosConcatenados = numeroG + "" + numeroP + numeroGElevadoAX.toString() ;

                // Hacemos el cifrado de el numero G, numero P y el numero G elevado a la x como String
                String numerosCifrados = cipherRSA.firmarString(numerosConcatenados, llavePrivada);

                // Enviamos el numero G al cliente
                out.writeObject(numeroG);
                // Enviamos el numero P al cliente
                out.writeObject(numeroP);
                // Enviamos el numero G elevdo a la X al cliente
                out.writeObject(numeroGElevadoAX);
                // Enviamos el vector de inciializacion para el cifrado simetrico
                out.writeObject(vectorInicializacion);
                // enviamos los numeros P, G y G elevado a la x cifrados, y se lo enviamos al cliente
                out.writeObject(numerosCifrados);

                

                //esperamos el mensaje de verificacion del cliente
                String verificacionNumeros = (String)in.readObject();

                // Alzamos una excepcion si existe un error en la verificacion de los numeros
                if(verificacionNumeros.equals("ERROR")){
                    clientSocket.close();
                    serverSocket.close();
                    throw new Exception("Reto no verificado, mal hecho");
                }
                

                // recibir el numero G elevado a la Y del cliente
                BigInteger numeroGElevadoALaY = (BigInteger)in.readObject();

                // elevar a la x el numero que nos dieron elevado a la y
                BigInteger numeroFinal = numeroGElevadoALaY.modPow(numeroX , numeroP);

                

                // Hacer digest con SHA-512
                byte[] digestWithSHA512 = calculateSHA512(numeroFinal.toByteArray());

                // Con los primeros 256 bits sacar la llave para encriptar
                byte[] bytesSimetrica = Arrays.copyOfRange(digestWithSHA512, 0, 32); // Primeros 256 bits

                // con los ultimos 256 bits sacar la llave para hacer el HMAC
                byte[] bytesHash = Arrays.copyOfRange(digestWithSHA512, 32, 64);      // Últimos 256 bits

                // pasamos a llaves los bytes de la llave Simetrica
                SecretKeySpec llaveSimetrica = new SecretKeySpec(bytesSimetrica, "AES");

                // pasamos a llave los bytes de la llave para Hash
                SecretKeySpec llaveHash =  new SecretKeySpec(bytesHash, "HMACSHA256");


                // Le decimos al cliente que continue con el proceso
                out.writeObject("CONTINUAR");

                // recibimos los datos que nos envia el cliente de su login 
                String loginCifrado = (String) in.readObject();
                String contraseñaCifrada = (String)  in.readObject();
                String loginHashEncriptado = (String) in.readObject();
                String contraseñaHashEncriptada = (String) in.readObject();

               
                // Desencriptamos todo
                String login = cipherAES.decrypt(loginCifrado, llaveSimetrica, vectorInicializacion);
                String contraseña = cipherAES.decrypt(contraseñaCifrada, llaveSimetrica, vectorInicializacion);
                String loginHash =  cipherAES.decrypt(loginHashEncriptado, llaveSimetrica, vectorInicializacion);
                String contraseñaHash = cipherAES.decrypt(contraseñaHashEncriptada, llaveSimetrica, vectorInicializacion);

                // pasamos los logins y contraseña a Hash
                byte[] loginVerificadoBytes = cipherRSA.calcularHMac(llaveHash, login);
                String loginVerificadoString = Base64.getEncoder().encodeToString(loginVerificadoBytes);

                // pasamos las contraseñas a hash
                byte[] contraseñaVerificadaBytes = cipherRSA.calcularHMac(llaveHash, contraseña);
                String contraseñaVerificadaString = Base64.getEncoder().encodeToString(contraseñaVerificadaBytes);

                // verificamos que lo que nos enviaron es verdadero
                if(!loginHash.equals(loginVerificadoString)){
                    out.writeObject("ERROR");
                    clientSocket.close();
                    
                }
                if(!contraseñaHash.equals(contraseñaVerificadaString)){
                    out.writeObject("ERROR");
                    clientSocket.close();
                }
                else{
                    out.writeObject("OK");
                }

                // recibimos los numeros que nos enviaron
                String numeroCifrado = (String) in.readObject();
                String numeroHash = (String) in.readObject();

                // verificamos que el numero cifrado es el mismo del hash
                String numeroDescifrado = cipherAES.decrypt(numeroCifrado, llaveSimetrica, vectorInicializacion);

                byte[] numeroDescifradoHashBytes = cipherRSA.calcularHMac(llaveHash, numeroDescifrado);

                String numeroDescifradoHashString = Base64.getEncoder().encodeToString(numeroDescifradoHashBytes);


                if(!numeroDescifradoHashString.equals(numeroHash)){
                    clientSocket.close();
                    serverSocket.close();
                    throw new Exception("El numero no es el mismo que el enviado en el hash");
                }

                // PASAMOS EL NUMERO DESCIFRADO A INT y le restamos 1 
                Integer numeroRespuesta = Integer.parseInt(numeroDescifrado)- 1;

                // ciframos este Numero
                String numeroRespuestaCifrado = cipherAES.encrypt(String.valueOf(numeroRespuesta), llaveSimetrica, vectorInicializacion);

                // mandamos el numero a hash
                byte[] numeroRespuestaBytes = cipherRSA.calcularHMac(llaveHash, String.valueOf(numeroRespuesta));
                String numeroRespuestaHash = Base64.getEncoder().encodeToString(numeroRespuestaBytes);

                out.writeObject(numeroRespuestaCifrado);
                out.writeObject(numeroRespuestaHash);




                






                clientSocket.close();
                serverSocket.close();
                


                






            
        } catch (Exception e) {
            e.printStackTrace();
        } 
        

        
    }
}
