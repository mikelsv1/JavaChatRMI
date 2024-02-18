import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatServer_itf extends Remote {
    int registerClient(ChatClient_itf client) throws IOException;
    void unregisterClient(ChatClient_itf client) throws IOException;
    void broadcastMessage(String message, int id) throws IOException;
    void broadcastNotification(String message) throws IOException;
    void getMessageHistory(ChatClient_itf client, int numberOfMessages) throws RemoteException;
}
