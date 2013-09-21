import java.lang.Thread;
import java.io.*;
import java.util.*;
import java.net.*;
import java.net.InetAddress;

class OutgoingConnectionHandlingThread extends Thread
{
	Socket outgoing_socket;
	String my_remote_ip;
	int my_remote_port;
	
	PrintWriter outpr,outpr1;
	BufferedReader inp;
	int result,flag = 0;
	String message;

	public OutgoingConnectionHandlingThread(Socket s,String ip,int port)
	{
		outgoing_socket = s;
		my_remote_ip = ip;
		my_remote_port = port;
		ListeningThread.number_of_hosts++;
	}
	
	public void run()
	{
		try
		{
			System.out.println("local port = " + outgoing_socket.getLocalPort());
			outpr = new PrintWriter(outgoing_socket.getOutputStream(),true); 
			outpr.println("SIMPELLA CONNECT/0.6\r");
			System.out.println("Connection request message " + "\n\t\t" + "SIMPELLA CONNECT/0.6\r" + "\n\tsent to " + my_remote_ip + ":" + my_remote_port + "\n");
			inp = new BufferedReader(new InputStreamReader(outgoing_socket.getInputStream()));
			message = inp.readLine().toString();
			
			while(message != "\0")
			{
				
				int result = Validate.detectMessage(message);
				if(result == 1)
				{
					ListeningThread.number_of_messages_received++;
					ListeningThread.number_of_bytes_received += 23;
					System.out.println("\nping msg detected via outgoing...");
					Validate.processPING(message,my_remote_ip,my_remote_port);
				}
				else if(result == 2)
				{
					ListeningThread.number_of_messages_received++;
					ListeningThread.number_of_bytes_received += 37;
					System.out.println("pong detected via outgoing...");
					Validate.processPONG(message,my_remote_ip,my_remote_port);
				}
				else if(result == 3)
				{
					ListeningThread.number_of_messages_received++;
					ListeningThread.number_of_queries_received++;
					ListeningThread.number_of_bytes_received += 30;
					System.out.println("\nquery detected via outgoing...");
					Validate.processQuery(message,my_remote_ip,my_remote_port);
				}
				else if(result == 4)
				{
					ListeningThread.number_of_messages_received++;
					System.out.println("query hit detected via outgoing...");
					Validate.processQueryHit(message,my_remote_ip,my_remote_port);
				}
				else if(result == 0)
				{
					if(message.substring(13,16).equals("200"))
					{
						System.out.println("Peer says : " + message);
						UserInputThread.neighbor[UserInputThread.neighbor_count].addNeighbor
												(my_remote_ip,my_remote_port,0,0,UserInputThread.outgoing_connection_count);
						UserInputThread.neighbor_count++;
						UserInputThread.outgoing_connection_count++;
						if(UserInputThread.neighbor_count == 1 && UserInputThread.first_connection == 0)
						{
							UserInputThread.first_connection = 1;								
							UserInputThread.new_ping_message[UserInputThread.ping_count] = new MessageFormat("00");
							UserInputThread.current_ping_id = UserInputThread.new_ping_message[UserInputThread.ping_count].ID;
							UserInputThread.ping_count++;
							Validate.generatePING();		
						}
					}
					else
						System.out.println("Peer says : " + message);
				}
				else;
			
				if(outgoing_socket.isConnected())
					message = inp.readLine().toString();
			}
		}
		catch(SocketException e)
		{
			System.out.println("Peer got disconnected... Removing it from the negihbor table");
			int num = NeighborTable.findNeighbor(my_remote_ip,my_remote_port);
			if(num != -1)
			{
				NeighborTable.removeNeighbor(num);
				Validate.tryAutoConnect();
			}
			else
				System.out.println("Error in Neighbor table.. Messages might not be sent or received correctly. Please abort and run again ");
		}
		catch(NullPointerException e)
		{
			System.out.println("The remote host got disconnected... ");
			//e.printStackTrace();
		}
		catch(Exception e)
		{
			//e.printStackTrace();
		}
	}	
}
	
	