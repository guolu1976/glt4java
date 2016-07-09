package glt.NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.event.EventListenerList;

public class TcpServer implements java.lang.Runnable{

    private Selector _selector = null; 
    private Thread _acceptThread = null;
    private ServerSocketChannel _serverChannel; 

    public synchronized void open(int port) throws IOException {  
    	try
	    {
    		_serverChannel = ServerSocketChannel.open();  
    		_serverChannel.configureBlocking(false);  
    		_serverChannel.socket().bind(new InetSocketAddress(port));  
	        this._selector = Selector.open();  
	        _serverChannel.register(_selector, SelectionKey.OP_ACCEPT);  
	        
	        _acceptThread = new Thread(this);
	        _acceptThread.start();
    	}
    	catch(Exception e)
    	{
    		if(_serverChannel!=null)
    		{
    			_serverChannel.close();
    			_serverChannel = null;
    		}
    		if(this._selector!=null)
    		{
    			this._selector.close();
    			this._selector = null;
    		}
    		throw e;
    	}
    	
    }  
    
    public synchronized void close()
    {
    	try
    	{
    		Iterator<Entry<String, TcpPeer>> iter = _tcpPeers.entrySet().iterator();
    		while (iter.hasNext()) {
    			Entry<String, TcpPeer> entry = iter.next();
    			TcpPeer peer = entry.getValue();
    			fireOnDisconnected(peer);
    			peer.close();
    		}
    		_tcpPeers.clear();
    		
    		_selector.close();
    		_selector = null;
    		_acceptThread.join();

    	}
    	catch(Exception e)
    	{
        	e.printStackTrace();
        	fireOnException(e);
    	}
    	finally
    	{
    		_acceptThread = null;
    	}
    }
    
    public boolean IsOpen()
    {
    	return _acceptThread !=null;
    }
    

	@Override
	public void run() {
		try
		{
	        while (true) {  
	        	select();
	        	checkTimeout();
	        }
		}
		catch(java.nio.channels.ClosedSelectorException c){
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fireOnException(e);
		}
		finally
		{
			if(_serverChannel!=null)
			{
				try {
					_serverChannel.close();
					_serverChannel = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}  
	
    private void select() throws IOException {  
        int count = _selector.select(60*1000);
        if(count<=0)
        	return;
        
        Iterator<SelectionKey> ite = this._selector.selectedKeys().iterator();  
        while (ite.hasNext()) {  
            SelectionKey key = ite.next();  
            ite.remove();  
            if (key.isAcceptable()) {  
                ServerSocketChannel server = (ServerSocketChannel) key  
                        .channel();  
                SocketChannel channel = server.accept();
                
                try
                {
                	String name = channel.getRemoteAddress().toString();
                	TcpPeer peer = new TcpPeer(this._selector, channel, name);
                	addPeer(name, peer);
                }
                catch(Exception e)
                {
                	fireOnException(e);
                }
                
            } else if (key.isReadable()) {
            	TcpPeer peer = (TcpPeer)key.attachment();
            	try
            	{
            		peer.doRead();
            	}
            	catch(Exception e)
            	{
                	fireOnException(e);
                	removePeer(peer.getName());
            	}
            }  
        }  
    }  
    
    private void checkTimeout(){
    	long now = (new Date()).getTime();
		Iterator<Entry<String, TcpPeer>> iter = _tcpPeers.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, TcpPeer> entry = iter.next();
			TcpPeer peer = entry.getValue();
			
			long diff = now - peer.get_recvTimeStamp().getTime();
			long minutes = diff/(1000*60);
			
			if(minutes>1)
			{
				fireOnDisconnected(peer);
				peer.close();
				iter.remove();
			}
		}
    }
	
	private HashMap<String, TcpPeer> _tcpPeers = new HashMap<String, TcpPeer>(); 
	private synchronized void addPeer(String name, TcpPeer peer){
		removePeer(name);
        fireOnConnected(peer);
        _tcpPeers.put(name, peer);
	}
	
	private synchronized void removePeer(String name){
		TcpPeer oldPeer = _tcpPeers.get(name);
		if(oldPeer!=null){
			fireOnDisconnected(oldPeer);
			oldPeer.close();
			_tcpPeers.remove(name);
		}
	}
	
	private EventListenerList  _listenerList = new EventListenerList();
	public synchronized void addListener(TcpServerListener l)
	{ 
		_listenerList.add(TcpServerListener.class, l); 
	} 
	
	public synchronized void removeListener(TcpServerListener l) 
	{
		_listenerList.remove(TcpServerListener.class, l); 
	}
	

    protected TcpServerListener[] getTcpServerListeners(){
        return (TcpServerListener[])_listenerList.getListeners(TcpServerListener.class);
    }

    
	private void fireOnConnected(TcpPeer peer)
	{
		TcpPeerEvent e=new TcpPeerEvent(this, peer); 
		synchronized(this) {
			TcpServerListener[] listeners = getTcpServerListeners();
			for(int i=0;i<listeners.length;i++){ 
				TcpServerListener l= listeners[i]; 
				l.OnConnected(e);
			}
		}
	}
	
	private void fireOnDisconnected(TcpPeer peer)
	{
		TcpPeerEvent e=new TcpPeerEvent(this, peer); 
		synchronized(this) {
			TcpServerListener[] listeners = getTcpServerListeners();
			for(int i=0;i<listeners.length;i++){ 
				TcpServerListener l= listeners[i]; 
				l.OnDisconnected(e);
			}
		}
	}
	
	private void fireOnException(Exception e)
	{
		synchronized(this) {
			TcpServerListener[] listeners = getTcpServerListeners();
			for(int i=0;i<listeners.length;i++){ 
				TcpServerListener l= listeners[i]; 
				l.OnException(e);
			}
		}
	}

}

