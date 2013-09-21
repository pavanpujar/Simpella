import java.io.*;
import java.net.*;
import java.lang.Thread.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadThread extends Thread
{
	Socket current_socket;
	static String ip,filename,fileindex,filesize;
	static int port;
	
	public getDownloadThread(Socket s,int fileno)
	{
		current_socket = s;
		ip = Validate.table[fileno-1].ip;
		port = Validate.table[fileno-1].port;
		filename = Validate.table[fileno-1].filename;
		fileindex = Validate.table[fileno-1].fileindex;
		filesize = Validate.table[fileno-1].filesize;
	}
	
	public void run()
	{
		try
		{
			BufferedReader ins = new BufferedReader(new InputStreamReader(current_socket.getInputStream()));
			PrintWriter outp = new PrintWriter(current_socket.getOutputStream(),true); 
			
			outp.println(	"GET /get/" + fileindex + "/" + filename + "/HTTP/1.1\n" +
							"User-Agent:Simpella\n" +
							"Host:" + ip + ":" + port + "\n" +
							"Connection:" + "Keep-Alive\n" +
							"Range:bytes=" + "0-\n"
						 );
			System.out.println("Sending download request... to " + ip + ":" + port);
			String message = ins.readLine();
			System.out.println("Peer says : " + message + "\n");
			if(message.substring(9,12).equals("200"))
			{
				String toget_file = UserInputThread.download_folder + "/" + filename;
				FileOutputStream fos = new FileOutputStream(toget_file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				InputStream is = current_socket.getInputStream();
					
				byte[] aByte = new byte[1024];
				int bytesRead;				
				bytesRead = is.read(aByte, 0, aByte.length);
				do 
				{
					baos.write(aByte);
					bytesRead = is.read(aByte);
				}while (bytesRead != -1);
				bos.write(baos.toByteArray());
				System.out.println("download complete...");
				bos.close();
				fos.close();
				baos.close();
				is.close();
				UserInputThread.download_count--;
				
				String copy_file = UserInputThread.shared_folder + "/" + filename;
				String command = "cp " + toget_file + " " + copy_file;
				System.out.println("command = " + command);
				Process p = Runtime.getRuntime().exec(command);
			}
		}
		catch(Exception e)
		{
			//e.printStackTrace();
		}
	}
	
	public static String getFilename()
	{
		return filename;
	}
}