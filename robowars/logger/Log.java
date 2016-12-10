/*  Log.java
 *  Created by: Yige
 *  Purpose: Class for accessing entries for each team.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  11/26/2016 - Yige : Added death death related changes.
 *  11/13/2016 - Yige : Added more comments and rewrote for-loop.
 *  11/12/2016 - Yige : Finished the first version of tests, code and comments.
 *  11/10/2016 - Yige : Created the file, added comments, stubs and part of tests.
 */

package robowars.logger;

import java.util.ArrayList;
import java.util.List;

//import robowars.board.HexCoord;
//import robowars.pieces.PieceEnum;
//import robowars.pieces.TeamEnum;

public class Log {


	static final int NUMBER_PIECE = 3;
	
	/**  A list of Entry representing the logs of each team */
	protected List<Entry> teamLog;
	/**  A list of Integer representing the index of movement logs of each team */
	private List<Integer> movementEntries;
	/**  A list of Integer representing the index of shooting logs of each team */
	private List<Integer> shootingEntries;
	/**  A list of Integer representing the index of damaged logs of each team */
	private List<Integer> damagedEntries;
	/**  A list of Integer representing the index of death logs of each team */
	private List<Integer> deathEntries;
	
	
	/**  Constructor for the Log class. */

	public Log(int numberOfPieces) {
		this.teamLog = new ArrayList<Entry>();
		this.movementEntries = new ArrayList<Integer>();
		this.shootingEntries = new ArrayList<Integer>();
		this.damagedEntries = new ArrayList<Integer>();
		this.deathEntries = new ArrayList<Integer>();
		

	}
	
	/**
	 * Add entry to the team log.
	 * @param entryType
	 */
	public void addEntry(EntryEnum entryEnum) {

		switch (entryEnum) {
		
		case MOVEMENT:
			this.movementEntries.add(this.teamLog.size());
			break;
		case SHOOTING:
			this.shootingEntries.add(this.teamLog.size());
			break;
		case DAMAGED:
			this.damagedEntries.add(this.teamLog.size());
			break;
		case DEATH:
			this.deathEntries.add(this.teamLog.size());
			break;
		default:
			break;
			
		}
		
	}
	
	/**
	 * Add entry to the team log.
	 * @param entryType
	 */
	public void addEntry(Entry entry) {

		this.teamLog.add(entry);
		
		this.addEntry(entry.getEntryEnum());
		
	}
	
	/**
	 * Getter for teamLog field.
	 * @return a list of String representing all of the team logs.
	 */
	public List<String> getTeamLog() {
		if (this.teamLog.size() == 0) {
			return null;
		}
		
		List<String> logs = new ArrayList<String>();
		for (Entry entry : this.teamLog) {
			logs.add(entry.toString());
		}
		return logs;
	}
	
	/**
	 * Getter for movementEntries field.
	 * @return a list of String representing all of the team movement logs.
	 */
	public List<String> getTeamMovement() {
		return this.getTypeLog(this.movementEntries);
	}

	/**
	 * Getter for shootingEntries field.
	 * @return a list of String representing all of the team shooting logs.
	 */
	public List<String> getTeamShooting() {
		return this.getTypeLog(this.shootingEntries);
	}
	
	/**
	 * Getter for damagedEntries field.
	 * @return a list of String representing all of the team damaged logs.
	 */
	public List<String> getTeamDamaged() {
		return this.getTypeLog(this.damagedEntries);
	}
	
	/**
	 * Getter for deathEntries field.
	 * @return a list of String representing all of the team death logs.
	 */
	public List<String> getTeamDeath() {
		return this.getTypeLog(this.deathEntries);
	}
	
	/**
	 * A helper function for getTeamMovement(), getTeamShooting() and getTeamDamaged().
	 * @param LogType one of movementEntries, shootingEntries and damagedEntries
	 * @return a specific type of logs according to the parameter
	 */
	private List<String> getTypeLog(List<Integer> LogType) {
		List<String> logs = new ArrayList<String>();
		for (Integer type: LogType) {
			logs.add(this.teamLog.get(type).toString());	
		}
		return logs;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//  Main class providing testing for the Log Class
		
		
		/*
		 * addEntry():
		 * 
		 * Test case: A MOVEMENT entry is added to the Log.
		 * Expected result: The entry is of type MOVEMENT and is formatted correctly. 
		 * The entry should also be placed into the teamLog list and the movementEntries list.
		 * 
		 * Test case: A SHOOTING entry is added to the Log.
		 * Expected result: The entry is of type MOVEMENT and is formatted correctly. 
		 * The entry should also be placed into the teamLog list and the shootingEntries list.
		 * 
		 * Test case: A DAMAGED entry is added to the Log. 
		 * Expected result: The entry is of type DAMAGED and is formatted correctly. 
		 * The entry should also be placed into the teamLog list and the damagedEntries list.
		 */

		// It doesn't work for current AddEntry().
//		System.out.println("\nTests for addEntry(): \n");
//
//		testLog = new Log(3);
//		List<Entry> entries = testLog.teamLog;
//		
//		testLog.addEntry(EntryEnum.MOVEMENT);
//		Entry movementEntry = entries.get(entries.size());
//		movementEntry.setMovement(TeamEnum.BLUE, PieceEnum.SCOUT, new HexCoord(1, 1, 1), new HexCoord(2, 2, 2));
//		
//		System.out.println("Test case 1: a movement entry is added. ");
//		System.out.println("Expected: entry is formatted correctly. ");
//		System.out.println("  Result: " + movementEntry.toString());
//		System.out.println("Expected: entry is added to teamLog and movementEntries. ");		
//		System.out.println("  Result from teamLog: " + testLog.teamLog.get(testLog.teamLog.size() - 1).toString());
//		System.out.println("  Result from movementEntries: \n    Entry index: " + testLog.movementEntries);
//		System.out.println("    Actual entry: " + testLog.getTeamMovement());
//		
//		
//		testLog.addEntry(EntryEnum.SHOOTING);
//		Entry shootingEntry = entries.get(entries.size() - 1);
//		shootingEntry.setShooting(TeamEnum.RED, PieceEnum.SCOUT, new HexCoord(1, 1, 1));
//		
//		System.out.println("\nTest case 2: a shooting entry is added. ");
//		System.out.println("Expected: entry is formatted correctly. ");
//		System.out.println("  Result: " + shootingEntry.toString());
//		System.out.println("Expected: entry is added to teamLog and shootingEntries. ");		
//		System.out.println("  Result from teamLog: " + testLog.teamLog.get(testLog.teamLog.size() - 1).toString());
//		System.out.println("  Result from shootingEntries: \n    Entry index: " + testLog.shootingEntries);
//		System.out.println("    Actual entry: " + testLog.getTeamShooting());
//		
//		testLog.addEntry(EntryEnum.DAMAGED);
//		Entry damagedEntry = entries.get(entries.size() - 1);
//		damagedEntry.setDamaged(TeamEnum.BLUE, PieceEnum.TANK, 2, TeamEnum.RED, PieceEnum.SNIPER);
//		
//		System.out.println("\nTest case 3: a damaged entry is added. ");
//		System.out.println("Expected: entry is formatted correctly. ");
//		System.out.println("  Result: " + damagedEntry.toString());
//		System.out.println("Expected: entry is added to teamLog and damagedEntries. ");		
//		System.out.println("  Result from teamLog: " + testLog.teamLog.get(testLog.teamLog.size() - 1).toString());
//		System.out.println("  Result from damagedEntries: \n    Entry index: " + testLog.damagedEntries);
//		System.out.println("    Actual entry: " + testLog.getTeamDamaged());
//				
	}

}
