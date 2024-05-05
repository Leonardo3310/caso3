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
            Cliente.sleep(1000);
            entry.getValue().start();
            Server.sleep(2000);
            Cliente.sleep(2000);
            System.out.println("");
        }
    }

    public static void main(String[] args) throws Exception {

        Principal principal = new Principal();

        Scanner scan = new Scanner(System.in);

        System.out.println("");
        System.out.print("Por favor ingrese el numero de Servidores y Clientes que desea correr: ");
        System.out.println("");

        String numeroDelegados = scan.nextLine();

        principal.generateDelegates(Integer.parseInt(numeroDelegados));

        principal.initDelegates();

        scan.close();


        

        
    }
    
}
