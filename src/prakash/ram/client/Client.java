package prakash.ram.client;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        						//terminate function
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
    							Message msg = mapper.readValue(message,Message.class);
    							//increase the number of received messages counter
    			        		int fromID = msg.getId();
    			        		System.out.println("Message received from "+msg.getIpAddress());
    			        		Node fromNode = dv.getNodeById(fromID);
    			        		List<String> receivedRT = msg.getRoutingTable();
    			        		Map<Node,Integer> createdReceivedRT = makeRT(receivedRT);
    			        		for(Map.Entry<Node, Integer> entry1 : dv.routingTable.entrySet()){
    			        			if(entry1.getKey().equals(dv.myNode)){
    			        				continue;
    			        			}
    			        			else{
    			        				int presentCost = entry1.getValue();
    			        				if(dv.neighbors.contains(entry1.getKey())){
    			        					int receivedCost = createdReceivedRT.get(dv.myNode);
    			        					if(receivedCost<presentCost){
    			        						dv.routingTable.put(entry1.getKey(),receivedCost);
    			        						System.out.println(entry1.getKey().getId()+" updated with cost "+receivedCost+".");
    			        					}
    			        				}else{
    			        					if(dv.routingTable.get(fromNode)+createdReceivedRT.get(entry1.getKey())<entry1.getValue()){
    			        						dv.routingTable.put(entry1.getKey(),dv.routingTable.get(fromNode)+createdReceivedRT.get(entry1.getKey()));
    			        						System.out.println(entry1.getKey().getId()+" updated with cost "+dv.routingTable.get(fromNode)+createdReceivedRT.get(entry1.getKey())+".");
    			        					}
    			        				}
    			        			}
    			        		}    			
        					}
                			buffer.clear();
                			if(message.trim().isEmpty())
								bytesRead =0;
							else
								bytesRead = socketChannel.read(buffer);
							bytesRead=0;
							selectedKeysIterator.remove();
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





