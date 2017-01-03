/*	MenuManager.java
 * 	Created by: Tushita Patel
 * 	Purpose: Handles the flow of inputs outside of the match and stores game settings
 * 	Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  11/16/2016 - Yige : Added comments, some code, also removed the errors.
 * 	11/11/2016	-	[Tushita] Create the class and set up all fields and methods
 */

package robowars.flow;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import robowars.display.Display;
import robowars.display.RobotArchiveScreen;
import robowars.display.ScreenEnum;
import robowars.display.SettingsScreen;
import robowars.display.GameOptionsScreen;
import robowars.display.PlayerSelectionScreen;

public class MenuManager {
	
	/**  The parent of the MenuManager */
	private EventCatcher catcher;
	
	
	/**  The possible button names in their scope of screens.
	 *	Deprecated code. Use the HashMap of actions to control the flow instead.
	 */
	//private List<String>[] buttons;
	
	/** Integer index of the current screen of the Display.
	 * Deprecated. Access the value directly from the display instead.
	 */
	//private Integer currentScreen;
	
	
	/**  The preferences from the Settings. */
	//  EXTENSION - These values are being set correctly but never used in the code.
	private Settings settings;
	
	/**  The information about the match given in the MatchOptions class. */
	private MatchOptions matchOptions;
	
	/**  A list of all records of the robots in the game. */
	private List<RobotRecord> robotRecords;
	
	/** A reference to the Display component of the application. */
	private Display display;
	
