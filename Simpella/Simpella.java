import java.io.*;

public class Simpella
{
	public static int incoming_connection_port = 6346;
	public static int downloading_connection_port = 5635;
	
	public static void main(String[] args)
	{
		try
		{
			BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
			if(args.length == 2)
			{
				incoming_connection_port = Integer.parseInt(args[0]);
				downloading_connection_port = Integer.parseInt(args[1]);
				Validate.validateInitialPorts(incoming_connection_port,downloading_connection_port);
			}
			else if(args.length == 0);
			else
			{
				System.out.println("Proper usage : java Simpella <port1> <port2> or java Simpella.. Please try again");
				System.exit(1);
			}
			
			//starting user thread
			Thread user_thread = new UserInputThread();
			user_thread.start();
			//starting listening thread
			Thread incoming_connection_thread = new ListeningThread(incoming_connection_port);
			incoming_connection_thread.start();
			//starting download listening thread
			Thread download_thread = new DownloadingThread(downloading_connection_port);
			download_thread.start();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}