import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Random;


public class Cliente extends Thread {

    private Server servidor;
    private UtilidadesRSA cipherRSA;

    private Integer numeroY;

    public Cliente(Server servidor){
        this.servidor = servidor;
        this.cipherRSA = new UtilidadesRSA();
    }

    public void createNumeroY(){
        Random random = new Random();
        numeroY =  random.nextInt(15);
    }

    @Override
    public void run()  {
        String hostName = "localhost"; // Dirección del servidor
        int port = 1234; // Puerto del servidor

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
            Integer numeroGElevadoALaX = (int)in.readObject();

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

            // crear el numeroY
            createNumeroY();

            // Crear el numero G elevado a la Y
            Integer numeroGElevadoALaY = (int) Math.pow((double)numeroG, (double)numeroY);

            System.out.println(numeroY);

            //enviar el numero Y
            out.writeObject(numeroGElevadoALaY);

            // elevar a la y el numero G elevado a la x
            Integer numeroFinal = (int)Math.pow((double) numeroGElevadoALaX, (double)numeroY);


            System.out.println("Numero Final Cliente: " + numeroFinal);




            


            


            
            


            

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
