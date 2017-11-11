package prakash.ram.model;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.Map.Entry;

import prakash.ram.client.Client;
import prakash.ram.server.Server;

public class dv {
	
	
	static int time;
	public static AdjacencyList al;
	public static List<SocketChannel> openChannels = new ArrayList<>();
	public static Selector read;
	public static Selector write;
	static String myIP = "";
	static int myID = Integer.MIN_VALUE;
	public static Node myNode = null;
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
				myNode = al.getNode(myID);
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
				display();
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
	
	public static void update(int serverId1, int serverId2, int cost) throws IOException {
		Node from = al.getNode(serverId1);
		Node to = al.getNode(serverId2);
		al.changeDistance(from, to, cost);
		Collection<Edge> edges = al.adjacencyList.get(myNode);
		Message message = new Message(myNode.getId(),myNode.getIpAddress(),myNode.getPort(),edges);
		sendMessage(to,message);
		System.out.println("Message sent to "+to.getIpAddress());
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
	
	public static void step() throws IOException{
		List<Node> neighbors = al.getNeighbors(myNode);
		Collection<Edge> edges = al.adjacencyList.get(myNode);
		Message message = new Message(myNode.getId(),myNode.getIpAddress(),myNode.getPort(),edges);
		for(Node eachNeighbor:neighbors) {
			sendMessage(eachNeighbor,message); //sending message to each neighbor
			System.out.println("Message sent to "+eachNeighbor.getIpAddress()+"!");
		}
		System.out.println("Step SUCCESS");
	}
	
	public static void sendMessage(Node eachNeigbor, Message message) throws IOException{
		int semaphore = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(message);
			out.flush();
			byte[] bytes = bos.toByteArray();
			semaphore = write.select();
			if(semaphore>0) {
				Set<SelectionKey> keys = write.selectedKeys();
				Iterator<SelectionKey> selectedKeysIterator = keys.iterator();
				ByteBuffer buffer = ByteBuffer.allocate(Integer.MAX_VALUE);
				buffer.put(bytes);
				buffer.flip();
				while(selectedKeysIterator.hasNext())
				{
					SelectionKey selectionKey=selectedKeysIterator.next();
					if(parseChannelIp((SocketChannel)selectionKey.channel()).equals(al.getNode(eachNeigbor.getId()).getIpAddress()))
					{
						SocketChannel socketChannel=(SocketChannel)selectionKey.channel();
						socketChannel.write(buffer);
					}
					selectedKeysIterator.remove();
				}
			}
		}catch(Exception e) {
			System.out.println("Sending failed because "+e.getMessage());
		}finally {
			bos.close();
		}
	}
	
	public static String parseChannelIp(SocketChannel channel){//parse the ip form the SocketChannel.getRemoteAddress();
		String ip = null;
		String rawIp =null;  
		try {
			rawIp = channel.getRemoteAddress().toString().split(":")[0];
			ip = rawIp.substring(1, rawIp.length());
		} catch (IOException e) {
			System.out.println("can't convert channel to ip");
		}
		return ip;
	}
	
	public static Integer parseChannelPort(SocketChannel channel){//parse the ip form the SocketChannel.getRemoteAddress();
		String port =null;  
		try {
			port = channel.getRemoteAddress().toString().split(":")[1];
		} catch (IOException e) {
			System.out.println("can't convert channel to ip");
		}
		return Integer.parseInt(port);
	}
	
	public static void display() {
		System.out.println("Next Hop ID\tNext Hop IP\t\tCost");
		Iterator entries = al.adjacencyList.entrySet().iterator();
		while(entries.hasNext()) {
			Entry thisEntry = (Entry)entries.next();
			Object key = (Node)thisEntry.getKey();
			Node n = (Node)key;
			if(n.getId()==myID) {
				Collection<Edge> links = al.adjacencyList.get(n);
				for(Edge edge:links) {
					System.out.println(edge.getTo().getId()+"\t\t"+edge.getTo().getIpAddress()+"\t\t"+edge.getCost());
				}
			}
		}
	}
	
}


