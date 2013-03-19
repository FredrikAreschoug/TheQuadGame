package clientPackage;

/*
 * Main class
 */
public class StartClass {
	
	static public GUI gui;
	public static void main(String[] args){
		//will only work whit 800*600
		gui = new GUI(800, 600);
		gui.strart();
	
	}

}
