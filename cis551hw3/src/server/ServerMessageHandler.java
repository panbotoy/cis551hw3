package server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
import Message.Message;
import Message.MessageType;
import Message.Nonce;
import Message.NonceAndHashKeyMessage;
import Message.SentObject;
import Message.ServerAuthenticationMessage;
import Message.ServerResponseAuthenticationMessage;

public class ServerMessageHandler {
	private String username = "user";
	private String password = "123";
	private SecretKey serverDesKey = null;
	private int clientsequencenumber; //always keep last client's sequence number
	private int serversequencenumber; //always keep current server's sequence number
	private long timestamp;
	private int serverauthrandom, clientauthrandom;
	private HashSet<MessageType> expectingMessageType;
	private int sessionNonce = 0;
	private SecretKey hashKey = null;
	private boolean isencrypted, checknonce;
	
	public ServerMessageHandler(){
		clientsequencenumber = 0;
		serversequencenumber = 0;
		timestamp = 0;
		serverauthrandom = (int)(Math.random()*Math.pow(10, 10));
		clientauthrandom = -1;
		isencrypted = checknonce = false;
		expectingMessageType = new HashSet<MessageType>();
	}
	
	/*****************
	 * Bo: 4/5
	 * Add HashKey, add nonce
	 * ****************/
	
