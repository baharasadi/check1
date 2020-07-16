import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javafx.application.Platform;

public class ServerClient implements Runnable {

    private int id;
    private String userName;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private UI ui;
    private ChatServer server;

    //    public ServerClient(UI u) {
//        ui = u;
//    }
//
    public ServerClient(int i, Socket sock, UI u) {
        id = i;
        socket = sock;
        ui = u;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setServer(ChatServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            while (socket.isConnected() && !socket.isClosed()) {
                ChatMessage m = (ChatMessage) input.readObject();
                //System.out.println(m.getMessage());

                // Handle status messages
                if (m instanceof StatusMessage) {
                    StatusMessage sm = (StatusMessage) m;
                    if (sm.isLogOutMessage()) {
                        return;
                    }
                    if (sm.isLogInMessage()) {
                        //System.out.println("loginmessage from " + sm.getSenderName());
                        userName = sm.getSenderName();
                        // check if username is already in use
                        server.sendUserNames(id);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                ui.addUserName(userName);
                            }
                        });

                        // send other clients the new username
                        StatusMessage rm = new StatusMessage();
                        rm.setUserJoined(true);
                        rm.setData(userName);
                        server.broadcastMessage(rm, id);
                    }
                }
                // handle regular messages
                else {
//                    System.out.println(m.getSenderName() +
//                    (m.getReceiverName().equals("") ? "" : "->" + m.getReceiverName()) +
//                    ": " + m.getMessage());
                    // don't show private messages?
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            ui.setMessage(m);
                        }
                    });
                    // message is to all users
                    if (m.getReceiverName().equals("")) {
                        //System.out.println("serverClient broadcastMessage");
                        server.broadcastMessage(m, 0);
                    }
                    // private message
                    else
                        server.sendTo(m);
                }
            }
            output.close();
            input.close();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ui.removeUserName(userName);
                }
            });
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
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
}
