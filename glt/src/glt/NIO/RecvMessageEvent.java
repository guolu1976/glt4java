package glt.NIO;

import java.util.EventObject;

public class RecvMessageEvent extends EventObject{

	private static final long serialVersionUID = 1L;
	private MessageHeader _header;
	private byte[] _body;
	public RecvMessageEvent(Object source, MessageHeader header, byte[] body) {
		super(source);
		
		_header = header;
		_body = body;
	}
	
	public MessageHeader getHeader(){
		return _header;
	}
	
	public byte[] getBody(){
		return _body;
	}

}

