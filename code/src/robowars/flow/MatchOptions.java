/*  MatchOptions.java
 *  Created by: Tushita
 *  Purpose: 
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  11/24/2016 - Tushita : Added getters and setters.
 */

package robowars.flow;

import java.util.HashMap;

public class MatchOptions {

	/** Information on each of the players of the game (names, are they AI, etc.) */
	private PlayerSettings[] players;
	
	/** The number of players in the match */
	private int numPlayers;
	
	/** The name of the map used in the match. */
	//  EXTENSION - Any additional maps used beyond the basic large and small can be passed into the board with this parameter.
	private String mapName;
	
	/** The size of the hexagon board used in the match. */
	private int boardSize;
	
	/** The number of scouts per player in the match. */
	//  EXTENSION - Mechanisms are in place to use values other than 1, and are only missing GUI elements to set these values.
	private int numScouts;
	
	/** The number of snipers per player in the match. */
	//  EXTENSION - Mechanisms are in place to use values other than 1, and are only missing GUI elements to set these values.
	private int numSnipers;
	
	/** The number of tanks per player in the match. */
	//  EXTENSION - Mechanisms are in place to use values other than 1, and are only missing GUI elements to set these values.
	private int numTanks;	
	
	/** Whether the statistics of each robot should be saved to the Robot Record at the end of the match */
	//  EXTENSION - This value has been hooked up to a GUI element but currently does nothing.
	private boolean saveRobotStatistics;
	
	/** Whether the match will be played with standard or advanced rules. 
	 * Advanced rules include: variable number of scouts/snipers/tanks per team, and the name of the map.
	 */
	//  EXTENSION - See the above values for more detail. Advanced rules can be implemented using existing board functions.
	private boolean useAdvancedRules;
	
	public MatchOptions(){
		//  Default values:
		numScouts = 1;
		numSnipers = 1;
		numTanks = 1;
		boardSize = 5;
		mapName = "default";
		numPlayers = 6;
		players = null;  //  We must create the array when the "Start Game" button is pressed.
		saveRobotStatistics = true;
		useAdvancedRules = false;
	}
	
	public PlayerSettings[] getPlayers() {
		return players;
	}

	public void setPlayers(PlayerSettings[] players) {
		this.players = players;
	}

	public int getNumPlayers() {
		return numPlayers;
	}

	public void setNumPlayers(int numPlayers) {
		this.numPlayers = numPlayers;
	}

	public String getMapName() {
		return mapName;
	}

	public void setMapName(String mapName) {
		this.mapName = mapName;
	}

	public int getNumScouts() {
		return numScouts;
	}

	public void setNumScouts(int numScouts) {
		this.numScouts = numScouts;
	}

	public int getNumSnipers() {
		return numSnipers;
	}

	public void setNumSnipers(int numSnipers) {
		this.numSnipers = numSnipers;
	}

	public int getNumTanks() {
		return numTanks;
	}

	public void setNumTanks(int numTanks) {
		this.numTanks = numTanks;
	}

	public boolean getSaveStatistics(){
		return this.saveRobotStatistics;
	}
	
	public void setSaveStatistics(boolean b){
		this.saveRobotStatistics = b;
	}
	
	public boolean getUseAdvanced(){
		return this.useAdvancedRules;
	}
	
	public void setUseAdvanced(boolean b){
		this.useAdvancedRules = b;
	}
	
	/**
	 * Method for initializing the players for the match, given some values taken from the user interface.
	 * @param numberOfPlayers - how many players to create
	 * @param names - Array containing the names of the players.
	 * @param ai - Array containing boolean values of whether the player is AI or not.
	 * @param records - The robot records corresponding to each piece for each player. Blank if the player is a human.
	 * @postcondition - The correct number of teams has been initialized.
	 */
	public void createPlayers(int numberOfPlayers, String[] names, Boolean[] ai, HashMap<Integer, RobotRecord>[] records){
		
		players = new PlayerSettings[numberOfPlayers];
		for (int i = 0; i < numberOfPlayers; i++){
			players[i] = new PlayerSettings();
			players[i].setName(names[i]);
			players[i].setAI(ai[i]);
			players[i].setMap(records[i]);
		}
		
	}
	
	public int getBoardSize(){
		return this.boardSize;
	}
	
	public void setBoardSize(int size){
		this.boardSize = size;
	}
	
}
