
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.util.Random;
import java.util.Scanner;


public class ServerWorkerThread extends Thread {

	private int ID;
	private DatagramPacket rxPacket;
	private DatagramSocket socket;
	private InetAddress address;
	private int port;
	private Scanner dataScanner = null;

	public ServerWorkerThread(DatagramPacket packet, DatagramSocket socket) {
		this.rxPacket = packet;
		this.socket = socket;
		this.address = packet.getAddress();
		this.port = packet.getPort();
	}

	@Override
	public void run() {
		
		
			String payload = new String(rxPacket.getData(), 0, rxPacket.getLength()).trim();

			System.out.println("Received " + payload + " from " + rxPacket.getAddress().toString());
			
			
			
			if (payload.startsWith("REGISTER")) {
				onRegisterRequested(payload);
				return;
			}
			else if(payload.startsWith("MATCH")){
				onMatchRequested(payload);
				return;
			}
			
			else if(payload.startsWith("MOVE")){
				onMoveRequested(payload);
				return;
			}
			else if (payload.startsWith("QUIT")){
				onQuitRequested(payload);
				return;
			}else {
				
			}
			
			if (Server.clientEndPoints.containsKey(ID)){
				Server.clientEndPoints.get(ID).lastHeardFromTime = System.currentTimeMillis();
			}
			
	}

	
		
	//payload wanted: REGISTER
	private void onRegisterRequested(String payload) {

		Random rand = new Random();
		ID = rand.nextInt();
		
		
		
		while (Server.clientEndPoints.containsKey(ID) || ID == -1){
			ID = rand.nextInt();
		}
		
		Server.clientEndPoints.put(ID, new ClientEndPoint(address, port));
				
		try {
			send("SETID " + ID, address, port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

			
		//Server.clientEndPoints.get(ID).messages.add("SETID " + ID);
		
	}
	
	//payload wanted: QUIT <USERID>
	private void onQuitRequested(String payload){
		dataScanner = new Scanner(payload);
		dataScanner.next();
		ID = dataScanner.nextInt();
		if (Server.clientEndPoints.containsKey(ID)){
			Server.clientEndPoints.remove(ID);
		}
		
	}
	
	
	//payload wanted: MOVE ID xCoordinate yCoordinate <x or o>
	private void onMoveRequested(String payload){
		dataScanner = new Scanner(payload);
		dataScanner.next();
		ID = dataScanner.nextInt();
		
		String move = dataScanner.nextLine();
		
		int opponentID = Server.clientEndPoints.get(ID).opponent;
		
		if (opponentID != -1){
			try {
				send("MOVE " + move, Server.clientEndPoints.get(opponentID).address, Server.clientEndPoints.get(opponentID).port);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//payload wanted: MATCH <USERID>
	private void onMatchRequested(String payload){
		Random rand = new Random();
		dataScanner = new Scanner(payload);
		dataScanner.next();
		int ID = dataScanner.nextInt();
		
		//If for some reason the person who is looking for a match isn't registered,
		//that person gets registered when they try to match
		if (!Server.clientEndPoints.containsKey(ID)){
			Server.clientEndPoints.put(ID, new ClientEndPoint(address, port));
		}
		
		//If the person who is lookingForMatch doesn't exist anymore,
		//lookingForMatch becomes -1
		if (!Server.clientEndPoints.containsKey(Server.lookingForMatch)){
			Server.lookingForMatch = -1;
	
		}
		
		if (Server.lookingForMatch == -1){
			Server.lookingForMatch = ID;
		}
		else{
			Server.clientEndPoints.get(Server.lookingForMatch).opponent = ID;
			Server.clientEndPoints.get(ID).opponent = Server.lookingForMatch;
			int opponentID = Server.clientEndPoints.get(ID).opponent;
			
			if (rand.nextBoolean()){
				try {
					send("PLAY", address, port);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Server.clientEndPoints.get(ID).messages.add("PLAY");
			}
			else{
				try {
					send("PLAY", Server.clientEndPoints.get(opponentID).address, Server.clientEndPoints.get(opponentID).port);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Server.clientEndPoints.get(Server.lookingForMatch).messages.add("PLAY");
			}
			Server.lookingForMatch = -1;
				
		}
	}
	


	// send a string, wrapped in a UDP packet, to the specified remote endpoint
	public void send(String payload, InetAddress address, int port)
			throws IOException {
		
		if (payload != null){
			DatagramPacket txPacket = new DatagramPacket(payload.getBytes(), payload.length(), address, port);
			this.socket.send(txPacket);
			System.out.println("Sending " +payload + " to " + txPacket.getAddress().toString());
		}
		
	}

	
	
	

	}




