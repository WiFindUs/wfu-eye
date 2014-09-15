package wifindus;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public abstract class Serializer
{
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T read(File path)
	{
		T t = null;
		try
		{
			FileInputStream fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			
			t = (T)in.readObject();
			in.close();
			fileIn.close();
		}
		catch (IOException i)
		{
			System.out.println("IOException thrown reading '"+path+"': " + i.getMessage());
			return null;
		}
		catch (ClassNotFoundException c)
		{
			System.out.println("ClassNotFoundException thrown reading '"+path+"': " + c.getMessage());
			return null;
		}
		return t;
	}
	
	public static <T extends Serializable> boolean write(File path, T object)
	{
		try
		{
			FileOutputStream fileOut = new FileOutputStream(path);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(object);
			out.close();
			fileOut.close();
		}
		catch (IOException i)
		{
			System.out.println("IOException thrown reading '"+path+"': " + i.getMessage());
			return false;
		}
		
		return true;
	}
}
