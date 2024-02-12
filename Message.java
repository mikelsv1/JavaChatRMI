import java.util.Date;

public class Message {

    private String message;
    private String username;
    private Date timestamp;

    public Message(String message, String username) {
        this.message = message;
        this.username = username;
        this.timestamp = new Date();
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return timestamp + " " + username + ": " + message;
    } 
    
}
