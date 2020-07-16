import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer implements Runnable {

    private ArrayList<ServerClient> clients = new ArrayList();
    private String userName;
    private int port;
    private UI ui;

    public ChatServer(UI ui) {
        this.ui = ui;
    }

    public ChatServer(UI ui, int port) {
        this.ui = ui;
        this.port = port;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public void run() {
        try {
            int i = 1;
            //Start the server to listen port 3010
            ServerSocket server = new ServerSocket(port);

            ServerClient srv = new ServerClient(0, null, null);
            srv.setServer(null);
            srv.setUserName(userName);
            clients.add(srv);

            //Start to listen and wait connections
            while (true) {
                Socket temp = server.accept();
                ServerClient client = new ServerClient(i++, temp, ui);
                client.setServer(this);
                clients.add(client);
                Thread t = new Thread(client);
                t.setDaemon(true);
                t.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Send a message to all users
     *
     * @param cm        Message
     * @param excludeId Client Id to exclude from broadcast
     */
    public void broadcastMessage(ChatMessage cm, int excludeId) {
        for (ServerClient temp : clients) {
            if (temp.getId() != excludeId && temp.getId() != 0)
                temp.sendMessage(cm);
        }
    }

    /**
     * Send a list of usernames to a specified client id
     *
     * @param clientId
     */
    public void sendUserNames(int clientId) {
        ArrayList<String> users = new ArrayList();
        ServerClient client = null;
        for (ServerClient temp : clients) {
            users.add(temp.getUserName());
            if (temp.getId() == clientId)
                client = temp;
        }
        StatusMessage sm = new StatusMessage();
        sm.setLogInReply(true);
        sm.setLogInStatus(true);
        sm.setData(users);
        client.sendMessage(sm);
    }

    /**
     * Send a message to a specified client.
     *
     * @param m
     */
    public void sendTo(ChatMessage m) {
        String receiver = m.getReceiverName();
        if (receiver.equals(userName)) {
            return;
        }
        System.out.println("sendTo " + receiver);
        for (ServerClient temp : clients) {
            if (temp.getUserName().equals(receiver)) {
                temp.sendMessage(m);
                return;
            }
        }
    }

    /**
     * Disconnect and remove a client
     *
     * @param name
     */
    public void disconnectClient(String name) {
        for (ServerClient temp : clients) {
            if (temp.getUserName().equals(name)) {
                StatusMessage sm = new StatusMessage();
                sm.setLogOutMessage(true);
                sm.setData((String) temp.getUserName());
                clients.remove(temp);
                temp.sendMessage(sm);

                sm.setLogOutMessage(false);
                sm.setUserLeft(true);
                clients.remove(temp);
                broadcastMessage(sm, 0);
                return;
            }
        }
    }

}
