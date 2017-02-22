package com.aslanchen.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.aslanchen.socket.client.ClientListner;
import com.aslanchen.socket.client.SocketClient;
import com.aslanchen.socket.model.DataModel;
import com.aslanchen.socket.server.ServerListner;
import com.aslanchen.socket.server.SocketServer;

/**
 * @author Aslanchen
 *
 */
public class SocketManager {
	private static SocketManager instance;

	private SocketClient client;
	private SocketServer server;

	private SocketOutputThread outThread;
	private SocketInThread inThread;

	public static SocketManager Instance() {
		if (instance == null) {
			instance = new SocketManager();
		}
		return instance;
	}

	public void InitServer(int port) throws IOException {
		InitThread();

		server = new SocketServer();
		server.setListner(new ServerListner() {

			@Override
			public void OtherException(SocketChannel channel, IOException ex) {
				// TODO Auto-generated method stub

			}

			@Override
			public void DataReceived(SocketChannel channel, ByteBuffer buffer) {
				DataModel item = new DataModel(channel, buffer);
				inThread.enqueue(item);
				inThread.wakeup();
			}

			@Override
			public void ClientDisconnected(SocketChannel channel) {
				// TODO Auto-generated method stub

			}

			@Override
			public void ClientConnected(SocketChannel channel) {
				// TODO Auto-generated method stub

			}
		});
		server.OpenServer(port);
	}

	public void InitClient(String ip, int port) throws IOException {
		InitThread();

		client = new SocketClient();
		client.setListner(new ClientListner() {

			@Override
			public void ServerDisconnected() {
				// TODO Auto-generated method stub

			}

			@Override
			public void ServerConnected() {
				// TODO Auto-generated method stub

			}

			@Override
			public void OtherException(IOException ex) {
				// TODO Auto-generated method stub

			}

			@Override
			public void DataReceived(ByteBuffer buffer) {
				DataModel item = new DataModel(buffer);
				inThread.enqueue(item);
				inThread.wakeup();
			}
		});
		client.ConnectServer(ip, port);
	}

	public void InitThread() {
		outThread = new SocketOutputThread();
		outThread.start();

		inThread = new SocketInThread();
		inThread.start();
	}

	public void StartListen() {
		if (server != null) {
			server.StartListen();
		}

		if (client != null) {
			client.StartListen();
		}
	}

	public void AddMessage(ByteBuffer buffer) {
		if (client != null) {
			DataModel item = new DataModel(buffer);
			outThread.enqueue(item);
			outThread.wakeup();
		}
	}

	public void AddMessage(SocketChannel channel, ByteBuffer buffer) {
		if (client != null) {
			DataModel item = new DataModel(channel, buffer);
			outThread.enqueue(item);
			outThread.wakeup();
		}
	}

	public void SendMessage(DataModel item) {
		System.out.println("SendMessage");
		if (client != null) {
			try {
				client.SendMessage(item.getBuffer());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (server != null) {
			try {
				server.SendMessage(item.getChannel(), item.getBuffer());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void OnMsg(DataModel item) {
		System.out.println("OnMsg");
		// ByteBuffer buffer = item.getBuffer();
		// buffer.order(ByteOrder.LITTLE_ENDIAN);
		// int msgLength = buffer.getInt();
		// short msgType = buffer.getShort();
		// byte[] msg = new byte[msgLength - 2];
		// buffer.get(msg, 0, msgLength - 2);
		//
		// MsgCenter.Instance().OnMsg(msgType, msg);
	}

	/**
	 * 高低位互换
	 * 
	 * @param value
	 * @return
	 */
	private byte[] IntToBytes(long value) {
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (value >> (i * 8));
		}
		return b;
	}
}
