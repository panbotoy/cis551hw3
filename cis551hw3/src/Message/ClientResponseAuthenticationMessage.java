package Message;

public class ClientResponseAuthenticationMessage extends Message{
	
	private int answer;
	public ClientResponseAuthenticationMessage(int answer, int seq){
		super(seq);
		this.messageType = MessageType.Client_rspauth;
		this.answer = answer;
	}
	public int getAnswer() {
		return answer;
	}
}
