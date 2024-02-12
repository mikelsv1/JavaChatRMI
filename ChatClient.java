import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ChatClient extends UnicastRemoteObject implements ChatClient_itf {
    private String name;
    private int id;

    public ChatClient(String name) throws RemoteException {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
            int id = chatServer.registerClient(chatClient, username);
            chatClient.setId(id);

            
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
                    chatServer.getMessageHistory((ChatClient_itf) chatClient);

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
