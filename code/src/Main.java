/*  Main.java
 *  Created by: Janelle Hindman
 *  Purpose: Main execution class for the Robo-Wars application.
 *  Revision History:
 *  11/26/2016 - Janelle : Created the file and began nesting the constructors.
 */

import robowars.display.Display;
import robowars.display.ScreenEnum;
import robowars.flow.EventCatcher;

public class Main {

	public static void main(String[] args){
		
		//  Load JSON files here?
		//System.out.println("Beginning Robo-Wars application.");
		
		//  Display must initialize all screens and GUI elements
		Display display = new Display();
		
		//System.out.println("Initialized display.");
		
		/*  EventCatcher must initialize:
				MenuManager, which must initialize:
					-The buttons PER SCREEN
					-A Settings object, which will either create a new one or load an old one  (new OK)
					-A MatchOptions object, which should contain the default values OK
					-An ArrayList<RobotRecord> set.
		*/
		//System.out.println("Prior to flow: is event dispatch? " + javax.swing.SwingUtilities.isEventDispatchThread());
		
		@SuppressWarnings("unused")
		EventCatcher flow = new EventCatcher(display);
		
		
		//System.out.println("Initialized flow.");
		display.switchTo(ScreenEnum.TITLE);
		//System.out.println("Is event dispatch? " + javax.swing.SwingUtilities.isEventDispatchThread());


		
	}
	
}
