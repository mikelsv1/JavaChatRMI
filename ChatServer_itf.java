import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatServer_itf extends Remote {
    int registerClient(ChatClient_itf client, String username) throws RemoteException;
    void unregisterClient(ChatClient_itf client, String username) throws RemoteException;
    void broadcastMessage(String message, String username) throws RemoteException;
    void connect(String username) throws RemoteException;
    public List<Message> getMessageHistory(int nbmessages) throws RemoteException;
}
