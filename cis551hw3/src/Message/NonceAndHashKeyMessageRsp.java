package Message;

import javax.crypto.SecretKey;

public class NonceAndHashKeyMessageRsp extends Message{

	/**
	 * 
	 */ 
	private static final long serialVersionUID = 1L;

	public NonceAndHashKeyMessageRsp(int seq, int nonce, SecretKey hashkey) {
		super(seq);
		this.messageType = MessageType.Nonce_Hash_rsp;
		this.nonce = nonce;
		this.hashedresult = this.hashAllInfo(hashkey);
	}
	
}
