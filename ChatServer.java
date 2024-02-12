import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ChatServer extends UnicastRemoteObject implements ChatServer_itf {
    private List<ChatClient_itf> clients = new ArrayList<>();
    private List<Message> messages = new ArrayList<>();

    protected ChatServer() throws RemoteException {
        super();
    }

    @Override
    public int registerClient(ChatClient_itf client, String username) throws RemoteException {
        clients.add(client);
        broadcastMessage(username + " has joined the chat.", "Server");
        client.notify("Welcome to the chat, " + username + "!");
        return clients.size()-1;
    }

    @Override
    public void unregisterClient(ChatClient_itf client, String username) throws RemoteException {
        clients.remove(client);
        broadcastMessage(username + " has left the chat.", "Server");
    }

    @Override
    public void broadcastMessage(String message, String username) throws RemoteException {
        messages.add(new Message(message, username));
        for (ChatClient_itf client : clients) {
            if (client.getName().equals(username)) {
                continue;
            }
            client.receiveMessage(message, username);
        }
    }

    @Override
    public void getMessageHistory(ChatClient_itf client) throws RemoteException {
        for (Message message : messages) {
            client.notify(message.toString());
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                System.out.println("Usage: java ChatServer <host> <port>");
                System.exit(0);
            }
            String host = args[0];
            int port = Integer.parseInt(args[1]);

            ChatServer chatServer = new ChatServer();
            Registry registry = LocateRegistry.getRegistry(host, port);
            registry.rebind("ChatServer", chatServer);
            System.out.println("ChatServer is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
