package prakash.ram.client;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
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
	    			        		
	    			        		List<String> receivedRT = msg.getRoutingTable();
	    			        		List<String> myRT = dv.routingTable;
	    			        		int costToReceiver = dv.getCost(fromID);
	    			        		Iterator<String> iter = receivedRT.iterator();
	    			        		Iterator<String> iter1 = myRT.iterator();
	    			        		while(iter.hasNext()){
	    			        			String eachReceivedEntry = iter.next();
	    			        			String[] parts1 = eachReceivedEntry.split("#");
	    			        			while(iter1.hasNext()){
	    			        				String eachMyEntry = iter1.next();
	    			        				String[] parts2 = eachMyEntry.split("#");
	    			        				if(parts1[0].equals(parts2[0])){
		    			        				if(Integer.parseInt(parts1[1])+costToReceiver<Integer.parseInt(parts2[1])){
		    			        					iter1.remove();
		    			        					dv.routingTable.add(parts1[0]+"#"+Integer.parseInt(parts1[1])+costToReceiver);
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





