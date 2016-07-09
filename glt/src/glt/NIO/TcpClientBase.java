package glt.NIO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class TcpClientBase {
	
	SocketChannel _channel = null;
	
	public synchronized void write(MessageHeader header, ByteBuffer body) throws IOException
	{
		_channel.write(header.getByteBuffer());
		if((body!=null) && (body.position()>0))
			_channel.write(body);
	}
	
	public synchronized void write(ByteBuffer[] srcs, int offset, int length) throws IOException
	{
		_channel.write(srcs, offset, length);
	}
	
	ByteBuffer _msgHeader = ByteBuffer.allocate(MessageHeader.getSize());
	ByteBuffer _msgBody = ByteBuffer.allocate(1024);
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
	
	protected abstract void fireOnMessage(MessageHeader head, byte[] body);
}
