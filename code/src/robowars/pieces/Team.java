/*  Team.java
 *  Created by: Yige
 *  Purpose: Class for storing pieces.
 *  Revision History:
 *  12/09/2016 - Janelle: Documentation sweep and edits.
 *  11/18/2016 - Janelle: Filled in the code for playAI() to integrate with the Interpreter.
 *  11/13/2016 - Yige : Added more comments and removed unused warnings by adding SuppressWarnings("unused").
 *  11/11/2016 - Yige : Fixed comments and removed main().
 *  11/10/2016 - Yige : Created the file, added the first version of code and comments.
 */

package robowars.pieces;

import robowars.board.Board;
import robowars.flow.PlayerSettings;
import robowars.interpreter.Interpreter;

public class Team {

	/**  The Pieces for this team. */
	private Piece[] pieces;
	/**  Whether this Team has been eliminated or not. */
	private Boolean isEliminated;
	/**  The Interpreter for this Team. */
	private Interpreter interpreter;
	/**  Which piece is currently active and taking a turn. */
	@SuppressWarnings("unused")
	private Integer activePiece;
	/**  The colour of the team. */
	private TeamEnum colour;
	/**  Whether this team is AI. */
	private Boolean isAI;
	/**  Reference to the Board */
	private Board board;
	/**  The name of this team */
	private String playerName;
	
	/**  Constructor for unit test purposes only */
	public Team(TeamEnum colour, Boolean isAI, int scouts, int snipers, int tanks) {
	
		//  Initialize the team members.
		this.pieces = new Piece[scouts + snipers + tanks];
		
		
		//  Initialize the team's Scout(s)
		for (int i = 0; i < scouts; i++){
			this.pieces[i] = new Piece(1, 1, 3, 2, PieceEnum.SCOUT);
		}
		//  Initialize the team's Sniper(s)
		for (int i = scouts; i < scouts + snipers; i++){
			this.pieces[i] = new Piece(2, 2, 2, 3, PieceEnum.SNIPER);
		}
		//  Initialize the team's Tank(s)
		for (int i = scouts + snipers; i < scouts + snipers + tanks; i++){
			this.pieces[i] = new Piece(3, 3, 1, 1, PieceEnum.TANK);
		}
		
		this.isEliminated = false;
		this.activePiece = -1;
		this.colour = colour;
		
		
		this.interpreter = null;
		this.board = null;
	
	}
	
	/**  Constructor for Team in the main application */
	public Team(TeamEnum colour, Boolean isAI, PlayerSettings p, Board b) {
	
		//  Initialize the team members.
		this.pieces = new Piece[b.getPiecesPerTeam()];
		
		
		//  Initialize the team's Scout(s)
		for (int i = 0; i < b.getMatchOptions().getNumScouts(); i++){
			this.pieces[i] = new Piece(1, 1, 3, 2, PieceEnum.SCOUT);
		}
		//  Initialize the team's Sniper(s)
		for (int i = b.getMatchOptions().getNumScouts(); i < b.getMatchOptions().getNumScouts() + b.getMatchOptions().getNumSnipers(); i++){
			this.pieces[i] = new Piece(2, 2, 2, 3, PieceEnum.SNIPER);
		}
		//  Initialize the team's Tank(s)
		for (int i = b.getMatchOptions().getNumScouts() + b.getMatchOptions().getNumSnipers(); i < b.getMatchOptions().getNumScouts() + b.getMatchOptions().getNumSnipers() + b.getMatchOptions().getNumTanks(); i++){
			this.pieces[i] = new Piece(3, 3, 1, 1, PieceEnum.TANK);
		}
		
		this.isEliminated = false;
		this.activePiece = -1;
		this.colour = colour;
		
		this.isAI = isAI;
		
		//  Initialize interpreters for all members of the AI team.
		if (this.isAI) {
			this.interpreter = new Interpreter(b.getPiecesPerTeam(), this, p);
		}
		this.board = b;
	
	}

	
	public Board getBoard() {
		return this.board;
	}
	
