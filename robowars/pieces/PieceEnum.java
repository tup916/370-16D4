/*  TeamEnum.java
 *  Created by: Yige
 *  Purpose: Enumeration for each piece.
 *  Revision History:
 *  11/11/2016 - Yige : Fixed typo.
 *  11/10/2016 - Yige : Created the file, added enumeration for pieces.
 */

package robowars.pieces;

public enum PieceEnum {
	SCOUT(1, 1, 3, 2), 
	SNIPER(2, 2, 2, 3), 
	TANK(3, 3, 1, 1);
	
	private final int attack;
	private final int health;
	private final int movement;
	private final int range;
	
	private PieceEnum(Integer attack, Integer health, Integer movement, Integer range) {
		this.attack = attack;
		this.health = health;	
		this.movement = movement;
		this.range = range;
	}

	public Integer getAttack() {
		return this.attack;
	}

	public Integer getRange() {
		return this.range;
	}

	public Integer getMovement() {
		return this.movement;
	}

	public Integer getHealth() {
		return this.health;
	}

}
