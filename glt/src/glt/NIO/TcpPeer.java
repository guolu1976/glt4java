package glt.NIO;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;

import javax.swing.event.EventListenerList;


public class TcpPeer extends TcpClientBase {
	
	private SocketChannel _channel = null;
	private String _name;
	private Date _recvTimeStamp ;
	
	public TcpPeer(Selector selector, SocketChannel channel, String name) throws IOException	{
	    channel.configureBlocking(false);  
	    SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
	    key.attach(this);
	    _channel = channel;
	    _name = name;
	    set_recvTimeStamp(new Date());
	}
	
	public void close()	{
		synchronized(this){
			if(_channel==null)
				return;
			try {
				_channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			_channel = null;
		}
	}
	
	public String getName(){
		return _name;
	}
	
	public SocketAddress getRemoteAddress() throws IOException{
		return _channel.getRemoteAddress();
	}
	
	public void doRead() throws IOException {
		super.readMessage();
	}
	
	
	private EventListenerList  _listenerList = new EventListenerList();
	public synchronized void addListener(TcpPeerListener l)
	{ 
		_listenerList.add(TcpPeerListener.class, l); 
	} 
	
	public synchronized void removeListener(TcpPeerListener l) 
	{
		_listenerList.remove(TcpPeerListener.class, l); 
	}
	
    protected TcpPeerListener[] getTcpPeerListeners(){
        return (TcpPeerListener[])_listenerList.getListeners(TcpPeerListener.class);
    }
    
    @Override
	protected void fireOnMessage(MessageHeader header, byte[] body) 
	{ 
		RecvMessageEvent e=new RecvMessageEvent(this, header, body); 
		synchronized(this) {
			TcpPeerListener[] listeners = getTcpPeerListeners();
			for(int i=0;i<listeners.length;i++){ 
				TcpPeerListener l= listeners[i]; 
				l.OnMessage(e);
			}
			
			set_recvTimeStamp(new Date());
		}
	}

	public Date get_recvTimeStamp() {
		return _recvTimeStamp;
	}

	public void set_recvTimeStamp(Date recvTimeStamp) {
		this._recvTimeStamp = recvTimeStamp;
	}
}


