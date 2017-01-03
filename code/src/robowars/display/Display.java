/*  Display.java
 *  Created by: Nickolas Gough
 *  Purpose: Models the Display component, the component whose sole purpose is tp interface with the user.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  11/07/2016 - Nickolas Gough : Created the file and began implementing the basics.
 *  14/07/2016 - Nickolas Gough : Separated the Screens into their own classes extended from Screen.
 *  20/07/2016 - Nickolas Gough : Added the robot archive screen.
 */

package robowars.display;

import java.awt.CardLayout;
import java.awt.Color;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import robowars.board.HexCoord;
import robowars.flow.GameManager;

public class Display extends JFrame{

	/**
	 *  The default serial ID. 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *  The collection of Screens to be displayed by the Display component.
	 */
	private Screen[] screens;

	/**
	 *  The current screen that is being displayed by the Display component.
	 */
	private ScreenEnum currentScreen;
	
	
	/**
	 *  The constructor for the Display component.
	 */
	public Display(){
		//  Initialize the array of screens.
		this.screens = new Screen[ScreenEnum.values().length];

		//  Set the layout of the Display.
		this.setLayout(new CardLayout());

		// Set the state of the frame.
		this.setExtendedState(MAXIMIZED_BOTH);
		this.setPreferredSize(this.getSize());

		//  Set the default close operation.
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Set the background color of the Display.
		this.setBackground(Color.WHITE);

		//  Make the screen visible and disable resizing of the Display.
		this.setVisible(true);

		// Initialize the Screen objects that are to be displayed by the Display component.
		//  For testing purposes only.
//		this.initializeTitle();
//		this.initializeSettings();
//		this.initializePlayerSelection();
//		this.initializeGameOptions();
//		this.initializeResults();
//		this.initializeRobotArchive();
//		this.initializeTurnTransition();
//		this.initializeGame(3, 5);
	

		//  Pack the frame to get the desired sizes of the elements.
		this.pack();

		//  Set the Display to be full-screen.
		this.setExtendedState(MAXIMIZED_BOTH);
		this.setResizable(false);
	}


	/**
	 *  Switches the screen currently being displayed by the Display component.
	 *  @param screenEnum - The enumeration of the screen to switch to.
	 */
	public void switchTo(ScreenEnum screenEnum){
		//  Set the current Screen to be hidden.
		if (this.currentScreen != null){
			this.screens[this.currentScreen.ordinal()].setVisible(false);
		}

		//  Record the new current Screen.
		this.currentScreen = screenEnum;

		//  Switch the current Screen being Displayed.
		this.screens[this.currentScreen.ordinal()].setVisible(true);

		//  Repaint the Display.
		this.repaint(0);
	}
	
	public GameScreen getGameScreen(){
		return (GameScreen)(screens[ScreenEnum.GAME.ordinal()]);
	}

	public Screen getScreen(int value){
		if (value < 0 || value > ScreenEnum.values().length){
			value = 0;
		}
		return (screens[value]);
	}

	/**
	 *  Initializes the title Screen.
	 *  @postcondition The title Screen is initialized and stored.
	 */
	public void initializeTitle(HashMap<String, AbstractAction> titleActions){
		//  Instantiate, store, and add the title Screen.
		TitleScreen title = new TitleScreen(this, titleActions);
		this.screens[title.getEnum().ordinal()] = title;
		this.add(this.screens[title.getEnum().ordinal()]);
	}


	/**
	 *  Initializes the settings Screen.
	 *  @postcondition The settings Screen is initialized and stored.
	 */
	public void initializeSettings(HashMap<String, AbstractAction> actions){
		//  Instantiate, store, and add the settings Screen.
		SettingsScreen settings = new SettingsScreen(this, actions);
		this.screens[settings.getEnum().ordinal()] = settings;
		this.add(this.screens[settings.getEnum().ordinal()]);
	}


