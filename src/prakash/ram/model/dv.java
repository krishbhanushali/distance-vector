package prakash.ram.model;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.fasterxml.jackson.databind.ObjectMapper;

import prakash.ram.client.Client;
import prakash.ram.server.Server;
import prakash.ram.utilies.TableBuilder;

public class dv {
	
	
	static int time;
	
	public static List<SocketChannel> openChannels = new ArrayList<>();
	public static Selector read;
	public static Selector write;
	static String myIP = "";
	static int myID = Integer.MIN_VALUE+2;
	public static Node myNode = null;
	//public static Map<Node,Integer> routingTable = new HashMap<Node,Integer>();
	public static List<Node> nodes = new ArrayList<Node>();
	public static List<String> routingTableMessage = new ArrayList<String>();
	public static Map<Node,Integer> routingTable = new HashMap<Node,Integer>();
	public static Set<Node> neighbors = new HashSet<Node>();
	public static int numberOfPacketsReceived = 0;
	public static Map<Node,Node> nextHop = new HashMap<Node,Node>();
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
		boolean serverCommandInput = false;
		while(run) {
			System.out.println("\n");
			System.out.println("*********Distance Vector Routing Protocol**********");
			System.out.println("Help Menu");
			System.out.println("--> Commands you can use");
			System.out.println("1. server <topology-file> -i <time-interval-in-seconds>");
			System.out.println("2. update <server-id1> <server-id2> <new-cost>");
			System.out.println("3. step");
			System.out.println("4. display");
			System.out.println("5. disable <server-id>");
			System.out.println("6. crash");
			String line = in.nextLine();
			String[] arguments = line.split(" ");
			String command = arguments[0];
			switch(command) {
			case "server": //server <topology-file-name> -i <routing-update-interval>
				if(arguments.length!=4){
					System.out.println("Incorrect command. Please try again.");
					break;
				}
				try{
				if(Integer.parseInt(arguments[3])<15){
					System.out.println("Please input routing update interval above 15 seconds.");
				}
				}catch(NumberFormatException nfe){
					System.out.println("Please input an integer for routing update interval.");
					break;
				}
				if((arguments[1]=="" || arguments[2]=="" || !arguments[2].equals("-i") || arguments[3]=="")){
					System.out.println("Incorrect command. Please try again.");
					break;
				}
				else{
					serverCommandInput = true;
					String filename = arguments[1];
					time = Integer.parseInt(arguments[3]);
					readTopology(filename);
					timer.scheduleAtFixedRate(new TimerTask(){
						@Override
						public void run() {
						try {
							step();
						} catch (IOException e) {
							e.printStackTrace();
						}
						}
						}, time*1000, time*1000);
				}
				break;
			case "update": //update <server-id1> <server-id2> <link Cost>
				if(serverCommandInput)
					update(Integer.parseInt(arguments[1]),Integer.parseInt(arguments[2]),Integer.parseInt(arguments[3]));
				else
					System.out.println("Please input the server command. Thank you.");
				break;
			case "step":
				if(serverCommandInput)
					step();
				else
					System.out.println("Please input the server command. Thank you.");
				break;
			case "packets":
				if(serverCommandInput)
					System.out.println("Number of packets received yet = "+numberOfPacketsReceived);
				else
					System.out.println("Please input the server command. Thank you.");
				break;
			case "display":
				if(serverCommandInput)
					display();
				else
					System.out.println("Please input the server command. Thank you.");
				break;
			case "disable":
				if(serverCommandInput){
					int id = Integer.parseInt(arguments[1]);
					Node disableServer = getNodeById(id);
					disable(disableServer);
				}
				else
					System.out.println("Please input the server command. Thank you.");
				break;
			case "crash":
				if(serverCommandInput){
					run = false;
					for(Node eachNeighbor:neighbors){
						disable(eachNeighbor);
					}
					System.out.println("Bubyee!! Thank you.");
					timer.cancel();
					System.exit(1);
				}
				else
					System.out.println("Please input the server command. Thank you.");
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
		try {
			Scanner scanner = new Scanner(file);
			int numberOfServers = scanner.nextInt();
			int numberOfNeighbors = scanner.nextInt();
			scanner.nextLine();
			for(int i = 0 ; i < numberOfServers;i++) {
				String line = scanner.nextLine();
				String[] parts = line.split(" ");
				Node node = new Node(Integer.parseInt(parts[0]),parts[1],Integer.parseInt(parts[2]));
				nodes.add(node);
				int cost = Integer.MAX_VALUE-2;
				if(parts[1].equals(myIP)) {
					myID = Integer.parseInt(parts[0]);
					myNode = node;
					cost = 0;
					nextHop.put(node, myNode);
				}
				else{
					nextHop.put(node, null);
				}
				routingTable.put(node,cost);
				connect(parts[1], Integer.parseInt(parts[2]),myID);
			}
			for(int i = 0 ; i < numberOfNeighbors;i++) {
				String line = scanner.nextLine();
				String[] parts = line.split(" ");
				int fromID = Integer.parseInt(parts[0]);int toID = Integer.parseInt(parts[1]); int cost = Integer.parseInt(parts[2]);
				if(fromID == myID){
					Node to = getNodeById(toID);
					routingTable.put(to, cost);
					neighbors.add(to);
					nextHop.put(to, to);
				}
				if(toID == myID){
					Node from = getNodeById(fromID);
					routingTable.put(from, cost);
					neighbors.add(from);
					nextHop.put(from, from);
				}
			}
			System.out.println("Reading topology done.");
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println(file.getAbsolutePath()+" not found.");
		}
        
	}
	
	
	public static Node getNodeById(int id){
		for(Node node:nodes) {
			if(node.getId() == id) {
				return node;
			}
		}
		return null;
	}
	public static void update(int serverId1, int serverId2, int cost) throws IOException {
		if(serverId1 == myID){
			Node to = getNodeById(serverId2);
			if(isNeighbor(to)){
				routingTable.put(to, cost);
				Message message = new Message(myNode.getId(),myNode.getIpAddress(),myNode.getPort(),"update");
				message.setRoutingTable(makeMessage());
				sendMessage(to,message);
				System.out.println("Message sent to "+to.getIpAddress());
				System.out.println("Update success");
			}
			else{
				System.out.println("You can only update cost to your own neigbour!");
			}
		}
		if(serverId2 == myID){
			Node to = getNodeById(serverId1);
			if(isNeighbor(to)){
				routingTable.put(to, cost);
				Message message = new Message(myNode.getId(),myNode.getIpAddress(),myNode.getPort(),"update");
				message.setRoutingTable(makeMessage());
				sendMessage(to,message);
				System.out.println("Message sent to "+to.getIpAddress());
				System.out.println("Update success");
			}
			else{
				System.out.println("You can only update cost to your own neigbour!");
			}
		}
	}
	
	public static boolean isNeighbor(Node server){
		if(neighbors.contains(server))
			return true;
		return false;
	}
	public static List<String> makeMessage(){
		List<String> message = new ArrayList<String>();
		for (Map.Entry<Node, Integer> entry : routingTable.entrySet()) {
		    Node key = entry.getKey();
		    Integer value = entry.getValue();
		    message.add(key.getId()+"#"+value);
		}
		return message;
	}
	public static void connect(String ip, int port, int id) {
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
	
	
	public static Node getNodeByIP(String ipAddress){
		for(Node node:nodes){
			if(node.getIpAddress().equals(ipAddress)){
				return node;
			}
		}
		return null;
	}
	public static void step() throws IOException{
		if(neighbors.size()>=1){
			Message message = new Message(myNode.getId(),myNode.getIpAddress(),myNode.getPort(),"step");
			message.setRoutingTable(makeMessage());
			for(Node eachNeighbor:neighbors) {
				sendMessage(eachNeighbor,message); //sending message to each neighbor
				System.out.println("Message sent to "+eachNeighbor.getIpAddress()+"!");
			}
			System.out.println("Step SUCCESS");
		}
		else{
			System.out.println("Sorry. No neighbors found to execute the step command.");
		}
		
	}
	
	public static void sendMessage(Node eachNeighbor, Message message) throws IOException{
		int semaphore = 0;
		try {
			semaphore = write.select();
			if(semaphore>0) {
				Set<SelectionKey> keys = write.selectedKeys();
				Iterator<SelectionKey> selectedKeysIterator = keys.iterator();
				ByteBuffer buffer = ByteBuffer.allocate(5000);
				ObjectMapper mapper = new ObjectMapper();
				String msg = mapper.writeValueAsString(message);
				
				buffer.put(msg.getBytes());
				buffer.flip();
				while(selectedKeysIterator.hasNext())
				{
					SelectionKey selectionKey=selectedKeysIterator.next();
					if(parseChannelIp((SocketChannel)selectionKey.channel()).equals(eachNeighbor.getIpAddress()))
					{
						SocketChannel socketChannel=(SocketChannel)selectionKey.channel();
						socketChannel.write(buffer);
					}
					selectedKeysIterator.remove();
				}
			}
		}catch(Exception e) {
			System.out.println("Sending failed because "+e.getMessage());
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
	
	public static boolean disable(Node server) throws IOException{
		if(isNeighbor(server)){
			
			sendMessage(server,new Message(myNode.getId(),myNode.getIpAddress(),myNode.getPort(),"disable"));
			for(SocketChannel channel:openChannels){
				if(server.getIpAddress().equals(parseChannelIp(channel))){
					try {
						channel.close();
					} catch (IOException e) {
						System.out.println("Cannot close the connection;");
					}
					openChannels.remove(channel);
					break;
				}
			}
			routingTable.put(server,Integer.MAX_VALUE-2);
			neighbors.remove(server);
			System.out.println("Disabled connection with server "+server.getId()+"("+server.getIpAddress()+")");
			return true;
		}
		else{
			System.out.println("You can only disable connection with your neighbor!!");
			return false;
		}
	}
	public static void display() {
		TableBuilder tb = new TableBuilder();
		tb.addRow("Destination Server ID","Next Hop Server ID","Cost");
		Collections.sort(nodes,new NodeComparator());
		for(Node eachNode:nodes){
			int cost = routingTable.get(eachNode);
			String costStr = ""+cost;
			if(cost==Integer.MAX_VALUE-2){
				costStr = "infinity";
			}
			String nextHopID = "N.A";
			if(nextHop.get(eachNode)!=null){
				nextHopID = ""+nextHop.get(eachNode).getId(); 
			}
			tb.addRow(""+eachNode.getId(),""+nextHopID,costStr);
		}
		System.out.println(tb.toString());
	}
	
	
}

class NodeComparator implements Comparator<Node> {
    @Override
    public int compare(Node n1, Node n2) {
    	Integer id1 = n1.getId();
    	Integer id2 = n2.getId();
        return id1.compareTo(id2);
    }
}

