package prakash.ram.client;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
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
    			        		Map<Node,Integer> createdReceivedRT = makeRT(receivedRT);
    			        		int neighborCost =0;
    			        		for(Map.Entry<Node, Integer> entry : dv.routingTable.entrySet()){
    			        			if(entry.getKey().getIpAddress().equals(msg.getIpAddress())){
    			        				neighborCost = entry.getValue();
    			        			}
    			        		}
    			        		for(Map.Entry<Node, Integer> entry1:createdReceivedRT.entrySet()){
    			        			for(Map.Entry<Node, Integer> entry2:dv.routingTable.entrySet()){
    			        				if(entry1.getKey().getId() == entry2.getKey().getId()){
    			        					if(entry1.getValue()+neighborCost<entry2.getValue()){
    			        						dv.routingTable.put(entry2.getKey(),entry1.getValue()+neighborCost );
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





