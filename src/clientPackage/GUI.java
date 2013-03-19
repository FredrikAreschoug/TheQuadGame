package clientPackage;

import game.PlayerInfo;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.util.ResourceLoader;

public class GUI {

	private final int WIDTH, HEIGHT;

	public boolean gotInfo = false;

	private TrueTypeFont font;

	public static Connection connection;

	private String cmdString = "-CONNECT localhost,1500";
	private String[] cmdArray = new String[59];

	private boolean showScreenCmd = true;
	public boolean connected = false;
	public boolean loading = true;

	ArrayList<PlayerInfo> playersData = new ArrayList<PlayerInfo>();

	GUI(int width, int height){
		WIDTH = width;
		HEIGHT = height;
	}

	/*
	 * Sets up the display
	 * and renders the display 60 times every second
	 */
	public void strart(){
		initGL(WIDTH,HEIGHT);
		fontLoader();

		while(!Display.isCloseRequested()){
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			if(connected){
				renderGL();
				screenHealth();
			}
			if(showScreenCmd){
				screenCmd();

			}
			
			if(!connected){
				controls();
			}
			if(gotInfo){
				controls();
			}
			

			Display.update();
			Display.sync(60);
		}

		Display.destroy();
		System.exit(0);

	}

	/*
	 * connects to server when client wants to.
	 */
	public void connect(String ip, int port ){
		
		connection = new Connection(ip, port);
		connection.connect();
	}

	/*
	 * sets up the display
	 */
	public void initGL(int width,int height){
		try{
			Display.setDisplayMode(new DisplayMode(width,height));
			Display.create();
			Display.setVSyncEnabled(true);
		}catch (LWJGLException e){
			e.printStackTrace();
			System.exit(0);
		}

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);  

