package Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

public class NonceAndHashKeyMessage extends Message{
	

	/**
	 * 
	 */ 
	private static final long serialVersionUID = 1L;
	
	protected SecretKey hashKey;
	protected int nonce;
	
	public NonceAndHashKeyMessage(int seq, MessageType messageType, SecretKey hashKey, int nonce) {
		super(seq);
		this.messageType = messageType;
		this.hashKey = hashKey;
		this.nonce = nonce;
		hashAllInfo(hashKey);
	}
	
	public SecretKey getHashKey(){
		return this.hashKey;
	}
	
	public int getNonce(){
		return this.nonce;
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
		ByteArrayOutputStream hashkeybytes = new ByteArrayOutputStream();
		byte[] hashkeyarr = null;
		try {
			ObjectOutputStream hashkeyoutputstream = new ObjectOutputStream(hashkeybytes);
			hashkeyoutputstream.writeObject(hashKey);
			hashkeyarr = hashkeybytes.toByteArray();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		len += hashkeyarr.length;
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
		
		//this part is the hashkey
		System.arraycopy(hashkeyarr, 0, result, seq.length+noncevalue.length+messagetypelen+13+(data==null?0:data.length), hashkeyarr.length);
		
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
}
