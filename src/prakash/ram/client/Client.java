package prakash.ram.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    int bytesRead;
    public void Run()
    {
        try {
        		while(true){
        			int channelReady = dv.read.selectNow();
        			keys = dv.read.selectedKeys();
        			selectedKeysIterator = keys.iterator();
        			if(channelReady!=0){
        				while(selectedKeysIterator.hasNext()){
        					SelectionKey key = selectedKeysIterator.next();
        					socketChannel = (SocketChannel)key.channel();
        					try{
        						bytesRead = socketChannel.read(buffer);
        					}catch(IOException ie){
        						selectedKeysIterator.remove();
        						String IP = dv.parseChannelIp(socketChannel);
        						Node node = dv.getNodeByIP(IP);
        						//terminate function
        						System.out.println(IP+" remotely closed the connection!");
        						break;
        					}
        					String message = "";
        					while(bytesRead!=0){
        						buffer.flip();
        						while(buffer.hasRemaining()){
        							message+=((char)buffer.get());
        							ObjectMapper mapper = new ObjectMapper();
        							Message msg = mapper.readValue(message,Message.class);
        							//increase the number of received messages counter
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
        						}
        					}
        				}
        			}
        		}
        }catch(Exception e) {
        		System.out.println("Something wrong with client side!!!");
        }
        
    }
 
}