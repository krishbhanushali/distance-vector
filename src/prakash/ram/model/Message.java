package prakash.ram.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Message implements Serializable{

	private static final long serialVersionUID = 1L;
	private int id;
	private String ipAddress;
	private int port;
	private List<String> routingTable= new ArrayList<String>();
	public Message(int id, String ipAddress, int port) {
		super();
		this.id = id;
		this.ipAddress = ipAddress;
		this.port = port;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public List<String> getRoutingTable() {
		return routingTable;
	}
	public void setRoutingTable(List<String> routingTable) {
		this.routingTable = routingTable;
	}
	
	
}
