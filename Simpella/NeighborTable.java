import java.io.*;

public class NeighborTable
{
	String IP;
	int port;
	int in_out; 	// 0 means outgoing
	int up_down;	// 0 means downloading
	String file;	//name of the file that is being up/downloaded
	int socketID;
	
	NeighborTable()
	{
		IP = "";
		port = 0;
		in_out = 0; 	// 0 means outgoing
		up_down = 0;	// 0 means downloading
		file = "";	//name of the file that is being up/downloaded
		socketID = 0;
		
	}
	
	public void addNeighbor(String ip1,int port1,int in_out1,int up_down1,int socket)
	{
		IP = ip1;
		port = port1;
		in_out = in_out1;
		up_down = up_down1;
		file = "";
		socketID = socket;
	}
	
	public static int findNeighbor(String ip1,int port1)
	{
		int result = -1;
		for(int i=0;i<UserInputThread.neighbor_count;i++)
		{
			if( ip1.equals(UserInputThread.neighbor[i].IP) && (port1 == UserInputThread.neighbor[i].port) )
				result = i;
		}
		return result;
	}
	
	public static void removeNeighbor(int number)
	{
		for(int i=number;i<5;i++)
			UserInputThread.neighbor[i] = UserInputThread.neighbor[i+1];
		UserInputThread.neighbor_count--;
		ListeningThread.incoming_connection_count--;
		System.out.println("Neighbor removed from neighbor table");
	}
	
	public static void displayNeighbors()
	{
		if(UserInputThread.neighbor_count == 0)
			System.out.println("There are no connections");
		else
		{
			String str;
			System.out.println("\n\t\tCONNECTION STAT");
			System.out.println("\t\t---------------\n");
			System.out.println("\nS.NO\tIP\t\tPORT\tIN/OUT");
			for(int i=0;i<UserInputThread.neighbor_count;i++)
			{
				if(UserInputThread.neighbor[i].in_out == 0)
					str = "outgoing";
				else
					str = "incoming";
				System.out.println("" + (i+1) + "\t" + UserInputThread.neighbor[i].IP + "\t" + UserInputThread.neighbor[i].port + "\t" +
										str);
			}
			System.out.println("\n");
		}
	}
}