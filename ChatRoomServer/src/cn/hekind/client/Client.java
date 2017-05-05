package cn.hekind.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import cn.hekind.chatroom.ChatServer;

public class Client 
extends Thread
{
	static DataInputStream dis = null;
	static Socket s;
	public static void main(String[] args){
		try{
			s = new Socket("127.0.0.1",8000);
			dis = new DataInputStream(s.getInputStream());
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			Scanner sc = new Scanner(System.in);
			String input = sc.nextLine();
			new Client().start();
			while(!"exit".equals(input))
			{
				dos.writeUTF(input);
				dos.flush();
				input = sc.nextLine();
			}
		}
		catch(IOException e)
		{
			return;
		}
	}
	
	@Override
	public void run() {
			while(true)
			{
				try {
					System.out.println(dis.readUTF());
				} catch (IOException e) {
					return;
				}
			}
		}

}
