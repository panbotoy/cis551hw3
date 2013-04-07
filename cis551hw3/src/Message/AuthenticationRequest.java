package Message;

import javax.crypto.SecretKey;

public class AuthenticationRequest extends Message{

	private static final long serialVersionUID = 1L;

	public AuthenticationRequest(int sequencenumber, SecretKey hashKey, int sessionNonce){
		super(sequencenumber);
		this.messageType = MessageType.Auth_Req;
		this.nonce = sessionNonce;
		hashedresult = hashAllInfo(hashKey);
	}
	
	public void testMethod(){}
}
