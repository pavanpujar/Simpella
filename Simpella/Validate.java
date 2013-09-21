import java.net.*;
import java.net.InetAddress;
import java.io.*;
import java.util.*;

public class Validate
{
	public static FileWriter ping_writer,query_writer;
	public static BufferedWriter ping_out,query_out;
	public static FileReader ping_reader,query_reader;
	public static BufferedReader ping_in,query_in;
	
	public static String[] match_files = new String[100];
	public static long[] match_files_size = new long[100];
	public static int[] is_selected = new int[100];
	public static int match_file_count = 0;
	public static int number_of_queryhit_received = 0;
	
	public static QueryHitList[] list = new QueryHitList[250];		public static int queryhit_list_line_count1 = 0;
	public static QueryHitTable[] table = new QueryHitTable[250];	public static int queryhit_table_line_count = 0;
	
	
	public static void main(String[] args)
	{
	}
	
	public static void tryAutoConnect()
	{
		try
		{
			ping_reader = new FileReader(ListeningThread.peer_table);
			ping_in = new BufferedReader(ping_reader);
			
			while(UserInputThread.outgoing_connection_count < 1)
			{
				for(int i=0;i<ListeningThread.peer_entry_line_count;i++)
				{
					String s = ping_in.readLine();
					String[] param = s.split("\t");
					String possible_ip = param[0];
					int possible_port = Integer.parseInt(param[1]);
					
					int flag = 0;
					for(int j=0;j<UserInputThread.neighbor_count;j++)
					{
						if(UserInputThread.neighbor[j].IP.equals(possible_ip) && UserInputThread.neighbor[j].port == possible_port)
							flag = 1;
					}
					if(flag == 1);
						//System.out.println("already a neighbor...");
					else
					{
						SocketAddress sockaddr = new InetSocketAddress(possible_ip,possible_port);
						UserInputThread.outgoing_connection_socket[UserInputThread.outgoing_connection_count] = new Socket();
						UserInputThread.outgoing_connection_socket[UserInputThread.outgoing_connection_count].connect(sockaddr,10000);
						Thread t = new OutgoingConnectionHandlingThread(UserInputThread.outgoing_connection_socket[UserInputThread.outgoing_connection_count++],possible_ip,possible_port);
						t.start();	
						break;
					}
				}
			}
			ping_in.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void processForInfo(String message)
	{
		try
		{
			int pos1 = message.indexOf(':',1);
			int pos2 = message.indexOf(':',pos1+1);
			int pos3 = message.indexOf(':',pos2+1);
		
			String orig_ip = message.substring(pos1+1,pos2);
			String orig_port = message.substring(pos2+1,pos3);
			int no_of_files = Integer.parseInt(message.substring(pos3+1,pos3+33));
			long size_of_files = Long.parseLong(message.substring(pos3+33,message.length()));
			
			//check to see if this host is my peer
			ping_reader = new FileReader(ListeningThread.peer_table);
			ping_in = new BufferedReader(ping_reader);
					
			int flag = 0;
			for(int i=0;i<ListeningThread.peer_entry_line_count;i++)
			{
				String s = ping_in.readLine();
				String[] param = s.split("\t");
				if(param[0].equals(orig_ip) && param[1].equals(orig_port))
				{
					flag = 1;
					ListeningThread.total_number_of_files -= Long.parseLong(param[2]);
					ListeningThread.total_size_of_files -= Long.parseLong(param[3]);
				}
			}
			if(flag == 1)
			{
				
				ListeningThread.number_of_hosts++;
				ListeningThread.total_number_of_files += no_of_files;
				ListeningThread.total_size_of_files += size_of_files;
			}
			ping_in.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static int validateOpenParameters(String[] in,int count)
	{
		try
		{
			if(count != 2)
			{
				System.out.println("Command Usage : open <host name/IP>:<port> ");
				return -1;
			}
			else
			{
				/*		checking validitiy of IP address		*/
				String[] inp = in[1].split(":");
				if(inp[0].equals("127.0.0.1") || inp[0].equals("localhost"))
				{
					System.out.println("LoopBack Address not Allowed...");
					return -1;
				}
				if(!inp[0].substring(0,1).matches("[0-9]"))	//given as hostname
				{
					try
					{
						String ip = InetAddress.getByName(inp[0]).getHostAddress().toString();
					}
					catch(UnknownHostException e)
					{
						System.out.println("unknownhost exception in Validate");
						System.out.println("Unable to find host. Please try again");
						e.printStackTrace();
						return -1;
					}
				}
				else //IPaddress is given
				{
					StringTokenizer st1 = new StringTokenizer(inp[0],".");
					try
					{
						while (st1.hasMoreTokens())
						{
							if( st1.nextToken().equals("") || Integer.parseInt(st1.nextToken()) > 254 )
							{
								System.out.println("\nEnter a valid IP address (0-255)");
								return -1;
							}
						}
					}
					catch(NoSuchElementException e)
					{
						System.out.println("\nEnter a valid IP address (0-255)");
						return -1;
					}
				}
				
				//checking if connection to this host already exists
				
				/*		checking validity of Port number	*/
				int port = Integer.parseInt(inp[1]);
				if(port > 60000||port<1025)
				{
					System.out.println("Enter a valid port number (1025 - 60000)");
					return -1;
				}
				if(port == Simpella.incoming_connection_port)
				{
					System.out.println("Cannot connect to self. Mention a different port number");
					return -1;
				}
			}
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println("Command Usage : open <host name/IP>:<port> ");
			return -1;
		}
		catch(NumberFormatException e)
		{
			System.out.println("\nEnter a valid port number (1025 - 60000) ");
			return -1;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return 1;
		
	}
	
	public static void validateInitialPorts(int port1,int port2)
	{
	    if(port1>60000||port1<1025||port2>60000||port2<1025)
		{
			System.out.println("Enter a valid port number (1025 - 60000)");
			System.exit(1);
		}	
		for(int i=0;i<250;i++)
		{
			list[i] = new QueryHitList();		
			table[i] = new QueryHitTable();
		}
	}
	
	public static String getNumberOfFiles(String directory)
	{
		int count = 0;
		try
		{
			File folder = new File(directory);
			File[] listOfFiles = folder.listFiles();
			
			for (int i = 0; i < listOfFiles.length; i++) 
			{
				if (listOfFiles[i].isFile()) 
				{
					//System.out.println("" + (i+1) + ")  " + listOfFiles[i].getName());
					count++;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		String s = "" + count;
		int len = s.length();
		String s1 = "";
		for(int i=32;i>len;i--)
			s1 += "0";
		s1 += s;
		return s1;
	}
	
	public static String getSizeOfFiles(String directory)
	{
		int count = 0;
		long size = 0;
		try
		{
			File folder = new File(directory);
			File[] listOfFiles = folder.listFiles();
			
			for (int i = 0; i < listOfFiles.length; i++) 
			{
				if (listOfFiles[i].isFile()) 
				{
					//System.out.println("" + (i+1) + ")  " + listOfFiles[i].getName() + "\t" + listOfFiles[i].length());
					size += listOfFiles[i].length();
					if(size > Long.MAX_VALUE)
						break;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		long byte_size = size/1024;
		String s = "" + byte_size;
		int len = s.length();
		String s1 = "";
		for(int i=32;i>len;i--)
			s1 += "0";
		s1 += s;
		return s1;
	}
	
	public static int detectMessage(String message)
	{
		if(message.length() > 32)
		{
			if(message.substring(32,34).equals("00"))
				return 1;
			else if(message.substring(32,34).equals("01"))
				return 2;
			else if(message.substring(32,34).equals("80"))
				return 3;
			else if(message.substring(32,34).equals("81"))
				return 4;
			else;
		}
		else
		{
			if(message.length() == 0)
				return -1;
			else
				return 0;
		}
		return 0;
	}

	public static int checkPING(String ID, String orig_ip, int orig_port, String my_remote_ip, int my_remote_port)
	{
		try
		{
			
			ping_reader = new FileReader(ListeningThread.ping_table);
			ping_in = new BufferedReader(ping_reader);
			ping_writer = new FileWriter(ListeningThread.ping_table,true);
			ping_out = new BufferedWriter(ping_writer);
			
			if(ListeningThread.ping_entry_line_count > 159)
			{
				ListeningThread.ping_table.delete();
				ListeningThread.ping_table = new File(ListeningThread.filename);
				ListeningThread.ping_table.createNewFile();
				ListeningThread.ping_entry_line_count = 0;
			}
			
			for(int i=0;i<ListeningThread.ping_entry_line_count;i++)
			{
				String s = ping_in.readLine();
				String[] param = s.split("\t");
				if(param[0].equals(ID))
					return 1; //this PING message already exists
			}	
			
			ping_out.write(ID + "\t" + orig_ip + "\t" + orig_port + "\t" + my_remote_ip + "\t" + my_remote_port);
			ping_out.newLine();
			ping_out.close();
			ListeningThread.ping_entry_line_count++;
			
			return 0;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}
	
	public static int checkQUERY(String ID, String orig_ip, int orig_port, String my_remote_ip, int my_remote_port)
	{
		try
		{
			
			query_reader = new FileReader(ListeningThread.query_table);
			query_in = new BufferedReader(query_reader);
			query_writer = new FileWriter(ListeningThread.query_table,true);
			query_out = new BufferedWriter(query_writer);
			
			if(ListeningThread.query_entry_line_count > 159)
			{
				ListeningThread.query_table.delete();
				ListeningThread.query_table = new File(ListeningThread.filename2);
				ListeningThread.query_table.createNewFile();
				ListeningThread.query_entry_line_count = 0;
			}
			
			for(int i=0;i<ListeningThread.query_entry_line_count;i++)
			{
				String s = query_in.readLine();
				String[] param = s.split("\t");
				if(param[0].equals(ID))
					return 1; //this QUERY message already exists
			}	
			
		   query_out.write(ID + "\t" + orig_ip + "\t" + orig_port + "\t" + my_remote_ip + "\t" + my_remote_port);
			query_out.newLine();
			query_out.close();
			ListeningThread.query_entry_line_count++;
			
			return 0;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}
	
	public static void checkFiles(String[] search)
	{
		String filename,filename1;
		match_file_count = 0;
		try
		{
			for(int i=0;i<50;i++)
			{
				match_files[i] = "";
				match_files_size[i] = 0;
			}
			
			String dir = UserInputThread.shared_folder;
			File folder = new File(dir);
			File[] listOfFiles = folder.listFiles();
			
			if(search.length == 0)
			{
				for (int i=0; i < listOfFiles.length; i++)				
				{
					if (listOfFiles[i].isFile() && is_selected[match_file_count] == 0) 
					{
						filename = listOfFiles[i].getName();
						match_files[match_file_count] = filename;
						match_files_size[match_file_count] = listOfFiles[i].length();
						is_selected[match_file_count] = 1;
						match_file_count++;
					}
				}
			}
			else
			{
				for (int i=0; i < listOfFiles.length; i++)				
				{
					for(int j=0;j<search.length;j++)
					{
						if (listOfFiles[i].isFile() && is_selected[match_file_count] == 0)  
						{
							filename = listOfFiles[i].getName();
							int pos1 = filename.indexOf('.');
							filename1 = filename.substring(0,pos1);
							if(search[j].equalsIgnoreCase(filename1) || search[j].equalsIgnoreCase(filename))
							{
								match_files[match_file_count] = filename;
								match_files_size[match_file_count] = listOfFiles[i].length();
								is_selected[match_file_count] = 1;
								match_file_count++;
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static String changeMessage(String message)
	{
		String str1 = message.substring(0,34);
		int TTL = Integer.parseInt(message.substring(35,36)) - 1;
		int hop = Integer.parseInt(message.substring(37,38)) + 1;
		if(TTL == 0)
		{
			if(hop == 1)
				return  "gen all";
			else
				return "";
		}
		String str2 = message.substring(38,message.length());
		message =  str1 + "0" + TTL + "0" + hop + str2;
		return message;
	}
	
	public static void generatePING()
	{
		PrintWriter outpr,outpr1;
		String my_ip;
		int my_port;
		Socket sock;
		try
		{
			ListeningThread.peer_table.delete();
			ListeningThread.peer_table.createNewFile();
			ListeningThread.peer_entry_line_count = 0;
			
			for(int i=0,in=0,out=0;i<UserInputThread.neighbor_count;i++)
			{
				ListeningThread.number_of_bytes_sent += 23;
				ListeningThread.number_of_messages_sent++;
				System.out.println("generating ping...");
				
				if(UserInputThread.neighbor[i].in_out == 0) //outgoing
				{
					sock = UserInputThread.outgoing_connection_socket[out];
					my_ip = sock.getLocalAddress().getHostAddress();
					my_port = sock.getLocalPort();
					
					outpr = new PrintWriter(sock.getOutputStream(),true);
					outpr.println(	UserInputThread.current_ping_id + "00" + "07" + "00" + "0000"  + ":" +
									 my_ip + ":" + my_port
								  );
					UserInputThread.ping_count++;
					out++;
				}
				else//incoming connection
				{
					sock = ListeningThread.incoming_connection_socket[in];
					my_ip = sock.getLocalAddress().getHostAddress();
					my_port = sock.getLocalPort();
					
					outpr1 = new PrintWriter(sock.getOutputStream(),true);
					outpr1.println(	UserInputThread.current_ping_id + "00" + "07" + "00" + "0000"  + ":" +
									 my_ip + ":" + my_port
								  );
					UserInputThread.ping_count++;
					in++;
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void processPING(String message,String my_remote_ip,int my_remote_port)
	{
		String processed_ping,orig_ip,id;
		int pos1,pos2,orig_port;
		PrintWriter outpg;
		int is_my_ping = 0;
		
		try
		{
			id = message.substring(0,32);
			pos1 = message.indexOf(':',1);
			pos2 = message.indexOf(':',pos1+1);
			
			orig_ip = message.substring(pos1+1,pos2);
			orig_port = Integer.parseInt(message.substring(pos2+1,message.length()));
			
			int is_exist = checkPING(id,orig_ip,orig_port,my_remote_ip,my_remote_port); //check to see if this PING is a duplicate
			if(is_exist == 1);
				//System.out.println("duplicate PING received from " + my_remote_ip + "  " + my_remote_port);
			else
			{	
				//forward the ping to everyone else
				for(int i=0;i< UserInputThread.neighbor_count;i++)
				{	
					ListeningThread.number_of_bytes_sent += 23;
					ListeningThread.number_of_messages_sent++;
					if(UserInputThread.neighbor[i].IP.equals(my_remote_ip) && UserInputThread.neighbor[i].port == my_remote_port);
						//System.out.println("cant send it back the same way...");
					else
					{
						processed_ping = changeMessage(message);
						if(processed_ping.equals(""))
						{
							//System.out.println("TTL is zero. Dropping packet...");
							break;
						}
						try
						{	
							int connection_num = UserInputThread.neighbor[i].socketID;
							if(UserInputThread.neighbor[i].in_out == 0) //if outgoing connection
							{
								System.out.println("forwarding ping to " + UserInputThread.neighbor[i].IP + ":" + UserInputThread.neighbor[i].port + "...");
								outpg = new PrintWriter(UserInputThread.outgoing_connection_socket[connection_num-1].getOutputStream(),true); 
								outpg.println(processed_ping);
							}
							else //if incoming connection
							{
								System.out.println("forwarding ping to " + UserInputThread.neighbor[i].IP + ":" + UserInputThread.neighbor[i].port + "...");
								outpg = new PrintWriter(ListeningThread.incoming_connection_socket[connection_num].getOutputStream(),true); 
								outpg.println(processed_ping);
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				generatePONG(message,my_remote_ip,my_remote_port);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void generatePONG(String message,String my_remote_ip,int my_remote_port)
	{
		PrintWriter outpr;
		int flag = 0;
		String ping_id = message.substring(0,32);
		try
		{
			String directory = UserInputThread.shared_folder;
			String no_of_files = getNumberOfFiles(directory);
			String size_of_files = getSizeOfFiles(directory);
			
			for(int i=0;i< UserInputThread.neighbor_count;i++)
			{
				ListeningThread.number_of_bytes_sent += 37;
				ListeningThread.number_of_messages_sent++;
				if(UserInputThread.neighbor[i].IP.equals(my_remote_ip) && UserInputThread.neighbor[i].port == my_remote_port)
				{
					int connection_num = UserInputThread.neighbor[i].socketID;
					if(UserInputThread.neighbor[i].in_out == 0)//outgoing
					{
						System.out.println("generating pong to " + UserInputThread.neighbor[i].IP + ":" + UserInputThread.neighbor[i].port + "...");
						outpr = new PrintWriter(UserInputThread.outgoing_connection_socket[connection_num-1].getOutputStream(),true); 
						String ip = UserInputThread.outgoing_connection_socket[connection_num-1].getLocalAddress().toString();
						outpr.println(  ping_id + "01" + "07" + "00" + "0014" + ":" +
										ip.substring(1,ip.length()) + ":" +
										UserInputThread.outgoing_connection_socket[connection_num-1].getLocalPort() + ":" +
										no_of_files + size_of_files
									);
					}
					else //if incoming connection
					{
						System.out.println("generating pong to " + UserInputThread.neighbor[i].IP + ":" + UserInputThread.neighbor[i].port + "...");
						String ip = ListeningThread.incoming_connection_socket[connection_num].getLocalAddress().toString();
						outpr = new PrintWriter(ListeningThread.incoming_connection_socket[connection_num].getOutputStream(),true); 
						outpr.println(  ping_id + "01" + "07" + "00" + "0014" + ":" +
										ip.substring(1,ip.length()) + ":" +
										ListeningThread.incoming_connection_socket[connection_num].getLocalPort() + ":" +
										no_of_files + size_of_files
									);
					}
					flag = 1;
				}
			}
			if(flag == 0)
				System.out.println("not found");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void processPONG(String message,String my_remote_ip,int my_remote_port)
	{
		String tosend_ip = "";
		int tosend_port = 0;
		int my_ping = 0,is_exist = 0,flag = 0;
		PrintWriter outpr;
		try
		{
			String ping_id = message.substring(0,32);
			String processed_pong = changeMessage(message);
			if(processed_pong == "");
				//System.out.println("TTL is zero. Dropping packet...");
			else
			{
				if(UserInputThread.current_ping_id.equals(ping_id))
					my_ping = 1;
				
				if(my_ping == 1)
				{
					System.out.println("pong for my ping...");
					addInfoFromPong(message);
					processForInfo(message);
					tryAutoConnect();
				}
				else
				{
					ping_reader = new FileReader(ListeningThread.ping_table);
					ping_in = new BufferedReader(ping_reader);
					
					for(int i=0;i<ListeningThread.ping_entry_line_count;i++)
					{
						String s = ping_in.readLine();
						String[] param = s.split("\t");
						if(param[0].equals(ping_id))
						{
							is_exist = 1;
							tosend_ip = param[3];
							tosend_port = Integer.parseInt(param[4]);
						}
					}
				
					if(is_exist == 0)
						System.out.println("error.... unable to find PING origin");
					else
					{
						for(int i=0;i< UserInputThread.neighbor_count;i++)
						{	
							ListeningThread.number_of_bytes_sent += 37;
							ListeningThread.number_of_messages_sent++;
							if(UserInputThread.neighbor[i].IP.equals(my_remote_ip) && UserInputThread.neighbor[i].port == my_remote_port);
								//System.out.println("cant send it back the same way...");
							else
							{	
								System.out.println(UserInputThread.neighbor[i].IP + ":" + UserInputThread.neighbor[i].port + "\t" + tosend_ip + ":" + tosend_port);
								if(UserInputThread.neighbor[i].IP.equals(tosend_ip) && UserInputThread.neighbor[i].port == tosend_port)
								{
									int connection_num = UserInputThread.neighbor[i].socketID;
									if(UserInputThread.neighbor[i].in_out == 0)//outgoing
									{
										System.out.println("forwarding pong to " + UserInputThread.neighbor[i].IP + ":" + UserInputThread.neighbor[i].port);
										outpr = new PrintWriter(UserInputThread.outgoing_connection_socket[connection_num-1].getOutputStream(),true	); 
										String ip = UserInputThread.outgoing_connection_socket[connection_num-1].getLocalAddress().toString();
										outpr.println(processed_pong);
									}
									else //if incoming connection
									{
										System.out.println("forwarding pong to " + UserInputThread.neighbor[i].IP + ":" + UserInputThread.neighbor[i].port);
										String ip = ListeningThread.incoming_connection_socket[connection_num].getLocalAddress().toString();
										outpr = new PrintWriter(ListeningThread.incoming_connection_socket[connection_num].getOutputStream(),true); 
										outpr.println(processed_pong);
									}
									flag = 1;
								}
							}
						}
						if(flag == 0)
							System.out.println("error...destination not found...");
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void addInfoFromPong(String message)
	{
		int pos1,pos2,pos3;
		String id,orig_ip;
		int orig_port,no_of_files;
		long size_of_files;
		
		try
		{
			ping_writer = new FileWriter(ListeningThread.peer_table,true);
			ping_out = new BufferedWriter(ping_writer);
			
			pos1 = message.indexOf(':',1);
			pos2 = message.indexOf(':',pos1+1);
			pos3 = message.indexOf(':',pos2+1);
		
			id = message.substring(0,32);
			orig_ip = message.substring(pos1+1,pos2);
			orig_port = Integer.parseInt(message.substring(pos2+1,pos3));
			no_of_files = Integer.parseInt(message.substring(pos3+1,pos3+33));
			size_of_files = Long.parseLong(message.substring(pos3+33,message.length()));
			
			ping_out.write(orig_ip + "\t" + orig_port + "\t" + no_of_files + "\t" + size_of_files);
			ping_out.newLine();
			ListeningThread.peer_entry_line_count++;
			ping_out.close();
			ListeningThread.total_number_of_files += no_of_files;
			ListeningThread.total_size_of_files += size_of_files;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void generateQuery(String search_string)
	{
		String my_ip;
		int my_port;
		Socket sock;
		PrintWriter outpr,outpr1;
		UserInputThread.monitor_query[UserInputThread.query_monitor_count++] = search_string;
		try
		{
			queryhit_table_line_count = 0;
			
			String speed = "00000000";
			int temp = speed.length() + search_string.length();
			String length = "" + temp;
			int len = length.length();
			String s = "";
			for(int i=32;i>len;i--)
				s += "0";
			s += length;
			
			for(int i=0,in=0,out=0;i<UserInputThread.neighbor_count;i++)
			{
				ListeningThread.number_of_bytes_sent += 30;
				ListeningThread.number_of_messages_sent++;
				System.out.println("generating query...");
				if(UserInputThread.neighbor[i].in_out == 0) //outgoing
				{
					sock = UserInputThread.outgoing_connection_socket[out];
					my_ip = sock.getLocalAddress().getHostAddress();
					my_port = sock.getLocalPort();
					
					outpr = new PrintWriter(sock.getOutputStream(),true);
					if(search_string == "    ")
						outpr.println(	UserInputThread.current_query_id + "80" + "01" + "00" + 
										s + speed + search_string +
										":" + my_ip + ":" + my_port
									);
					else
						outpr.println(	UserInputThread.current_query_id + "80" + "07" + "00" + 
										s + speed + search_string +
										":" + my_ip + ":" + my_port
									);
					UserInputThread.ping_count++;
					out++;
				}
				else//incoming connection
				{
					sock = ListeningThread.incoming_connection_socket[in];
					my_ip = sock.getLocalAddress().getHostAddress();
					my_port = sock.getLocalPort();
					
					outpr1 = new PrintWriter(sock.getOutputStream(),true);
					if(search_string == "    ")
						outpr1.println(	UserInputThread.current_query_id + "80" + "01" + "00" + 
										s + speed + search_string +
										":" + my_ip + ":" + my_port
									);
					else
						outpr1.println(	UserInputThread.current_query_id + "80" + "07" + "00" + 
										s + speed + search_string +
										":" + my_ip + ":" + my_port
									);
					UserInputThread.ping_count++;
					in++;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void processQuery(String message,String my_remote_ip,int my_remote_port)
	{
		String processed_query,orig_ip,query_id;
		int pos1,pos2,orig_port;
		PrintWriter outpg;
		
		for(int i=0;i<100;i++)
			is_selected[i] = 0;
		
		try
		{
			query_id = message.substring(0,32);
			pos1 = message.indexOf(':',1);
			pos2 = message.indexOf(':',pos1+1);
			
			orig_ip = message.substring(pos1+1,pos2);
			orig_port = Integer.parseInt(message.substring(pos2+1,message.length()));
			
			String search_string = message.substring(78,pos1);
			UserInputThread.monitor_query[UserInputThread.query_monitor_count++] = search_string;
			
			int is_exist = checkQUERY(query_id,orig_ip,orig_port,my_remote_ip,my_remote_port); //check to see if this QUERY is a duplicate
			if(is_exist == 1);
				//System.out.println("duplicate QUERY received from " + my_remote_ip + "  " + my_remote_port);
			else
			{	
				ListeningThread.number_of_bytes_sent += 30;
				ListeningThread.number_of_messages_sent++;
				//forward the query to everyone else
				for(int i=0;i< UserInputThread.neighbor_count;i++)
				{
					if(UserInputThread.neighbor[i].IP.equals(my_remote_ip) && UserInputThread.neighbor[i].port == my_remote_port);
						//System.out.println("cant send it back the same way...");
					else
					{
						processed_query = changeMessage(message);
						if(processed_query.equals(""))
						{
							//System.out.println("TTL is zero. Dropping packet...");
							break;
						}
						if(processed_query.equals("gen all"))
							break;
							
						try
						{	
							int connection_num = UserInputThread.neighbor[i].socketID;
							if(UserInputThread.neighbor[i].in_out == 0) //if outgoing connection
							{
								System.out.println("forwarding query to " + UserInputThread.neighbor[i].IP + ":" + UserInputThread.neighbor[i].port + "...");
								outpg = new PrintWriter(UserInputThread.outgoing_connection_socket[connection_num-1].getOutputStream(),true); 
								outpg.println(processed_query);
							}
							else //if incoming connection
							{
								System.out.println("forwarding query to " + UserInputThread.neighbor[i].IP + ":" + UserInputThread.neighbor[i].port + "...");
								outpg = new PrintWriter(ListeningThread.incoming_connection_socket[connection_num].getOutputStream(),true); 
								outpg.println(processed_query);
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				generateQueryHit(message,my_remote_ip,my_remote_port);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void generateQueryHit(String message,String my_remote_ip,int my_remote_port)
	{
		PrintWriter outpr;
		int flag = 0;
		String my_ip;
		int my_port;
		
		int pos1 = message.indexOf(':',1);
		String search_string = message.substring(78,pos1);
		String query_id = message.substring(0,32);
		try
		{
			String[] search = search_string.split(" ");
			checkFiles(search);
			String[] temp = new String[match_file_count];
			if(match_file_count == 0)
				System.out.println("no match found. Not generating query hit...");
			else
			{
				String bandwidth = "10000000000000000000000000000000";
				long payload_length = 0;
				String result_set = "";
				
				for(int i=0;i< UserInputThread.neighbor_count;i++)
				{
					if(UserInputThread.neighbor[i].IP.equals(my_remote_ip) && UserInputThread.neighbor[i].port == my_remote_port)
					{
						ListeningThread.number_of_queryhit_sent++;
						ListeningThread.number_of_messages_sent++;
						for(int j=0;j<match_file_count;j++)
						{
							temp[j] = RandomGenerator.generateFileID();
							result_set += match_files[j] + ":" + temp[j] + ":" + match_files_size[j] + ":";
							payload_length += match_files[j].length() + 8;
						}
						
						int connection_num = UserInputThread.neighbor[i].socketID;
						ListeningThread.number_of_bytes_sent += 14 + payload_length;
						if(UserInputThread.neighbor[i].in_out == 0)//outgoing
						{
							my_ip = UserInputThread.outgoing_connection_socket[connection_num-1].getLocalAddress().toString();
							my_port = Simpella.downloading_connection_port;
							
							System.out.println("generating query hit to " + UserInputThread.neighbor[i].IP + ":" + UserInputThread.neighbor[i].port	 + "...");
							outpr = new PrintWriter(UserInputThread.outgoing_connection_socket[connection_num-1].getOutputStream(),true); 
							outpr.println(  query_id + "81" + "07" + "00" + payload_length + ":" + match_file_count + ":" + my_port + ":" +
											my_ip.substring(1,my_ip.length()) + ":" + bandwidth + ":" + result_set + 
											UserInputThread.my_ID
										);
						}
						else //if incoming connection
						{
							my_ip = ListeningThread.incoming_connection_socket[connection_num].getLocalAddress().toString();
							my_port = Simpella.downloading_connection_port;
							
							System.out.println("generating query hit to " + UserInputThread.neighbor[i].IP + ":" + UserInputThread.neighbor[i].port	 + "...");
							String ip = ListeningThread.incoming_connection_socket[connection_num].getLocalAddress().toString();
							outpr = new PrintWriter(ListeningThread.incoming_connection_socket[connection_num].getOutputStream(),true); 
							outpr.println(  query_id + "81" + "07" + "00" + payload_length + ":" + match_file_count + ":" + my_port + ":" +
											my_ip.substring(1,my_ip.length()) + ":" + bandwidth + ":" + result_set + 
											UserInputThread.my_ID
										);
						}
						flag = 1;
					}
				}
				if(flag == 0)
					System.out.println("not found...");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void processQueryHit(String message,String my_remote_ip,int my_remote_port)
	{
		String tosend_ip = "";
		int tosend_port = 0;
		int my_query = 0,is_exist = 0,flag = 0;
		PrintWriter outpr;
		try
		{
			String query_id = message.substring(0,32);
			String processed_queryhit = changeMessage(message);
			
			
			if(processed_queryhit == "");
				//System.out.println("TTL is zero. Dropping packet...");
			else
			{
				if(UserInputThread.current_query_id.equals(query_id))
					my_query = 1;
				
				if(my_query == 1)
				{
					System.out.println("queryhit being received ...");
					number_of_queryhit_received++;
					ListeningThread.number_of_bytes_received += message.length();
					addInfoFromQueryHit(message);
				}
				else
				{
					query_reader = new FileReader(ListeningThread.query_table);
					query_in = new BufferedReader(query_reader);
					
					for(int i=0;i<ListeningThread.query_entry_line_count;i++)
					{
						String s = query_in.readLine();
						String[] param = s.split("\t");
						if(param[0].equals(query_id))
						{
							is_exist = 1;
							tosend_ip = param[1];
							tosend_port = Integer.parseInt(param[2]);
						}
					}
				
					if(is_exist == 0)
						System.out.println("error.... unable to find QUERY origin");
					else
					{
						for(int i=0;i< UserInputThread.neighbor_count;i++)
						{
							ListeningThread.number_of_messages_sent++;
							ListeningThread.number_of_bytes_sent += message.length();
							ListeningThread.number_of_bytes_received += message.length();
							if(UserInputThread.neighbor[i].IP.equals(my_remote_ip) && UserInputThread.neighbor[i].port == my_remote_port);
								//System.out.println("cant send it back the same way...");
							else
							{	
								if(UserInputThread.neighbor[i].IP.equals(tosend_ip) && UserInputThread.neighbor[i].port == tosend_port)
								{
									int connection_num = UserInputThread.neighbor[i].socketID;
									if(UserInputThread.neighbor[i].in_out == 0)//outgoing
									{
										System.out.println("forwarding query hit to " + UserInputThread.neighbor[i].IP + ":" + UserInputThread.neighbor[i].port);
										outpr = new PrintWriter(UserInputThread.outgoing_connection_socket[connection_num-1].getOutputStream(),true	); 
										String ip = UserInputThread.outgoing_connection_socket[connection_num-1].getLocalAddress().toString();
										outpr.println(processed_queryhit);
									}
									else //if incoming connection
									{
										System.out.println("forwarding query hit to " + UserInputThread.neighbor[i].IP + ":" + UserInputThread.neighbor[i].port);
										String ip = ListeningThread.incoming_connection_socket[connection_num].getLocalAddress().toString();
										outpr = new PrintWriter(ListeningThread.incoming_connection_socket[connection_num].getOutputStream(),true); 
										outpr.println(processed_queryhit);
									}
									flag = 1;
								}
							}
						}
						if(flag == 0)
							System.out.println("error...destination not found...");
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void addInfoFromQueryHit(String message)
	{
		
		int pos1;
		String id,orig_ip;
		int orig_port,no_of_files;
		int[] size_of_files;
		int[]pos = new int[250];	int pos_count = 0;
		
		String[] filename = new String[50];
		String[] fileindex = new String[50];
		String[] filesize = new String[50];
		
		try
		{
			if(queryhit_list_line_count1 > 250)
				queryhit_list_line_count1 = 0;
			if(queryhit_table_line_count > 250)
				queryhit_table_line_count = 0;
				
			pos1 = message.indexOf(':',1);
			pos[0] = message.indexOf(':',pos1+1);
			pos_count++;
			id = message.substring(0,32);
			no_of_files = Integer.parseInt(message.substring(pos1+1,pos[0]));
			
			for(int i=1;i<(3*no_of_files+4);i++)
			{
				pos[i] = message.indexOf(':',pos[i-1]+1);
				pos_count++;
			}
			
			orig_port = Integer.parseInt(message.substring(pos[0]+1,pos[1]));
			orig_ip = message.substring(pos[1]+1,pos[2]);
			
			for(int i=3,j=0;i<pos_count-2;i+=3,j++)
			{
				filename[j] = message.substring(pos[i]+1,pos[i+1]);
				fileindex[j] = message.substring(pos[i+1]+1,pos[i+2]);
				filesize[j] = message.substring(pos[i+2]+1,pos[i+3]);
				list[queryhit_list_line_count1++].addEntry(orig_ip,orig_port,filename[j],fileindex[j],filesize[j]);
				table[queryhit_table_line_count++].addEntry(orig_ip,orig_port,filename[j],fileindex[j],filesize[j]);
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void displayQueryhitList(int code)
	{
		try
		{
			BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
			if(code == 1)
			{
				if(queryhit_table_line_count == 0)
					System.out.println("No query is generated...");
				for(int i=0;i<queryhit_table_line_count;i++)
					System.out.println((i+1) + "\t" + table[i].ip + "\t" + table[i].port + "\t" + table[i].filename + "\t" + table[i].filesize);
			}
			else if(code == 2)
			{
				if(queryhit_list_line_count1 == 0)
					System.out.println("No query is generated...");
				for(int i=0,len=0;i<queryhit_list_line_count1;i++,len++)
				{
					if(len < 10)
						System.out.println((i+1) + "\t" + list[i].ip + "\t" + list[i].port + "\t" + list[i].filename + "\t" + list[i].filesize);
					else
					{
						System.out.println("Press enter to view more...");
						d.readLine();
						i--;
						len = -1;
					}
				}
			}
		}
		catch(Exception e)
		{
		}
	}
	
	public static void removeEntry(int number)
	{
		try
		{
			for(int i=number-1;i<queryhit_list_line_count1;i++)
			{
				list[i].ip = list[i+1].ip;
				list[i].port = list[i+1].port;
				list[i].filename = list[i+1].filename;
				list[i].fileindex = list[i+1].fileindex;
				list[i].filesize = list[i+1].filesize;
			}
			queryhit_list_line_count1--;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}