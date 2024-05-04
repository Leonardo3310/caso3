

public class Principal {

    public static void main(String[] args) {
        try {
            Server servidor = new Server();

            Cliente cliente =  new Cliente(servidor);

            servidor.start();
            Server.sleep(100);

            cliente.start();



        } catch (Exception e) {
            
            e.printStackTrace();
        }

        
    }
    
}
