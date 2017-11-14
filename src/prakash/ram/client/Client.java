package prakash.ram.client;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
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
    ByteBuffer buffer = ByteBuffer.allocate(1024);
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
    			        		Node fromNode = dv.getNodeById(fromID);
    			        		int cost = dv.routingTable.get(fromNode);
    			        		List<String> receivedRT = msg.getRoutingTable();
    			        		for(String eachReceivedEntry:receivedRT){
    			        			int id = Integer.parseInt(eachReceivedEntry.split("#")[0]);
    			        			Node eachNode = dv.getNodeById(id);
    			        			int costFromMessage = Integer.parseInt(eachReceivedEntry.split("#")[1]);
	    			        		for(Map.Entry<Node, Integer> entry : dv.routingTable.entrySet()){
	    			        			if(id == entry.getKey().getId()){
	    			        				if(dv.neighbors.contains(fromNode) && costFromMessage<cost){
	    			        					dv.routingTable.put(fromNode,costFromMessage);
	    			        				}
	    			        				else if(costFromMessage+cost<dv.routingTable.get(entry.getKey())){
	    			        					dv.routingTable.put(entry.getKey(),costFromMessage+cost);
	    			        				}
	    			        			}
	    			        		}
    			        		}
        					}
        				}
        			}
        		}
        }catch(Exception e) {
        		e.printStackTrace();
        }
        
    }
 
}





