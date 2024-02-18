import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ChatClient extends UnicastRemoteObject implements ChatClient_itf {

    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RESET = "\u001B[0m";

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
        System.out.println(ANSI_YELLOW + message + ANSI_RESET);
    }

    public static void main(String[] args) throws NotBoundException, IOException {
        try {
            if (args.length != 2) {
                System.out.println("Usage: java ChatClient <host> <port>");
                System.exit(0);
            }
            String host = args[0];
            int port = Integer.parseInt(args[1]);

            System.out.print("Enter your username: ");
            String username = System.console().readLine();
            
            while (username.isEmpty()) {
                System.out.println("Invalid username. Try again.\nEnter your username:");
                username = System.console().readLine();
            }

            ChatClient chatClient = new ChatClient(username);
            Registry registry = LocateRegistry.getRegistry(host, port);
            ChatServer_itf chatServer = (ChatServer_itf) registry.lookup("ChatServer");
            int id = chatServer.registerClient(chatClient);
            chatClient.setId(id);
            
            while (true) {
                String message = System.console().readLine();
                if (message.equalsIgnoreCase("/exit")) {
                    chatServer.unregisterClient(chatClient);
                    System.exit(0);
                } else if (message.startsWith("/history")) {
                    String[] parts = message.split(" ");
                    int numberOfMessages = 0;
                    if (parts.length > 1) {
                        try {
                            numberOfMessages = Integer.parseInt(parts[1]);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid number of messages.");
                            continue;
                        }
                    }
                    else {
                        numberOfMessages = Integer.MAX_VALUE;
                    }
                    chatServer.getMessageHistory(chatClient, numberOfMessages);
                
                } else {
                    try {
                        chatServer.broadcastMessage(message, chatClient.getId());
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
