package prakash.ram.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Selector;

import prakash.ram.model.dv;

public class Server extends Thread{
	private Socket socket = null;
	private ServerSocket server = null;
	private DataInputStream in = null;
	private int port = 0;
	public Server(int port)
    {
       this.port = port;
    }
	public void run() {
		 // starts server and waits for a connection
        try
        {
            dv.read = Selector.open();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
	}
}
