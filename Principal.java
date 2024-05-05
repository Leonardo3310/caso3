import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Principal {

    private HashMap<Server,Cliente> mapDelegates;


    public void generateDelegates(Integer numberOfDelegates) throws NoSuchAlgorithmException{
        mapDelegates = new HashMap<>();
        for(int i = 0; i < numberOfDelegates ; i++){
            Server servidor = new Server(1234+i);
            mapDelegates.put(servidor, new Cliente(servidor,1234+i));

        }
    }


    public void initDelegates() throws InterruptedException{
       
        for (Map.Entry<Server, Cliente> entry : mapDelegates.entrySet()) {
            entry.getKey().start();
            entry.getValue().start();
            Server.sleep(1000);
            Cliente.sleep(1000);
        }
    }

    public static void main(String[] args) throws Exception {

        Principal principal = new Principal();

        Scanner scan = new Scanner(System.in);

        System.out.println("Por fravor ingrese el numero de Servidores y clientes que desea correr: ");

        String numeroDelegados = scan.nextLine();
        principal.generateDelegates(Integer.parseInt(numeroDelegados));

        principal.initDelegates();

        scan.close();


        

        
    }
    
}
