package Message;

import javax.crypto.SecretKey;

public class ExitMessage extends Message{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExitMessage(SecretKey key, int seq, int sessionNonce){
		super(seq);
		this.messageType = MessageType.Exit;
		this.nonce = sessionNonce;
		this.hashedresult = hashAllInfo(key);
	}
}