		GL11.glViewport(0,0, 800,600);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, 800, 600, 0, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);

	}

	/*
	 * Load the font Wendy
	 * takes some time, but can't optimize with out changing in slick
	 */
	public void fontLoader(){
		try {
			InputStream inputStream = ResourceLoader.getResourceAsStream("wendy.ttf");
			Font awtFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
			awtFont = awtFont.deriveFont(18f);
			font = new TrueTypeFont(awtFont,true);
		} catch (FileNotFoundException e) {
			System.out.println("font file \"wendy.ttf\" is missing");
			e.printStackTrace();
		} catch (FontFormatException e) {
			System.out.println("Somthing wrong whit font \"wendy.ttf\"");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException");
			e.printStackTrace();
		}
	}

	/*
	 * Renders the console text
	 */
	public void screenCmd(){

		if(!connected){
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glEnable(GL11.GL_BLEND);
		}


		Color.white.bind();

		for(int i = 0; i < cmdArray.length;i++){
			if(!(cmdArray[i] == null)){
				font.drawString(0, 574 - i *10, cmdArray[i], Color.white);
			}
		}

		font.drawString(0, 586, ">"+ cmdString, Color.white);
	}

	/*
	 * Renders the health to the screen
	 */
	public void screenHealth(){
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);

		String health;
		if(connection.myInfo.getHealt() > 0)
			health = "health: " + connection.myInfo.getHealt();
		else
			health = "health: dead";
		


		font.drawString(800 - (health.length() * 7), 586, health, Color.white);
		
		for(int i = 0; i < playersData.size(); i++){
			if(playersData.get(i).getHealt() > 0)
				font.drawString(0, 0 + (i*10), playersData.get(i).getUserName() + ": " + playersData.get(i).getHealt(), Color.white);
			else
				font.drawString(0, 0 + (i*10), playersData.get(i).getUserName() + ": dead", Color.white);
		}

	}

	/*
	 * Renders all the players
	 */
	public void renderGL(){

		GL11.glDisable(GL11.GL_BLEND);


		for(int i = 0; i < playersData.size(); i++){
			if(!playersData.get(i).dead){
				GL11.glColor3f(playersData.get(i).getRed(), playersData.get(i).getGreen(), playersData.get(i).getBlue());


				GL11.glPushMatrix();
				GL11.glTranslatef(playersData.get(i).getX(), playersData.get(i).getY(), 0);
				GL11.glTranslatef(-playersData.get(i).getX(), -playersData.get(i).getY(), 0);

				GL11.glBegin(GL11.GL_QUADS);
				GL11.glVertex2f(playersData.get(i).getX() + 5, playersData.get(i).getY() - 5);
				GL11.glVertex2f(playersData.get(i).getX() - 5, playersData.get(i).getY() - 5 );
				GL11.glVertex2f(playersData.get(i).getX() - 5, playersData.get(i).getY() + 5);
				GL11.glVertex2f(playersData.get(i).getX() + 5, playersData.get(i).getY() + 5);
				GL11.glEnd();
				GL11.glBegin(GL11.GL_QUADS);

				if(!connection.allPInfo.get(i).getFire()){
					if(connection.allPInfo.get(i).getDirection() == connection.command.DOWN){
						GL11.glVertex2f(playersData.get(i).getX() - 5, playersData.get(i).getY() + 8);
						GL11.glVertex2f(playersData.get(i).getX() + 5, playersData.get(i).getY() + 8);
						GL11.glVertex2f(playersData.get(i).getX() + 5, playersData.get(i).getY() + 6);
						GL11.glVertex2f(playersData.get(i).getX() - 5, playersData.get(i).getY() + 6);

					}else if(connection.allPInfo.get(i).getDirection() == connection.command.UP){
						GL11.glVertex2f(playersData.get(i).getX() - 5, playersData.get(i).getY() - 8);
						GL11.glVertex2f(playersData.get(i).getX() + 5, playersData.get(i).getY() - 8);
						GL11.glVertex2f(playersData.get(i).getX() + 5, playersData.get(i).getY() - 6);
						GL11.glVertex2f(playersData.get(i).getX() - 5, playersData.get(i).getY() - 6);

					}else if(connection.allPInfo.get(i).getDirection() == connection.command.LEFT){
						GL11.glVertex2f(playersData.get(i).getX() - 8, playersData.get(i).getY() - 5);
						GL11.glVertex2f(playersData.get(i).getX() - 8, playersData.get(i).getY() + 5);
						GL11.glVertex2f(playersData.get(i).getX() - 6, playersData.get(i).getY() + 5);
						GL11.glVertex2f(playersData.get(i).getX() - 6, playersData.get(i).getY() - 5);

					}else if(connection.allPInfo.get(i).getDirection() == connection.command.RIGHT){
						GL11.glVertex2f(playersData.get(i).getX() + 8, playersData.get(i).getY() - 5);
						GL11.glVertex2f(playersData.get(i).getX() + 8, playersData.get(i).getY() + 5);
						GL11.glVertex2f(playersData.get(i).getX() + 6, playersData.get(i).getY() + 5);
						GL11.glVertex2f(playersData.get(i).getX() + 6, playersData.get(i).getY() - 5);
					}
				}else{
					if(connection.allPInfo.get(i).getDirection() == connection.command.DOWN){
						GL11.glVertex2f(playersData.get(i).getX() - 1, playersData.get(i).getY() + 13);
						GL11.glVertex2f(playersData.get(i).getX() + 1, playersData.get(i).getY() + 13);
						GL11.glVertex2f(playersData.get(i).getX() + 1, playersData.get(i).getY() + 6);
						GL11.glVertex2f(playersData.get(i).getX() - 1, playersData.get(i).getY() + 6);

					}else if(connection.allPInfo.get(i).getDirection() == connection.command.UP){
						GL11.glVertex2f(playersData.get(i).getX() - 1, playersData.get(i).getY() - 13);
						GL11.glVertex2f(playersData.get(i).getX() + 1, playersData.get(i).getY() - 13);
						GL11.glVertex2f(playersData.get(i).getX() + 1, playersData.get(i).getY() - 6);
						GL11.glVertex2f(playersData.get(i).getX() - 1, playersData.get(i).getY() - 6);

					}else if(connection.allPInfo.get(i).getDirection() == connection.command.LEFT){
						GL11.glVertex2f(playersData.get(i).getX() - 13, playersData.get(i).getY() - 1);
						GL11.glVertex2f(playersData.get(i).getX() - 13, playersData.get(i).getY() + 1);
						GL11.glVertex2f(playersData.get(i).getX() - 6, playersData.get(i).getY() + 1);
						GL11.glVertex2f(playersData.get(i).getX() - 6, playersData.get(i).getY() - 1);

					}else if(connection.allPInfo.get(i).getDirection() == connection.command.RIGHT){
						GL11.glVertex2f(playersData.get(i).getX() + 13, playersData.get(i).getY() - 1);
						GL11.glVertex2f(playersData.get(i).getX() + 13, playersData.get(i).getY() + 1);
						GL11.glVertex2f(playersData.get(i).getX() + 6, playersData.get(i).getY() + 1);
						GL11.glVertex2f(playersData.get(i).getX() + 6, playersData.get(i).getY() - 1);
					}
				}


				GL11.glEnd();
				GL11.glPopMatrix();
			}
		}
	}
	
	/*
	 * All key input and sends the commands
	 */
	public void controls(){
		
		boolean sendCommand = false;
		/*
		 * Movement
		 */
		if(!showScreenCmd){
			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				//			StartBoxGame.client.myInfo.setDirection(DOWN);
				connection.command.setDirection(connection.command.DOWN);
				gotInfo = false;
				sendCommand = true;
			}else if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				//			StartBoxGame.client.myInfo.setDirection(UP);
				connection.command.setDirection(connection.command.UP);
				gotInfo = false;
				sendCommand = true;
			}else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
				//			StartBoxGame.client.myInfo.setDirection(RIGHT);
				connection.command.setDirection(connection.command.RIGHT);
				gotInfo = false;
				sendCommand = true;
			}else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
				//			StartBoxGame.client.myInfo.setDirection(LEFT);
				connection.command.setDirection(connection.command.LEFT);
				gotInfo = false;
				sendCommand = true;
			}
			/*
			 * Fire
			 */
			if(!connection.myInfo.fire){
				if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)){

					fire();
					sendCommand = true;
				}
			}
			/*
			 * bring up the console
			 */
			while(Keyboard.next()){
				if (Keyboard.getEventKey() == Keyboard.KEY_RETURN && cmdString.equals("")) {
					if (Keyboard.getEventKeyState()) {
						showScreenCmd = true;
					}
				}
			}
		}else{
			/*
			 * when the console is up
			 */
			while(Keyboard.next()){
				if (Keyboard.getEventKey() == Keyboard.KEY_RETURN && cmdString.equals("") && connected) {
					if (Keyboard.getEventKeyState()) {
						showScreenCmd = false;
					}
				}
				/*
				 * Commands in the console
				 */
				if(Keyboard.getEventKey() == Keyboard.KEY_RETURN){
					if (Keyboard.getEventKeyState()) {
						if(cmdString.equals("-HELP")){
							saveCmd("---Command Help---");
							saveCmd("-Connect. [ip], [port]<--- will connect you to server");
							saveCmd("-Start<--- Starts a new game");
							saveCmd("-Team [1/0] <--- if 1 teams is on if 0 teams ar of");
							saveCmd("-ChangeTeam<--- if team match you will change team to opposit team");
							saveCmd("-Health [num] <--- sets health to provided number for the next game");
							saveCmd("-Nick [nick]<--- sets your nick to provided nick");
							saveCmd("-Adminpass [password]<--- sets provided password to your adminpass for the server");
							
							cmdString = "";
						}else if(cmdString.startsWith("-CONNECT")){
							saveCmd(cmdString);
							cmdString = cmdString.replaceFirst("-CONNECT", "");
							cmdString = cmdString.trim();
							int brake = cmdString.indexOf(',');
							String ip = cmdString.substring(0, brake);
							cmdString = cmdString.replaceFirst(ip + ",", "");
							cmdString = cmdString.trim();
							int port = 1500;
							try{
								port = Integer.parseInt(cmdString);
							}catch(Exception e){
								saveCmd("port must be just numbers");
							}
							
							cmdString = "";
							connect(ip, port);
							
						}
						else if(cmdString.startsWith("-ADMINPASS") || cmdString.startsWith("-CHANGE TEAM") || cmdString.startsWith("-HEALTH") || cmdString.startsWith("-TEAM 1") || cmdString.startsWith("-TEAM 0") || cmdString.startsWith("-START")){
							connection.command.setCmd(cmdString);
							saveCmd(cmdString);
							sendCommand = true;
						}
						else{
						
							connection.command.setCmd(cmdString);
							if(connected)
								sendCommand = true;
							else
								saveCmd(cmdString);
							cmdString = "";
						}
					}
				}
				/*
				 * for some special characters
				 */
				if (Keyboard.getEventKeyState()) {
					if(Keyboard.getKeyName(Keyboard.getEventKey()).equals("BACK")){
						if(cmdString.length() > 0){
							cmdString = cmdString.substring(0, cmdString.length()-1);
						}
					}else if(Keyboard.getKeyName(Keyboard.getEventKey()).equals("MINUS")){
						cmdString += "-";
					}else if(Keyboard.getKeyName(Keyboard.getEventKey()).equals("PERIOD")){
						cmdString += ".";
					}else if(Keyboard.getKeyName(Keyboard.getEventKey()).equals("COMMA")){
						cmdString += ",";
					}else if(Keyboard.getKeyName(Keyboard.getEventKey()).equals("SPACE")){
						cmdString += " ";
					}else if(Keyboard.getKeyName(Keyboard.getEventKey()).length() > 1){

					}else{
						cmdString += Keyboard.getKeyName(Keyboard.getEventKey());
					}
				}
			}
			
		}
		/*
		 * sends if true
		 */
		if(sendCommand){
			connection.sendCommand();
			cmdString = "";
			gotInfo =false;
		}
		
		
		/*
		 * exit if closes
		 */
		if (Display.isCloseRequested()) {
			Display.destroy();
			System.exit(0);
		}
		
	}

	/*
	 * saves the command or message to screen
	 */
	public void saveCmd(String cmd){
		for(int i = cmdArray.length-1; i > 0; i--){
			cmdArray[i] = cmdArray[i-1];
		}
		cmdArray[0] = cmd;
	}

	/*
	 * saves players data for use in this class
	 */
	public void setPlayersData(ArrayList<PlayerInfo> playersData){
		this.playersData = playersData;
	}
	
	/*
	 * starts the fireTimer
	 */
	public void fire(){
		FireTimer ft = new FireTimer();
		Thread t = new Thread(ft);
		t.start();
		connection.command.fire = true;
		
	}

}
