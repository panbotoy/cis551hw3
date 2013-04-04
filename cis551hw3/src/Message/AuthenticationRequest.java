package Message;

import javax.crypto.SecretKey;

public class AuthenticationRequest extends Message{

	private static final long serialVersionUID = 1L;

	public AuthenticationRequest(int sequencenumber, SecretKey serverDesKey){
		super(sequencenumber);
		this.messageType = MessageType.Auth_Req;
		hashedresult = hashAllInfo(serverDesKey);
	}
	
	public void testMethod(){}
}