	public void generateHashKeyAndNonce()
	{
		this.sessionNonce = Nonce.getNonce().getValue();
        KeyGenerator kg = null;
		try {
			kg = KeyGenerator.getInstance("HmacMD5");
			this.hashKey = kg.generateKey();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Generate shared hashKey failed in Server!");
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
	
	
	/**********
	 * Bo: 4/5 End
	 * *************/

	
	public boolean handleMsg(Message msg, ObjectOutputStream oos, ObjectInputStream ois, BufferedReader userInput, KeyAgreement serverKeyAgree) throws IOException
	{
		
		if(msg.getSequencenumber()==(clientsequencenumber+1)){
			clientsequencenumber = msg.getSequencenumber();
		}else {
			//defend block message attack
			return false;
		}
		
		if(timestamp==0){
			timestamp = msg.getTimestamp();
		}else if(timestamp==msg.getTimestamp()||(System.currentTimeMillis()-msg.getTimestamp())>2000){
			//defend replay attack and delay attack
			return false; //restart server
		}else{
			timestamp = msg.getTimestamp();
		}
		
		if(checknonce){
			int receivednonce = msg.getNonce();
			if (receivednonce!=sessionNonce) return false;
		}
		
		switch(msg.getMessageType())
		{
			case Client_pub:
				if(!this.isExpectingMessageType(MessageType.Client_pub))return false;
				this.clearExpectingMessageTypes();
				this.setExpectingMessageType(MessageType.Client_rspauth);
				ClientPublicKeyMessage clientpubkeymsg = (ClientPublicKeyMessage)msg;
				generateServerDesKey(clientpubkeymsg, serverKeyAgree);
				ServerAuthenticationMessage serverauthmsg=  new ServerAuthenticationMessage(++serversequencenumber, serverDesKey, serverauthrandom);
				sendMessage(oos, serverauthmsg);
				System.out.println("server auth sent");
				return true;
			case Client_rspauth:
				System.out.println("get client auth rsp");
				if(!this.isExpectingMessageType(MessageType.Client_rspauth))return false;
				this.clearExpectingMessageTypes();
				this.setExpectingMessageType(MessageType.Client_auth);
				ClientResponseAuthenticationMessage clientrspauthmsg = (ClientResponseAuthenticationMessage)msg;
				int clientrspauthrandom = clientrspauthmsg.getAnswer();
				
				System.out.println("client auth is: "+(clientrspauthrandom == serverauthrandom));
				if(!(clientrspauthrandom == serverauthrandom)){
					System.out.println("clientrspauthrandom: "+clientrspauthrandom);
					System.out.println("serverauthrandom: "+serverauthrandom);
				}
				
				return clientrspauthrandom == serverauthrandom;
			
			case Client_auth:
				System.out.println("get client auth");
				if(!this.isExpectingMessageType(MessageType.Client_auth))return false;
				this.clearExpectingMessageTypes();
				this.setExpectingMessageType(MessageType.Nonce_Hash_rsp);
				ClientAuthenticationMessage clientauthmsg = (ClientAuthenticationMessage)msg;
				clientauthrandom = Integer.parseInt(printData(clientauthmsg.MessageDecrypt(serverDesKey)));
				//send answer to client
				ServerResponseAuthenticationMessage serverrspauthmsg=  new ServerResponseAuthenticationMessage(++serversequencenumber, clientauthrandom);
				sendMessage(oos,serverrspauthmsg);
				generateHashKeyAndNonce();
				isencrypted = true;
				checknonce = true;
				//send nonce and hashkey to client
				NonceAndHashKeyMessage nonceandhashkeymsg = new NonceAndHashKeyMessage(++serversequencenumber, MessageType.Nonce_Hash, this.hashKey, this.sessionNonce);
				sendMessage(oos, nonceandhashkeymsg);
				System.out.println("send Nonce Hash");
				
				return true;
			case Nonce_Hash_rsp:
				
				System.out.println("get Nonce Hash rsp");
				if(!this.isExpectingMessageType(MessageType.Nonce_Hash_rsp)||!msg.checkIntegrity(hashKey))return false;
				this.clearExpectingMessageTypes();
				this.setExpectingMessageType(MessageType.Auth_Rsp);
				if(!msg.checkIntegrity(hashKey)) {
					System.out.println("hash check failure");
					return false;
				}
				//AuthenticationReq(oos, hashKey);
				AuthenticationRequest authReq = new AuthenticationRequest(++serversequencenumber, hashKey, sessionNonce);
				sendMessage(oos, authReq);
				return true;			
			case Auth_Rsp:
				System.out.println("Received client username and password");
				if(!this.isExpectingMessageType(MessageType.Auth_Rsp)||!msg.checkIntegrity(hashKey))return false;
				this.clearExpectingMessageTypes();
				this.setExpectingMessageType(MessageType.Data);
				this.setExpectingMessageType(MessageType.Exit);
				AuthenticationResponse authRsp = (AuthenticationResponse) msg;
				
				byte[] userinfobytearr = authRsp.getData();
				String userinfo = printData(userinfobytearr);
				boolean isAuthentication = (userinfo.equals(username + " " + password));
				
				AuthenticationConfirmation authConf = new AuthenticationConfirmation(isAuthentication, ++serversequencenumber, hashKey, sessionNonce);
				sendMessage(oos, authConf);
				return isAuthentication;
			case Data:
				if(!this.isExpectingMessageType(MessageType.Data)||!msg.checkIntegrity(hashKey))return false;
				DataMessage dataMsg = (DataMessage) msg;
				byte[] dataarr = dataMsg.getData();
				String data = printData(dataarr);
				System.out.println("Client Said: " + data);
				return true;
			case Exit:
				System.out.println("Client requires close connection.");
				if(!this.isExpectingMessageType(MessageType.Exit)||!msg.checkIntegrity(hashKey))return true;
				return false;
				
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
	        byte[] encryteddata = arrayEncrypt(plaindata); 
	        SentObject sentobj = new SentObject(encryteddata);
			oos.writeObject(sentobj);
			oos.flush();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("");
			e.printStackTrace();
		}
	}
	
	private void generateServerDesKey(ClientPublicKeyMessage clientpubkeymsg, KeyAgreement serverKeyAgree){
		
		try {
			
			KeyFactory serverKeyFac = KeyFactory.getInstance("DH");
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(clientpubkeymsg.getPublickey());
	        PublicKey clientPubKey = serverKeyFac.generatePublic(x509KeySpec);
	        
	        // Server generates its shared secret
	        serverKeyAgree.doPhase(clientPubKey, true);
	        serverDesKey = serverKeyAgree.generateSecret("DES");
	        //System.out.println("Server shared secret generated!");
	        
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] arrayEncrypt(byte[] data){
		
		if (!isencrypted) return data;
		try {
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, serverDesKey);
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
			cipher.init(Cipher.DECRYPT_MODE, serverDesKey);
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
	
	public SecretKey getServerDesKey() {
		return serverDesKey;
	}

	public int getServersequencenumber() {
		return serversequencenumber;
	}
	
	public void setServersequencenumber(int serversequencenumber) {
		this.serversequencenumber = serversequencenumber;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	public SecretKey getHashKey() {
		return hashKey;
	}
	
	public int getSessionNonce() {
		return sessionNonce;
	}

	/*private void AuthenticationReq(ObjectOutputStream oos, SecretKey serverDesKey){

		AuthenticationRequest authReq=  new AuthenticationRequest(++serversequencenumber, hashKey, sessionNonce);
		try {
			oos.writeObject(authReq);
			oos.flush();
		} catch (IOException e) {
			System.out.println("Error in sending authentication request--server authenticationReq");
			e.printStackTrace();
		}
	}
	
	private void sendServerAuthenticationMessage(ObjectOutputStream oos){
		ServerAuthenticationMessage serverauthmsg=  new ServerAuthenticationMessage(++serversequencenumber, serverDesKey, serverauthrandom);
		try {
			oos.writeObject(serverauthmsg);
			oos.flush();
		} catch (IOException e) {
			System.out.println("Error in sending authentication request--server authenticationReq");
			e.printStackTrace();
		}
	}
	
	private void sendServerResponseAuthenticationMessage(ObjectOutputStream oos){
		ServerResponseAuthenticationMessage serverauthmsg=  new ServerResponseAuthenticationMessage(++serversequencenumber, clientauthrandom);
		try {
			oos.writeObject(serverauthmsg);
			oos.flush();
		} catch (IOException e) {
			System.out.println("Error in sending authentication request--server authenticationReq");
			e.printStackTrace();
		}
	}*/
}
