package Client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import Message.DataMessage;
import Message.ExitMessage;
import Message.Message;
import Message.MessageMultiplexer;

public class ChatClientImpl {
	private String hostName;
	private int portNumber;
	private Socket connection;
	private String authenticationReq = new String("please input username and password");
	private ClientMessageHandler clientmessagehandler;
	
	public ChatClientImpl(){
		clientmessagehandler = new ClientMessageHandler();
	}
	
	public void Start(String hostName, int portNumber)
	{
		this.hostName = hostName;
		this.portNumber = portNumber;
		
		try {
			this.connection = new Socket(this.hostName, this.portNumber);
			
			/****
			 * When connection started, server will send request for username and password
			 * Send user name and password
			 * Server will return authentication successful/failed 
			 * 
			 * Once failed, server will close the socket
			 * once successful, client can start chat, and can close connection by typing "EXIT"
			 * ***/
			
			/********
			 * Possible Extensions:
			 * 1. Create different types of packets: server authentication messages, client authentication messages, data messages, closing messages etc.
			 * 		instead of using just socket stream (DONE)
			 * 
			 * 2. Different encryptions/encoding for different msg, also maybe we can encode the stream
			 * 3. Encryption for the server side user info
			 * 
			 * *********/
			DataInputStream socketInput = new DataInputStream(this.connection.getInputStream());   //Read from socket
			ObjectInputStream ois = new ObjectInputStream(socketInput);                            // Read Object(message) from socket
			ObjectOutputStream oos = new ObjectOutputStream(this.connection.getOutputStream());    // write to socket

			BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));   //Read user input
			boolean clientWorking = true;
			while(clientWorking)
			{
				if(socketInput.available()>0)     // if client receives any message from server
				{
					try {
						Message msg = (Message)ois.readObject();
						clientWorking = clientmessagehandler.handleMsg(msg, oos, ois, userInput);  //handle the message
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						System.out.println("Cannot cast to any message types when received from server");
						e.printStackTrace();
					}
				}
				if(System.in.available()>0)   //if client user sends anything
				{
					String responseLine = userInput.readLine();
					if(responseLine.equals("exit"))   //Client Sends exit
					{
						ExitMessage exit = new ExitMessage(clientmessagehandler.getClientDesKey(),clientmessagehandler.getClientsequencenumber()+1);
						clientmessagehandler.setClientsequencenumber(clientmessagehandler.getClientsequencenumber()+1);
						oos.writeObject(exit);
						oos.flush();
						return;
					}
					else    // Client sends normal data message
					{
						DataMessage data = new DataMessage(clientmessagehandler.getClientDesKey(),responseLine,clientmessagehandler.getClientsequencenumber()+1);
						clientmessagehandler.setClientsequencenumber(clientmessagehandler.getClientsequencenumber()+1);
						oos.writeObject(data);
						oos.flush();
					}
				}
			}			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("Unknown host, check host name and port number");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Problems with the socket IO");
			e.printStackTrace();
		}		
	}
}
