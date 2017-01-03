/*  Screen.java
 *  Created by: Nickolas Gough
 *  Purpose: Model a Screen to be displayed within the Display component.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  11/07/2016 - Nickolas Gough : Created the file, added stubs.
 */

package robowars.display;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class Screen extends JPanel{
	
	/** The default serial ID.
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 *  The ScreenEnum assigned to this Screen object.
	 */
	private ScreenEnum screenEnum;
	private List<String> buttonNames;
	
	/**
	 *  The constructor for the Screen class. 
	 *  @param enumeration - The enumeration that is assigned to this Screen object.
	 *  @postcondition A new instance of Screen is produced.
	 */
	public Screen(ScreenEnum enumeration){
		this.screenEnum = enumeration;
		buttonNames = new ArrayList<String>();
	}
	
	
	/**
	 *  Accessor to retrieve the enumeration of this Screen object.
	 *  @return - The ScreenEnum assigned to this Screen object.
	 *  @postcondition The ScreenEnum is retrieved.
	 */
	public ScreenEnum getEnum(){
		return this.screenEnum;
	}
	
	public List<String> getButtonNames(){
		return this.buttonNames;
	}
	
	public void addButtonName(String name){
		this.buttonNames.add(name);
	}
	
	public void clearButtonNames(){
		this.buttonNames.clear();
	}
	
	/**
	 *  Used for testing purposes.
	 *  @param args - The arguments to the main method.
	 */
	public static void main(String[] args){
		//  Instantiate a new Screen object and assign it to be the title screen.
		Screen screen = new Screen(ScreenEnum.TITLE);
		
		//  Test to determine the getEnum() method returns the correct enumeration assigned to screen.
		if (screen.getEnum() != ScreenEnum.TITLE){
			System.out.println("Failure: getEnum() did not return the correct enumeration.");
		}
	}
}
