package glt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.EventObject;

import glt.NIO.RecvMessageEvent;
import glt.NIO.TcpClient;
import glt.NIO.TcpClientListener;
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
				String cmd = br.readLine();
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
	}
	
	public class PeerListener implements TcpPeerListener{

		@Override
		public void OnMessage(RecvMessageEvent e) {
			System.out.println("服务器接收消息");
		}
	}

	@Override
	public void OnException(Exception e) {
		e.printStackTrace();
	}
}
