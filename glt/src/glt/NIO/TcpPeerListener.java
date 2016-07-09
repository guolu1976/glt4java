package glt.NIO;

public interface TcpPeerListener extends java.util.EventListener{
	//这里是当事件发生后的响应过程 
	public void OnMessage(RecvMessageEvent e); 
}
