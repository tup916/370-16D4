/*  TurnTransitionScreen.java
 *  Created by: Nickolas Gough
 *  Purpose: Models the game Screen.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  21/07/2016 - Nickolas Gough : Created the file and began implementing the basics. 
 */

package robowars.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

public class TurnTransitionScreen extends Screen{

	
	/** 
	 *  The default serial ID.
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 *  Store the Display component.
	 */
	@SuppressWarnings("unused")
	private Display display;
	
	
	/**
	 *  Construct the turn transition Screen.
	 *  @param display - The Display component.
	 *  @postcondition The turn transition Screen is constructed.
	 */
	public TurnTransitionScreen(Display display, HashMap<String, AbstractAction> actions){
		//  Assign this Screen to be the turn transition Screen.
		super(ScreenEnum.TURNTRANSITION);
		
		//  Initialize the game options screen.
		this.setBackground(Color.BLACK);
		this.setVisible(false);
		this.setLayout(new BorderLayout());
	}
	
	
	/**
	 *  The next player is shown the dialog box indicating it is their turn.
	 *  @param playerName - The name of the player whose turn is about to begin.
	 *  @postcondition - The player is shown the message indicating it is their turn.
	 */
	public void showMessagePlayerMessage(String playerName, boolean isAI){
		if (isAI){
			JOptionPane.showMessageDialog(null, "It is now " + playerName + "'s turn! Please wait...");
		}
		else {
			JOptionPane.showMessageDialog(null, "It is now " + playerName + "'s turn!");
		}
	}
	
	/**
	 *  A message is displayed on transition if the game is over.
	 *  @param playerName - The name of the player who was the winner (empty if a draw)
	 *  @param isDraw - Whether the match ended in a draw
	 *  @postcondition - The player is shown the message.
	 */
	public void showVictoryMessage(String playerName, boolean isDraw){
		if (!isDraw){
			JOptionPane.showMessageDialog(null, "" + playerName + " is the winner!");
		}
		else {
			JOptionPane.showMessageDialog(null, "This match has ended in a draw!");
		}
	}
	
}