	/**
	 *  Initializes the player selection Screen.
	 *  @postcondition The player selection Screen is initialized and stored.
	 */
	public void initializePlayerSelection(HashMap<String, AbstractAction> actions){
		//  Instantiate, store, and add the player selection Screen.
		PlayerSelectionScreen playerSelection = new PlayerSelectionScreen(this, actions);
		this.screens[playerSelection.getEnum().ordinal()] = playerSelection;
		this.add(this.screens[playerSelection.getEnum().ordinal()]);
	}
	
	
	/**
	 *  Initializes the game options Screen.
	 *  @postcondition The game options Screen is initialized and stored.
	 */
	public void initializeGameOptions(HashMap<String, AbstractAction> actions){
		//  Instantiate, store, and add the game options Screen.
		GameOptionsScreen gameOptions = new GameOptionsScreen(this, actions);
		this.screens[gameOptions.getEnum().ordinal()] = gameOptions;
		this.add(this.screens[gameOptions.getEnum().ordinal()]);
	}


	/**
	 *  Initializes the results Screen.
	 *  @postcondition The results Screen is initialized and stored.
	 */
	public void initializeResults(HashMap<String, AbstractAction> actions){
		//  Instantiate, store, and add the results Screen.
		ResultsScreen results = new ResultsScreen(this, actions);
		this.screens[results.getEnum().ordinal()] = results;
		this.add(this.screens[results.getEnum().ordinal()]);
	}


	/**
	 *  Initializes the robot archive Screen.
	 *  @postcondition The robot archive Screen is initialized and stored.
	 */
	public void initializeRobotArchive(HashMap<String, AbstractAction> actions){
		//  Instantiate, store, and add the robot archive Screen.
		RobotArchiveScreen archive = new RobotArchiveScreen(this, actions);
		this.screens[archive.getEnum().ordinal()] = archive;
		this.add(this.screens[archive.getEnum().ordinal()]);
	}
	
	
	/**
	 *  Initializes the game Screen.
	 *  @postcondition The game Screen is initialized and stored.
	 */
	public void initializeGame(GameManager gm){
		//  Instantiate, store, and add the robot archive Screen.
		GameScreen game = new GameScreen(this, gm);
		this.screens[game.getEnum().ordinal()] = game;
		this.add(this.screens[game.getEnum().ordinal()]);
	}
	
	
	/**
	 *  Initializes the turn transition Screen.
	 *  @postcondition The game Screen is initialized and stored.
	 */
	public void initializeTurnTransition(HashMap<String, AbstractAction> actions){
		//  Instantiate, store, and add the robot archive Screen.
		TurnTransitionScreen turnTransition = new TurnTransitionScreen(this, actions);
		this.screens[turnTransition.getEnum().ordinal()] = turnTransition;
		this.add(this.screens[turnTransition.getEnum().ordinal()]);
	}

	
	/**
	 *  Retrieves the current Screen being displayed.
	 *  @return The current Screen.
	 *  @postcondition The Screen currently being displayed is returned.
	 */
	public Screen getCurrentScreen(){
		return this.screens[this.currentScreen.ordinal()];
	}
	
	
	/**
	 *  Used for testing purposes.
	 *  @param args - The arguments provided to the main method.
	 */
	public static void main(String[] args){
		
		//  Main method for testing purposes only
		
		Display display = new Display();
		display.getGameScreen().setCurrentStats(0, "Nickolas", 1, 1, 1, 1);
		display.switchTo(ScreenEnum.GAMEOPTIONS);
		display.getGameScreen().shadeHexesInRange(0, 3);
		//display.getGameScreen().movePiece(0, new HexCoord(-4, 0, -4), new HexCoord(4, 3, 2));
		display.getGameScreen().shootSpace(new HexCoord(-4, 0, -4), new HexCoord(0, 0, 0));
		display.getGameScreen().rotatePlayer(0, "Nickolas", 1, 1, 1, 1);
	}
}
