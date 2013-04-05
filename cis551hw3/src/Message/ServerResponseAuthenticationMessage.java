package Message;

public class ServerResponseAuthenticationMessage extends Message{
	
	int answer;
	public ServerResponseAuthenticationMessage(int seq, int random){
		super(seq);
		this.messageType = MessageType.Server_rspauth;
		answer = random;
	}
	public int getAnswer() {
		return answer;
	}
	
	
}
