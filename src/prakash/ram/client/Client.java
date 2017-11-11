package prakash.ram.client;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import prakash.ram.model.Edge;
import prakash.ram.model.Message;
import prakash.ram.model.Node;
import prakash.ram.model.dv;

public class Client extends Thread
{
    Set<SelectionKey> keys;
    Iterator<SelectionKey> selectedKeysIterator;
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    SocketChannel socketChannel;
    int byteRead;
    public void Run()
    {
        try {
        		ByteArrayInputStream input = new ByteArrayInputStream(buffer.array());
        		ObjectInput in = new ObjectInputStream(input);
        		Message msg = (Message)in.readObject();
        		String ip = msg.getIpAddress();
        		int fromID = msg.getId();
        		Node myNode = dv.myNode;
        		Collection<Edge> links = dv.al.adjacencyList.get(myNode);
        		Collection<Edge> changes = msg.getChanges();
        		for(Edge myLink:links) {
        			for(Edge change:changes) {
        				if(myLink.getTo().equals(change.getFrom()) && myLink.getFrom().equals(change.getTo())) {
        					myLink.setCost(change.getCost());
        				}
        			}
        		}
        }catch(Exception e) {
        		System.out.println("Something wrong with client side!!!");
        }
        
    }
 
}