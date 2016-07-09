package glt.NIO;

import java.nio.ByteBuffer;

public class MessageHeader {
	
	private int _id;
	private int _index;
	private byte _flag; //body编码方式 0-二进制 1-xml 2-json
	private int _param;
	private int _bodySize;
	
	public MessageHeader(ByteBuffer buffer)
	{
		buffer.flip();
		_id = buffer.getInt();
		_index = buffer.getInt();
		_flag = buffer.get();
		_param = buffer.getInt();
		_bodySize = buffer.getShort();//TODO:test 无符号数
	}
	
	public static int getSize()
	{
		return 15;
	}
	
	public int getID(){
		return _id;
	}
	
	public int getIndex(){
		return _index;
	}
	
	public byte getFlag(){
		return _flag;
	}
	
	public int getParam(){
		return _param;
	}
	
	public int getBodySize(){
		return _bodySize;
	}
}

