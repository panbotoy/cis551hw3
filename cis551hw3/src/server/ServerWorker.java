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
import Message.ServerPublicKeyMessage;

import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;

public class ServerWorker {
	
	private Socket connection;
	private DHParameterSpec dhSkipParamSpec;
	private KeyAgreement serverKeyAgree;
	private byte[] serverPubKeyEnc;
	private ServerMessageHandler servermessagehandler;
	
	public ServerWorker(){
		
		servermessagehandler = new ServerMessageHandler();
		
		//System.out.println("Creating Diffie-Hellman parameters...");
		AlgorithmParameterGenerator paramGen;
		try {
			
			paramGen = AlgorithmParameterGenerator.getInstance("DH");
			paramGen.init(512);
			AlgorithmParameters params = paramGen.generateParameters();
			dhSkipParamSpec = (DHParameterSpec)params.getParameterSpec(DHParameterSpec.class);	
			
			
	        KeyPairGenerator serverKpairGen = KeyPairGenerator.getInstance("DH");
	        serverKpairGen.initialize(dhSkipParamSpec);
	        KeyPair serverKpair = serverKpairGen.generateKeyPair();
	        
	        //Server creates and initializes her DH KeyAgreement object
	        serverKeyAgree = KeyAgreement.getInstance("DH");
	        serverKeyAgree.init(serverKpair.getPrivate());
	        
	        //get the public key and send it to the client
	        serverPubKeyEnc = serverKpair.getPublic().getEncoded();
	       

		} catch (NoSuchAlgorithmException e){
			e.printStackTrace();
		} catch (InvalidParameterSpecException e){
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
	
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
		boolean serverWorking = sendServerPubKey(oos, serverPubKeyEnc);                      //Send public key encrypt to client
		while(serverWorking)
		{
			if(socketInput.available()>0)   //If receives anything from socket
			{
				try {
					Message msg = (Message)ois.readObject();  //Get message from socket
					serverWorking = servermessagehandler.handleMsg(msg, oos, ois, userInput, serverKeyAgree);   //Handles the msg
				} catch (ClassNotFoundException e) {
					System.out.println("Cannot cast to any message types when received from client");
					e.printStackTrace();
				}
			}
			if(System.in.available()>0)// If user output anything, send a DataMessage
			{
				String responseLine = userInput.readLine();
				DataMessage data = new DataMessage(servermessagehandler.getServerDesKey(),responseLine, servermessagehandler.getServersequencenumber()+1);
				servermessagehandler.setServersequencenumber(servermessagehandler.getServersequencenumber()+1);
				oos.writeObject(data);
				oos.flush();
			}
		}
		return true;
	}
	
	private boolean sendServerPubKey(ObjectOutputStream oos, byte[] serverPubKeyEnc){
		ServerPublicKeyMessage serverpubkeymsg=  new ServerPublicKeyMessage(serverPubKeyEnc,servermessagehandler.getServersequencenumber()+1);
		servermessagehandler.setServersequencenumber(servermessagehandler.getServersequencenumber()+1);
		try {
			oos.writeObject(serverpubkeymsg);
			oos.flush();
		} catch (IOException e) {

			System.out.println("Error in sending server public key encrypt request--server serverPubKeyMessage");
			e.printStackTrace();
		}
		return true;
	}
	
}
