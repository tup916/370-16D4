/*  PlayerSettings.java
 *  Created by: 
 *  Purpose: 
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  11/16/2016 - Yige : Added comments according to the design doc.
 */

package robowars.flow;

import java.util.HashMap;

public class PlayerSettings {

	/**  If the player is an AI or a human player. */
	private boolean isAI;
	
	/**  The name given to the player. */
	private String name;
	
	/**  It maps the index of the robot to the record of the robot. */

	public HashMap<Integer, RobotRecord> mapRobots;


	/**
	 * Constructor
	 */
	public PlayerSettings(){
		this.isAI = false;
		this.name = "Player";
		this.mapRobots = new HashMap<Integer, RobotRecord>();
	}
	
	/**
	 * Getter for isAI field.
	 * @return true if the player is an AI or a human player, false otherwise
	 */
	public boolean isAI() {
		return this.isAI;
	}

	/**
	 * Setter for isAI field.
	 * @param isAI if the player is an AI or a human player
	 */
	public void setAI(boolean isAI) {
		this.isAI = isAI;
	}

	/**
	 * Getter for name field.
	 * @return the name given to the player
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Setter for isAI field.
	 * @param name the name given to the player
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public HashMap<Integer, RobotRecord> getMap(){
		return this.mapRobots;
	}
	
	public void setMap(HashMap<Integer, RobotRecord> m){
		this.mapRobots = m;
	}

}
