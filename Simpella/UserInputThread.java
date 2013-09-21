import java.lang.Thread;
import java.io.*;
import java.util.*;
import java.net.*;
import java.net.InetAddress;

public class UserInputThread extends Thread
{
	public String choice[] = new String[16];
	public static String shared_folder;											public static int shared_file_count;
	public static String download_folder;
	public static Socket outgoing_connection_socket[] = new Socket[3];			public static int outgoing_connection_count = 0;
	public static Socket[] download_socket = new Socket[3];						public static int download_count = 0;
	public static long total_shared_files_size = 0;								public static int total_no_of_shared_files = 0;
	public static int first_connection = 0;
		
	public static NeighborTable[] neighbor = new NeighborTable[6];				public static int neighbor_count = 0;
	static MessageFormat new_ping_message[] = new MessageFormat[200];			public static int ping_count = 0;
	static MessageFormat new_query_message[] = new MessageFormat[200];			public static int query_count = 0;
	public static String[] monitor_query = new String[100];						public static int query_monitor_count = 0;
	
	public static String current_ping_id = "";
	public static String current_query_id = "";
	public static String my_ID = "";
	
	PrintWriter outpr,outpr1;
	
	public UserInputThread()
	{
		try
		{
			BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
			my_ID = RandomGenerator.generateID();
			for(int i=0;i<6;i++)
				neighbor[i] = new NeighborTable();
			
			int flag = 0;
			while(flag == 0)
			{
				System.out.print("\nPlease choose the shared folder : ");
				String temp = d.readLine();
				if(!(temp.substring(0,1).equals('/')))
				{	
					try
					{
						temp = new File(".").getCanonicalPath() + "/" + temp;
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				if(new File(temp).exists())
				{
					System.out.println("Setting the shared Folder to : " + temp);
					shared_folder = temp;
					flag = 1;
				}
				else
					System.out.println("Enter a valid Path.. ");
			}
					
			flag = 0;
			while(flag == 0)
			{
				//setting the downloads folder
				System.out.print("\nPlease choose the download folder : ");
				String temp = d.readLine();
				if(!(temp.substring(0,1).equals('/')))
				{	
					try
					{
						temp = new File(".").getCanonicalPath() + "/" + temp;
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				if(new File(temp).exists())
				{
					System.out.println("Setting the download Folder to : " + temp);
					download_folder = temp;
					flag = 1;
				}
				else
					System.out.println("Enter a valid Path.. ");
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void info()
	{
		if(choice[1].equals("c"))
			NeighborTable.displayNeighbors();
		else if(choice[1].equals("d"))
		{
			System.out.println("\n\tDOWNLOAD STATS");
			System.out.println("\t--------------\n");
			if(download_count == 0)
				System.out.println("No download is currently in progress...");
			else
			{
				for(int i=0;i<download_count;i++)
					System.out.println((i+1) +"\t" + download_socket[i].getRemoteSocketAddress() + "\t" + getDownloadThread.getFilename());
			}
		}
		else if(choice[1].equals("h"))
		{
			System.out.println("\n\tHOST STATS");
			System.out.println("\t----------\n");
			System.out.println("Number of hosts   :" + ListeningThread.number_of_hosts);
			System.out.println("Number of files   :" + ListeningThread.total_number_of_files);
			System.out.println("Size of files     :" + ListeningThread.total_size_of_files + "\n");
		}			
		else if(choice[1].equals("n"))
		{
			System.out.println("\n\tNET STATS");
			System.out.println("\t---------\n");
			System.out.println("Number of messages received :" + ListeningThread.number_of_messages_received);
			System.out.println("Number of messages sent     :" + ListeningThread.number_of_messages_sent + "\n");
			System.out.println("Number of bytes received :" + ListeningThread.number_of_bytes_received);
			System.out.println("Number of bytes sent     :" + ListeningThread.number_of_bytes_sent);
			System.out.println();
		}
		else if(choice[1].equals("q"))
		{
			System.out.println("\n\tQUERY STATS");
			System.out.println("\t-----------\n");
			System.out.println("Number of Queries received :" + ListeningThread.number_of_queries_received);
			System.out.println("Number of Responses sent   :" + ListeningThread.number_of_queryhit_sent);
			System.out.println();
		}
		else if(choice[1].equals("s"))
		{
			System.out.println("\n\tSHARE STATS");
			System.out.println("\t---------\n");
			System.out.println("Total number of shared files : " + total_no_of_shared_files);
			if(total_shared_files_size > 102400)
			{
				total_shared_files_size = total_shared_files_size/1024;
				System.out.println("Size of shared files         : " +  total_shared_files_size + " KB\n");
			}
			else
				System.out.println("Size of shared files         : " +  total_shared_files_size + "\n");
		}
		else
			System.out.println("Command usage : \"info [cdhnqs]\"");
	}
	
	public void share(String[] choice,int c)
	{
		try
		{
			String temp = "";
			if(!choice[1].equals("-i"))
			{
				if(c > 2)
				{
					for(int i=1;i<c;i++)
						temp += choice[i] + " ";
				}
				else
					temp = choice[1];
				System.out.println("" + temp);
			
				if(!(choice[1].substring(0,1)).equals('/'))
				{	
					try
					{
						temp = new File(".").getCanonicalPath() + "/" + temp;
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				if(new File(temp).exists())
				{
					System.out.println("Changing the shared Folder to : " + temp);
					shared_folder = temp;
				}
				else
					System.out.println("Enter a valid Path.. ");
			}
			else if(choice[1].equals("-i"))
			{				
				if(choice[1].equals("-i"))
					System.out.println("\n    " + shared_folder);
			}
			else
				System.out.println("Command usage : \"Share <path>\" or \"Share -i\"");
		}
		catch(Exception e)
		{
			System.out.println("Enter a valid path...");
		}
	}
	
	public void scan()
	{
		try
		{
			File folder = new File(shared_folder);
			File[] listOfFiles = folder.listFiles();
			
			System.out.println("\n S.NO\tFILENAME\t\tSIZE");
			System.out.println(" ----\t--------\t\t----\n");
			for (int i = 0; i < listOfFiles.length; i++) 
			{
				if (listOfFiles[i].isFile()) 
					System.out.println("   " + (i+1) + ")\t" + listOfFiles[i].getName() + "\t\t\t" + listOfFiles[i].length());
			}
			System.out.println();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
			
	}
	
	public void open(int c)
	{
		if(outgoing_connection_count < 4)
		{
			int count = c;
			int is_valid = Validate.validateOpenParameters(choice,count);
			try
			{
				if(outgoing_connection_count == 3)
					System.out.println("\nThere are already three outgoing connections.. Please try again later. \n");
				else
				{
					if(is_valid == 1)
					{
						String spl[] = choice[1].split(":");
						String ip = InetAddress.getByName(spl[0]).getHostAddress().toString();
						int port = Integer.parseInt(spl[1]);
						
						int flag = 0;
						for(int i=0;i<neighbor_count;i++)
							if(neighbor[i].IP.equals(ip) && neighbor[i].port == port)
							{
								System.out.println("An outgoing connection to this host already exists");
								flag = 1;
							}
						if(flag == 0)
						{
							SocketAddress sockaddr = new InetSocketAddress(ip,port);
							outgoing_connection_socket[outgoing_connection_count] = new Socket();
							outgoing_connection_socket[outgoing_connection_count].connect(sockaddr,10000);
							Thread t = new OutgoingConnectionHandlingThread(outgoing_connection_socket[outgoing_connection_count++],ip,port);
							t.start();	
						}
					}
				}
			}
			catch(UnknownHostException e)
			{
				System.out.println("unknownhost exception");
				System.out.println("Unable to find host.. Please try again");
			}
			catch(SocketException e)
			{
				System.out.println("socket exception");
				System.out.println("Unable to find host.. Please try again");
			}	
			catch(SocketTimeoutException e)
			{
				System.out.println("sockettimeout exception");
				System.out.println("Unable to find host.. Please try again");
			}
			catch(NullPointerException e)
			{
				System.out.println("Null pointer exception");
				e.printStackTrace();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
			System.out.println("\nThere are already three outgoing connections.. Please try connecting later ");
	}
	
	public void update()
	{
		new_ping_message[ping_count] = new MessageFormat("00");
		current_ping_id = new_ping_message[ping_count].ID;
		ping_count++;
		Validate.generatePING();
	}
	
	public void find(String[] input,int no_of_words)
	{
		try
		{
			BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
			String s = "";
			for(int i=1;i<no_of_words;i++)
				s += input[i] + " ";
			int len = s.length();
			if(len > 512)
				System.out.println("Enter a smaller string...");
			if(neighbor_count == 0)
				System.out.println("You are not connected to anyone... Please connect and then try find...\n");
			else
			{
				if(s.equals(""))
					s = "    ";
				new_query_message[query_count] = new MessageFormat("80");
				current_query_id = new_query_message[query_count].ID;
				Validate.generateQuery(s);
				/*
				sleep(2000);
				System.out.print("\nWant to view response(s)... (y/n)?  ");
				String ch = d.readLine();
				if(ch.equals("y"))
					Validate.displayQueryhitList(1);
				else;
				*/
			}	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void list()
	{
		Validate.displayQueryhitList(2);
	}
	
	public void clear(int count)
	{
		try
		{
			if(count != 2)
				System.out.println("Command usage : \"clear <file.no>\"");
			else
			{
				int fileno = Integer.parseInt(choice[1]);
				if(fileno > Validate.queryhit_list_line_count1)
					System.out.println("Enter a valid number...");
				else
					Validate.removeEntry(fileno);
			}
		}
		catch(NumberFormatException e)
		{
			System.out.println("Enter a number for filenumber...");
			//e.printStackTrace();
		}
	}
	
	public void download(int count)
	{
		try
		{
			if(count != 2)
				System.out.println("Command usage : \"download <file.no>\"");
			else
			{
				int fileno = Integer.parseInt(choice[1]);
				if(Validate.queryhit_list_line_count1 == 0)
					System.out.println("Please initiate a query and then download files...");
				else
				{
					if(fileno > Validate.queryhit_list_line_count1 || fileno < 1)
						System.out.println("Enter a valid number...");
					else
					{
						String ip = Validate.list[fileno-1].ip;
						int port = Validate.list[fileno-1].port;
						SocketAddress sockaddr = new InetSocketAddress(ip,port);
						download_socket[download_count] = new Socket();
						download_socket[download_count] .connect(sockaddr,10000);
					
						Thread t = new getDownloadThread(download_socket[download_count++],fileno);
						t.start();
					}
				}
			}
		}
		catch(SocketTimeoutException e)
		{
			System.out.println("Unable to connect to the host.. Please try again...");
			e.printStackTrace();
		}
		catch(NumberFormatException e)
		{
			System.out.println("Enter a number for filenumber...");
			//e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	 
	public void monitor()
	{
		try
		{
			if(query_monitor_count == 0)
				System.out.println("No recent searches...");
			else
			{
				System.out.println("recently searched strings...\n\n");
				for(int i=0;i<query_monitor_count;i++)
					System.out.println((i+1) + "\t" + monitor_query[i]);
			}
		}
		catch(NumberFormatException e)
		{
			System.out.println("Enter a number for filenumber...");
			//e.printStackTrace();
		}
	}
	
	public void exit()
	{
		//check if there are any uploads/downloads in progress
		//abrupt disconnection should be notified to the other connection end
		System.exit(1);
	}
	
	public void run()
	{
		int input_words_count = 0;
		try
		{
			BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
			do
			{
				System.out.println();
				input_words_count = 0;
				
				choice = d.readLine().split(" ");
				input_words_count = choice.length;
				
				if(choice[0].equals("info"))
					info();
				else if(choice[0].equals("share"))
					share(choice,input_words_count);
				else if(choice[0].equals("scan"))
					scan();
				else if(choice[0].equals("open"))
					open(input_words_count);
				else if(choice[0].equals("update"))
					update();
				else if(choice[0].equals("find"))
					find(choice,input_words_count);
				else if(choice[0].equals("list"))
					list();
				else if(choice[0].equals("clear"))
					clear(input_words_count);
				else if(choice[0].equals("download"))
					download(input_words_count);
				else if(choice[0].equals("monitor"))
					monitor();
				else if(choice[0].equals("quit") || choice[0].equals("close") || choice[0].equals("exit"))
					exit();
				else if(choice[0].equals(""));
				else
					System.out.println(" Invalid Command. ");
			}while(true);
		}		
		catch(NullPointerException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}