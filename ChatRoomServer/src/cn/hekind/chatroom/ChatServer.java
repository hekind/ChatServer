package cn.hekind.chatroom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatServer {
	static int port = 8000;
	static HashMap<String, Socket> loginList;
	static HashMap<String, String> userList;
	static HashMap<String, Queue> messageList;
	static boolean running = true;
	
	static ServerThread st = null;
	
	private static void initServer()
	{
		userList = new HashMap<String, String>();
		loginList = new HashMap<String, Socket>();
		messageList = new HashMap<String, Queue>();
		//装入用户信息
		userList.put("ZhangSan", "ZhangSan");
		messageList.put("ZhangSan", new ConcurrentLinkedQueue<String>());
		userList.put("LiSi", "LiSi");
		messageList.put("LiSi", new ConcurrentLinkedQueue<String>());
	}
	
	public static void start()
	{
		initServer();
		running = true;
		st = new ServerThread(port);
		Thread backThread = new Thread(st);
		backThread.start();
	}
	public static void stop()
	{
		st.stop_server();
		System.out.println("Server shutdown");
		Set<String> key = (Set<String>)loginList.keySet();
		if(key != null)
			for(Iterator it = key.iterator();it.hasNext();)
			{
				String str = (String) it.next();
				if(!loginList.get(str).isClosed())
				try {
						loginList.get(str).close();
				} catch (Exception e) {}
				loginList.remove(str);
			}
	}
	public static void loginList()
	{
		Set<String> key = (Set<String>)loginList.keySet();
		System.out.print("Login User:");
		if(key != null)
			for(Iterator it = key.iterator();it.hasNext();)
			{
				String str = (String) it.next();
				System.out.print(str+" ");
			}
		System.out.println();
	}
	public static void message()
	{
		Set<String> key = (Set<String>)messageList.keySet();
		if(key != null)
			for(Iterator it = key.iterator();it.hasNext();)
			{
				String name = (String)it.next();
				System.out.print(name+":");
				Queue myq = messageList.get(name);
				if(myq!=null)
					for(Iterator i = myq.iterator();i.hasNext();)
						System.out.print(" "+(String)i.next());
				System.out.println();
			}
	}
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String input = null;
		System.out.println("start---启动服务器");
		System.out.println("list---显示登陆用户");
		System.out.println("shutdown---关闭服务器");
		System.out.println("message---读取消息表");
		System.out.println("exit---退出");
		while(running)
		{
			input = sc.nextLine();
			if(input.equals("start"))
				start();
			else if(input.equals("shutdown"))
				stop();
			else if("message".equals(input))
				message();
			else if(input.equals("exit"))
			{
				stop();
				running = false;
				return;
			}
			else if(input.equals("list"))
				loginList();
		}
	}

}
