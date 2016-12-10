/*  Logger.java
 *  Created by: Yige
 *  Purpose: Class for storing logs for each team.
 *  Revision History:
 *  12/09/2016 - Tushita : add printLogger() method.
 *  12/09/2016 - Janelle : Documentation sweep and final edits. Made constructors modular for number of pieces per team.
 *  11/13/2016 - Yige : Rewrote for-loop in a more elegant way.
 *  11/12/2016 - Yige : Finished the first version of tests, code and comments.
 *  11/10/2016 - Yige : Created the file, added comments and stubs.
 *  
 */

package robowars.logger;

import java.util.List;

import robowars.pieces.TeamEnum;

public class Logger {

	/**  Contains all of the logs in the game */
	private Log[] teamLogs;

	/**  Constructor of Logger class for six teams */
	public Logger(int numberOfPieces) {
		this.teamLogs = new Log[7];
		for (int i = 0; i < this.teamLogs.length; i++) {
			this.teamLogs[i] = new Log(numberOfPieces);
		}
	}
	
	/**  Constructor of Logger class for a variable number of teams. */
	public Logger(int teamNumber, int numberOfPieces) {
		this.teamLogs = new Log[teamNumber+1];
		for (int i = 0; i < this.teamLogs.length; i++) {
			this.teamLogs[i] = new Log(numberOfPieces);
		}
	}
	

	public Log[] getTeamLogs() {
		return teamLogs;
	}

	/**Display all of the logs for this team, includes movement, shooting and damaged/death/game over.
	 * @param inTeam one of the TeamEnum
	 */
	public void displayLog(TeamEnum inTeam) {
		List<String> logs = this.teamLogs[inTeam.ordinal()].getTeamLog();
		this.displayHelper(logs);
	}

	/**
	 * Prints the logger - all of it, for each team pieces.
	 */
	public void printLogger(){
		
		System.out.println("Loggers");
		
		for (int teamNumber = 0; teamNumber < this.getTeamLogs().length; teamNumber++){
			TeamEnum inTeam = TeamEnum.RED;
			this.displayMovement(inTeam);
			
			this.displayDamaged(inTeam);
			this.displayDeath(inTeam);
			//The displayShooting(intTeam) is commented out, because it is not working.
			//this.displayShooting(inTeam);
		}
		
		System.out.println("----End of logs------");
	}
	
	
	/**
	 * Display all of the movement logs for this team.
	 * @param inTeam one of the TeamEnum
	 */
	public void displayMovement(TeamEnum inTeam) {
		List<String> logs = this.teamLogs[inTeam.ordinal()].getTeamMovement();
		this.displayHelper(logs);
	}

	/**
	 * Display all of the shooting logs for this team.
	 * @param inTeam one of the TeamEnum
	 */
	public void displayShooting(TeamEnum inTeam) {
		List<String> logs = this.teamLogs[inTeam.ordinal()].getTeamShooting();
		this.displayHelper(logs);
	}

	/**
	 * Display all of the damaged logs for this team.
	 * @param inTeam one of the TeamEnum
	 */
	public void displayDamaged(TeamEnum inTeam) {
		List<String> logs = this.teamLogs[inTeam.ordinal()].getTeamDamaged();
		this.displayHelper(logs);
	}
	
	/**
	 * Display all of the death logs for this team.
	 * @param teamEnum one of the TeamEnum
	 */
	public void displayDeath(TeamEnum teamEnum) {
		List<String> logs = this.teamLogs[teamEnum.ordinal()].getTeamDeath();
		this.displayHelper(logs);
	}
	
	/**
	 * A helper function for displayMovement(), displayShooting() and displayDamaged().
	 * @param logs a list of String to be displayed
	 */
	private void displayHelper(List<String> logs) {
		for (String log : logs) {
			System.out.println(log);
		}
	}

	/**
	 * Clear all the current logs for the user.
	 */
	public void clear() {
		for (Log log : this.teamLogs) {
			log.teamLog.clear();
		}
	}


	public static void main(String[] args) {
		
		//  Main class providing testing for the Logger Class

		/* clear():
		 * 
		 * Test case: Set up a series of logged events of various types from various 
		 * teams and pieces, then execute the clear() method.
		 * Expected result: The entire collection of teamLog has been cleared.
		 */
		
		// It doesn't work for current AddEntry().
//		Logger logger = new Logger();
//		TeamEnum testTeam = TeamEnum.RED;
//		Log testLog = logger.teamLogs[testTeam.ordinal()];
//		
//		testLog.addEntry(EntryEnum.MOVEMENT);
//		List<Entry> entries = testLog.teamLog;
//		Entry movementEntry = entries.get(entries.size() - 1);
//		movementEntry.setMovement(TeamEnum.BLUE, PieceEnum.SCOUT, new HexCoord(1, 1, 1), new HexCoord(2, 2, 2));
//		testLog.addEntry(EntryEnum.SHOOTING);
//		Entry shootingEntry = entries.get(entries.size() - 1);
//		shootingEntry.setShooting(TeamEnum.RED, PieceEnum.SCOUT, new HexCoord(1, 1, 1));
//		testLog.addEntry(EntryEnum.DAMAGED);
//		Entry damagedEntry = entries.get(entries.size() - 1);
//		damagedEntry.setDamaged(TeamEnum.BLUE, PieceEnum.TANK, 2, TeamEnum.RED, PieceEnum.SNIPER);
//		
//		System.out.println("Test for clear()");
//		System.out.println("- Before clear(), results: ");
//		logger.displayLog(testTeam);
//		System.out.println("- After clear(), results: ");
//		logger.clear();
		
	}

}
