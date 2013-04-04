package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;
import com.sun.crypto.provider.SunJCE;

import Message.AuthenticationConfirmation;
import Message.AuthenticationRequest;
import Message.AuthenticationResponse;
import Message.ClientPublicKeyMessage;
import Message.DataMessage;
import Message.Message;

public class ServerMessageHandler {
	private String username = "user";
	private String password = "123";
	private SecretKey serverDesKey;
	private int clientsequencenumber = 0; //always keep last client's sequence number
	private int serversequencenumber = 0; //always keep current server's sequence number
	private long timestamp = 0;
	
	public boolean handleMsg(Message msg, ObjectOutputStream oos, ObjectInputStream ois, BufferedReader userInput, KeyAgreement serverKeyAgree) throws IOException
	{
		if(!msg.checkIntegrity(serverDesKey)) return false;
		
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
		
		switch(msg.getMessageType())
		{
			case Auth_Rsp:
				System.out.println("Received client username and password");
				AuthenticationResponse authRsp = (AuthenticationResponse) msg;
				
				byte[] userinfobytearr = authRsp.MessageDecrypt(serverDesKey);
				String userinfo = printData(userinfobytearr);
				boolean isAuthentication = (userinfo.equals(username + " " + password));
				//sequence number==2, need pass a 3 to this message
				AuthenticationConfirmation authConf = new AuthenticationConfirmation(isAuthentication, ++serversequencenumber, serverDesKey);
				oos.writeObject(authConf);
				oos.flush();
				return isAuthentication;
			case Data:
				DataMessage dataMsg = (DataMessage) msg;
				byte[] dataarr = dataMsg.MessageDecrypt(serverDesKey);
				String data = printData(dataarr);
				System.out.println("Client Said: " + data);
				return true;
			case Exit:
				System.out.println("Client requires close connection.");
				return false;
			case Client_pub:
				ClientPublicKeyMessage clientpubkeymsg = (ClientPublicKeyMessage)msg;
				generateServerDesKey(clientpubkeymsg, serverKeyAgree);
				AuthenticationReq(oos, serverDesKey);
				return true;
			default:
				System.out.println("Received unknown message type");
				return true;
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
	
	private boolean AuthenticationReq(ObjectOutputStream oos, SecretKey serverDesKey){

		AuthenticationRequest authReq=  new AuthenticationRequest(++serversequencenumber, serverDesKey);
		try {
			oos.writeObject(authReq);
			oos.flush();
		} catch (IOException e) {
			System.out.println("Error in sending authentication request--server authenticationReq");
			e.printStackTrace();
		}
		return true;
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
	
}
