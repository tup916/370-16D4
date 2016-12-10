/*  Entry.java
 *  Created by: Yige
 *  Purpose: Class for storing each entry.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  11/25/2016 - Janelle : Added log function for piece death.
 *  11/23/2016 - Tushita : Separated functionality. Move to, and Move From, Who shot (instead of 'Who shot who') 
 *  11/12/2016 - Yige : Finished the first version of comments and added toString().
 *  11/11/2016 - Yige : Finished the first version of tests and code.
 *  11/10/2016 - Yige : Created the file, added stubs and comments.
 *  
 */

package robowars.logger;

import robowars.board.HexCoord;
import robowars.pieces.PieceEnum;
import robowars.pieces.TeamEnum;

public class Entry {

	/**  Stores one entry. */
	private String entry;
	/**  Stores the EntryEnum for this entry. */
	private EntryEnum entryEnum;
	
	/**  Constructor for the Entry class. */
	public Entry(EntryEnum entryEnum) {
		this.entry = "";
		this.entryEnum = entryEnum;
	}


	public EntryEnum getEntryEnum() {
		return entryEnum;
	}

	/**
	 * Set movement - starting point - entry to the entry field.
	 * @param team one of the TeamEnum
	 * @param piece one of the PieceEnum
	 * @param start the original location before move in the format HexCoord
	 */
	public void setMovementFrom(TeamEnum team, PieceEnum piece, HexCoord start) {
		this.entry = team.toString() + " " + piece.toString() + " moved from "+ start.toString();
	}
	
	
	/**
	 * Set movement - destination entry to the entry field.
	 * @param team one of the TeamEnum
	 * @param piece one of the PieceEnum
	 * @param end the destination for movement in the format HexCoord
	 */
	public void setMovementTo(TeamEnum team, PieceEnum piece, HexCoord end) {
		this.entry = team.toString() + " " + piece.toString() + " moved to "+ end.toString();
	}
	
	/**
	 * Set movement - overall entry to the entry field.
	 * @param team one of the TeamEnum
	 * @param piece one of the PieceEnum
	 * @param start the original location before move in the format HexCoord
	 * @param end the destination for movement in the format HexCoord
	 */
	public void setMovement(TeamEnum team, PieceEnum piece, HexCoord start, HexCoord end) {
		this.entry = team.toString() + " " + piece.toString() + " moved from " + start.toString() + " and moved to "+ end.toString();
	}
	
	
	/**
	 * Set shooting entry to the entry field.
	 * @param team one of the TeamEnum
	 * @param piece one of the PieceEnum
	 * @param target the target location in the format HexCoord
	 */
	public void setShooting(TeamEnum team, PieceEnum piece, HexCoord target) {
		this.entry = team.toString() + " " + piece.toString() + " shoots " + target.toString() + ". ";
	}
	
	/**
	 * Set shooting entry to the entry field when the target can't be seen.
	 * @param team one of the TeamEnum
	 * @param piece one of the PieceEnum
	 */
	public void setShooting(TeamEnum team, PieceEnum piece) {
		this.entry = team.toString() + " " + piece.toString() + " shot.";
	}
	
	/**
	 * Set shooting entry to the entry field.
	 * @param team one of the TeamEnum receiving damage
	 * @param piece one of the PieceEnum receiving damage
	 * @param damage the amount of damage this piece received
	 * @param pieceSource one of the PieceEnum doing damage
	 */
	public void setDamaged(TeamEnum team, PieceEnum piece, Integer damage, TeamEnum teamSource, PieceEnum pieceSource) {
		this.entry = team.toString() + " " + piece.toString() + " received " + damage 
				+ " point(s) damage from " + teamSource.toString() + " " + pieceSource.toString() + ". ";
	}
	
	/**
	 * Set shooting entry to the entry field when the shooter can't be seen
	 * @param team one of the TeamEnum receiving damage
	 * @param piece one of the PieceEnum receiving damage
	 * @param damage the amount of damage this piece received
	 */
	public void setDamaged(TeamEnum team, PieceEnum piece, Integer damage) {
		this.entry = team.toString() + " " + piece.toString() + " received " + damage 
					+ " point(s) damage.";
	}
	
	/**
	 * Set death entry to the entry field.
	 * @param team one of the TeamEnum whose piece died
	 * @param piece one of the PieceEnum that died
	 */
	public void setDeath(TeamEnum team, PieceEnum piece) {
		this.entry = team.toString() + " " + piece.toString() + " died.";
	}
	
	public String toString() {
		return this.entry;
	}

	public static void main(String[] args) {

		//  Main class providing testing for the Entry Class
		
		/* setMovement(TeamEnum team, PieceEnum piece, HexCoord start, HexCoord end):
		 * 
		 * Test cases: The setMovement() method is called with various teams, pieces, 
		 * and starting and ending spaces.
		 * Expected result: The entry is formatted as a MOVEMENT entry and stored correctly 
		 * in the entry attribute. An example format is: â€œBlue Scout moves from (a, b, c) to (x, y, z).â€�
		 */

		Entry movementTest = new Entry(EntryEnum.MOVEMENT);
		HexCoord start = new HexCoord(1, 1, 1);
		HexCoord end = new HexCoord(2, 2, 2);
		movementTest.setMovement(TeamEnum.RED, PieceEnum.SCOUT, start, end);
		
		System.out.println("Test for setMovement(), anything similar to the following is right: ");
		System.out.println("Blue Scout moves from (a, b, c) to (x, y, z).");
		System.out.println("Result for setMovement(): \n" + movementTest.entry + "\n");

		/* setShooting(TeamEnum team, PieceEnum piece, HexCoord target):
		 * 
		 * Test cases: The setShooting() method is called with various teams, pieces, 
		 * and starting and ending spaces.
		 * Expected result: The entry is formatted as a SHOOTING entry and stored correctly 
		 * in the entry attribute. An example format is: â€œRed Scout shoots (x, y, z), kills 
		 * Blue Scout, and damages Green Tank (Health: 3 -> 2).â€�
		 */
		
		Entry shootingTest = new Entry(EntryEnum.SHOOTING);
		HexCoord target = new HexCoord(1, 1, 1);
		shootingTest.setShooting(TeamEnum.RED, PieceEnum.SCOUT, target);
		
		System.out.println("Test for setShooting(), anything similar to the following is right: ");
		System.out.println("Red Scout shoots (x, y, z), kills Blue Scout, and damages Green Tank (Health: 3 -> 2). ");
		// TODO we need information of all pieces from that HexCoord for the rest part.
		System.out.println("Result for setShooting(): \n" + shootingTest.entry + "\n");
		
		/* setDamaged(TeamEnum team, PieceEnum piece, Integer damage):
		 * 
		 * Test cases: The setDamaged() method is called with various teams, pieces, and 
		 * starting and ending spaces. 
		 * Expected result: The entry is formatted as a DAMAGED entry and stored correctly 
		 * in the entry attribute. An example format is: â€œBlue Tank received 2 point(s) damage 
		 * from Red Sniper.â€�
		 */
		
		Entry damagedTest = new Entry(EntryEnum.DAMAGED);
		damagedTest.setDamaged(TeamEnum.RED, PieceEnum.TANK, 2);
		
		System.out.println("Test for setDamaged(), anything similar to the following is right: ");
		System.out.println("BLUE TANK received 2 point(s) damage.");
		System.out.println("Result for setDamaged(): \n" + damagedTest.entry + "\n");
		
	}

}
