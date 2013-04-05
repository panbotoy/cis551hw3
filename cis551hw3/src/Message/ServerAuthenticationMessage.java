package Message;

import javax.crypto.SecretKey;

public class ServerAuthenticationMessage extends Message{
	
	public ServerAuthenticationMessage(int seq, SecretKey key, int random){
		super(seq);
		this.messageType = MessageType.Server_auth;
		MessageEncrypt(key, new Integer(random).toString());
	}
}
