package Message;

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
		// TODO Auto-generated constructor stub
	}
	
	public SecretKey getHashKey(){
		return this.hashKey;
	}
	
	public int getNonce(){
		return this.nonce;
	}
}
