package cn.hekind.chatroom;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

public class SendThread implements Runnable {
	Socket s;
	Queue<String> msgQueue;
	DataOutputStream dos;
	String username;
	
	SendThread(Socket s, String username)
	{
		this.s=s;
		this.msgQueue = (Queue<String>) ChatServer.messageList.get(username);
		try {
			this.dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.username = username;
	}
	private void logout()
	{
		if(ChatServer.loginList.containsKey(username))
		{
			ChatServer.loginList.remove(username);
			System.out.println(username+" logout");
		
			Set<String> key = (Set<String>)ChatServer.loginList.keySet();
			if(key != null)
				for(Iterator it = key.iterator();it.hasNext();)
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
		System.out.println(username+" SendThread start");
		while(true)
		{
			String msg = (String)msgQueue.peek();
			if(msg != null)
				try {
					dos.writeUTF(msg);
					dos.flush();
					msgQueue.remove();
					System.out.println(username + " get message:" + msg);
				} catch (IOException e) {
					this.logout();
				}
		}
	}
}