import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ChatServer extends UnicastRemoteObject implements ChatServer_itf {


    private static final long serialVersionUID = 1L; // Required for serialization

    private static final String MESSAGES_FILE = "messages.txt";
    //private static final String USERNAMES_FILE = "usernames.txt";

    private Map<Integer, String> usernames = new HashMap<>();
    private List<Message> messages = new ArrayList<>();
    private Set<Integer> usedIds = new HashSet<>();

    private List<ChatClient_itf> clients = new ArrayList<>();

    //constructor
    public ChatServer() throws RemoteException {
        super();
        loadMessagesFromFile();
        //loadUsernamesFromFile();
    }

    private int generateId() {
        int id;
        do {
            id = UUID.randomUUID().hashCode();
        } while (usedIds.contains(id));
        usedIds.add(id);
        return id;
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
    public void connect(String username) throws RemoteException {
        //System.out.println(username + " has connected.");
        //broadcastMessage(""+username+"has joined the chat", username);

        int id = generateId();
        usernames.put(id, username);
        //saveUsernamesToFile();
        System.out.println("[" + id + "] " + username + " connected");
        broadcastMessage("**" + username + " has joined the chat**",username);
        // Send past messages to newly connected user
        //return id;
    }


    @Override
    public void broadcastMessage(String message, String username) throws RemoteException {
        messages.add(new Message(message,username));
        saveMessagesToFile();
        for (ChatClient_itf client : clients) {
            try {
                client.receiveMessage(message,username);
            } catch (RemoteException e) {
                System.err.println("Error sending message to client: " + e.getMessage());
            }
        }
        }
    
    @Override
    public List<Message> getMessageHistory(int nbmessages) throws RemoteException{
        List<Message> previousMessage = new ArrayList<>();
        for (int i = messages.size() - nbmessages; i < messages.size(); i++) {
            previousMessage.add(messages.get(i));
        }
        return previousMessage;
    }

        private void loadMessagesFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MESSAGES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                messages.add(new Message(new Date (Long.parseLong(parts[0])), parts[1], parts[2]));
            }
        } catch (IOException e) {
            System.err.println("Error loading messages from file: " + e.getMessage());
        }
    }


    private void saveMessagesToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(MESSAGES_FILE))) {
            for (Message message : messages) {
                writer.println(message.getTimestamp() + ":" + message.getUsername() + ":" + message.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Error saving messages to file: " + e.getMessage());
        }
    }
    /* 
    private void loadUsernamesFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERNAMES_FILE))) {
            String username;
            while ((username = reader.readLine()) != null) {
                usernames.put(usernames.size(), username);
            }
        } catch (IOException e) {
            System.err.println("Error loading usernames from file: " + e.getMessage());
        }
    }

    private void saveUsernamesToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERNAMES_FILE))) {
            for (String username : usernames.values()) {
                writer.println(username);
            }
        } catch (IOException e) {
            System.err.println("Error saving usernames to file: " + e.getMessage());
        }
    }

    private void saveUsedIds() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("used_ids.txt"))) {
            for (Integer id : usedIds) {
                writer.println(id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadUsedIds() {
        try (BufferedReader reader = new BufferedReader(new FileReader("used_ids.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                usedIds.add(Integer.parseInt(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
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
