
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
		
		try{
			String payload = new String(rxPacket.getData(), 0, rxPacket.getLength()).trim();

			//System.out.println(payload);
			
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
			}
			
			
			/*
			else if (payload.startsWith("GETGROUPS")) {
				onGetGroupsRequested();
				return;
			} else if (payload.startsWith("QUIT")){
				onUnregisterRequested(payload);
			}else if (payload.startsWith("SETGROUPS")){
				payload = payload.substring("SETGROUPS".length() + 1,
		                payload.length()).trim();
				onSetGroupsRequested(payload);
			}else if (payload.startsWith("ACK")){
				onClientAck();
			}else if(payload.startsWith("SENDMETHESTUFF")){
				onSendSTUFF();
			}else if(payload.startsWith("CREATEGROUP")){
				payload = payload.substring("CREATEGROUP".length() + 1,
		                payload.length()).trim();
				onCreateGroup(payload);
			}*/else {
				//onChatReceived(payload);
			}
		}finally{
			
		}
		
			
			
	}

	//Reads in name followed by what groups name belongs to
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
	
	/*
	private void onGetGroupsRequested() throws IOException{
		String aString = "GETGROUPS ";
		for(String s : Server.allGroups){
			aString = aString + s + " ";
		}
		send(aString,address,port);	
			
		
	}
	
	private void onCreateGroup(String payload) throws IOException{
		dataScanner = new Scanner(payload);
		while (dataScanner.hasNext()){
			 Server.allGroups.add(dataScanner.next());
		 }
		onGetGroupsRequested();
		
	}
	
	
	
	private void onSendSTUFF() throws IOException{
		for (ClientEndPoint clientEndPoint : Server.clientEndPoints) {
			
			 if (clientEndPoint.address.equals(address) && clientEndPoint.port == port){
				 if (!clientEndPoint.messages.isEmpty()){
					 if (clientEndPoint.numberOfTimesMessageSent > 20000000){
						 clientEndPoint.messages.remove();
						 clientEndPoint.numberOfTimesMessageSent = 0;
					 }else if (clientEndPoint.messages.size() > 1){
						 
						 send(clientEndPoint.messages.peek(), address, port);
						 clientEndPoint.numberOfTimesMessageSent++;
						 onSendSTUFF();
						 return;
					 }
					 //System.out.println("here");
					 
					 try {
						send(clientEndPoint.messages.peek(), address, port);
						clientEndPoint.numberOfTimesMessageSent++;
					} catch (IOException e) {
						
						e.printStackTrace();
					}
				 }
			 }
		}
		
		
	}
	
	private void onClientAck(){
		Server.clientEndPoints.get(key)
		
		for (ClientEndPoint clientEndPoint : Server.clientEndPoints) {
			 if (clientEndPoint.address.equals(address) && clientEndPoint.port == port){
				 clientEndPoint.numberOfTimesMessageSent = 0;
				
				 clientEndPoint.messages.poll();
			 }
		}
	}
	
	private void onSetGroupsRequested(String payload){
		Set<String> groups = Collections.synchronizedSet(new HashSet<String>());
		dataScanner = new Scanner(payload);
		
		
			for (ClientEndPoint clientEndPoint : Server.clientEndPoints) {
				 if (clientEndPoint.address.equals(address) && clientEndPoint.port == port){
					 String someGroups = "";
					 clientEndPoint.groups.clear();
					 while (dataScanner.hasNext()){
						 String aGroup = dataScanner.next();
						 groups.add(aGroup);
						 someGroups = someGroups + aGroup + " ";
					 }
					 
					 
					 clientEndPoint.groups = groups;
					 clientEndPoint.messages.add("Groups set to " + someGroups);
				 }
			}
			
			
		
	}
	
	private void onUnregisterRequested(String payload) {
        
		
		 for (ClientEndPoint clientEndPoint : Server.clientEndPoints) {
			 if (clientEndPoint.address.equals(address) && clientEndPoint.port == port){
				 Server.clientEndPoints.remove(clientEndPoint);
				 
				 return;
			 }       
		 } 
		 
		 for (ClientEndPoint clientEndPoint : Server.clientEndPoints) {
			 if (clientEndPoint.address.equals(address) && clientEndPoint.port == port){
				 clientEndPoint.messages.add("NOT REGISTERED, so nothing to unregister");
			 }
        
		 }
	}
		 */


	// send a string, wrapped in a UDP packet, to the specified remote endpoint
	public void send(String payload, InetAddress address, int port)
			throws IOException {
		//System.out.println("HERE's what's being sent" +payload );
		if (payload != null){
			DatagramPacket txPacket = new DatagramPacket(payload.getBytes(), payload.length(), address, port);
			this.socket.send(txPacket);
		}
		
	}

	
	
	

/*
	private void onChatReceived(String payload) {
		// get the address of the sender from the rxPacket
				InetAddress address = this.rxPacket.getAddress();
				// get the port of the sender from the rxPacket
				int port = this.rxPacket.getPort();
				Set<String> groups = Collections.synchronizedSet(new HashSet<String>());
				String name = "foobar";
		
	        for (ClientEndPoint clientEndPoint : Server.clientEndPoints) {
	        	
	        		if (clientEndPoint.address.equals(address) && clientEndPoint.port == port){
	        			groups = clientEndPoint.groups;
	        			name = clientEndPoint.name;
	        		}
	        }
	        if (groups != null){
			    for (String groupName : groups){
			        for (ClientEndPoint clientEndPoint : Server.clientEndPoints) {
			        	if (clientEndPoint.groups.contains(groupName)){
			        		
			        		clientEndPoint.messages.add(name + ": " + payload);
			        	}
			        }
			    }   
	        }   
        
        }*/
	}




