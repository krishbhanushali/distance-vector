package prakash.ram.model;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import prakash.ram.client.Client;
import prakash.ram.server.Server;

public class dv {
	
	
	static int time;
	static AdjacencyList al;
	static List<SocketChannel> openChannels = new ArrayList<>();
	static Selector read;
	static Selector write;
	static String myIP = "";
	static int myID = Integer.MIN_VALUE;
	public static void main(String[] args) throws IOException{
		
		read = Selector.open();
		write = Selector.open();
		Server server = new Server(2000);
		server.start();
		System.out.println("Server started running...");
		Client client = new Client();
		client.start();
		System.out.println("Client started running...");
		myIP = getMyLanIP();
		
		Timer timer = new Timer();
		Scanner in = new Scanner(System.in);
		boolean run = true;
		while(run) {
			String line = in.nextLine();
			String[] arguments = line.split(" ");
			String command = arguments[0];
			switch(command) {
			case "server": //server <topology-file-name> -i <routing-update-interval>
				String filename = arguments[1];
				time = Integer.parseInt(arguments[3]);
				readTopology(filename);
				break;
			case "update": //update <server-id1> <server-id2> <link Cost>
				update(Integer.parseInt(arguments[1]),Integer.parseInt(arguments[2]),Integer.parseInt(arguments[3]));
				break;
			case "step":
				step();
				break;
			case "packets":
				break;
			case "display":
				break;
			case "disable":
				break;
			case "crash":
				run = false;
				System.out.println("Bubyee!! Thank you.");
				timer.cancel();
				System.exit(1);
				break;
				default:
					System.out.println("Wrong command! Please check again.");
			}
		}
		in.close();
	}
	
	private static String getMyLanIP() {
		// TODO Auto-generated method stub
		try {
		    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		    while (interfaces.hasMoreElements()) {
		        NetworkInterface iface = interfaces.nextElement();
		        if (iface.isLoopback() || !iface.isUp() || iface.isVirtual() || iface.isPointToPoint())
		            continue;

		        Enumeration<InetAddress> addresses = iface.getInetAddresses();
		        while(addresses.hasMoreElements()) {
		            InetAddress addr = addresses.nextElement();

		            final String ip = addr.getHostAddress();
		            if(Inet4Address.class == addr.getClass()) return ip;
		        }
		    }
		} catch (SocketException e) {
		    throw new RuntimeException(e);
		}
		return null;
	}

	public static void readTopology(String filename) {
		File file = new File("src/"+filename);
        al = new AdjacencyList(file,myIP);
        System.out.println("Reading topology done.");
	}
	
	public static void update(int serverId1, int serverId2, int cost) {
		Node from = al.getNode(serverId1);
		Node to = al.getNode(serverId2);
		al.changeDistance(from, to, cost);
		System.out.println("Update success");
	}
	
	public static void connect(String ip, int port, int id) {
		myID = id;
		System.out.println("Connecting to ip:- "+ip);
		try {
			if(!ip.equals(myIP)) {
				SocketChannel socketChannel = SocketChannel.open();
				socketChannel.connect(new InetSocketAddress(ip,port));
				socketChannel.configureBlocking(false);
				socketChannel.register(read, SelectionKey.OP_READ);
				socketChannel.register(write,SelectionKey.OP_WRITE);
				openChannels.add(socketChannel);
				System.out.println(".......");
				System.out.println("Connected to "+ip);
			}
			else {
				System.out.println("You cannot connect to yourself!!!");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void step() {
		
	}
}


