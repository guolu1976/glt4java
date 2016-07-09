package glt.NIO;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.EventObject;
import java.util.Iterator;

import javax.swing.event.EventListenerList;

public class TcpClient implements Runnable{
	Selector _selector;
	SocketChannel _channel;
	Thread _recvThread;
	boolean _exitEvent = false;
	SocketAddress _socketAddress;
	public TcpClient(){
		
	}
	
	public boolean isOpen(){
		return _recvThread!=null;
	}
	
	public synchronized void Open(SocketAddress remote) throws Exception{
		try
		{
			if(isOpen())
				return;
			_exitEvent = false;
			_socketAddress = remote;
			_channel = null;
			_selector = Selector.open();
						
			_recvThread = new Thread(this);
			_recvThread.start();
			
		}
		catch(Exception e)
		{
			if(_channel!=null)
			{
				try {
					_channel.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				_channel = null;
			}
			
			if(_selector!=null)
			{
				_selector.close();
				_selector = null;
			}
			_recvThread = null;
			throw e;
		}
	}
	
	public synchronized void close()	{
		try
		{
			if(!isOpen())
				return;
			
			_exitEvent  = true;
			if(_channel!=null){
				_channel.close();
			}
			_selector.close();
			_recvThread.join();
			_channel = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			_channel = null;
			_selector = null;
			_recvThread = null;
		}
	}

	public synchronized boolean isConnected(){
		try{
			if(_channel==null)
				return false;
			return _channel.isConnected();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	@Override
	public void run() {
		boolean connected = false;
		while(!_exitEvent )
		{
			try
			{
				connected = false;
				connect();
				fireOnConnected();
				connected = true;
			}
			catch(Exception ce)
			{
				ce.printStackTrace();
			}
			
			if(connected){
				try
				{
					select();
				}
				catch(Exception se)
				{
					se.printStackTrace();
				}
				
				fireOnDisconnected();
			}
		
			
			int count = 0;
			while(!_exitEvent)
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				count++;
				if(count>=10)
					break;
			}
		}
	}
	
	private synchronized void connect() throws IOException
	{
		if(_channel==null)
		{
			_channel = SocketChannel.open();
		}
		else 
		{
			_channel.close();
			_channel = SocketChannel.open();
		}
		_channel.configureBlocking(true);
		_channel.connect(_socketAddress);
		_channel.configureBlocking(false);
		_channel.register(_selector, SelectionKey.OP_READ);
	}
	
	private void select() throws IOException{
		while(true){
			//当注册的事件到达时，方法返回；否�?,该方法会�?直阻�?  
	        _selector.select();  
	        // 获得selector中�?�中的项的迭代器，�?�中的项为注册的事件  
	        Iterator<SelectionKey> ite = this._selector.selectedKeys().iterator();  
	        while (ite.hasNext()) {  
	            SelectionKey key = ite.next();  
	            //TODO: 删除已�?�的key,以防重复处理  
	            ite.remove();  
	            if (key.isReadable()) {  
	            	// 获得了可读的事件  
	            	doRead();
	            }  
	        }  
		}
	}
	
	ByteBuffer _msgHeader = ByteBuffer.allocate(MessageHeader.getSize());//TODO:接收缓存大小
	ByteBuffer _msgBody = ByteBuffer.allocate(1024);
	private void doRead() throws IOException{
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
	public synchronized void addListener(TcpClientListener l)
	{ 
		_listenerList.add(TcpClientListener.class, l); 
	} 
	
	public synchronized void removeListener(TcpClientListener l) 
	{
		_listenerList.remove(TcpClientListener.class, l); 
	}
	
	/**
     * @return 在此对象上监听的�?有MyListener类型的监听器
     */
    protected TcpClientListener[] getTcpClientListeners(){
        return (TcpClientListener[])_listenerList.getListeners(TcpClientListener.class);
    }
    
	protected void fireOnConnected() 
	{ 
		EventObject e=new EventObject(this); 
		synchronized(this) {
			TcpClientListener[] listeners = getTcpClientListeners();
			for(int i=0;i<listeners.length;i++){ 
				TcpClientListener l= listeners[i]; 
				l.OnConnected(e);
			}
		}
	}
	
	protected void fireOnDisconnected() 
	{ 
		EventObject e=new EventObject(this); 
		synchronized(this) {
			TcpClientListener[] listeners = getTcpClientListeners();
			for(int i=0;i<listeners.length;i++){ 
				TcpClientListener l= listeners[i]; 
				l.OnDisconnected(e);
			}
		}
	}
	
	protected void fireOnMessage(MessageHeader header, byte[] body) 
	{ 
		RecvMessageEvent e=new RecvMessageEvent(this, header, body); 
		synchronized(this) {
			TcpClientListener[] listeners = getTcpClientListeners();
			for(int i=0;i<listeners.length;i++){ 
				TcpClientListener l= listeners[i]; 
				l.OnMessage(e);
			}
		}
	}
	
}
