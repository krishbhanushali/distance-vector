package prakash.ram.client;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
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
        		
        		Map<Node,Integer> receivedRoutingTable = msg.getRoutingTable();
        		Map<Node,Integer> myRoutingTable = dv.routingTable;
        		Iterator myRTEntries = myRoutingTable.entrySet().iterator();
        		Iterator receivedRTEntries = receivedRoutingTable.entrySet().iterator();
        		while(myRTEntries.hasNext()) {
        			while(receivedRTEntries.hasNext()){
            			Entry myRTEntry = (Entry)myRTEntries.next();
            			Entry receivedRTEntry = (Entry)receivedRTEntries.next();
            			
            			Object key1 = (Node)myRTEntry.getKey();
            			Node myRTNode = (Node)key1;
            			
            			Object key2 = (Node)receivedRTEntry.getKey();
            			Node receivedRTNode = (Node)key2;
            			
            			Object value1 = (Integer)myRTEntry.getValue();
            			Integer myRTCost = (Integer)value1;
            			
            			Object value2 = (Integer)receivedRTEntry.getValue();
            			Integer receivedRTCost = (Integer)value2;
            			
            			if(myRTNode.equals(receivedRTNode)){
            				if(receivedRTCost+dv.routingTable.get(receivedRTNode)<myRTCost){
            					dv.routingTable.put(receivedRTNode, receivedRTCost+dv.routingTable.get(receivedRTNode));
            				}
            			}
        			}
        		}
        }catch(Exception e) {
        		System.out.println("Something wrong with client side!!!");
        }
        
    }
 
}