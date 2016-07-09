package glt.NIO;

import java.nio.ByteBuffer;
import java.util.EventObject;

public class RecvMessageEvent extends EventObject{

	private static final long serialVersionUID = 1L;
	private MessageHeader _header;
	private ByteBuffer _body;
	public RecvMessageEvent(Object source, MessageHeader header, ByteBuffer body) {
		super(source);
		
		_header = header;
		_body = body;
	}
	
	public MessageHeader getHeader(){
		return _header;
	}
	
	public ByteBuffer getBody(){
		return _body;
	}

}

