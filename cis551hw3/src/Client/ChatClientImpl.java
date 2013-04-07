package Client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import server.WrappedTimer;

import Message.DataMessage;
import Message.ExitMessage;
import Message.Message;
import Message.MessageMultiplexer;
import Message.SentObject;

public class ChatClientImpl {
	private String hostName;
	private int portNumber;
	private Socket connection;
	//private String authenticationReq;
	private ClientMessageHandler clientmessagehandler;
	private WrappedTimer wrappedtimer;
	
	public ChatClientImpl(){
		//authenticationReq = new String("please input username and password");
		clientmessagehandler = new ClientMessageHandler();
		wrappedtimer = new WrappedTimer();
	}
	
	public void Start(String hostName, int portNumber)
	{
		this.hostName = hostName;
		this.portNumber = portNumber;
		
		try {
			this.connection = new Socket(this.hostName, this.portNumber);
			clientmessagehandler.setConnection(connection);
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
			ObjectOutputStream oos = new ObjectOutputStream(this.connection.getOutputStream());
			ObjectInputStream socketois = new ObjectInputStream(socketInput); 
			BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));   //Read user input
			boolean clientWorking = true;
			while(clientWorking)
			{
				if(socketInput.available()>0)     // if client receives any message from server
				{
					try {
						//set up timer
						wrappedtimer.getTimer().cancel();
						wrappedtimer.setTimer(new Timer(true));
						wrappedtimer.getTimer().schedule(new Strobe(), wrappedtimer.getTimerinterval());
												
						SentObject rcvobj = (SentObject)socketois.readObject();
				        
				        byte[] decryptedarray =  clientmessagehandler.arrayDecrypt(rcvobj.getContent());
				        ByteArrayInputStream inputarrayhelper = new ByteArrayInputStream(decryptedarray);
				        ObjectInputStream ois = new ObjectInputStream(inputarrayhelper);
				        
						Message msg = (Message)ois.readObject();
						clientWorking = clientmessagehandler.handleMsg(msg, oos, ois, userInput,wrappedtimer);  //handle the message
					} catch (ClassNotFoundException e) {
						System.out.println("Cannot cast to any message types when received from server");
						e.printStackTrace();
					}
				}
				if(System.in.available()>0)   //if client user sends anything
				{
					String responseLine = userInput.readLine();
					if(responseLine.equals("exit"))   //Client Sends exit
					{
						ExitMessage exit = new ExitMessage(clientmessagehandler.getHashKey(),clientmessagehandler.getClientsequencenumber()+1, clientmessagehandler.getSessionNonce());
						clientmessagehandler.setClientsequencenumber(clientmessagehandler.getClientsequencenumber()+1);
						clientmessagehandler.sendMessage(oos, exit);
						return;
					}
					else    // Client sends normal data message
					{
						DataMessage data = new DataMessage(clientmessagehandler.getHashKey(),responseLine,clientmessagehandler.getClientsequencenumber()+1, clientmessagehandler.getSessionNonce());
						clientmessagehandler.setClientsequencenumber(clientmessagehandler.getClientsequencenumber()+1);
						clientmessagehandler.sendMessage(oos, data);
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
	
	/****************************** Add Timer Task ******************************/
	private class Strobe extends TimerTask {
		
		public void run() {
			try {
				connection.close();
			} catch (IOException e) {
				System.out.println("In timertask");
				e.printStackTrace();
			}
		}
	}
}