	/**
	 * Getter for colour.
	 * @return the colour of the team.
	 */
	public TeamEnum getColour() {
		return this.colour;
	}
	
	/**
	 * Getter for isAI.
	 * @return whether this piece is AI.
	 */
	public Boolean isAI() {
		return this.isAI;
	}
	

//	/**
//	 * Getter for next piece.
//	 * @return the next active piece
//	 */
//	public int getNextPiece() {
//		// TODO talk to Janelle
//		//  Wouldn't this prioritize the scout each time? When do we pick up where we left off?
//		//  If i = 0 each time, we'll check Scout each time?
//		//  Are we even using this or is this the domain of the Board?
//		//  If we get rid of this, we need to setEliminated() somehow. Is that also the Board's job?
//
//
//		//  Examine each Piece.
//		for (int i = 0; i < pieces.length; i++) {
//			if (pieces[i].isAlive()) {
//				this.setActivePiece(i);
//				return i;
//			}
//		}
//		
//		//  No Piece is able to take a turn.
//		this.setEliminated();
//		return -1;
//	}
	
	/**
	 * Getter for isEliminated field.
	 * @return true if it is eliminated, false otherwise.
	 */
	public boolean isEliminated() {
		return this.isEliminated;
	}
	
	/**
	 * Set isEliminated to true.
	 */
	private void setEliminated() {
		this.isEliminated = true;
	}

	/**
	 * Method for checking whether the entire team is eliminated.
	 * This method is called after a piece on the team takes damage in order to update the team's status.
	 * @return true if the piece is alive, false otherwise.
	 */
	public boolean checkEliminated(){
		boolean alive = false;
		for (int i = 0; i < this.pieces.length; i++){
			if (this.pieces[i].isAlive()){
				alive = true;
				break;
			}
		}
		
		if (!alive){
			this.setEliminated();
			return true;
		}
		return false;
	}
	
	/**
	 * Getter for interpreter field.
	 * @return own interpreter
	 */
	public Interpreter getInterpreter() {
		return this.interpreter;		
	}
	
	public void initAI(){
		if (this.isAI()){
			for (int i = 0; i < this.pieces.length; i++){
				this.interpreter.initialize(i);
			}
		}
	}
	
	
	/**
	 * Method for running the Interpreter's play() on pieceID.
	 * @param pieceID the piece index
	 */
	public void playAI(int pieceID) {	
		System.out.println("All right, piece " + pieceID + ", let's do this.");
		this.interpreter.play(pieceID);
		System.out.println("All done with the interpreter.");
	}
	
	/**
	 * Setter for activePiece field.
	 * @param pieceID the piece index
	 */
	public void setActivePiece(int pieceID) {
		this.activePiece = pieceID;
	}
	
	/**
	 *  Method for getting a piece from this team using an index.
	 *  @param index Index for accessing the nth piece for this team.
	 *  @return The piece corresponding to the index.
	 *  @throws RuntimeException
	 */
	public Piece getPiece(int index) throws RuntimeException{
		if (index >= 0 && index < this.pieces.length){
			return this.pieces[index];
		}
		else {
			throw new RuntimeException("Invalid piece index in team field for getPiece().");
		}
	}
	
	public String getPlayerName(){
		return this.playerName;
	}
	
	public void setPlayerName(String s){
		this.playerName = s;
	}

	/**
	 * Method for resetting game logic values for each piece when a new round begins.
	 * @postcondition The resetRound() method for each piece has been called if that piece is alive.
	 */
	public void resetRound(){
		for (int i = 0; i < this.board.getPiecesPerTeam(); i++){
			if (this.pieces[i].isAlive()){
				this.pieces[i].resetRound();
			}
		}
	}
	
	/**
	 * Method for handling death flags for each piece for animation purposes.
	 * @postcondition The processDeathFlag() method has been called for each piece.
	 */
	public void processDeathFlags(){
		for (int i = 0; i < this.pieces.length; i++){
			this.pieces[i].processDeathFlag();
		}
	}
	

}
