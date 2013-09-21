import java.io.*;
import java.net.*;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.lang.Thread.*;
import java.lang.String;


class ListeningThread extends Thread
{
	public static Socket incoming_connection_socket[] = new Socket[3]; 			public static int incoming_connection_count = 0;
	int port;
	public static Socket infosocket;
	public static long number_of_queries_received = 0;
	public static long number_of_queryhit_sent = 0;
	public static long number_of_messages_received = 0; 
	public static long number_of_messages_sent = 0;
	public static long number_of_bytes_received = 0;
	public static long number_of_bytes_sent = 0;
	public static long total_number_of_files = 0;
	public static long total_size_of_files = 0;
	public static long number_of_hosts = 0;
	public static int no_of_cleared_entries = 0;
	
	
	/*  for queued connections */
	File queued_file;
	FileWriter queued_connection;
	BufferedWriter txt_out;
	String filename1;
	
	/* for ping entries  */
	public static File ping_table;
	public static int ping_entry_line_count = 0;
	public static String filename;
	
	/* for pong entries  */
	public static File peer_table;
	public static int peer_entry_line_count = 0;
	public static String filename6;
	
	/* for query entries  */
	public static File query_table;
	public static int query_entry_line_count = 0;
	public static String filename2;
	
	public ListeningThread(int incoming_connection_port)
	{
		try
		{
			port = incoming_connection_port;
			filename1 = "Files/queued_connection_" + port + ".txt";
			queued_file = new File(filename1);
			if(queued_file.exists())
				queued_file.delete();
			queued_file.createNewFile();
				
			filename = "Files/ping_" + port + ".txt";
			ping_table = new File(filename);
			if(ping_table.exists())
				ping_table.delete();
			ping_table.createNewFile();
			
			filename6 = "Files/peer_" + port + ".txt";
			peer_table = new File(filename6);
			if(peer_table.exists())
				peer_table.delete();
			peer_table.createNewFile();
			
			filename2 = "Files/query_" + port + ".txt";
			query_table = new File(filename2);
			if(query_table.exists())
				query_table.delete();
			query_table.createNewFile();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
		
	public void run()
	{
		try
		{ 
			infosocket =  new Socket("8.8.8.8",53);
			ServerSocket listening_socket = new ServerSocket(port);
			for(;;)
			{
				Socket connect_socket = listening_socket.accept();
				if(incoming_connection_count < 3)
				{
					incoming_connection_socket[incoming_connection_count] = connect_socket;
					incoming_connection_count++;
					Thread t = new IncomingConnectionHandlingThread(connect_socket);
					t.start();	
				}
					
				else
				{
					queued_connection = new FileWriter(queued_file,true);
					txt_out = new BufferedWriter(queued_connection);
					PrintWriter print_out = new PrintWriter(connect_socket.getOutputStream(),true); 
					print_out.println("SIMPELLA/0.6 503 Three connections already exist.. Please try again later.");
					System.out.println("Incoming connection request from " + infosocket.getLocalAddress().getHostAddress() 
										+ ":" + connect_socket.getPort() + " rejected.. ");
					
					String ip = infosocket.getLocalAddress().getHostAddress();
					int port = connect_socket.getPort();
					txt_out.write( ip + "\t" + port);
					txt_out.newLine();
					txt_out.close();
				}
			}	
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}