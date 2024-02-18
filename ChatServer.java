import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatServer extends UnicastRemoteObject implements ChatServer_itf {

    private static final String HISTORY_FILE_PATH = "history.txt";

    private List<ChatClient_itf> clients = new ArrayList<>();
    private static List<Message> messages = new ArrayList<>();

    protected ChatServer() throws RemoteException {
        super();
    }

    @Override
    public int registerClient(ChatClient_itf client) throws IOException {
        clients.add(client);
        broadcastNotification("*" + client.getName() + " has joined the chat*");
        client.notify("Welcome to the chat, " + client.getName() + "!");
        client.notify("Type '/exit' to leave the chat.");
        client.notify(
                "Type '/history <number of messages>' to see previous messages. If no number is given, all messages will be shown.\n");
        return clients.size() - 1;
    }

    @Override
    public void unregisterClient(ChatClient_itf client) throws IOException {
        broadcastNotification("*" + client.getName() + " has left the chat*");
        client.notify("You have left the chat. Goodbye!");
        clients.remove(client);
    }

    @Override
    public void broadcastNotification(String message) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE_PATH, true));
        writer.write(new Date().getTime() + ";" + "SERVER" + ";" + message + "\n");
        writer.close();
        messages.add(new Message(message, "SERVER"));
        for (ChatClient_itf client : clients) {
            client.notify(message);
        }
    }

    @Override
    public void broadcastMessage(String message, int id) throws IOException {
        String username = clients.get(id).getName();
        BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE_PATH, true));
        writer.write(new Date().getTime() + ";" + username + ";" + message + "\n");
        writer.close();
        messages.add(new Message(message, username));
        for (ChatClient_itf client : clients) {
            if (client.getName().equals(username)) {
                continue;
            }
            client.receiveMessage(message, username);
        }
    }

    @Override
    public void getMessageHistory(ChatClient_itf client, int numberOfMessages) throws RemoteException {
        if (numberOfMessages > messages.size()) {
            numberOfMessages = messages.size();
        }
        for (int i = messages.size() - numberOfMessages; i < messages.size(); i++) {
            client.notify(messages.get(i).toString());
        }
    }

    private static void loadAllMessagesFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                long timestamp = Long.parseLong(parts[0]);
                String username = parts[1];
                String text = "";
                if (parts.length > 3) {
                    parts[2] = parts[2];
                    for (int i = 3; i < parts.length; i++) {
                        parts[2] = parts[2] + parts[i];
                    }
                    text = parts[2];
                }
                else if (parts.length < 3) {
                    text = "";                    
                }
                else{
                    text = parts[2];
                }
                messages.add(new Message(text, username, new Date(timestamp)));
            }
        } catch (IOException e) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE_PATH, true));
            String timestamp = new Date().getTime() + "";
            writer.write(timestamp + ";" + "SERVER" + ";" + "START OF NEW SESSION\n");
            writer.close();
            System.err.println("Error loading messages from file: " + e.getMessage());
        }
    }

    private static void loadSessionMessagesFromFile() throws IOException{
        try {
            BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE_PATH));
            String line;
            boolean previousSession = false;
            List<String> messagesFromLastSession = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (previousSession) {
                    messagesFromLastSession.add(line);
                }
                if (line.contains("START OF NEW SESSION")) {
                    previousSession = true;
                    messagesFromLastSession.clear();
                }
            }
            for (String message : messagesFromLastSession) {
                String[] parts = message.split(";");
                long timestamp = Long.parseLong(parts[0]);
                String username = parts[1];
                String text = "";
                if (parts.length > 3) {
                    parts[2] = parts[2];
                    for (int i = 3; i < parts.length; i++) {
                        parts[2] = parts[2] + parts[i];
                    }
                    text = parts[2];
                }
                else if (parts.length < 3) {
                    text = "";                    
                }
                else{
                    text = parts[2];
                }
                messages.add(new Message(text, username, new Date(timestamp)));
            }
            reader.close();
        } catch (IOException e) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE_PATH, true));
            String timestamp = new Date().getTime() + "";
            writer.write(timestamp + ";" + "SERVER" + ";" + "START OF NEW SESSION\n");
            writer.close();
            System.err.println("Error loading messages from file: " + e.getMessage());

        }
    }

    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                // String line;
                System.out.println("Usage: java ChatServer <host> <port>");
                System.exit(0);
            }

            String host = args[0];
            int port = Integer.parseInt(args[1]);

            ChatServer chatServer = new ChatServer();
            Registry registry = LocateRegistry.getRegistry(host, port);
            registry.rebind("ChatServer", chatServer);
            System.out.println("ChatServer is running...");

            try {
                // BufferedReader reader = new BufferedReader(new
                // FileReader(HISTORY_FILE_PATH));
                // String line;
                System.out.println(
                        "Do you want to load the chat history h, the chat history from previous session p or start a new session? [h/p/n]");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String answer = br.readLine();
                if (answer.toLowerCase().equals("h")) {
                    loadAllMessagesFromFile();

                } else if (answer.toLowerCase().equals("p")) {
                    loadSessionMessagesFromFile();
                }

                else if (answer.toLowerCase().equals("n")) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE_PATH, true));
                    String timestamp = new Date().getTime() + "";
                    writer.write(timestamp + ";" + "SERVER" + ";" + "START OF NEW SESSION\n");
                    writer.close();
                    System.out.println("No chat history loaded.");
                }

            } catch (IOException e) {
                System.err.println("Error loading messages from file: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (RemoteException e) {
        }
    }
}
