package clientPackage;

import game.Commands;
import game.PlayerInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Connection {
	
	private final String IP;
	private final int PORT;

	private ObjectInputStream input;
	private ObjectOutputStream output;
	private Socket socket;
	
	public volatile PlayerInfo myInfo;
	public ArrayList<PlayerInfo> allPInfo = new ArrayList<PlayerInfo>();
	
	public Commands command = new Commands();
	
	
//	private String[] cmdArray = new String[3];
	
//	private ArrayList<PlayerInfo> allPInfo = new ArrayList<PlayerInfo>();
	
	Connection(String ip, int port){
		IP = ip;
		PORT = port;
	}
	
	/*
	 * Setting up the sockets,
	 * Gets information from the server
	 * sets up a listener
	 * Changes data and sends it to the server.
	 */
	
	public void connect(){
		try{
			socket = new Socket(IP, PORT);
		}catch(Exception e){
			StartClass.gui.saveCmd("Coudent connect to: " + IP + " on port: " + PORT);
			return;
		}
		
		
		try{
			input = new ObjectInputStream(socket.getInputStream());
			output = new ObjectOutputStream(socket.getOutputStream());
		}catch(IOException eIO){
			System.out.println("Faild to setup in and out streams: " + eIO);
			return;
		}
		
		getStartPosision();
		
		ListenFromServer ls = new ListenFromServer();
		Thread t = new Thread(ls);
		t.start();

		StartClass.gui.saveCmd("Server: Connected");

		StartClass.gui.connected = true;
	
		
		
	}
	
	/*
	 * waiting for the first information from the server.
	 */
	public void getStartPosision(){
		
		try{
			myInfo = (PlayerInfo) input.readObject();

		}catch(IOException e){
			System.out.println("Server has closed the connection: " + e);
		} catch (ClassNotFoundException e) {
			System.out.println("Wrong Class not PlayerInfo. " + e);
		}
	}

	
	/*
	 * Sends player's command to the server.
	 */
	synchronized public void sendCommand(){
		try{
			output.reset();
			output.writeObject(command);
			output.flush();
		}catch(IOException e){
			System.out.println("Couden«t send command" + e);
		}
		StartClass.gui.gotInfo = false;
		command.fire = false;
		command.setDirection(0);
	}
	
	/*
	 * Disconnects
	 */
	public void disconnect(){
		try{
			if(input != null)
				input.close();
		}catch(Exception e) {
			System.out.println(e);
		} 
		try{
			if(output != null)
				output.close();
		}catch(Exception e) {
			System.out.println(e);
		} 
		try{
			if(socket != null)
				socket.close();
		}catch(Exception e) {
			System.out.println(e);
		} 
		
		
	}
	
	/*
	 * listens for information from the server
	 */
	class ListenFromServer implements Runnable{
		
		public void run(){
			
			while(true){
				Object obj = null;
				try{
					
					obj = input.readObject();
					
				}catch(IOException e){
					System.out.println("The connection to the server have been lost.");
					disconnect();
					break;
				} catch (ClassNotFoundException e) {
					System.out.println("Wrong Class not ArrayList" + e);
				}
				
				if(obj != null && obj instanceof ArrayList){
					allPInfo = (ArrayList) obj;
				}
				
				if(allPInfo == null){
					System.out.println("wrong input object!");
				}
				
				StartClass.gui.setPlayersData(allPInfo);
				
				
				
				for(int i = 0; i < allPInfo.size(); i ++){
					if(i == myInfo.getID()){
						myInfo = allPInfo.get(i);
					}
					if(!allPInfo.get(i).getCmd().equals("")){
						StartClass.gui.saveCmd(allPInfo.get(i).getCmd());
					}
				}
				StartClass.gui.gotInfo = true;
				
			}
		}
	}
}
