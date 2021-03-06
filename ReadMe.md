# 聊天室备注

## 新增：
* 注册功能。
* 查询所有用户功能。
* 查询登陆用户功能。
* 定时检查功能，如果客户端5分钟内没有和服务器通信，则强制将客户端注销。
## 功能简介：
* 基于文本的数据传输， 采用Eclipse Neon编写，可以直接导入运行。
* 支持用户登陆。
* 向好友发送信息。
* 注销登陆。
## 文本数据封装格式：
>报文类型#发送者用户名#内容

* 注册格式：
	REGISTER#username#password  
	注册成功则返回SUCCESS，失败则返回FAIL
* 登陆格式：
	LOGIN#username#password  
	登陆失败返回FAIL,登陆成功返回SUCCESS，和登陆用户表  
	USERLIST#username#username#username（用户表不包括自己）
* 注销格式：
	LOGOUT#username  
	当有用户退出时，服务器会把他注销的消息发送给其他的在线用户。当用户注销时，如果要重新登陆，应该重新建立一个与服务器的TCP连接。
* 聊天格式：
	TALKTO#username_dst,username_src#context  
	服务器会把聊天数据不经过处理，转发给username_dst，如果没登陆，则会等登陆时发送。目的用户如果收到，可以获取发送用户。
* 查询登陆用户：
	USERLIST  
	服务器会会查询登陆用户（包括自己），发送给客户端  
	USERLIST#username#username#username
* 查询所有用户：
	ALLLIST  
	服务端会查询所有用户，发送给服务端。  
	ALLLIST#username#username#username
***
## 服务器端介绍
1. 服务类ChatServer  
	构造服务器的数据如用户登陆表，用户消息缓冲队列表等等。启动服务器后台线程，监听8000端口。   
	负责与用户交互。

		start---启动服务器
		list---显示登陆用户
		shutdown---关闭服务器
		message---读取消息表
		exit---退出

2. 服务器后台线程ServerThread  
	监听8000端口，当接收到连接时，启动登陆线程。
3. 登陆线程LoginThread  
	接受用户登陆消息，如果登陆成功则启动发送线程和收取线程，并向用户发送登陆成功的消息，退出。否则，返回登陆失败的消息，继续接受用户的输入。
4. 发送线程SendThread  
	从消息缓冲队列取出消息，发送给用户
5. 收取线程RecvThread  
	收取消息，解析消息，查询用户列表，将消息添加至目标用户队列后。 启动定时器，5分钟到达，则注销用户，如果收到用户的消息，重置定时器。

***
## 客户端介绍
客户端会启动一个收取消息线程，并把收到的消息，打印出来。主线程里面会一直从命令行读取数据，并向服务端发送。  

		exit---退出