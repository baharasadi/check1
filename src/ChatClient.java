import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient implements Runnable {

    private int id;
    private String userName;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private UI ui;

    public ChatClient(UI u) {
        ui = u;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean connect(String address, int port) {
        try {
            socket = new Socket(address, port);
            output = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        try {
            input = new ObjectInputStream(socket.getInputStream());

            // handle incoming messages
            while(true) {
                ChatMessage m = (ChatMessage)input.readObject();
                //System.out.println(m.getMessage());

                Platform.runLater(() -> {
                    // status messages
                    if(m instanceof StatusMessage) {
                        StatusMessage sm = (StatusMessage)m;
                        // user has been disconnected from the server
                        if(sm.isLogOutMessage()) {
                            ui.setStatusBarText("You have been disconnected");
                            ui.enableUi(false);
                            return;
                        }
                        // reply to login message
                        if(sm.isLogInReply()) {
                            // check status of login
                            //System.out.println("loginreply...");
                            // a list of user names
                            ui.setUserNames((ArrayList<String>)sm.getData());
                            ui.setStatusBarText("Connected to " + socket.getInetAddress().getHostName() +
                                    " as " + userName);
                        }
                        // a new user has joined the chat
                        else if(sm.isUserJoined()) {
                            System.out.println("userjoined... new user: " + (String)sm.getData());
                            ui.addUserName((String)sm.getData());
                        }
                        // a user has left the chat
                        else if(sm.isUserLeft()) {
                            System.out.println("userleft... user: " + (String)sm.getData());
                            ui.removeUserName((String)sm.getData());
                        }
                    }
                    else // show message
                        ui.setMessage(m);
                });
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            closeSocket();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ui.setStatusBarText("disconnected");
                    ui.enableUi(false);
                }
            });
            return;
        }
    }

    public void sendMessage(ChatMessage cm) {
        try {
            output.writeObject(cm);
            output.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}