package Message;

public class AuthenticationRequest extends Message{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	/**
	 * 
	 */

	public AuthenticationRequest()
	{
		this.messageType = MessageType.Auth_Req;
	}
	public void testMethod(){}

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
		System.out.print("");
	}
}
