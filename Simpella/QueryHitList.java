public class QueryHitList
{
	public String ip;
	public int port;
	public String filename;
	public String fileindex;
	public String filesize;
	
	public QueryHitList()
	{
		ip = "";
		port = 0;
		filename = "";
		fileindex = "";
		filesize = "";
	}
	
	public void addEntry(String ip1,int port1,String f1,String f2,String f3)
	{
		ip = ip1;
		port = port1;
		filename = f1;
		fileindex = f2;
		filesize = f3;
	}
}