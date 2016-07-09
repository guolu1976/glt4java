package glt.NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import javax.swing.event.EventListenerList;

/*
 * 参�?? http://weixiaolu.iteye.com/blog/1479656
 * TODO:
 * - 添加异常事件
 * */
public class TcpServer implements java.lang.Runnable{
    //通道管理�?  
    private Selector _selector = null; 
    private Thread _acceptThread = null;
    private ServerSocketChannel serverChannel; 
    /** 
     * 获得�?个ServerSocket通道，并对该通道做一些初始化的工�? 
     * @param port  绑定的端口号 
     * @throws IOException 
     */  
    public void open(int port) throws IOException {  
    	try
	    {
    		// 获得�?个ServerSocket通道  
	    	serverChannel = ServerSocketChannel.open();  
	        // 设置通道为非阻塞  
	        serverChannel.configureBlocking(false);  
	        // 将该通道对应的ServerSocket绑定到port端口  
	        serverChannel.socket().bind(new InetSocketAddress(port));  
	        // 获得�?个�?�道管理�?  
	        this._selector = Selector.open();  
	        //将�?�道管理器和该�?�道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件,注册该事件后�?  
	        //当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞�??  
	        serverChannel.register(_selector, SelectionKey.OP_ACCEPT);  
	        
	        _acceptThread = new Thread(this);
	        
	        _acceptThread.start();
    	}
    	catch(Exception e)
    	{
    		if(serverChannel!=null)
    		{
    			serverChannel.close();
    			serverChannel = null;
    		}
    		throw e;
    	}
    	
    }  
    
    public void close()
    {
    	try
    	{
    		//TODO: close peer fireOnDisconnected
    		
    		
    		_selector.close();
    		_acceptThread.join();
    		_acceptThread = null;
    	}
    	catch(Exception err)
    	{
    		System.out.println("服务端�??出异�?, "+err.getMessage());  
    	}
    }
    
    public boolean IsOpen()
    {
    	return _acceptThread !=null;
    }
    
    /** 
     * 采用轮询的方式监听selector上是否有�?要处理的事件，如果有，则进行处理 
     * @throws IOException 
     */  
    private void select() throws IOException {  
        //当注册的事件到达时，方法返回；否�?,该方法会�?直阻�?  
        _selector.select();  
        // 获得selector中�?�中的项的迭代器，�?�中的项为注册的事件  
        Iterator<SelectionKey> ite = this._selector.selectedKeys().iterator();  
        while (ite.hasNext()) {  
            SelectionKey key = ite.next();  
            //TODO: 删除已�?�的key,以防重复处理  
            ite.remove();  
            // 客户端请求连接事�?  
            if (key.isAcceptable()) {  
                ServerSocketChannel server = (ServerSocketChannel) key  
                        .channel();  
                // 获得和客户端连接的�?�道  
                SocketChannel channel = server.accept();  
                
                TcpPeer peer = new TcpPeer(this._selector, channel);
                
                fireOnConnected(peer);
                
            } else if (key.isReadable()) {  
            	// 获得了可读的事件  
            	TcpPeer peer = (TcpPeer)key.attachment();
            	peer.doRead();
            }  
        }  
    }  
    

	@Override
	public void run() {
		try
		{
	        while (true) {  
	        	// 轮询访问selector  
	        	select();
	        	
	        	//TODO:�?查连接�?�讯是否超时
	        }
		}
		catch(java.nio.channels.ClosedSelectorException c){
			//退出
		}
		catch(Exception e)
		{
			System.out.println("服务器监听线程异常, "+e.getClass() + ", "+e.getMessage());  
			e.printStackTrace();
		}
		finally
		{
			if(serverChannel!=null)
			{
				try {
					serverChannel.close();
					serverChannel = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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
	
	@SuppressWarnings("unused")
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

}

