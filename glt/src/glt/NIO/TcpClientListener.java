package glt.NIO;

import java.util.EventObject;

public interface TcpClientListener extends java.util.EventListener{
	//连接
	public void OnConnected(EventObject e);
	//断开
	public void OnDisconnected(EventObject e);
	//接收到消息
	public void OnMessage(RecvMessageEvent e); 
}
