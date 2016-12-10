/*  Piece.java
 *  Created by: Yige
 *  Purpose: Class for holding the data relevant to each piece on the board.
 *  Revision History:
 *  12/09/2016 - Janelle: Documentation sweep and final edits.
 *  11/25/2016 - Janelle: Added field for turnFinished to handle round changes properly.
 *  11/13/2016 - Yige : Added more comments.
 *  11/11/2016 - Yige : Fixed some comments and removed main().
 *  11/10/2016 - Yige : Created the file, added the first version of code and comments.
 */




package robowars.pieces;

public class Piece {
	
	/**  How much damage the piece does when it shoots an enemy. */
	private Integer attack;
	/**  How far the Piece can see and how far it can shoot. */
	private Integer range;
	/**  How many spaces the Piece can move per turn. */
	private Integer movement;
	/**  How many more spaces the Piece can move on this turn. */
	private Integer currentMovement;
	/**  How much total damage the Piece can absorb before it dies. */
	private Integer health;	
	/**  How much more damage the Piece can absorb before it dies. */
	private Integer currentHealth;
	/**  Whether the Piece is currently alive. */
	private Boolean isAlive;
	/**  A running total of all of the damage this Piece has dealt to
	*  other Pieces (including itself) during this match. */
	private Integer damageDealt;
	/**  A running total of how much damage this Piece has absorbed during this match. */
	private Integer damageTaken;
	/**  A running total of how many spaces this Piece has moved during this match. */
	private Integer spacesMoved;
	/**  A running total of how many opposing Pieces this Piece has defeated during this match. */
	private Integer enemiesDefeated;
	/**  The absolute rotation of this piece, given from 0 to 5, on the board. */
	private Integer absoluteRotation;
	/**  Whether the Piece has shot during its turn. */
	private Boolean hasShot;
	/**  A running total of how many turns this Piece has taken during this match. */
	private Integer turnsTaken;
	/**  The type this piece is */
	private PieceEnum type;
	/**  Has this piece finished a turn this round? */
	private Boolean turnFinished;
	/**  Death flag for animations */
	private Boolean deathFlag;
	
	
	
	/** Constructor for Piece that takes attack, health, movement and range. */
	public Piece(Integer attack, Integer health, Integer movement, Integer range, PieceEnum type) {
		this.attack = attack;
		this.range = range;
		this.movement = movement;
		this.currentMovement = this.movement;
		this.health = health;
		this.currentHealth = this.health;
		this.isAlive = true;
		this.damageDealt = 0;
		this.damageTaken = 0;
		this.spacesMoved = 0;
		this.enemiesDefeated = 0;
		this.absoluteRotation = 0;
		this.hasShot = false;
		this.turnsTaken = 0;
		this.type = type;
		this.turnFinished = false;
		this.deathFlag = false;
	}
	
	/** Constructor for Piece that takes in type and assigns values based on the type. */
	public Piece(PieceEnum type){
		
		this.attack = type.getAttack();
		this.health = type.getHealth();
		this.movement = type.getMovement();
		this.range = type.getRange();

		this.currentMovement = this.movement;
		this.currentHealth = this.health;
		this.isAlive = true;
		this.damageDealt = 0;
		this.damageTaken = 0;
		this.spacesMoved = 0;
		this.enemiesDefeated = 0;
		this.absoluteRotation = 0;
		this.hasShot = false;
		this.turnsTaken = 0;
		this.type = type;
		this.turnFinished = false;
		this.deathFlag = false;
	}
	
	/**
	 * Getter for type field.
	 * @return Scout, Sniper or Tank
	 */
	public PieceEnum getType() {
		return this.type;
	}
	
	
	/**
	 * Getter for attack field.
	 * @return how much damage it does when it shoots an enemy
	 */
	public Integer getAttack() {
		return this.attack;
	}
	
	/**
	 * Getter for range field.
	 * @return how far the Piece can see and how far it can shoot
	 */
	public Integer getRange() {
		return this.range;
	}
	
	/**
	 * Getter for movement field.
	 * @return how many spaces the Piece can move per turn
	 */
	public Integer getMovement() {
		return this.movement;
	}

	/**
	 * Getter for currentMovement field.
	 * @return how many more spaces the Piece can move on this turn
	 */
	public Integer getCurrentMovement() {
		return this.currentMovement;
	}
	
	/**
	 * Getter for health field.
	 * @return how much total damage the Piece can absorb before it dies
	 */
	public Integer getHealth() {
		return this.health;
	}
	
	/**
	 * Getter for currentHealth field.
	 * @return how much more damage the Piece can absorb before it dies
	 */
	public Integer getCurrentHealth() {
		return this.currentHealth;
	}
	
