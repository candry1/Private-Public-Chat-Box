import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;
/*
 * Clicker: A: I really get it    B: No idea what you are talking about
 * C: kind of following
 */

public class Server{
	int count = 1;	
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Serializable> callback;

	Server(Consumer<Serializable> call){
		callback = call;
		server = new TheServer();
		server.start();
	}

	public class TheServer extends Thread{
		public void run() {
			try(ServerSocket mysocket = new ServerSocket(5555);){
				System.out.println("Server is waiting for a client!");

				while(true) {
					ClientThread c = new ClientThread(mysocket.accept(), count);
					callback.accept("client has connected to server: " + "client #" + count);	//sends to server
					clients.add(c);
					System.out.println("added one to the arraylist-->");
					System.out.println(clients.size());
					c.start();

					count++;
				}
			}//end of try
				catch(Exception e) {
					callback.accept("Server socket did not launch");			//send to server
				}
			}//end of while
		}

		class ClientThread extends Thread{
			Socket connection;
			int count;
			ObjectInputStream in;
			ObjectOutputStream out;
			
			ClientThread(Socket s, int count){
				this.connection = s;
				this.count = count;	
			}
			
			public void updateClients(String message) {
				for(int i = 0; i < clients.size(); i++) {
					ClientThread t = clients.get(i);
					try {
					 t.out.writeObject(message);				//prints to the client's listview
					}
					catch(Exception e) {}
				}
			}

			//update a specific client(who to send to)
			public void updateIndividualClients(String message, int clientNumber) {
//				try {
//					this.out.writeObject(message);
//				} catch(Exception e) {}

				for(int i = 0; i < clients.size(); i++) {
					if(i+1 == clientNumber){
						ClientThread t = clients.get(i);
						try {
							t.out.writeObject(message);
						} catch(Exception e) {}
					}
				}
			}
			
			public void run(){
				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);	
				}
				catch(Exception e) {
					System.out.println("Streams not open");
				}
				
				updateClients("new client on server: client #"+count);		//sends to clients
					
				 while(true) {
					    try {
							String data = in.readObject().toString();
							if (data.equals("sending info to individuals")) {
								System.out.println("read in the \'sending info to individuals\'");
								String message = in.readObject().toString();
								System.out.println("read in 2nd data");
								String whoToSendTo = in.readObject().toString();
								String[] groupToText = whoToSendTo.split(", ");
								System.out.println("splitting info");
								callback.accept("*private message* client: " + count + " sent: " + message);    //update server
								updateIndividualClients("sending \"" + message + "\" to " + whoToSendTo, count);
								for (int i = 0; i < groupToText.length; i++) {
									System.out.println(i);
									int j = 0;
//
									while (j < clients.size() && j + 1 != Integer.parseInt(groupToText[i])) {
										j++;
									}
//
									if (j + 1 == Integer.parseInt(groupToText[i])) {
										updateIndividualClients("client " + count + " privately sent: " + message, Integer.parseInt(groupToText[i]));
									}
								}
							} else {
								System.out.println("didn't get into the if statement");
								callback.accept("client: " + count + " sent: " + data);    //update server
								updateClients("client #" + count + " said: " + data);                //update clients
							}
						}catch(Exception e) {
					    	callback.accept("OOOOPPs...Something wrong with the socket from client: " + count + "....closing down!");	//updates server
					    	updateClients("Client #"+count+" has left the server!");	//updates clients
					    	clients.remove(this);
					    	break;
					    }
					}
				}//end of run
		}//end of client thread
}


	
	

	
