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
	//����ȡ���ݴ����ʱ�򣬵�½�߳�������
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
				//��½�ɹ�����SUCCESS����½�û������˳���
				switch(parts[0])
				{
				//��½
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
						//��½ʧ�ܣ�����FAIL�����µ�½
						try {
							dos.writeUTF("FAIL");
							dos.flush();
						} catch (IOException e) {
							this.restartLogin();
						}
					}
					break;
				//ע��
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
						//��½ʧ�ܣ�����FAIL�����µ�½
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
