import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatServer_itf extends Remote {
    int registerClient(ChatClient_itf client, String username) throws RemoteException;
    void unregisterClient(ChatClient_itf client, String username) throws RemoteException;
    void broadcastMessage(String message, String username) throws RemoteException;
    void getMessageHistory(ChatClient_itf client) throws RemoteException;
}
