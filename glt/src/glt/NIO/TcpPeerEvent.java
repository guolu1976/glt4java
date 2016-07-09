package glt.NIO;

import java.util.EventObject;

public class TcpPeerEvent extends EventObject{

	private static final long serialVersionUID = 1L;
	private TcpPeer _peer;
	public TcpPeerEvent(Object source, TcpPeer peer) {
		super(source);
		_peer = peer;
	}
	
	public TcpPeer getTcpPeer()
	{
		return _peer;
	}
}

