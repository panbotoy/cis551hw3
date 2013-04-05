package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import java.util.HashSet;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;

import Message.AuthenticationConfirmation;
import Message.AuthenticationRequest;
import Message.AuthenticationResponse;
import Message.ClientAuthenticationMessage;
import Message.ClientPublicKeyMessage;
import Message.ClientResponseAuthenticationMessage;
import Message.DataMessage;
import Message.ExitMessage;
import Message.Message;
import Message.MessageType;
import Message.NonceAndHashKeyMessage;
import Message.NonceAndHashKeyMessageRsp;
import Message.ServerAuthenticationMessage;
import Message.ServerPublicKeyMessage;
import Message.ServerResponseAuthenticationMessage;

public class ClientMessageHandler {
	/******
	 * Returns the state of the client after handling the msg. true is continue working, false is stop working
	 * @throws IOException 
	 * *******/
	private SecretKey clientDesKey;
	private byte[] clientPubKeyEnc;
	private int serversequencenumber; //always keep last server's sequence number
	private int clientsequencenumber; //always keep current clients's sequence number
	private long timestamp;
	private int serverauthrandom, clientauthrandom;
	
	public ClientMessageHandler(){
		serversequencenumber = 0;
		clientsequencenumber = 0;
		timestamp = 0;
		serverauthrandom = -1;
		clientauthrandom = (int)(Math.random()*Math.pow(10, 10));
		sessionNonce = 0;
	}
	
	/**********4/5 Bo	 * 
	 * **************/
	private int sessionNonce;
	private SecretKey hashKey;
	private HashSet<MessageType> expectingMessageType;
	
	public void sendMessage(ObjectOutputStream oos, Message msg)
	{
		if(msg == null)
		{
			return;
		}
		try {
			oos.writeObject(msg);
			oos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("");
			e.printStackTrace();
		}
	}
	
	public boolean isExpectingMessageType(MessageType messageType)
	{
		return this.expectingMessageType.contains(messageType);
	}
	public void clearExpectingMessageTypes()
	{
		this.expectingMessageType.clear();
	}
	public void setExpectingMessageType(MessageType messageType)
	{
		this.expectingMessageType.add(messageType);
	}
	/**********4/5 Bo End********/
	
	public boolean handleMsg(Message msg, ObjectOutputStream oos, ObjectInputStream ois, BufferedReader userInput) throws IOException
	{
		//if(!msg.checkIntegrity(clientDesKey)) return false;
		
		if(msg.getSequencenumber()==(serversequencenumber+1)){
			serversequencenumber = msg.getSequencenumber();
		}else {
			return false;
		}
		
		if(timestamp==0){
			timestamp = msg.getTimestamp();
		}else if(timestamp==msg.getTimestamp()||(System.currentTimeMillis()-msg.getTimestamp())>2000){
			return false;
		}else{
			timestamp = msg.getTimestamp();
		}
		
		switch(msg.getMessageType())
		{
			case Auth_Req:
				System.out.println("Please input your username and password:");
				String responseLine = userInput.readLine();
				AuthenticationResponse authRsp = new AuthenticationResponse(clientDesKey, responseLine,++clientsequencenumber);
				oos.writeObject(authRsp);
				oos.flush();
				return true;
			case Auth_Conf:
				AuthenticationConfirmation authConf = (AuthenticationConfirmation) msg;
				if(authConf.isAuthenticated()){
					System.out.println("Log in successful! You can start to chat!");
					return true;
				}
				else{
					System.out.println("Log in failed! Close connection!");
					return false;
				}
			case Data:
				DataMessage dataMsg = (DataMessage) msg;
				byte[] dataarr = dataMsg.MessageDecrypt(clientDesKey);
				String data = printData(dataarr);
				System.out.println("Server Said: " + data);
				return true;
			case Server_pub:
				ServerPublicKeyMessage serverpubkeymsg = (ServerPublicKeyMessage)msg;
				generateClientDesKey(serverpubkeymsg);
				sendClientPubKeyEnc(oos, clientPubKeyEnc);
				System.out.println("get server pub");
				return true;
			case Server_auth:
				ServerAuthenticationMessage serverauthmsg = (ServerAuthenticationMessage)msg;
				serverauthrandom = serverauthrandom==-1?Integer.parseInt(printData(serverauthmsg.MessageDecrypt(clientDesKey))):-1;
				sendClientResponseAuthenticationMessage(oos);
				sendClientAuthenticationMessage(oos);
				System.out.println("get server auth");
				return true;
			case Server_rspauth:
				ServerResponseAuthenticationMessage serverrspauthmsg = (ServerResponseAuthenticationMessage)msg;
				System.out.println(clientauthrandom==serverrspauthmsg.getAnswer());
				return clientauthrandom==serverrspauthmsg.getAnswer();
			case Nonce_Hash:
				System.out.println("received Nonce Hash");
				NonceAndHashKeyMessage nonceandhashmsg = (NonceAndHashKeyMessage)msg;
				this.sessionNonce = nonceandhashmsg.getNonce();
				this.hashKey = nonceandhashmsg.getHashKey();
				NonceAndHashKeyMessageRsp nonceandhashmsgrsp = new NonceAndHashKeyMessageRsp(++clientsequencenumber,sessionNonce, hashKey);
				sendMessage(oos, nonceandhashmsgrsp);
				System.out.println("sent Nonce Hash rsp");
				return true;
			default:
				System.out.println("Received unknown message type");
				return true;
		}
	}
		
