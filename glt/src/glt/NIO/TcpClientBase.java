package glt.NIO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

public abstract class TcpClientBase {
	
	protected SocketChannel _channel = null;
	ByteBuffer _msgHeader = ByteBuffer.allocate(MessageHeader.getSize());
	ByteBuffer _msgBody = ByteBuffer.allocate(1024);
	
	public TcpClientBase(){
		_msgHeader.order(ByteOrder.LITTLE_ENDIAN);
		_msgBody.order(ByteOrder.LITTLE_ENDIAN);
	}
	public synchronized void write(MessageHeader header, ByteBuffer body) throws IOException
	{
		write(header.getByteBuffer());
		if((body!=null) && (body.limit()>0))
			write(body);
	}
	
	public synchronized void write(ByteBuffer[] srcs, int offset, int length) throws IOException
	{
		_channel.write(srcs, offset, length);
	}
	
	protected void write(ByteBuffer buffer) throws IOException{
		buffer.rewind();
		int count = 0;
		int size = buffer.limit();
		while(count!=size){
			int write = _channel.write(buffer);
			count += write;
		}
			
	}
	
	
	protected void readMessage() throws IOException
	{
		while(true)
		{
			int recv = 0;
			if(_msgHeader.hasRemaining())
			{
				recv = _channel.read(_msgHeader);
				if(recv==0)
					break;
				if(recv<0)
					throw new IOException();
				
				if(_msgHeader.position()<_msgHeader.limit())
					break;				
			}
			
			MessageHeader header = new MessageHeader(_msgHeader);
			
			if(header.getBodySize()>0)
			{
				if(_msgBody.capacity()<header.getBodySize())
				{
					_msgBody = ByteBuffer.allocate(header.getBodySize());
					_msgBody.order(ByteOrder.LITTLE_ENDIAN);
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
			else
			{
				_msgBody.limit(0);
			}

			try
			{
				_msgBody.flip();
				fireOnMessage(header, _msgBody);
			}
			finally
			{
				_msgHeader.clear();
				_msgBody.clear();
			}
		}
	}
	
	protected abstract void fireOnMessage(MessageHeader head, ByteBuffer body);
}
