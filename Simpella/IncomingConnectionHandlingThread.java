import java.net.*;
import java.net.InetAddress;
import java.io.*;
import java.util.*;
import java.lang.Thread.*;

public class IncomingConnectionHandlingThread extends Thread
{
	Socket connection_socket;
	NeighborTable incoming_connection = new NeighborTable();
	String my_remote_ip;
	int my_remote_port;
	Socket infosocket;
	
	public IncomingConnectionHandlingThread(Socket s)
	{	
		connection_socket = s;
		String temp = connection_socket.getInetAddress().toString();
		my_remote_ip = temp.substring(1,temp.length());
		my_remote_port = connection_socket.getPort();
		UserInputThread.neighbor[UserInputThread.neighbor_count].addNeighbor(my_remote_ip,my_remote_port,1,0,ListeningThread.incoming_connection_count-1);
		UserInputThread.neighbor_count++;
	}
	public void run()
	{
		try
		{
			BufferedReader in_stream = new BufferedReader(new InputStreamReader(connection_socket.getInputStream()));
			
			PrintWriter outp = new PrintWriter(connection_socket.getOutputStream(),true); 
			System.out.println("\nConnection request from " + connection_socket.getRemoteSocketAddress().toString() + " accepted");
			outp.println("SIMPELLA/0.6 200 OK\r");
			String message = in_stream.readLine().toString();
			while(message != "\0")
			{
				int result = Validate.detectMessage(message);
				if(result == 1)
				{
					ListeningThread.number_of_messages_received++;
					ListeningThread.number_of_bytes_received += 23;
					System.out.println("\nping msg detected via incoming...");
					Validate.processPING(message,my_remote_ip,my_remote_port);
				}
				else if(result == 2)
				{
					ListeningThread.number_of_messages_received++;
					ListeningThread.number_of_bytes_received += 37;
					System.out.println("pong detected via incoming...");
					Validate.processPONG(message,my_remote_ip,my_remote_port);
				}
				else if(result == 3)
				{
					ListeningThread.number_of_messages_received++;
					ListeningThread.number_of_queries_received++;
					ListeningThread.number_of_bytes_received += 30;
					System.out.println("\nquery detected via incoming...");
					Validate.processQuery(message,my_remote_ip,my_remote_port);
				}
				else if(result == 4)
				{
					ListeningThread.number_of_messages_received++;
					System.out.println("query hit detected via incoming...");
					Validate.processQueryHit(message,my_remote_ip,my_remote_port);
				}
				else if(result == 0)
					System.out.println("Peer says : " + message);
				else;
			
				if(connection_socket.isConnected())
					message = in_stream.readLine().toString();
			}
		}
		catch(SocketException e)
		{
			System.out.println("\nConnection severed by remote host\n");
			int num = NeighborTable.findNeighbor(my_remote_ip,my_remote_port);
			if(num != -1)
			{
				NeighborTable.removeNeighbor(num);
				Validate.tryAutoConnect();
			}
			else
				System.out.println("Error in Neighbor table.. Messages might not be sent or received correctly. Please abort and run again ");
		}
		catch(IOException e)			{	e.printStackTrace();	}
		catch(NullPointerException e)	{	e.printStackTrace();	}
		catch(Exception e)				{	e.printStackTrace();	}
	}
}

