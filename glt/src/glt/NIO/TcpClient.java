package glt.NIO;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.EventObject;
import java.util.Iterator;

import javax.swing.event.EventListenerList;

public class TcpClient extends TcpClientBase implements Runnable{
	Selector _selector;

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
	        _selector.select();  
	        Iterator<SelectionKey> ite = this._selector.selectedKeys().iterator();  
	        while (ite.hasNext()) {  
	            SelectionKey key = ite.next();  
	            ite.remove();  
	            if (key.isReadable()) {  
	            	doRead();
	            }  
	        }  
		}
	}

	private void doRead() throws IOException{
		super.readMessage();
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
	
	@Override
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
