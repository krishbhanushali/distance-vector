package prakash.ram.model;

import java.io.Serializable;
import java.util.Collection;

public class Message implements Serializable{

	private static final long serialVersionUID = 1L;
	private int id;
	private String ipAddress;
	private int port;
	private Collection<Edge> changes;
	public Message(int id, String ipAddress, int port, Collection<Edge> changes) {
		super();
		this.id = id;
		this.ipAddress = ipAddress;
		this.port = port;
		this.changes = changes;
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
	public Collection<Edge> getChanges() {
		return changes;
	}
	public void setChanges(Collection<Edge> changes) {
		this.changes = changes;
	}
	
	
}
