package glt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.EventObject;

import glt.NIO.MessageHeader;
import glt.NIO.RecvMessageEvent;
import glt.NIO.TcpClient;
import glt.NIO.TcpClientListener;
import glt.NIO.TcpPeer;
import glt.NIO.TcpPeerEvent;
import glt.NIO.TcpPeerListener;
import glt.NIO.TcpServer;
import glt.NIO.TcpServerListener;

public class glt implements TcpClientListener, TcpServerListener{

	private TcpServer server ;
	private TcpClient client;
	public static void main(String args[]) {
		
		glt c = new glt();		
		while(true){
			c.doCommand();
		}
	}
	
	public glt()
	{
		server = new TcpServer();
		server.addListener(this);
		client = new TcpClient();
		client.addListener(this);
		try {
			//client.Open(new InetSocketAddress("123.206.101.55", 9000));
			client.Open(new InetSocketAddress("localhost", 9000));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void doCommand(){
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(true)
		{
			showHelp();
			try {
				String line = br.readLine();
				String[] args = line.split(" ");
				if(args.length<=0)
					continue;
				String cmd = args[0];
				if(cmd.trim().equals("start")){
					if(!server.IsOpen()){
						try {
							System.out.println("服务器启动...");
							server.open(9000);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
				} else if(cmd.trim().equals("stop")){
					if(server.IsOpen()){
						System.out.println("服务器停止...");
						server.close();
					}
				}
				else if(cmd.trim().equals("client")){
					MessageHeader head = new MessageHeader();
					head.setID(0);
					head.setIndex(1);
					head.setBodySize(10);
					ByteBuffer  body = ByteBuffer.allocate(10);
					for(int i=0;i<body.capacity();i++){
						body.put((byte)i);
					}
						
					client.write(head, body);
				}
				else{
					System.out.println("指令无效");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void showHelp(){
		System.out.println("服务控制指令：");
		System.out.println("  start - 重启服务");
		System.out.println("  stop - 停止服务");
	}

	PeerListener _peerListener = new PeerListener();
	@Override
	public void OnConnected(TcpPeerEvent e) {
		System.out.println("服务器接入客户端, "+e.getTcpPeer().getName());
		e.getTcpPeer().addListener(_peerListener);
	}

	@Override
	public void OnDisconnected(TcpPeerEvent e) {
		System.out.println("服务器断开客户端"+e.getTcpPeer().getName());
		e.getTcpPeer().removeListener(_peerListener);
	}

	@Override
	public void OnConnected(EventObject e) {
		System.out.println("客户端连接服务器");
	}

	@Override
	public void OnDisconnected(EventObject e) {
		System.out.println("客户端连接断开");
		
	}

	@Override
	public void OnMessage(RecvMessageEvent e) {
		System.out.println("客户端接收消息");
		System.out.println("ID : "+String.valueOf(e.getHeader().getID()));
		System.out.println("Index : "+String.valueOf(e.getHeader().getIndex()));
		System.out.println("Flag : "+String.valueOf(e.getHeader().getFlag()));
		System.out.println("Param : "+String.valueOf(e.getHeader().getParam()));
		System.out.println("BodySize : "+String.valueOf(e.getHeader().getBodySize()));
		System.out.println("Body : "+String.valueOf(e.getBody()));
		
	}
	
	public class PeerListener implements TcpPeerListener{

		@Override
		public void OnMessage(RecvMessageEvent e) {
			TcpPeer peer = (TcpPeer)e.getSource();

			System.out.println("服务器接收消息, "+peer.getName());
			try {
				peer.write(e.getHeader(), e.getBody());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}		
		}
	}

	@Override
	public void OnException(Exception e) {
		e.printStackTrace();
	}
}
