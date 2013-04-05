package Message;

public enum MessageType {
	Server_auth,
	Client_auth,
	Client_rspauth,
	Server_rspauth,
	Server_pub,
	Client_pub,
	Auth_Req,
	Auth_Rsp,
	Auth_Conf,
	Data,
	Exit,
	Nonce_Hash,
	Nonce_Hash_rsp
}
