package prakash.ram.client;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import prakash.ram.model.Message;
import prakash.ram.model.Node;
import prakash.ram.model.dv;

public class Client extends Thread
{
    Set<SelectionKey> keys;
    Iterator<SelectionKey> selectedKeysIterator;
    ByteBuffer buffer = ByteBuffer.allocate(5000);
    SocketChannel socketChannel;
    int bytesRead;
    public void run()
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
        						dv.disable(node);
        						System.out.println(IP+" remotely closed the connection!");
        						break;
        					}
        					String message = "";
        					while(bytesRead!=0){
        						buffer.flip();
        						while(buffer.hasRemaining()){
        							message+=((char)buffer.get());
        						}
    							ObjectMapper mapper = new ObjectMapper();
    							Message msg = null;
    							boolean messageReceived = false;
    							int fromID = 0;
    							try{
									msg = mapper.readValue(message,Message.class);
									messageReceived = true;
	    							dv.numberOfPacketsReceived++;
    			        			fromID = msg.getId();
    							}catch(JsonMappingException jme){
    								System.out.println("Server "+dv.parseChannelIp(socketChannel)+" crashed.");
    							}
    			        		Node fromNode = dv.getNodeById(fromID);
    			        		if(msg!=null){
    			        			
    			        			if(msg.getType().equals("update") && messageReceived){
    			        				List<String> receivedRT = msg.getRoutingTable();
	        			        		Map<Node,Integer> createdReceivedRT = makeRT(receivedRT);
	        			        		int presentCost = dv.routingTable.get(fromNode);
	        			        		int updatedCost = createdReceivedRT.get(dv.myNode);
	        			        		if(presentCost!=updatedCost){
	        			        			dv.routingTable.put(fromNode,updatedCost);
	        			        		}
    			        			}
	    			        		if(msg.getType().equals("step") && messageReceived) {
	    			        			List<String> receivedRT = msg.getRoutingTable();
	        			        		Map<Node,Integer> createdReceivedRT = makeRT(receivedRT);
	        			        		for(Map.Entry<Node, Integer> entry1 : dv.routingTable.entrySet()){
	        			        			if(entry1.getKey().equals(dv.myNode)){
	        			        				continue;
	        			        			}
	        			        			else{
	        			        				int presentCost = entry1.getValue();
	        			        				int costToReceipient = createdReceivedRT.get(dv.myNode); 
	        			        				int costToFinalDestination = createdReceivedRT.get(entry1.getKey());
        			        					if(costToReceipient+costToFinalDestination < presentCost){
        			        					dv.routingTable.put(entry1.getKey(),costToReceipient+costToFinalDestination);
        			        					dv.nextHop.put(entry1.getKey(),fromNode);
	        			        				
	        			        				/*if(dv.neighbors.contains(entry1.getKey())){
	        			        					int receivedCost = createdReceivedRT.get(dv.myNode);
	    			        						dv.routingTable.put(entry1.getKey(),receivedCost);
	    			        						System.out.println("Server "+entry1.getKey().getId()+" updated with cost "+receivedCost+".");
	        			        				}else{
	        			        					if(dv.routingTable.get(fromNode)+createdReceivedRT.get(entry1.getKey())<presentCost){
	        			        						dv.nextHop.put(entry1.getKey(), fromNode);
	        			        						dv.routingTable.put(entry1.getKey(),dv.routingTable.get(fromNode)+createdReceivedRT.get(entry1.getKey()));
	        			        						System.out.println("Server "+entry1.getKey().getId()+" updated with cost "+dv.routingTable.get(fromNode)+createdReceivedRT.get(entry1.getKey())+".");
	        			        					}
	        			        				}*/
	        			        			}
	        			        		}
	    			        		}
	        					
	    			        		if(msg.getType().equals("disable") || !messageReceived){
	    			        			dv.routingTable.put(fromNode, Integer.MAX_VALUE-2);
	    			        			System.out.println("Routing Table updated with Server "+fromID+"'s cost set to infinity");
	    			        			if(dv.isNeighbor(fromNode)){
	    			        				for(SocketChannel channel:dv.openChannels){
	    			        					if(fromNode.getIpAddress().equals(dv.parseChannelIp(channel))){
	    			        						try {
	    			        							channel.close();
	    			        						} catch (IOException e) {
	    			        							System.out.println("Cannot close the connection;");
	    			        						}
	    			        						dv.openChannels.remove(channel);
	    			        						break;
	    			        					}
	    			        				}
	    			        				dv.routingTable.put(fromNode, Integer.MAX_VALUE-2);
	    			        				dv.neighbors.remove(fromNode);
	    			        			}
	    			        		}
    			        		}
    			        		if(message.isEmpty()){
    			        			break;
    			        		}
    			        		else{
    			        			System.out.println("Message received from Server "+msg.getId()+" ("+dv.parseChannelIp(socketChannel)+")");
    			        			System.out.println("Current Routing Table:-");
    			        			dv.display();
    			        		}
    			        		buffer.clear();
                    			if(message.trim().isEmpty())
    								bytesRead =0;
    							else{
    								try{
    								bytesRead = socketChannel.read(buffer);
    								}catch(ClosedChannelException cce){
    									System.out.println("Channel closed for communication with Server "+fromID+".");
    								}
    							}
    								
    							bytesRead=0;
    							selectedKeysIterator.remove();
        					}
        				}
        			}
        			}
        		}
        }catch(Exception e) {
        		e.printStackTrace();
        }
        
    }
	private Map<Node, Integer> makeRT(List<String> receivedRT) {
		Map<Node,Integer> rt = new HashMap<Node,Integer>();
		for(String str:receivedRT){
			String[] parts = str.split("#");
			int id = Integer.parseInt(parts[0]);
			int cost = Integer.parseInt(parts[1]);
			rt.put(dv.getNodeById(id), cost);
		}
		return rt;
	}
 
}





