import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiServer extends Application{
	TextField s1,s2,s3,s4, chatField, individualChatField;
	Button serverChoice,clientChoice, sendButton, chatToOthers;
	HashMap<String, Scene> sceneMap;
	GridPane grid;
	HBox buttonBox;
	VBox clientBox;
	Scene startScene;
	BorderPane startPane;
	Server serverConnection;
	Client clientConnection;
	int clientCount = 0;
	//create another listview, where we'll store the clients. Everytime server's count goes up, we put the client enters, when server
	//have a textbox, where they can enter what number client they want to talk to
	//the textbox will determine if the number is valid or not, based on server's client thread count
	//if it's valid... repeat b1 on action
	
	ListView<String> listItems, listItems2, listItems3, listItems4;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("The Networked Client/Server GUI Example");

		//--------------------------------------------------------------------------------------------------------------

		this.serverChoice = new Button("Server");
		this.serverChoice.setStyle("-fx-pref-width: 300px");
		this.serverChoice.setStyle("-fx-pref-height: 300px");

		listItems3 = new ListView<String>();
		listItems4 = new ListView<String>();
		
		this.serverChoice.setOnAction(e->{ primaryStage.setScene(sceneMap.get("server"));
											primaryStage.setTitle("This is the Server");
				serverConnection = new Server(data -> {
					Platform.runLater(()->{
						listItems.getItems().add(data.toString());
					});

				});
											
		});

		this.clientChoice = new Button("Client");
		this.clientChoice.setStyle("-fx-pref-width: 300px");
		this.clientChoice.setStyle("-fx-pref-height: 300px");
		
		this.clientChoice.setOnAction(e-> {primaryStage.setScene(sceneMap.get("client"));
											primaryStage.setTitle("This is a client");
											clientConnection = new Client(data->{
							Platform.runLater(()->{
								listItems2.getItems().add(data.toString());
//								if(clientCount < serverConnection.clients.size()){		//can't use this
//									listItems3.getItems().add("someone has joined");
//									listItems4.getItems().add("someone has joined");
//								} else if(clientCount > serverConnection.clients.size()){	//can't use this
//									listItems3.getItems().add("someone left");
//									listItems4.getItems().add("someone left");
//								}
//								clientCount = serverConnection.clients.size();
											});
							});
							
											clientConnection.start();
		});

		//-------------------------------------------------------------------------------------------------------------

		this.buttonBox = new HBox(400, serverChoice, clientChoice);
		startPane = new BorderPane();
		startPane.setPadding(new Insets(70));
		startPane.setCenter(buttonBox);
		
		startScene = new Scene(startPane, 800,800);
		
		listItems = new ListView<String>();
		listItems2 = new ListView<String>();

		chatField = new TextField();
		sendButton = new Button("Send");
		sendButton.setOnAction(e->{clientConnection.send(chatField.getText()); chatField.clear();});

		individualChatField = new TextField("Who do you want to chat to?");
		individualChatField.setVisible(false);
		chatToOthers = new Button("chat to specific clients");
		chatToOthers.setOnAction(e->{
			individualChatField.setVisible(true);
		});

		individualChatField.setOnKeyPressed(e->{
			if(e.getCode().equals(KeyCode.ENTER)){
				clientConnection.send("sending info to individuals");
				clientConnection.send(chatField.getText());
				clientConnection.send(individualChatField.getText());

				individualChatField.clear();
			}
		});
		
		sceneMap = new HashMap<String, Scene>();
		
		sceneMap.put("server",  createServerGui());
		sceneMap.put("client",  createClientGui());
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		primaryStage.setScene(startScene);
		primaryStage.show();
	}
	
	public Scene createServerGui() {
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(30));
		pane.setStyle("-fx-background-color: coral");
		
		pane.setLeft(listItems);
		pane.setRight(listItems3);
	
		return new Scene(pane, 500, 500);
	}
	
	public Scene createClientGui() {
		HBox sendAndChatToOthers = new HBox(20, sendButton, chatToOthers);
		HBox lists = new HBox(listItems2, listItems4);
		clientBox = new VBox(10, chatField,sendAndChatToOthers, individualChatField, lists);
		clientBox.setStyle("-fx-background-color: blue");

		return new Scene(clientBox, 500, 500);
	}

}
