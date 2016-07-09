package glt.NIO;

import java.nio.ByteBuffer;

public class MessageHeader {
	
	private int _id;
	private int _index;
	private byte _flag; //body encode 0-binary 1-xml 2-json
	private int _param;
	private int _bodySize;//unsigned short
	
	public MessageHeader(ByteBuffer buffer)	{
		buffer.flip();
		_id = buffer.getInt();
		_index = buffer.getInt();
		_flag = buffer.get();
		_param = buffer.getInt();
		_bodySize = buffer.getShort();//TODO:test 无符号数
	}
	
	public ByteBuffer getByteBuffer(){
		ByteBuffer buff =  ByteBuffer.allocate(getSize());
		buff.putInt(_id);
		buff.putInt(_index);
		buff.put(_flag);
		buff.putShort((short)_bodySize);
		
		return buff;
	}
	
	public static int getSize()	{
		return 15;
	}
	
	public int getID(){
		return _id;
	}
	
	public void setID(int id){
		_id = id;
	}
	
	public int getIndex(){
		return _index;
	}
	
	public void setIndex(int index){
		_index = index;
	}
	
	public byte getFlag(){
		return _flag;
	}
	
	public void setFlag(byte flag){
		_flag = flag;
	}
	
	public int getParam(){
		return _param;
	}
	
	public void setParam(int param){
		_param = param;
	}
	
	public int getBodySize(){
		return _bodySize;
	}
	
	public void setBodySize(int bodySize){
		_bodySize = bodySize;
	}
}

