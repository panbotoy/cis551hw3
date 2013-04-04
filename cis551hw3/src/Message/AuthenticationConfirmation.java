package Message;

import javax.crypto.SecretKey;

public class AuthenticationConfirmation extends Message{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean authenticated;
	public AuthenticationConfirmation(boolean authenticated, int sequencenumber,SecretKey serverDesKey)
	{
		super(sequencenumber);
		this.messageType = MessageType.Auth_Conf;
		this.authenticated = authenticated;
		hashedresult = hashAllInfo(serverDesKey);
	}
	
	public boolean isAuthenticated() {
		return authenticated;
	}
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
	
	@Override
	public byte[] hashAllInfo(SecretKey key){
		byte[] superresult = super.hashAllInfo(key);
		byte[] autharr = authenticated?"true".getBytes():"false".getBytes();
		byte[] result = new byte[superresult.length+autharr.length];
		System.arraycopy(superresult, 0, result, 0, superresult.length);
		System.arraycopy(autharr, 0, result, superresult.length, autharr.length);
		return result;
	}
}
