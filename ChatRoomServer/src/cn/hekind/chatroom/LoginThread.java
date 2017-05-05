package cn.hekind.chatroom;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LoginThread 
implements Runnable
{
	Socket s;
	DataInputStream dis;
	DataOutputStream dos;
	boolean running = true;
	LoginThread(Socket s) throws IOException
	{
		this.s = s;
		dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
		dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
	}
	//当读取数据错误的时候，登陆线程重启。
	void restartLogin()
	{
		if(s.isClosed())
		{
			running = false;
			return;
		}
		try {
			dis.close();
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
			dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		String msg = null;
		while(running)
		{
			try {
				msg = dis.readUTF();
			} catch (IOException e) {
				this.restartLogin();
			}
			if(msg != null)
			{
				//decode
				String[] parts = msg.split("#");
				//登陆成功返回SUCCESS，登陆用户表，并退出。
				switch(parts[0])
				{
				//登陆
				case "LOGIN":
					if(parts[2].equals((String)ChatServer.userList.get(parts[1])) && !ChatServer.loginList.containsKey(parts[1]))
					{
						String username = parts[1];


						StringBuilder loginMessage = new StringBuilder("");
						Set<String> key = (Set<String>)ChatServer.loginList.keySet();
						
						loginMessage.append("USERLIST");
						if(key != null)
							for(Iterator it = key.iterator();it.hasNext();)
							{
								String str = (String) it.next();
								loginMessage.append("#"+str);
							}
						try {
							dos.writeUTF("SUCCESS");
							dos.writeUTF(new String(loginMessage));
							dos.flush();
							new Thread(new RecvThread(s,username)).start();
							new Thread(new SendThread(s,username)).start();
						} catch (IOException e1) {
							this.restartLogin();
						}

						ChatServer.loginList.put(username, this.s);
						System.out.println(username+" login successfully");
						return;
					}
					else
					{
						//登陆失败，返回FAIL，重新登陆
						try {
							dos.writeUTF("FAIL");
							dos.flush();
						} catch (IOException e) {
							this.restartLogin();
						}
					}
					break;
				//注册
				case "REGISTER":
					if(!ChatServer.userList.containsKey(parts[1]))
					{
						ChatServer.userList.put(parts[1], parts[2]);
						ChatServer.messageList.put(parts[1], new ConcurrentLinkedQueue<String>());
						try {
							dos.writeUTF("SUCCESS");
							dos.flush();
						} catch (IOException e1) {
							this.restartLogin();
						}
					}
					else
					{
						//登陆失败，返回FAIL，重新登陆
						try {
							dos.writeUTF("FAIL");
							dos.flush();
						} catch (IOException e) {
							this.restartLogin();
						}
					}
				}
			}
		}
	}

}
