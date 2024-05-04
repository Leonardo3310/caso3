import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

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
            Server.sleep(5000);
            Cliente.sleep(5000);
        }
    }

    public static void main(String[] args) throws Exception {

        Principal principal = new Principal();
        principal.generateDelegates(2);

        principal.initDelegates();


        

        
    }
    
}
