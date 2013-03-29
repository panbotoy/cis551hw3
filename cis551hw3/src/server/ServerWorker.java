package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import Client.ClientMessageHandler;
import Message.AuthenticationRequest;
import Message.DataMessage;
import Message.ExitMessage;
import Message.Message;

public class ServerWorker {
	private Socket connection;
	
	public ServerWorker(){
		
	}
	
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
	
	public boolean Work(Socket connection) throws IOException
	{
		this.connection = connection;

		DataInputStream socketInput = new DataInputStream(this.connection.getInputStream()); //Read From socket
		ObjectOutputStream oos = new ObjectOutputStream(this.connection.getOutputStream());   // Write to socket
		ObjectInputStream ois = new ObjectInputStream(socketInput);							  // Read from socket
		
		BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));      // Read user input
		boolean serverWorking =  AuthenticationReq(oos);                                      //Send authentication request to client
		while(serverWorking)
		{
			if(socketInput.available()>0)   //If receives anything from socket
			{
				try {
					Message msg = (Message)ois.readObject();  //Get message from socket
					serverWorking = ServerMessageHandler.handleMsg(msg, oos, ois, userInput);   //Handles the msg
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					System.out.println("Cannot cast to any message types when received from client");
					e.printStackTrace();
				}
			}
			if(System.in.available()>0)    // If user output anything, send a DataMessage
			{
				String responseLine = userInput.readLine();
				DataMessage data = new DataMessage(responseLine);
				oos.writeObject(data);
				oos.flush();
			}
		}
		return true;
	}
	
	private boolean AuthenticationReq(ObjectOutputStream oos){
		// TODO Auto-generated method stub
		AuthenticationRequest authReq=  new AuthenticationRequest();
		try {
			oos.writeObject(authReq);
			oos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in sending authentication request--server authenticationReq");
			e.printStackTrace();
		}
		return true;
	}
}
