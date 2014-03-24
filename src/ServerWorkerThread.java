
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

			System.out.println("Recieved " + payload + " from " + rxPacket.getAddress().toString());
			
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
			else if (payload.startsWith("GAMEOVER")){
				onGameOverRequested(payload);
				return;
			}else if (payload.startsWith("QUIT")){
				onQuitRequested(payload);
				return;
			}else {
				
			}
			
			
	}

	
		private void onRegisterRequested(String payload) {

			Random rand = new Random();
			ID = rand.nextInt();
			
			while (Server.clientEndPoints.containsKey(ID)){
				ID = rand.nextInt();
			}
			
			Server.clientEndPoints.put(ID, new ClientEndPoint(address, port));
					

			Server.clientEndPoints.get(ID).messages.add("SETID " + ID);
			
		}
	
	private void onQuitRequested(String payload){
		dataScanner = new Scanner(payload);
		dataScanner.next();
		ID = dataScanner.nextInt();
		Server.clientEndPoints.remove(ID);
	}
	
	private void onGameOverRequested(String payload){
		dataScanner = new Scanner(payload);
		dataScanner.next();
		ID = dataScanner.nextInt();
		int opponentID = Server.clientEndPoints.get(ID).opponent;
		Server.clientEndPoints.get(ID).opponent = -1;
		Server.clientEndPoints.get(opponentID).opponent = -1;
	}
	
	private void onMoveRequested(String payload){
		dataScanner = new Scanner(payload);
		dataScanner.next();
		ID = dataScanner.nextInt();
		
		String move = dataScanner.nextLine();
		
		int opponentID = Server.clientEndPoints.get(ID).opponent;
		
		if (opponentID != -1){
			Server.clientEndPoints.get(opponentID).messages.add("MOVE " + move);
		}
	}
	
	private void onMatchRequested(String payload){
		Random rand = new Random();
		dataScanner = new Scanner(payload);
		dataScanner.next();
		int ID = dataScanner.nextInt();
		
		if (Server.lookingForMatch == -1){
			Server.lookingForMatch = ID;
		}
		else{
			Server.clientEndPoints.get(Server.lookingForMatch).opponent = ID;
			Server.clientEndPoints.get(ID).opponent = Server.lookingForMatch;
			
			
			if (rand.nextBoolean()){
				Server.clientEndPoints.get(ID).messages.add("PLAY");
			}
			else{
				Server.clientEndPoints.get(Server.lookingForMatch).messages.add("PLAY");
			}
			Server.lookingForMatch = -1;
				
		}
	}
	


	// send a string, wrapped in a UDP packet, to the specified remote endpoint
	public void send(String payload, InetAddress address, int port)
			throws IOException {
		//System.out.println("HERE's what's being sent" +payload );
		if (payload != null){
			DatagramPacket txPacket = new DatagramPacket(payload.getBytes(), payload.length(), address, port);
			this.socket.send(txPacket);
		}
		
	}

	
	
	

	}




