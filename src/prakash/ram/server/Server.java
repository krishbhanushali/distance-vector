package prakash.ram.server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import prakash.ram.model.dv;

public class Server extends Thread{
	private int port = 0;
	public Server(int port)
    {
       this.port = port;
    }
	public void run() {
        try
        {
            dv.read = Selector.open();
            dv.write = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            while(true)
			{
				SocketChannel socketChannel=serverSocketChannel.accept();
				if(socketChannel != null)
				{
					socketChannel.configureBlocking(false);
					socketChannel.register(dv.read, SelectionKey.OP_READ);
					socketChannel.register(dv.write, SelectionKey.OP_WRITE);
					dv.openChannels.add(socketChannel);
					System.out.println("The connection to peer "+dv.parseChannelIp(socketChannel)+" is succesfully established");
				}
			}
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
	}
}
