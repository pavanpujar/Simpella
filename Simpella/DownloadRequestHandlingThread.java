import java.io.*;
import java.net.*;
import java.lang.Thread.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadRequestHandlingThread extends Thread
{
	public static Socket download_socket;
	
	DownloadRequestHandlingThread(Socket s)
	{
		download_socket = s;
	}
	
	public void run()
	{
		int pos1,pos2,pos3,pos4;
		int flag = 0;
		try
		{
			BufferedReader in_stream = new BufferedReader(new InputStreamReader(download_socket.getInputStream()));			
			PrintWriter outp = new PrintWriter(download_socket.getOutputStream(),true); 
			
			System.out.println("\nDownload request from " + download_socket.getRemoteSocketAddress().toString());
			String message = in_stream.readLine().toString();
			
			pos1 = message.indexOf('/',1);
			pos2 = message.indexOf('/',pos1+1);
			pos3 = message.indexOf('/',pos2+1);
			pos4 = message.indexOf('/',pos3+1);
			
			String fileindex = message.substring(pos2+1,pos3);
			String filename = message.substring(pos3+1,pos4);
			System.out.println("Filename : " + filename + "\n");
			
			//search for this file in shared_folder
			File folder = new File(UserInputThread.shared_folder);
			File[] listOfFiles = folder.listFiles();
			String tosend_filename = "";
			long tosend_filesize = 0;
			
			for (int i = 0; i < listOfFiles.length; i++) 
			{
				if (listOfFiles[i].isFile())
					if(filename.equalsIgnoreCase(listOfFiles[i].getName()))
					{
						tosend_filename = listOfFiles[i].getName();
						tosend_filesize = listOfFiles[i].length();
						flag = 1;
					}
			}
			if(flag == 0)
				outp.println("HTTP/1.1 503 File not found\r\n");
			else
			{
				UserInputThread.total_no_of_shared_files++;
				UserInputThread.total_shared_files_size += tosend_filesize;
				
				String filepath = UserInputThread.shared_folder + "/" + tosend_filename;
				String filetype = Files.probeContentType(Paths.get(filepath));
				outp.println(	"HTTP/1.1 200 OK\r\n" + 
								"Simpella0.6\n" + 
								filetype + "\n" +
								tosend_filesize
							);
				
				File tosend_file = new File(filepath);
				byte[] mybytearray = new byte[1024];
				
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tosend_file));
				BufferedOutputStream bos = new BufferedOutputStream(download_socket.getOutputStream());
				while(tosend_filesize > 0)
				{
    			    bis.read(mybytearray, 0, mybytearray.length);
                    bos.write(mybytearray, 0, mybytearray.length);
                    tosend_filesize = tosend_filesize - mybytearray.length;
    			}
				bis.close();
				bos.close();
			}
		}
		catch(FileNotFoundException e)
		{
			System.out.println("File not found...");
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}		
}

