package prakash.ram.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class AdjacencyList {
	private Map<Node,Collection<Edge>> adjacencyList;
	
	public AdjacencyList() {
		adjacencyList = new HashMap<Node,Collection<Edge>>();
	}
	public AdjacencyList(File file) {
		adjacencyList = new HashMap<Node,Collection<Edge>>();
		try {
			Scanner scanner = new Scanner(file);
			int numberOfServers = scanner.nextInt();
			int numberOfNeighbors = scanner.nextInt();
			scanner.nextLine();
			for(int i = 0 ; i < numberOfServers;i++) {
				String line = scanner.nextLine();
				String[] parts = line.split(" ");
				Node server = new Node(Integer.parseInt(parts[0]),parts[1],Integer.parseInt(parts[2]));
				addNode(server);
			}
			for(int i = 0 ; i < numberOfNeighbors;i++) {
				String line = scanner.nextLine();
				String[] parts = line.split(" ");
				Edge edge = new Edge(getNode(Integer.parseInt(parts[0])),getNode(Integer.parseInt(parts[1])),Integer.parseInt(parts[2]));
				addEdge(edge);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println(file.getAbsolutePath()+" not found.");
		}
	}
	
	public boolean addNode(Node n) {
		if(adjacencyList.get(n)==null) {
			adjacencyList.put(n, null);
			return true;
		}
		return false;
	}
	
	public List<Node> getNeighbors(Node n){
		Collection<Edge> edges = adjacencyList.get(n);
		List<Node> neighbors = new ArrayList<Node>();
		if(edges!=null) {
			for(Edge edge:edges) {
				neighbors.add(edge.getTo());
			}
		}
		return neighbors;
	}
	
	public boolean addEdge(Edge edge) {
		Node from = edge.getFrom();
		boolean present = false;
		Collection<Edge> edges = adjacencyList.get(from);
		if(edges==null) {
			present = false;
		}
		else {
			for(Edge eachEdge:edges) {
				if(eachEdge.equals(edge)) {
					present = true;
					break;
				}
			}
		}
		if(present)
			return false;
		else {
			if(edges == null) {
				edges = new ArrayList<Edge>();
			}
			edges.add(edge);
			adjacencyList.put(from, edges);
			return true;
		}
	}
	
	public int getCost(Node from, Node to) {
		Collection<Edge> edges = adjacencyList.get(from);
		for(Edge edge:edges) {
			if(edge.getTo().equals(to)) {
				return edge.getCost();
			}
		}
		return Integer.MAX_VALUE;
	}
	
	public Node getNode(int id) {
		Iterator entries = adjacencyList.entrySet().iterator();
		while(entries.hasNext()) {
			Entry thisEntry = (Entry)entries.next();
			Object key = (Node)thisEntry.getKey();
			Node n = (Node)key;
			if(n.getId() == id) {
				return n;
			}
		}
		return null;
	}
}
