package glt.NIO;

public interface TcpServerListener extends java.util.EventListener{
	
	public void OnConnected(TcpPeerEvent e);
	
	public void OnDisconnected(TcpPeerEvent e);
	
	public void OnException(Exception e);
}




