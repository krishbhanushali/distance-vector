import java.io.File;
import java.util.Scanner;
import java.util.Timer;
import prakash.ram.client.Client;
import prakash.ram.model.AdjacencyList;
import prakash.ram.model.Node;
import prakash.ram.server.Server;

public class dv {
	
	
	static int time;
	static AdjacencyList al;
	public static void main(String[] args) {
		Server server = new Server(2000);
		server.start();
		System.out.println("Server started running...");
		Client client = new Client();
		client.start();
		System.out.println("Client started running...");
		
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
			case "update": //update <
				update(Integer.parseInt(arguments[1]),Integer.parseInt(arguments[2]),Integer.parseInt(arguments[3]));
				break;
			case "step":
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
	
	public static void readTopology(String filename) {
		File file = new File("src/"+filename);
        al = new AdjacencyList(file);
        System.out.println("Reading topology done.");
	}
	
	public static void update(int serverId1, int serverId2, int cost) {
		Node from = al.getNode(serverId1);
		Node to = al.getNode(serverId2);
		al.changeDistance(from, to, cost);
		System.out.println("Update success");
	}
}


