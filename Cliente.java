import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;


public class Cliente extends Thread {

    private Server servidor;
    private UtilidadesRSA cipherRSA;


    private UtilidadesAES cipherAES;

    private BigInteger numeroY;

    private String login;

    private String contraseña;

    private int port;

    public Cliente(Server servidor, int port){
        this.servidor = servidor;
        this.cipherRSA = new UtilidadesRSA();
        this.cipherAES = new UtilidadesAES();
        this.port = port;
    }

   

    public void createNumeroY(BigInteger numeroP){
         // Usamos Random para crear el numero x
         Random random = new Random();

         // añadimos el limite al que puede estar
         Integer limite = numeroP.subtract(new BigInteger("1")).intValue();
 
         // Generar un número aleatorio dentro del rango [0, limiteInt)
         int numeroXInt= random.nextInt(limite);

         // asignamos al numero x 
         numeroY = BigInteger.valueOf(numeroXInt);
        
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


    public void createLoginAndPassword(){
        // en este caso creamos un login y un password aleatorios

        // usamos la clase UIID para crear cadenas de caracteres aleatorias
        UUID uuid =  UUID.randomUUID();

        // Convertimos este en un stirng y sacamos las primeras 10 letras
        login  =uuid.toString().substring(0,10);

        // usamos las otras diez letras para hacer la contraseña
        contraseña = uuid.toString().substring(11,21);

    }

    @Override
    public void run()  {
        String hostName = "localhost"; // Dirección del servidor

        try{
            Socket socket = new Socket(hostName, port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());


            // PASO 1, ENVIO DE SECURE INIT Y RETO
            // Generamos el reto que es un arreglo de bytes.

            byte[] reto = generateReto();
            // enviamos el SECURE INIT para asegurar la conexion con el servidor

            out.writeObject("SECURE INIT");

            // enviamos el objeto reto para que el servidor haga con este lo que tiene que hacer
            out.writeObject(reto);

            // ahora tenemos el reto cifrado con la llave privada del servidor
            byte[] retoCifrado = (byte[]) in.readObject();

            // Pedimos la llave publica del servidor
            PublicKey publicKey = servidor.llavePublica;


            // Con la llave publica desciframos el reto que teniamos
            Boolean retoVerificado = cipherRSA.verificarFirma(reto, retoCifrado, publicKey);
           
            // Verificamos en esta parte si coincidio la verificacion del reto o si no
            if(retoVerificado){
                out.writeObject("OK");
            }
            else{
                out.writeObject("ERROR");
            }


            //esperamos el numero G
            Integer numeroG = (int)in.readObject();
            //esperamos el numero P
            BigInteger numeroP = (BigInteger)in.readObject();

            //esperamos el numero G elevado a la X
            BigInteger numeroGElevadoALaX =(BigInteger) in.readObject();

            // esperamos el vector de inicializacion
            byte[] vectorInicializacion = (byte[])in.readObject();

            // esperamos los numero cifrados
            String numerosCifrados = (String) in.readObject();

            // Generamos la concatenacion de los numeros
            String numerosConcatenados = numeroG + "" + numeroP + numeroGElevadoALaX ;
            
            
            //verificamos los numero cifrados
            Boolean verificacionNumerosCifrados = cipherRSA.verificarFirmaString(numerosConcatenados, numerosCifrados, publicKey);

            // Mandamos la confirmacion de los numeros cifrados al servidor
            if(verificacionNumerosCifrados){
                out.writeObject("OK");
            }
            else{
                out.writeObject("ERROR");
            }

            

            

            // crear el numero X Secreto
            createNumeroY(numeroP);

            // Crear el numero G elevado a la Y
            BigInteger numeroGElevadoALaY = BigInteger.valueOf(numeroG).pow(numeroY.intValue()).mod(numeroP);

          

            //enviar el numero G elevado a la Y
            out.writeObject(numeroGElevadoALaY);

            // elevar a la y el numero G elevado a la x
            BigInteger numeroFinal = numeroGElevadoALaX.modPow( numeroY,numeroP);


            

            // Hacer digest con SHA-512
            byte[] digestWithSHA512 = calculateSHA512(numeroFinal.toByteArray());

            // Con los primeros 256 bits sacar la llave para encriptar
            byte[] bytesSimetrica = Arrays.copyOfRange(digestWithSHA512, 0, 32); 
                
            // con los ultimos 256 bits sacar la llave para hacer el HMAC
            byte[] bytesHash = Arrays.copyOfRange(digestWithSHA512, 32, 64);      

            // pasamos a llaves los bytes de la llave Simetrica
            SecretKeySpec llaveSimetrica = new SecretKeySpec(bytesSimetrica, "AES");

            // pasamos a llave los bytes de la llave para Hash
            SecretKeySpec llaveHash =  new SecretKeySpec(bytesHash, "HMACSHA256");


            // recibimos la respuesta del servidor para continuar con el proceso
            String preguntaContinuar = (String)in.readObject();

            // validamos la respuesta del servidor para continuar con lo propuesto
            if(!preguntaContinuar.equals("CONTINUAR")){
                socket.close();
                throw new Exception("El servidor no desea continuar con la conexion");
            }


            // creamos un nuevo login y una contraseña
            createLoginAndPassword();


            

            // ciframos con AES y la llave simetrica el login
            String loginCifrado = cipherAES.encrypt(login, llaveSimetrica, vectorInicializacion);

            //ciframos con AES y la llave simetrica la contraseña

            String contraseñaCifrada = cipherAES.encrypt(contraseña, llaveSimetrica, vectorInicializacion);

            // ahora hacemos un hash de el login y lo enviamos encriptado
            byte[] loginHash = cipherRSA.calcularHMac(llaveHash, login);
            String loginHashString = Base64.getEncoder().encodeToString(loginHash);

            // ahora hacemos un hash de la contraseña y lo enviamos encriptado
            byte[] contraseñaHash = cipherRSA.calcularHMac(llaveHash, contraseña);
            String contraseñaHashString = Base64.getEncoder().encodeToString(contraseñaHash);

            // ahora encriptamos estos dos hash
            String contraseñaHashEncriptada = cipherAES.encrypt(contraseñaHashString, llaveSimetrica, vectorInicializacion);
            String loginHashEncriptado = cipherAES.encrypt(loginHashString, llaveSimetrica, vectorInicializacion);

            out.writeObject(loginCifrado);
            out.writeObject(contraseñaCifrada);
            out.writeObject(loginHashEncriptado);
            out.writeObject(contraseñaHashEncriptada);


            // Recibimos la confirmacion de el servidor que las contraseñas son correctas
            String confirmacionLoginYContraseña = (String) in.readObject();

            if(confirmacionLoginYContraseña.equals("ERROR")){
                socket.close();
                throw new Exception("La confirmacion del login y la contraseña no fue correcto");
            }

            // AHORA SI HACEMOS LA CONSULTA DEL NUMERO

            // Generamos un numero Random
            Random random =  new Random();
            //  el numero es de dos cifras y es maximo 99
            Integer numeroEnviar = random.nextInt(99);
            // imprimimos el numero
            System.out.println("Numero enviado desde el cliente: " + numeroEnviar);
            

            // CIFRAMOS
            // Ciframos con la llave simetrica el numero
            String numeroCifrado = cipherAES.encrypt(String.valueOf(numeroEnviar), llaveSimetrica, vectorInicializacion);

            // Hacemoss hash del numero que vamos a enviar
            byte[] numeroHash =  cipherRSA.calcularHMac(llaveHash, String.valueOf(numeroEnviar));
            String numeroHashString = Base64.getEncoder().encodeToString(numeroHash);


            // enviamos el numero cifrado y el hash de ese numero en string
            out.writeObject(numeroCifrado);
            out.writeObject(numeroHashString);

            // AHROA RECIBIMOS LAS RESPUESTAS DEL SERVIDOR

            String numeroRespuestaCifrado = (String) in.readObject();
            String numeroRespuestaHash = (String) in.readObject();


            // Desciframos el numero 
            String numeroRespuestaDescifrado = cipherAES.decrypt(numeroRespuestaCifrado, llaveSimetrica, vectorInicializacion);
            // lo pasamos a un hash para verificar integridad
            byte[] numeroRespuestaDescifradoHashBytes = cipherRSA.calcularHMac(llaveHash, numeroRespuestaDescifrado);
            String numeroRespuestaDescifradoHashString = Base64.getEncoder().encodeToString(numeroRespuestaDescifradoHashBytes);

            if(!numeroRespuestaHash.equals(numeroRespuestaDescifradoHashString)){
                socket.close();
                throw new Exception("El numero que esta en el hash no corresponde al numero cifrado");
            }
            


            // ahora verificamos que el numero es el que esperabamos 
            Integer numeroRespuestaInteger = Integer.parseInt(numeroRespuestaDescifrado);

           
            System.out.println("Numero recibido desde el servidor: " + numeroRespuestaInteger);

            if((numeroEnviar -1) == numeroRespuestaInteger){
                System.out.println(" La conexion funciono, numero correcto");
            }
            else{
                socket.close();
                throw new Exception("La consulta no funciono, numero incorrecto");
            }


            // Cerramos el socket  
            socket.close();



        } catch (UnknownHostException e) {
            System.err.println("No se conoce sobre el host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("No se pudo obtener I/O para la conexión a " + hostName);
            System.exit(1);
        } catch(Exception e){
                e.printStackTrace();
                
        } 
    }


    public static byte[] generateReto(){
        SecureRandom random = new SecureRandom();
        byte[] reto = new byte[16];
        random.nextBytes(reto);
        return reto;
    }
}
