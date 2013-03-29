package Message;

public class AuthenticationResponse extends Message{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String data;
	private int dataLength;
	
	public AuthenticationResponse (String data)
	{
		this.setData(data);
		this.setDataLength(data.length());
		this.messageType = MessageType.Auth_Rsp;
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

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}
	
	
}
