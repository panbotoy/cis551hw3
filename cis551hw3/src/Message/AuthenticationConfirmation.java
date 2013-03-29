package Message;

public class AuthenticationConfirmation extends Message{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean authenticated;
	public AuthenticationConfirmation(boolean authenticated)
	{
		this.messageType = MessageType.Auth_Conf;
		this.authenticated = authenticated;
	}
	
	public boolean isAuthenticated() {
		return authenticated;
	}
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	@Override
	public void MessageEncode() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void MessageDecode() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void MessageEncrypt(String key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void MessageDecrypt(String key) {
		// TODO Auto-generated method stub
		
	}
}
