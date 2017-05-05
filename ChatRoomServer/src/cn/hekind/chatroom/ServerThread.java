package cn.hekind.chatroom;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class ServerThread
implements Runnable
{
	int port;
	boolean running = false;
	ServerSocket server = null;
	ServerThread()
	{
		this.port = 8000;
	}
	ServerThread(int port)
	{
		this.port=port;
	}
	
	public void start_server()
	{
		this.running = true;
		try
		{
			server = new ServerSocket(port);
			System.out.println("Server start success. listening port:"+this.port);
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	public void stop_server()
	{
		this.running = false;
		try 
		{
			if(!server.isClosed())
				server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.start_server();
		while(this.running)
		{
			try
			{
				Socket s = server.accept();
				new Thread(new LoginThread(s)).start();
			}
			catch(IOException ex)
			{
				if(running)
					ex.printStackTrace();
			}
		}
	}

}
