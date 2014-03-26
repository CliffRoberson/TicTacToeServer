import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Queue;

public class ClientEndPoint {
	
	int opponent;
	protected InetAddress address;
	protected int port;
	
	protected Queue<String> messages;
	protected int numberOfTimesMessageSent;
	long lastHeardFromTime;
	
	public ClientEndPoint(InetAddress addr, int port) {
		
		this.opponent = -1;
		this.address = addr;
		this.port = port;
		
		this.numberOfTimesMessageSent = 0;
		this.messages = new LinkedList<String>();
		lastHeardFromTime = System.currentTimeMillis();
	}

	public void setOpponent(int opponent){
		this.opponent = opponent; 
	}
	
	@Override
	public int hashCode() {
		// the hashcode is the exclusive or (XOR) of the port number and the hashcode of the address object
		return this.port ^ this.address.hashCode();
	}

}
