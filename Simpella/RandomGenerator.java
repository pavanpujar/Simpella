import java.io.*;
import java.util.Random;

public class RandomGenerator
{
	public static void main(String[] args)
	{
		generateID();
	}
	
	public static String generateID()
	{
		String s = "";
		int j;
		for(int i=0;i<8;i++)
		{
			j = (int)(Math.random()*100);
			if(j < 16)
			{
				i--;
				continue;
			}
			s += String.format("%x",(int)j);
		}
		
		s += "ff";
			
		for(int i=0;i<6;i++)
		{
			j = (int)(Math.random()*100);
			if(j < 16)
			{
				i--;
				continue;
			}
			s += String.format("%x",(int)j);
		}
		
		s += "00";
		
		return s;
	}
	
	public static String generateFileID()
	{
		String s = "";
		int j;
		for(int i=0;i<4;i++)
		{
			j = (int)(Math.random()*100);
			if(j < 16)
			{
				i--;
				continue;
			}
			s += String.format("%x",(int)j);
		}
		return s;
	}
}