import java.io.*;
import java.net.*;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.lang.Thread.*;
import java.lang.String;

public class DownloadingThread extends Thread
{
	int downloading_port;
	
	DownloadingThread(int port)
	{
		downloading_port = port;
	}
	
	public void run()
	{
		try
		{
			ServerSocket downloading_server_socket = new ServerSocket(downloading_port);
			for(;;)
			{
				Socket download_socket = downloading_server_socket.accept();				
				Thread t = new DownloadRequestHandlingThread(download_socket);
				t.start();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
	