	/**
	 * Getter for damageDealt field.
	 * @return a running total of all of the damage this Piece has dealt to 
	 * 		   other Pieces (including itself) during this match
	 */
	public Integer getDamageDealt() {
		return this.damageDealt;
	}
	
	/**
	 * Getter for damageTaken field.
	 * @return a running total of how much damage this Piece has absorbed during this match
	 */
	public Integer getDamageTaken() {
		return this.damageTaken;
	}
	
	/**
	 * Getter for spacesMoved field.
	 * @return a running total of how many spaces this Piece has moved during this match
	 */
	public Integer getSpacesMoved() {
		return this.spacesMoved;
	}
	
	/**
	 * Getter for enemiesDefeated field.
	 * @return a running total of how many opposing Pieces this Piece has defeated during this match
	 */
	public Integer getEnemiesDefeated() {
		return this.enemiesDefeated;
	}
	
	/**
	 * Getter for turnsTaken field.
	 * @return a running total of how many turns this Piece has taken during this match
	 */
	public Integer getTurnsTaken() {
		return this.turnsTaken;
	}
	
	/**
	 * Getter for isAlive field.
	 * @return true if the Piece is currently alive, false otherwise
	 */
	public Boolean isAlive() {
		return this.isAlive;
	}
	
	/**
	 * Getter for absoluteRotation field.
	 * @return the absolute rotation of this piece, given from 0 to 5, on the board
	 */
	public Integer getAbsoluteRotation() {
		return this.absoluteRotation;
	}
	
	/**
	 * Getter for hasShot field.
	 * @return whether the Piece has shot during its turn
	 */
	public Boolean getHasShot() {
		return this.hasShot;
	}
	
	/**
	 * Calculate the relative rotation from the absolute rotation.
	 * @param direction some absolute rotation value
	 * @return the relative rotation [-5, 5]
	 */
	public Integer getRelativeRotation(Integer direction) {
		return this.absoluteRotation + direction;
	}
	
	/**
	 * Calculate the absolute rotation from relative rotation.
	 * @param direction some relative rotation value [-5, 5]
	 * @return the absolute rotation [0, 5]
	 */
	public Integer getAbsoluteRotation(Integer direction) {
		Integer dir = this.absoluteRotation + direction;
		if (dir <  0){
			while (dir < 0){
				dir += 6;
			}
		}
		else if (dir > 5){
			while (dir > 5){
				dir -= 6;
			}
		}
		return dir;
	}
	
	/**
	 * Update the absoluteRotation field.
	 * @param direction a relative rotation [-5, 5]
	 */
	public void rotate(Integer direction) {
		this.absoluteRotation = this.getAbsoluteRotation(direction);
	}
	
	
	
	public Boolean getTurnFinished() {
		return turnFinished;
	}

	public void setTurnFinished(Boolean turnFinished) {
		this.turnFinished = turnFinished;
	}
	
	public Boolean isAvailableForTurn() {
		return !this.getTurnFinished() && this.isAlive();
	}

	/**
	 * Reset currentMovement, turnFinished and hasShot for next turn
	 * @postcondition The above values have been reset for the next round.
	 */
	public void resetRound() {
		this.currentMovement = this.movement;
		this.hasShot = false;
		this.turnFinished = false;
	}

	/**
	 * Update the statistics related to shoot.
	 * @param inDamageDealt the amount of damage dealt on a shot
	 * @param inEnemiesDefeated the amount of enemies defeated on a shot
	 */
	public void updateShoot(Integer damageDealt, Integer enemiesDefeated) {
		this.damageDealt += damageDealt;
		this.enemiesDefeated += enemiesDefeated;
		this.hasShot = true;
	}
	
	/**
	 * Update the statistics related to move.
	 * @param spacesMoved the number of spaces moved
	 * @param relativeDirection the relative direction of movement
	 */
	public void updateMove(Integer spacesMoved, Integer relativeDirection) {
		this.spacesMoved += spacesMoved;
		this.currentMovement -= spacesMoved;
		this.rotate(relativeDirection);
	}
	
	/**
	 * Update the statistics related to damage taken.
	 * @param damageReceived the amount of damage received
	 */
	public void takeDamage(Integer damageReceived) {
		this.damageTaken += damageReceived;
		this.currentHealth -= damageReceived;
		if (this.currentHealth <= 0) {
			this.currentHealth = 0;
			this.deathFlag = true;
			this.isAlive = false;
		}
	}

	
	
	/**
	 * Method for ending the piece's turn in the game logic, and updating its statistics.
	 * @postcondition The number of turns taken has been incremented, and the turnFinished value has been set to true.
	 */
	public void endTurn(){
		this.turnsTaken += 1;
		this.turnFinished = true;
	}
	
	/**
	 * Method for processing death flags for animations.
	 */
	public void processDeathFlag(){
		if (this.deathFlag){
			this.isAlive = false;
		}
	}
	
}
