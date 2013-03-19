package clientPackage;

/*
 * ends the fire
 */
public class FireTimer implements Runnable{

	public FireTimer(){
	}
	public void run() {
		try {
			Thread.sleep(400);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		StartClass.gui.connection.command.endFire = true;
		StartClass.gui.connection.sendCommand();
		StartClass.gui.connection.command.endFire = false;
	}
	

}
