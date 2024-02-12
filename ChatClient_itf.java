import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatClient_itf extends Remote {
    void receiveMessage(String message, String username) throws RemoteException;
    void notify(String message) throws RemoteException;
    String getName() throws RemoteException;
}
