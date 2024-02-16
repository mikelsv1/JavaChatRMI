import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ChatClient extends UnicastRemoteObject implements ChatClient_itf  {
    private String username;
    //private int id;
    private ChatServer_itf server;

    public ChatClient(String username /*, int id*/) throws RemoteException {
        this.username = username;
        //this.id=id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username){
        this.username = username;
    }

    //public int getId() {
    //    return id;
    //}

    //public void setId(int id) {
    //    this.id = id;
    //}

    public List<Message> getMessageHistory(int nbMessage) throws RemoteException{
        return server.getMessageHistory(nbMessage);
    }

    public void sendMessage(String message) throws RemoteException {
        server.broadcastMessage(message, username);
    }

    @Override
    public void receiveMessage(String message, String username) {
        System.out.println(username + ": " + message);
    }

    @Override
    public void notify(String message) {
        System.out.println("Server: " + message);
    }

    public static void main(String[] args) throws NotBoundException {
        try {
            if (args.length != 2) {
                System.out.println("Usage: java ChatClient <host> <port>");
                System.exit(0);
            }
            String host = args[0];
            int port = Integer.parseInt(args[1]);

            System.out.print("Enter your username: ");
            String username = System.console().readLine();

            ChatClient chatClient = new ChatClient(username);
            Registry registry = LocateRegistry.getRegistry(host, port);
            ChatServer_itf chatServer = (ChatServer_itf) registry.lookup("ChatServer");
            chatClient.setUsername(username);
            //int id = chatServer.registerClient(chatClient, username);
            //chatClient.setId(id);

            
            // Welcome message. Exits if user types "exit". Otherwise, sends message to server. History in order to see previous messages.
            System.out.println("Welcome to the chat, " + username + "!");
            System.out.println("Type 'exit' to leave the chat.");
            System.out.println("Type 'history' to see previous messages.\n");
            
            while (true) {
                String message = System.console().readLine();
                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    chatServer.unregisterClient(chatClient, username);
                    System.exit(0);
                } else if (message.equalsIgnoreCase("history")) {
                    List<Message> history = chatClient.getMessageHistory(25);
                    for (Message m : history) {
                        System.out.println(""+m.getUsername()+" : "+m.getMessage());
                    }

                } else {
                    try {
                        chatServer.broadcastMessage(message, username);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
