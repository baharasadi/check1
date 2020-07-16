
import java.io.Serializable;

public class StatusMessage extends ChatMessage implements Serializable {

    private boolean logInMessage;   // First message from client to server
    private boolean logInReply;     // Reply to previous message
    private boolean logInStatus;    // Indicates if login to server was succesfull
    private boolean logOutMessage;  // Sent on disconnect
    private boolean userJoined;     // Sent when a new user has joined the chat
    private boolean userLeft;       // Sent when a user has left the chat

    public boolean isLogInMessage() {
        return logInMessage;
    }

    public void setLogInMessage(boolean logInMessage) {
        this.logInMessage = logInMessage;
    }

    public boolean isLogInReply() {
        return logInReply;
    }

    public void setLogInReply(boolean logInReply) {
        this.logInReply = logInReply;
    }

    public void setLogInStatus(boolean logInStatus) {
        this.logInStatus = logInStatus;
    }

    public boolean isLogOutMessage() {
        return logOutMessage;
    }

    public void setLogOutMessage(boolean logOutMessage) {
        this.logOutMessage = logOutMessage;
    }

    public boolean isUserJoined() {
        return userJoined;
    }

    public void setUserJoined(boolean userJoined) {
        this.userJoined = userJoined;
    }

    public boolean isUserLeft() {
        return userLeft;
    }

    public void setUserLeft(boolean userLeft) {
        this.userLeft = userLeft;
    }

}