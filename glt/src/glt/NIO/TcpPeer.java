package glt.NIO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import javax.swing.event.EventListenerList;


public class TcpPeer {
	
	private SocketChannel _channel = null;
	
	public TcpPeer(Selector selector, SocketChannel channel) throws IOException
	{
	    // 设置成非阻塞  
	    channel.configureBlocking(false);  
	    //在和客户端连接成功之后，为了可以接收到客户端的信息，�?要给通道设置读的权限�?  
	    SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
	    key.attach(this);
	    
	    _channel = channel;
	}
	
	public void close()
	{
		synchronized(this){
			
		}
	}
	
	public void write(ByteBuffer[] srcs, int offset, int length) throws IOException
	{
		_channel.write(srcs, offset, length);
	}
	
	ByteBuffer _msgHeader = ByteBuffer.allocate(MessageHeader.getSize());//TODO:接收缓存大小
	ByteBuffer _msgBody = ByteBuffer.allocate(1024);
	public void doRead() throws IOException
	{
		//TODO:数据解析异常，关闭socket
		while(true)
		{
			int recv = 0;
			if(_msgHeader.hasRemaining())
			{
				recv = _channel.read(_msgHeader);
			}
			if(recv==0)
				break;
			if(recv<0)
				throw new IOException();
			
			if(_msgHeader.position()<_msgHeader.limit())
				break;
			
			MessageHeader header = new MessageHeader(_msgHeader);
			
			if(header.getBodySize()>0)
			{
				if(_msgBody.capacity()<header.getBodySize())
				{
					_msgBody = ByteBuffer.allocate(header.getBodySize());
				}
				
				_msgBody.limit(header.getBodySize());
				
				recv = _channel.read(_msgBody);
				
				if(recv==0)
					break;
				if(recv<0)
					throw new IOException();
				if(_msgBody.position()<header.getBodySize())
					break;
			}

			try
			{
				fireOnMessage(header, _msgBody.array());
			}
			finally
			{
				_msgHeader.clear();
				_msgBody.clear();
			}
		}
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
	
	/**
     * @return 在此对象上监听的�?有MyListener类型的监听器
     */
    protected TcpPeerListener[] getTcpPeerListeners(){
        return (TcpPeerListener[])_listenerList.getListeners(TcpPeerListener.class);
    }
    
	protected void fireOnMessage(MessageHeader header, byte[] body) 
	{ 
		RecvMessageEvent e=new RecvMessageEvent(this, header, body); 
		synchronized(this) {
			TcpPeerListener[] listeners = getTcpPeerListeners();
			for(int i=0;i<listeners.length;i++){ 
				TcpPeerListener l= listeners[i]; 
				l.OnMessage(e);
			}
		}
	}
}


