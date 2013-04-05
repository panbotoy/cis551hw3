package Message;

import javax.crypto.SecretKey;

public class ClientAuthenticationMessage extends Message{
	
	public ClientAuthenticationMessage(int seq, SecretKey key, int random){
		super(seq);
		this.messageType = MessageType.Client_auth;
		MessageEncrypt(key, new Integer(random).toString());
	}
}