	private void generateClientDesKey(ServerPublicKeyMessage serverpubkeymsg){		
        
        try {
        	// get server public key from server public key encryption 
        	KeyFactory clientKeyFac = KeyFactory.getInstance("DH");
    		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(serverpubkeymsg.getPublickey());
    		PublicKey serverPubKey = clientKeyFac.generatePublic(x509KeySpec);
    		
    		// client creates his own DH key pair
    		DHParameterSpec dhParamSpec = ((DHPublicKey)serverPubKey).getParams();
        	KeyPairGenerator clientKpairGen = KeyPairGenerator.getInstance("DH");
			clientKpairGen.initialize(dhParamSpec);
			KeyPair clientKpair = clientKpairGen.generateKeyPair();

	        // client creates and initializes his DH KeyAgreement object
	        KeyAgreement clientKeyAgree = KeyAgreement.getInstance("DH");
	        clientKeyAgree.init(clientKpair.getPrivate());
	        
	        // client encodes his public key, and sends it over to Server.
	        clientPubKeyEnc = clientKpair.getPublic().getEncoded();
	        
	        // client generates his shared secret
	        clientKeyAgree.doPhase(serverPubKey, true);
	        clientDesKey = clientKeyAgree.generateSecret("DES");
	        //System.out.println("Client shared secret generated!");
	             
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
    
	}
	
	private void sendClientPubKeyEnc(ObjectOutputStream oos, byte[] clientPubKeyEnc){
		// send out client's public key encrypt via socket
        ClientPublicKeyMessage clientpubkeymsg = new ClientPublicKeyMessage(clientPubKeyEnc, ++clientsequencenumber);
        try {
			oos.writeObject(clientpubkeymsg);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendClientResponseAuthenticationMessage(ObjectOutputStream oos){
		ClientResponseAuthenticationMessage clientrspauthmsg = new ClientResponseAuthenticationMessage(clientauthrandom, ++clientsequencenumber);
		try {
			oos.writeObject(clientrspauthmsg);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendClientAuthenticationMessage(ObjectOutputStream oos){
		ClientAuthenticationMessage clientauthmsg = new ClientAuthenticationMessage(++clientsequencenumber,clientDesKey,clientauthrandom);
		try {
			oos.writeObject(clientauthmsg);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	private String printData(byte[] data){
		StringBuffer string = new StringBuffer();
		for(int i=0;i<data.length;i++){
			string.append((char)data[i]);
		}
		return string.toString();
	}
	
	public int getClientsequencenumber() {
		return clientsequencenumber;
	}

	public void setClientsequencenumber(int clientsequencenumber) {
		this.clientsequencenumber = clientsequencenumber;
	}

	public SecretKey getClientDesKey() {
		return clientDesKey;
	}
}
