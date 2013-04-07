package Message;

import javax.crypto.SecretKey;

public class AuthenticationConfirmation extends Message{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean authenticated;
	public AuthenticationConfirmation(boolean authenticated, int sequencenumber,SecretKey hashKey, int sessionNonce)
	{
		super(sequencenumber);
		this.messageType = MessageType.Auth_Conf;
		this.authenticated = authenticated;
		this.data = authenticated?"true".getBytes():"false".getBytes();
		this.nonce = sessionNonce;
		hashedresult = hashAllInfo(hashKey);
	}
	
	public boolean isAuthenticated() {
		return authenticated;
	}
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
	
}
