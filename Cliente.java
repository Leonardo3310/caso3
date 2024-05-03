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

            out.println("SECURE INIT,LLEGAMOS LOKA"); // Enviar el RETO al servidor





        } catch (UnknownHostException e) {
            System.err.println("No se conoce sobre el host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("No se pudo obtener I/O para la conexión a " + hostName);
            System.exit(1);
        }
    }
}
