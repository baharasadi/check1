import java.util.ArrayList;
import java.util.Collections;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.swing.JOptionPane;

/**
 * @author hth
 */
public class UI extends VBox implements EventHandler {

    private final boolean serverMode;
    private String userName;
    private int selectedIndex;
    private String selectedUser;
    private final HBox chatBox = new HBox(5);
    private final HBox msgBox = new HBox(5);
    private final VBox userBox = new VBox(5);
    private final TextArea chat = new TextArea();
    private final TextField message = new TextField();
    private final Label msgLabel = new Label("Message:");
    private ListView lw = new ListView();

    private Button closeButton;
    private Button sendButton;
    private Button dcButton;
    private Label statusBar;

    private ChatClient client;
    private ChatServer server;

    // Names to show on ListView
    ObservableList<String> userNames;

    public UI(String userName, boolean serverMode) {
        this.userName = userName;
        this.serverMode = serverMode;
        selectedIndex = 0;
        selectedUser = "";
        chat.setMinWidth(320);
        chat.setMaxWidth(320);
        chat.setEditable(false);
        chat.setWrapText(true);
        lw.setMinWidth(160);
        lw.setMaxWidth(160);
        lw.setEditable(false);
        userNames = FXCollections.observableArrayList();
        if (this.serverMode) {
            userBox.getChildren().add(lw);
            dcButton = new Button("Disconnect");
            dcButton.setOnAction(this);
            userBox.getChildren().add(dcButton);
            chatBox.getChildren().add(userBox);
            userNames.add(0, "");
            userNames.add(userName);
            lw.setItems(userNames);
            client = null;
        } else {
            chatBox.getChildren().add(lw);
            server = null;
        }
        chatBox.getChildren().add(chat);
        chatBox.setPadding(new Insets(5, 5, 5, 5));
        this.getChildren().add(chatBox);

        // Add a listener to ListView to track the selected name.
        lw.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov,
                                        String old_val, String new_val) {
                        selectedIndex = lw.getSelectionModel().getSelectedIndex();
                        selectedUser = userNames.get(selectedIndex);
                        if (selectedUser.equals(userName)) {
                            lw.getSelectionModel().select(0);
                            selectedUser = "";
                        }
                    }
                });
        msgBox.setPadding(new Insets(10, 10, 10, 10));
        msgBox.getChildren().add(msgLabel);
        message.setMinWidth(320);
        message.setMaxWidth(320);
        message.setOnAction(this);
        msgBox.getChildren().add(message);
        sendButton = new Button("Send");
        sendButton.setOnAction(this);
        msgBox.getChildren().add(sendButton);
        msgBox.setAlignment(Pos.CENTER);
        this.getChildren().add(msgBox);
        statusBar = new Label("");
        this.getChildren().add(statusBar);

    }

    public ChatClient getClient() {
        return client;
    }

    public void setClient(ChatClient client) {
        this.client = client;
    }

    public ChatServer getServer() {
        return server;
    }

    public void setServer(ChatServer server) {
        this.server = server;
    }

    @Override
    public void handle(Event event) {
        if (event.getSource().equals(sendButton) || event.getSource().equals(message)) {
            String msg = message.getText();
            if (!msg.equals(""))
                sendMessage(msg);
        }
    }

    private void sendMessage(String msg) {
        message.setText("");
        if (!selectedUser.equals(""))
            chat.appendText("\n" + userName + "->" + selectedUser + ": " + msg);

        ChatMessage cmsg = new ChatMessage();
        cmsg.setSenderName(userName);
        cmsg.setReceiverName(selectedUser);
        cmsg.setMessage(msg);
        if (serverMode) {
            if (cmsg.getReceiverName().equals("")) {
                chat.appendText("\n" + userName + ": " + msg);
                server.broadcastMessage(cmsg, 0);
            } else {
                server.sendTo(cmsg);
            }
        } else
            client.sendMessage(cmsg);
    }

    public void setMessage(ChatMessage cmsg) {
        chat.appendText("\n" + cmsg.getSenderName() +
                (cmsg.getReceiverName().equals("") ? "" : "->" + cmsg.getReceiverName()) +
                ": " + cmsg.getMessage());
    }

    public void setUserNames(ArrayList<String> names) {
        userNames.clear();
        userNames.add(0, "");
        for (String name : names) {
            userNames.add(name);
            //sortUserNames();
        }
        lw.setItems(userNames);
    }

    public void addUserName(String name) {
        userNames.add(name);
        chat.appendText("\n" + name + " has joined the chat.");
        //sortUserNames();
    }

    public void removeUserName(String name) {
        userNames.remove(name);
        chat.appendText("\n" + name + " has left the chat.");
        //sortUserNames();
    }

    private void sortUserNames() {
        if (userNames.size() > 2)
            Collections.sort(userNames);
        lw.setItems(userNames);
    }

    public void clearUserNames() {
        userNames.clear();
        lw.setItems(userNames);
    }

    public void setStatusBarText(String text) {
        statusBar.setText(text);
    }

    public void enableUi(boolean enabled) {
        if (!enabled)
            userNames.clear();
        msgBox.setDisable(enabled);
        sendButton.setDisable(enabled);
    }
}
