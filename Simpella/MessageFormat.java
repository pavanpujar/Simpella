public class MessageFormat
{
	String ID;
	byte message_type ;
	String TTL;	//7 initially
	String hop;	//0 initially
	byte[] payload_length;
	byte[] payload; //max 4KB
	
	public MessageFormat(String type)
	{
		ID = RandomGenerator.generateID();
		TTL = "07";
		hop = "00";
		if(type == "00")	//ping message
		{
			message_type = 0x00;
			payload_length = new byte[] {0,0,0,0};
			payload = null;
		}
		if(type == "80")	//query message
		{
			message_type = 0x8;
			payload_length = new byte[] {0,0,0,0};
			payload = null;
		}
	}
	public static void main()
	{
	}
}