package Message;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


/************
 * Different messages all inherits from this message
 * 
 * Different message types may have different encoding schemes as well as decoding schemes
 * 
 * Different messages may have different encryptions. i.e. Auth_Resp may have strongest encryption, data may have less strong
 * 
 * After serializing messages, we can also encode them before sending them over the socket, maybe.
 * 
 * Auth_Rsp has data(user name, password)
 * 
 * Auth_Conf has data(boolean indicating the result of the authentication)
 * 
 * Data has data(chatting contents)
 * 
 * 
 * Work needed to do:
 * 1. Think of new message types, if any
 * 2. Think of different encoding/encryption mechanisms for different msgs And implements the abstract methods MessageEncode, MessageDecode, MessageEncrypt, MessageDecrypt
 * 3. We also need to encrypt the user info on the server
 * *************/
public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int sequencenumber;
	protected int nonce;
	protected MessageType messageType;
	protected long timestamp;
	protected byte[] data = null;
	protected byte[] hashedresult = null;
	//int encodingType;
	//int encryptionMethod;
	//private int dataLength;

	public Message(int seq){
		timestamp = System.currentTimeMillis();
		sequencenumber = seq;
		nonce = -1;
	}
	
	/*public Message(int seq, int nonce, MessageType messageType, long timestamp){
		this.timestamp = timestamp;
		sequencenumber = seq;
		this.nonce = nonce;
		this.messageType = messageType;
	}*/
	
	/************** Getters and Setters  ****************/
	public MessageType getMessageType() {
		return messageType;
	}
	public long getTimestamp() {
		return timestamp;
	}
	
	public int getSequencenumber() {
		return sequencenumber;
	}

	public byte[] getData() {
		return data;
	}

	public byte[] getHashedresult() {
		return hashedresult;
	}
	
	
	public int getNonce() {
		return nonce;
	}

	public void setNonce(int nonce) {
		this.nonce = nonce;
	}

	public void setSequencenumber(int sequencenumber) {
		this.sequencenumber = sequencenumber;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setHashedresult(byte[] hashedresult) {
		this.hashedresult = hashedresult;
	}
	/*public int getEncodingType() {
		return encodingType;
	}
	public void setEncodingType(int encodingType) {
		this.encodingType = encodingType;
	}
	public int getEncriptionMethod() {
		return encryptionMethod;
	}
	public void setEncriptionMethod(int encriptionMethod) {
		this.encryptionMethod = encriptionMethod;
	}*/

	

	/********************** local methods **********************/
	//public void MessageEncode(){}
	//public void MessageDecode(){}
	
	public void MessageEncrypt(SecretKey key, String data){
		try {
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			this.data = cipher.doFinal(data.getBytes());
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
	}
	public byte[] MessageDecrypt(SecretKey key){

		try {
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
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
	
	public byte[] hashAllInfo(SecretKey key){
		byte[] result;
		byte[] seq = new Integer(sequencenumber).toString().getBytes();
		byte[] noncevalue = new Integer(nonce).toString().getBytes();
		int len = 0;
		len += seq.length;
		len += noncevalue.length;
		int messagetypelen = this.messageType.toString().length();
		len += messagetypelen;
		len += data==null?0:data.length;
		result = new byte[len+14];
		//System.out.println("total:"+(len+14));
		
		//This part is the sequence number
		System.arraycopy(seq, 0, result, 0, seq.length);
		//System.out.println("seq: "+seq.length);
		
		//this part is the message type
		byte[] msgtype = messageType.toString().getBytes();
		System.arraycopy(msgtype, 0, result, seq.length, messagetypelen);
		//System.out.println("msg type: "+messagetypelen);
		
		//this part is time stamp
		byte[] timestamparr = new Long(timestamp).toString().getBytes();
		System.arraycopy(timestamparr, 0, result, seq.length+messagetypelen, 13);
		//System.out.println("time: "+timestamparr.length);
		
		//this part is the nonce encrypted
		System.arraycopy(noncevalue, 0, result, seq.length+messagetypelen+13, noncevalue.length);
		//System.out.println("nonce: "+noncevalue.length);
		
		//this part is the data encrypted
		if (data!=null)
		System.arraycopy(data, 0, result, seq.length+noncevalue.length+messagetypelen+13, data.length);
		
		Mac mac = null;
        try {
        	mac = Mac.getInstance("HmacMD5");
			mac.init(key);
			return mac.doFinal(result);
			
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
        return null;
	}
	
	public boolean checkIntegrity(SecretKey key){
		if (key==null) return true;
		return Arrays.equals(hashAllInfo(key), hashedresult);
	}
}