	/**
	 * Constructor
	 */
	@SuppressWarnings({ "serial", "unchecked" })
	public MenuManager(EventCatcher ec, Display display){
		this.catcher = ec;
		this.display = display;

		//this.buttons = (ArrayList<String>[])new ArrayList[ScreenEnum.values().length];
		this.settings = new Settings();
		this.matchOptions = new MatchOptions();
		this.robotRecords = new ArrayList<RobotRecord>();
		
		//  Load the robot records here
		
		RecordLoader loader = new RecordLoader(false);
		
		this.robotRecords = loader.getJsonFile();
		
		
		//  Define the actions for each button (or other element if necessary) per screen here.
		
		
		//  Title screen
		HashMap<String, AbstractAction> titleActions = new HashMap<>();
		titleActions.put("newGame", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("New Game");
				display.switchTo(ScreenEnum.PLAYERSELECTION);
			}
			
		});
		titleActions.put("archive", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Robot Archive");
				display.switchTo(ScreenEnum.ROBOTARCHIVE);
			}
			
		});
		titleActions.put("settings", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Settings");
				display.switchTo(ScreenEnum.SETTINGS);
			}
			
		});
		titleActions.put("exit", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Exit");
				display.dispose();
				System.exit(0);
			}
			
		});
		
		
	//  Player Selection screen
		HashMap<String, AbstractAction> playerSelectionActions = new HashMap<>();
		playerSelectionActions.put("back", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Player Selection - Back");
				display.switchTo(ScreenEnum.TITLE);
				
			}
			
		});
		playerSelectionActions.put("options", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Player Selection - Game Options");
				display.switchTo(ScreenEnum.GAMEOPTIONS);
			}
			
		});
		playerSelectionActions.put("start", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Player Selection - Start Game");
				
				//  Save everything to MatchOptions
				PlayerSelectionScreen selectionScreen = (PlayerSelectionScreen) display.getCurrentScreen();
				if (selectionScreen.getNumberOfPlayers() != 0){
					matchOptions.setNumPlayers(selectionScreen.getNumberOfPlayers());
					
					ArrayList<JPanel> panels = selectionScreen.getPlayerPanels();
					
					String[] names = new String[matchOptions.getNumPlayers()];
					Boolean[] ai = new Boolean[matchOptions.getNumPlayers()];
					HashMap<Integer, RobotRecord>[] records = (HashMap<Integer, RobotRecord>[]) new HashMap[matchOptions.getNumPlayers()];
					
					for (int i = 0; i < matchOptions.getNumPlayers(); i++){
						
						names[i] = "";
						ai[i] = false;
						records[i] = new HashMap<Integer, RobotRecord>();
						
						Component[] panelPieces = panels.get(i).getComponents();
						for (int j = 0; j < panelPieces.length; j++){
							switch(panelPieces[j].getName()){
							case "NameField":
								names[i] = ((JTextField) panelPieces[j]).getText();
								break;
							case "AIField":
								ai[i] = ((JCheckBox) panelPieces[j]).isSelected();
								break;
							default:
								break;
							}
						}
						
						if (ai[i] == true){
							records[i] = new HashMap<Integer, RobotRecord>();
							for (int j = 0; j < matchOptions.getNumScouts(); j++){
								//  EXTENSION - Hashmap these later for easy searching?
								records[i].put(j, robotRecords.get(0));
							}
							for (int j = matchOptions.getNumScouts(); j < matchOptions.getNumScouts() + matchOptions.getNumSnipers(); j++){
								//  EXTENSION - Hashmap these later for easy searching?
								records[i].put(j, robotRecords.get(0));
							}
							for (int j = matchOptions.getNumScouts() + matchOptions.getNumSnipers(); j < matchOptions.getNumScouts() + matchOptions.getNumSnipers() + matchOptions.getNumTanks(); j++){
								//  EXTENSION - Hashmap these later for easy searching?
								records[i].put(j, robotRecords.get(0));
							}
						}
						
					}
					
					matchOptions.createPlayers(matchOptions.getNumPlayers(), names, ai, records);
					
					int boardSize = selectionScreen.getBoardSize();
					matchOptions.setBoardSize(boardSize);
					
					//  Instantiate the GameManager
					catcher.createGameManager(matchOptions);
					
					//  Create the GameScreen

					display.initializeGame(catcher.getGameManager());
					
					catcher.getGameManager().getActions().get("endTurn").actionPerformed(e);
								
				}
				else {
					throw new RuntimeException("PlayerSelectionScreen error: illegal number of players called when starting a match.");
				}
			}
			
		});

		
		//  Game Options screen
		HashMap<String, AbstractAction> gameOptionsActions = new HashMap<>();
		gameOptionsActions.put("save", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Game Options - Save");
				matchOptions.setSaveStatistics(((GameOptionsScreen) display.getCurrentScreen()).getSaveCheckValue());
				boolean b = false;
				if (((GameOptionsScreen) display.getCurrentScreen()).getRulesComboValue().equals("Advanced")){
					b = true;
				}
				matchOptions.setUseAdvanced(b);
				display.switchTo(ScreenEnum.PLAYERSELECTION);
			}
			
		});
		gameOptionsActions.put("cancel", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				((GameOptionsScreen) display.getCurrentScreen()).setSaveCheckValue(matchOptions.getSaveStatistics());
				String s = "";
				if (matchOptions.getUseAdvanced()){
					s = "Advanced";
				}
				else {
					s = "Normal";
				}
				((GameOptionsScreen) display.getCurrentScreen()).setRulesComboValue(s);
				display.switchTo(ScreenEnum.PLAYERSELECTION);
			}
			
		});
				
		//  Turn transition screen
		HashMap<String, AbstractAction> turnTransitionActions = new HashMap<>();
					
		//  Game Screen goes in GameManager
		
		//  Results screen
		HashMap<String, AbstractAction> resultsActions = new HashMap<>();
		resultsActions.put("title", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Results - Title");
				display.switchTo(ScreenEnum.TITLE);
			}
			
		});
		resultsActions.put("viewLog", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Results - View Log");
				// TODO Empty for now
			}
			
		});
		resultsActions.put("rematch", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Results - Rematch");
				// TODO Instantiate a Game Manager and new match with the old Match Options
			}
			
		});
		
		//  Robot Archive screen
		HashMap<String, AbstractAction> robotArchiveActions = new HashMap<>();
		robotArchiveActions.put("search", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Robot Archive - Search");
				search();
			}
			
		});
		robotArchiveActions.put("back", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Robot Archive - Back");
				// TODO Make sure the values on this screen reset.
				display.switchTo(ScreenEnum.TITLE);
			}
			
		});
		robotArchiveActions.put("register", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Robot Archive - Register");
				int returnValue = ((RobotArchiveScreen)display.getScreen(ScreenEnum.ROBOTARCHIVE.ordinal())).getChooser().showOpenDialog(null);
				if (0 == returnValue){
					@SuppressWarnings("unused")
					String name = JOptionPane.showInputDialog("Please enter the name to assign the Robot");
					// Assign this value
				}
			}
			
		});
		robotArchiveActions.put("revise", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Robot Archive - Revise");
				try {
					File file = new File(System.getProperty("user.dir") + "newfile.txt");
					file.createNewFile();
					Desktop.getDesktop().edit(file);
					file.delete();
				} catch (IOException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
				}
			}
			
		});
		robotArchiveActions.put("retire", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Robot Archive - Retire");
				//  Don't do anything if a robot is not selected.
				if (((RobotArchiveScreen)display.getScreen(ScreenEnum.ROBOTARCHIVE.ordinal())).getCurrentLabel() == null){
					return;
				}
				
				// Confirm the decision.
				int returnValue = JOptionPane.showConfirmDialog(null, "Are you sure you want to retire: " + ((RobotArchiveScreen)display.getScreen(ScreenEnum.ROBOTARCHIVE.ordinal())).getCurrentLabel().getText() + "?");
				if (returnValue == 0){
					retire();
				}
			}
			
		});
		
		//  Settings screen
		HashMap<String, AbstractAction> settingsActions = new HashMap<>();
		settingsActions.put("cancel", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Settings - Cancel");
				// TODO Keymapping stuff is not a priority right now
				
				for (JComboBox<String> comboBox : ((SettingsScreen)display.getCurrentScreen()).getMenus()){
					comboBox.setSelectedIndex(0);
				}
				JSlider volume = ((SettingsScreen)display.getCurrentScreen()).getVolumeSlider();
				volume.setValue(settings.getVolume());
				display.switchTo(ScreenEnum.TITLE);
				
			}
			
		});
		settingsActions.put("save", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Settings - Save");
				//TODO Keymapping is not a priority
				settings.setVolume(((SettingsScreen)display.getCurrentScreen()).getVolumeSlider().getValue());
				display.switchTo(ScreenEnum.TITLE);
			}
			
		});
	
		
		System.out.println("Initializing display screens.");
		
		this.display.initializeTitle(titleActions);
		this.display.initializePlayerSelection(playerSelectionActions);
		this.display.initializeGameOptions(gameOptionsActions);
		this.display.initializeTurnTransition(turnTransitionActions);
		this.display.initializeResults(resultsActions);
		this.display.initializeRobotArchive(robotArchiveActions);
		this.display.initializeSettings(settingsActions);
		
		//currentScreen = 0;
		
	}
	
	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}
	
	/**
	 * Loads settings from the file, or creates a new file if none exists.
	 * Not implemented.
	 * @throws IOException 
	 */
	@SuppressWarnings("unused")
	private void loadSettings() {
		//  EXTENSION - Ideally, the application's settings would be saved locally to some config file and loaded when the application starts.
		Path path = Paths.get("Settings");
		if (path == null) {
			try {
				Files.createFile(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		
		try {
			Files.readAllLines(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Updates the values of the settings and saves to file.
	 */
	@SuppressWarnings("unused")
	private void saveSettings() {
		//  EXTENSION - Ideally, the application's settings would be saved locally to some config file.
		Path path = Paths.get("Settings");
		if (path == null) {
			try {
				Files.createFile(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		List<String> settings = Arrays.asList("Some", "Settings"); 
		try {
			Files.write(path, settings, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

	
	/**
	 * Add record of a robot to the robotRecords.
	 * @param robot
	 */
	public void addRobot(RobotRecord robot) {
		
		this.robotRecords.add(robot);
		
	}
	
	public void retire(){
		
	}
	
	public void search(){
		
	}
	
}
