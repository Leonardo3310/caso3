import java.io.*;
import java.net.*;

public class Cliente {
    public static void main(String[] args) throws IOException {
        String hostName = "localhost"; // Dirección del servidor
        int port = 1234; // Puerto del servidor

        try{
            Socket socket = new Socket(hostName, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String numero = "3";
            out.println(numero); // Enviar un número al servidor
            String response = in.readLine(); // Leer la respuesta del servidor
            System.out.println("Respuesta del servidor: " + response);

            // Hacer un ciclo en el cual cuando recibe el mensaje entonce verifique que si es el que espere y conteste con lo que sea que tiene que contestar
            // Luego enviar el mensaje que se espera y verificar que la respuesta sea la essperada
            // esperar ahora los datos que no tiene que enviar el servidor, como el p y q y esas madres 
            // verificar el cifrado que nos manda el servidor 
            // contestar si ese es el cifrado que esperaabamos 
            // enviar clave G A LA Y, revisa la cosa de el algoritmo de hiffie eldman alguna madre asi 




        } catch (UnknownHostException e) {
            System.err.println("No se conoce sobre el host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("No se pudo obtener I/O para la conexión a " + hostName);
            System.exit(1);
        }
    }
}
