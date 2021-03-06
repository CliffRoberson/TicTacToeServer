import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Server {

	// constants
	public static final int DEFAULT_PORT = 20000;
	public static final int MAX_PACKET_SIZE = 512;

	// port number to listen on
	protected int port;

	// set of clientEndPoints 
	// note that this is synchronized, i.e. safe to be read/written from
	// concurrent threads without additional locking
	protected static final Map<Integer, ClientEndPoint> clientEndPoints = Collections.synchronizedMap(new HashMap<Integer, ClientEndPoint>());
	static volatile int lookingForMatch = -1;
	//protected static final Set<String> allGroups = Collections.synchronizedSet(new HashSet<String>());

	

	// constructor
	Server(int port) {
		this.port = port;
	}

	// start up the server
	public void start() {
		
		
		DatagramSocket socket = null;
		try {
			// create a datagram socket, bind to port port. See
			// http://docs.oracle.com/javase/tutorial/networking/datagrams/ for
			// details.
			new CleanOutOldUsersThread().start();
			socket = new DatagramSocket(port);
			

			// receive packets in an infinite loop
			while (true) {
				// create an empty UDP packet
				byte[] buf = new byte[Server.MAX_PACKET_SIZE];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				// call receive (this will populate the packet with the received
				// data, and the other endpoint's info)
				socket.receive(packet);
				// start up a worker thread to process the packet (and pass it
				// the socket, too, in case the
				// worker thread wants to respond)
				ServerWorkerThread t = new ServerWorkerThread(packet, socket);
				t.start();
			}
		} catch (IOException e) {
			// we jump out here if there's an error, or if the worker thread (or
			// someone else) closed the socket
			e.printStackTrace();
		} finally {
			if (socket != null && !socket.isClosed())
				socket.close();
		}
	}

	// main method
	public static void main(String[] args) {
		int port = Server.DEFAULT_PORT;

		// check if port was given as a command line argument
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Invalid port specified: " + args[0]);
				System.out.println("Using default port " + port);
			}
		}

		// instantiate the server
		Server server = new Server(port);

		System.out
				.println("Starting server. Connect with netcat (nc -u localhost "
						+ port
						+ ") or start multiple instances of the client app to test the server's functionality.");

		
		// start it
		server.start();
		

	}
	
	private class CleanOutOldUsersThread extends Thread{
		@Override
		public void run() {
			
			//Iterates over the map and if the entry has been around for over an hour without being heard from
			//it is removed.
			while (true){
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for(Iterator<Entry<Integer, ClientEndPoint>> it = Server.clientEndPoints.entrySet().iterator(); it.hasNext(); ) {
				      Entry<Integer, ClientEndPoint> entry = it.next();
				      if((System.currentTimeMillis() - entry.getValue().lastHeardFromTime) > 3600000) {
				        it.remove();
				        System.out.println("UserID " + entry.getKey() + " was removed due to inactivity.");
				      }
				    }
			}
		}
		
	}

}
