import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException {
        int port = 1234; // Puerto en el que escucha el servidor
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Servidor iniciado en el puerto " + port);

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Aceptar conexión del cliente
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Recibido: " + inputLine);
                    int number = Integer.parseInt(inputLine);
                    out.println(number - 1); // Responder con número - 1
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }
    }
}
