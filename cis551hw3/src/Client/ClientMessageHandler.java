package Client;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import Message.SentObject;
import Message.ServerAuthenticationMessage;
import Message.ServerPublicKeyMessage;
import Message.ServerResponseAuthenticationMessage;

public class ClientMessageHandler {
	/******
	 * Returns the state of the client after handling the msg. true is continue working, false is stop working
	 * @throws IOException 
	 * *******/
	private SecretKey clientDesKey = null;
	private byte[] clientPubKeyEnc;
	private int serversequencenumber; //always keep last server's sequence number
	private int clientsequencenumber; //always keep current clients's sequence number
	private long timestamp;
	private int serverauthrandom, clientauthrandom;
	private int sessionNonce;
	private SecretKey hashKey;
	private HashSet<MessageType> expectingMessageType;
	private boolean isencrypted, checknonce;
	
	public ClientMessageHandler(){
		serversequencenumber = 0;
		clientsequencenumber = 0;
		timestamp = 0;
		serverauthrandom = -1;
		clientauthrandom = (int)(Math.random()*Math.pow(10, 10));
		sessionNonce = 0;
		isencrypted = checknonce = false;
		expectingMessageType = new HashSet<MessageType>();
		setExpectingMessageType(MessageType.Server_pub);
	}
	
	/**********4/5 Bo	 * 
	 * **************/
	
	
	
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
		
		if(checknonce){
			int receivednonce = msg.getNonce();
			if (receivednonce!=sessionNonce) {
				System.out.println(msg.getMessageType());
				System.out.println("received nonce: "+receivednonce);
				System.out.println("current nonce: "+sessionNonce);
				return false;
			}
		}
		
		switch(msg.getMessageType())
		{
			case Server_pub:
				if(!this.isExpectingMessageType(MessageType.Server_pub))return false;
				this.clearExpectingMessageTypes();
				this.setExpectingMessageType(MessageType.Server_auth);
				ServerPublicKeyMessage serverpubkeymsg = (ServerPublicKeyMessage)msg;
				generateClientDesKey(serverpubkeymsg);
				ClientPublicKeyMessage clientpubkeymsg = new ClientPublicKeyMessage(clientPubKeyEnc, ++clientsequencenumber);
				sendMessage(oos, clientpubkeymsg);
				System.out.println("send client pub key");
				return true;
			case Server_auth:
				System.out.println("get server auth");
				if(!this.isExpectingMessageType(MessageType.Server_auth)) return false;
				this.clearExpectingMessageTypes();
				this.setExpectingMessageType(MessageType.Server_rspauth);
				ServerAuthenticationMessage serverauthmsg = (ServerAuthenticationMessage)msg;
				serverauthrandom = Integer.parseInt(printData(serverauthmsg.MessageDecrypt(clientDesKey)));
				ClientResponseAuthenticationMessage clientrspauthmsg = new ClientResponseAuthenticationMessage(serverauthrandom, ++clientsequencenumber);
				sendMessage(oos, clientrspauthmsg);
				ClientAuthenticationMessage clientauthmsg = new ClientAuthenticationMessage(++clientsequencenumber,clientDesKey,clientauthrandom);
				sendMessage(oos, clientauthmsg);	
				return true;
			case Server_rspauth:
				System.out.println("get server auth rsp");
				if(!this.isExpectingMessageType(MessageType.Server_rspauth))return false;
				this.clearExpectingMessageTypes();
				this.setExpectingMessageType(MessageType.Nonce_Hash);
				ServerResponseAuthenticationMessage serverrspauthmsg = (ServerResponseAuthenticationMessage)msg;
				isencrypted = true;
				System.out.println("client auth is: "+(clientauthrandom==serverrspauthmsg.getAnswer()));
				if(!(clientauthrandom==serverrspauthmsg.getAnswer())){
					System.out.println("clientauthrandom: "+clientauthrandom);
					System.out.println("serverrspauthrandom: "+serverrspauthmsg.getAnswer());
				}
				return clientauthrandom==serverrspauthmsg.getAnswer();
			case Nonce_Hash:
				System.out.println("received Nonce Hash");	
				if(!this.isExpectingMessageType(MessageType.Nonce_Hash)||!msg.checkIntegrity(hashKey)) return false;
				this.clearExpectingMessageTypes();
				this.setExpectingMessageType(MessageType.Auth_Req);
				checknonce = true;
				NonceAndHashKeyMessage nonceandhashmsg = (NonceAndHashKeyMessage)msg;
				this.sessionNonce = nonceandhashmsg.getNonce();
				this.hashKey = nonceandhashmsg.getHashKey();
				NonceAndHashKeyMessageRsp nonceandhashmsgrsp = new NonceAndHashKeyMessageRsp(++clientsequencenumber,sessionNonce, hashKey);
				sendMessage(oos, nonceandhashmsgrsp);
				System.out.println("sent Nonce Hash rsp");
				return true;		
			case Auth_Req:
				System.out.println("Please input your username and password:");
				if(!this.isExpectingMessageType(MessageType.Auth_Req)||!msg.checkIntegrity(hashKey))return false;
				this.clearExpectingMessageTypes();
				this.setExpectingMessageType(MessageType.Auth_Conf);
				String responseLine = userInput.readLine();
				AuthenticationResponse authRsp = new AuthenticationResponse(hashKey, responseLine,++clientsequencenumber, sessionNonce);
				sendMessage(oos,authRsp);
				return true;
			case Auth_Conf:
				if(!this.isExpectingMessageType(MessageType.Auth_Conf)||!msg.checkIntegrity(hashKey))return false;
				this.clearExpectingMessageTypes();
				this.setExpectingMessageType(MessageType.Data);
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
				if(!this.isExpectingMessageType(MessageType.Data)||!msg.checkIntegrity(hashKey))return false;
				DataMessage dataMsg = (DataMessage) msg;
				byte[] dataarr = dataMsg.getData();
				String data = printData(dataarr);
				System.out.println("Server Said: " + data);
				return true;
			default:
				System.out.println("Received unknown message type");
				return true;
		}
	}
	
	public void sendMessage(ObjectOutputStream oos, Message msg)
	{
		
		if(msg == null)
		{
			return;
		}
		try {
			
			ByteArrayOutputStream bytearray = new ByteArrayOutputStream();
	        ObjectOutputStream o = new ObjectOutputStream(bytearray);
	        o.writeObject(msg);
	        byte[] plaindata = bytearray.toByteArray();
	        if (isencrypted){
	        	byte[] encryteddata = arrayEncrypt(plaindata); 
		        SentObject sentobj = new SentObject(encryteddata);
				oos.writeObject(sentobj);
				oos.flush();
	        }else{
	        	//isencrypted = true;
		        SentObject sentobj = new SentObject(plaindata);
				oos.writeObject(sentobj);
				oos.flush();
	        }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("");
			e.printStackTrace();
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
	
	public byte[] arrayEncrypt(byte[] data){
		
		if (!isencrypted) return data;
		try {
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, clientDesKey);
			return cipher.doFinal(data);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	public byte[] arrayDecrypt(byte[] data){
		
		if (!isencrypted) return data;
		try {
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, clientDesKey);
			return cipher.doFinal(data);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
	
	public SecretKey getHashKey() {
		return hashKey;
	}
	
	public int getSessionNonce() {
		return sessionNonce;
	}
	
	/*private void sendClientPubKeyEnc(ObjectOutputStream oos, byte[] clientPubKeyEnc){
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
	}	*/
}
