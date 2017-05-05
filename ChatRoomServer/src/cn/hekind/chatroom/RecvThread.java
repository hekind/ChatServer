package cn.hekind.chatroom;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class RecvThread implements Runnable {
	Socket s;
	DataInputStream dis;
	String username;
	boolean running = true;
	
	RecvThread(Socket s, String username) throws IOException
	{
		this.s = s;
		this.dis = new DataInputStream(s.getInputStream());
		this.username = username;
	}
	//×¢Ïú
	private void logout()
	{
		if(ChatServer.loginList.containsKey(username))
		{
			ChatServer.loginList.remove(username);
			System.out.println(username+" logout");
		
			Set<String> key = (Set<String>)ChatServer.loginList.keySet();
			if(key != null)
				for(Iterator<String> it = key.iterator();it.hasNext();)
				{
					ChatServer.messageList.get((String)it.next()).add("LOGOUT#"+username);
				}
		}
		try {
			if(!s.isClosed())
				s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		System.out.println(username+" RecvThread start");
		//handle message
		class LogoutTimer extends TimerTask
		{
			@Override
			public void run() {
				logout();
				this.cancel();
			}
		}
		Timer time = new Timer();
		time.schedule(new LogoutTimer(), 5*60*1000);
		while(running)
		{
			String msg = null;
			try {
				msg = dis.readUTF();
				System.out.println(username + " put:"+msg);
			} catch (IOException e) {
				this.logout();
			}
			if(msg != null)
			{
				//decode
				String[] parts = msg.split("#");
				switch(parts[0])
				{
				case "TALKTO":
					String desUser = parts[1].split(",")[0];
					if(ChatServer.messageList.containsKey(desUser)&&username.equals(parts[1].split(",")[1]))
						ChatServer.messageList.get(desUser).add(msg);
					break;
				case "USERLIST":
					StringBuilder usermsg = new StringBuilder("USERLIST");
					Set<String> userkey = (Set<String>)ChatServer.loginList.keySet();
					if(userkey != null)
						for(Iterator it = userkey.iterator();it.hasNext();)
						{
							String str = (String) it.next();
							usermsg.append("#"+str);
						}
					ChatServer.messageList.get(username).add(new String(usermsg));
					break;
				case "ALLLIST":
					StringBuilder allmsg = new StringBuilder("ALLLIST");
					Set<String> allkey = (Set<String>)ChatServer.userList.keySet();
					if(allkey != null)
						for(Iterator it = allkey.iterator();it.hasNext();)
						{
							String str = (String) it.next();
							allmsg.append("#"+str);
						}
					ChatServer.messageList.get(username).add(new String(allmsg));
					break;
				case "LOGOUT":
					this.logout();
					return;
				}
				//set timer
				time.cancel();
				time = new Timer();
				time.schedule(new LogoutTimer(),5*60*1000);
			}
		}
	}

}
