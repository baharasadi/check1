import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

public class Chat extends Application {

    private Stage stage;
    private boolean servermode = true;
    private String userName = "";
    private String address;
    private int port;

    @Override
    public void start(Stage primaryStage) {

        stage = primaryStage;

        Scene scene = logInScene();

        primaryStage.setTitle("Chat");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Scene logInScene() {
        GridPane root = new GridPane();
        root.add(new Label("User name:"), 0, 0);
        TextField nameField = new TextField();
        root.add(nameField, 1, 0);
        root.add(new Label("Server address:"), 0, 1);
        TextField addressField = new TextField("127.0.0.1");
        root.add(addressField, 1, 1);
        root.add(new Label("Port:"), 0, 2);
        TextField portField = new TextField("3010");
        root.add(portField, 1, 2);
        Button connectButton = new Button("Connect");
        CheckBox cb = new CheckBox("Start Server ");
        cb.setSelected(false);
        cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                                Boolean old_val, Boolean new_val) {
                if(cb.isSelected()) {
                    addressField.setDisable(true);
                    connectButton.setText("Start");
                }
                else {
                    addressField.setDisable(false);
                    connectButton.setText("Connect");
                }
            }
        });
        root.add(cb, 0, 3);

        connectButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent t){
                if(!nameField.getText().equals("") &&
                        !addressField.getText().equals("") &&
                        !portField.getText().equals("") ) {
                    userName = nameField.getText();
                    address = addressField.getText();
                    port = Integer.decode(portField.getText());
                    servermode = cb.isSelected();
                    stage.setScene(createMainUi());
                }
            }
        });
        root.add(connectButton, 1, 4);
        return(new Scene(root));
    }

    private Scene createMainUi() {
        StackPane root = new StackPane();

        UI ui = new UI(userName, servermode);
        // Start Server
        if(servermode) {
            ChatServer server = new ChatServer(ui, port);
            server.setUserName(userName);
            ui.setServer(server);
            Thread serverThread = new Thread(server);
            serverThread.setDaemon(true);
            serverThread.start();
        }
        // Start Client
        else {
            ChatClient client = new ChatClient(ui);
            client.setUserName(userName);
            if(!client.connect(address, port)) {
                JOptionPane.showMessageDialog(null,
                        "Connection to " + address + " failed.",
                        "Chat",
                        JOptionPane.ERROR_MESSAGE);
                Platform.exit();
            }
            ui.setClient(client);
            Thread clientThread = new Thread(client);
            clientThread.setDaemon(true);
            clientThread.start();

            StatusMessage sm = new StatusMessage();
            sm.setLogInMessage(true);
            sm.setSenderName(userName);
            client.sendMessage(sm);
        }

        root.getChildren().add(ui);
        return(new Scene(root));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
