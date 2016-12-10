/*	Board.java
 * 	Created by: Tushita Patel
 * 	Purpose: Models the game board.
 * 	Revision History:
 * 	11/11/2016 - Tushita : Create the class and set up all fields and methods. No implementations.
 *  11/11/2016 - Tushita : Implement the isInBounds() function.
 *  11/14/2016 - Tushita : Implement the shortestDistance() function. Uff! Finally.
 *  11/20/2016 - Tushita : Implement move and shoot functions
 *  11/22/2016 - Tushita : Add some functions and add logging options to move and shoot
 *  11/24/2016 - Tushita : Added the displacement function, absoluteDirection method.
 *  11/25/2016 - Tushita : Fix changes after code review.
 *  12/04/2016 - Janelle : Integrating Board with the Interpreter.
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 */
package robowars.board;

import java.security.InvalidParameterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.activity.InvalidActivityException;

import robowars.display.Display;

import robowars.flow.MatchOptions;
import robowars.logger.Entry;
import robowars.logger.EntryEnum;
import robowars.logger.Logger;
import robowars.pieces.Piece;
import robowars.pieces.PieceEnum;
import robowars.pieces.Team;
import robowars.pieces.TeamEnum;

public class Board {

	/* Constants */
	/** Stores the side length of a small board */
	private final int SMALLSIDELENGTH = 5;
	
	/** Stores the side length of a large board */
	private final int LARGESIDELENGTH = 7;
	
	/** The starting positions for each team on a map with side length 5. */
	private final HexCoord[] SMALL_MAP_START_POSITIONS = new HexCoord[] {new HexCoord(0, -4, 0),
																		new HexCoord(0, 0, -4),
																		new HexCoord(4, 0, 0),
																		new HexCoord(0, 4, 0),
																		new HexCoord(0, 0, 4),
																		new HexCoord(-4, 0, 0)};
	
	/** The starting positions for each team on a map with side length 7. */
	private final HexCoord[] LARGE_MAP_START_POSITIONS = new HexCoord[] {new HexCoord(0, -6, 0),
																		new HexCoord(0, 0, -6),
																		new HexCoord(6, 0, 0),
																		new HexCoord(0, 6, 0),
																		new HexCoord(0, 0, 6),
																		new HexCoord(-6, 0, 0)};
	
	/** Container for constant offsets in order to calculate absolute direction from the Interpreter's relative direction. */
	private HashMap<HexCoord, Integer> ABSOLUTE_DIRECTION_OFFSETS;
	/** Container for HexCoords in order to calculate location of a hex in absolute direction from the Interpreter's relative direction. */
	private ArrayList<HexCoord> ABSOLUTE_DIRECTION_COORDS;
	
	
	
	/** Stores a collection of coordinates that represent the locations of the game pieces on the game board.	 */
	private HexCoord[] pieceCoords;
	
	/** Stores the collection of teams */
	private Team[] teams;
	
	/** Stores the index of the current piece. */
	private int currentPiece;
	
	/** Stores the number of pieces per team. Typically 3: 1 Scout, 1 Sniper, and 1 Tank */
	private final int piecesPerTeam; 
	
	
	/** Stores the logger */
	private Logger loggerManager;
	
	/** Stores the option for the match */
	private MatchOptions matchOptions;
	
	/** Stores the display */
	private Display display;
	
	/** Stores the length of one side of the game board. Value is either 5 or 7 */
	private int sideLength;	
	
	private int remainingTeams;
	
	private boolean initialized;
	
	
	/**
	 * Constructor for Board. Not a constructor for testing purposed. Integrated with the rest of the game.
	 * @param numberOfTeams
	 * @param sideLength
	 * @param display
	 * @param matchOptions
	 * @throws InvalidActivityException
	 */
	public Board(int numberOfTeams, Display display,  MatchOptions matchOptions) throws InvalidActivityException{
		
		//  Load the absolute direction offsets.
		ABSOLUTE_DIRECTION_OFFSETS = new HashMap<HexCoord, Integer>();
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,0,0), 0);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,1,0), 0);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,0,1), 1);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(-1,0,0), 2);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,-1,0), 3);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,0,-1), 4);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(1,0,0), 5);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,2,0), 0);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,1,1), 1);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,0,2), 2);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(-1,0,1), 3);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(-2,0,0), 4);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(-1,-1,0), 5);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,-2,0), 6);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,-1,-1), 7);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,0,-2), 8);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(1,0,-1), 9);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(2,0,0), 10);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(1,1,0), 11);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,3,0), 0);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,2,1), 1);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,1,2), 2);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,0,3), 3);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(-1,0,2), 4);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(-2,0,1), 5);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(-3,0,0), 6);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(-2,-1,0), 7);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(-1,-2,0), 8);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,-3,0), 9);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,-2,-1), 10);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,-1,-2), 11);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(0,0,-3), 12);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(1,0,-2), 13);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(2,0,-1), 14);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(3,0,0), 15);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(2,1,0), 16);
		ABSOLUTE_DIRECTION_OFFSETS.put(new HexCoord(1,2,0), 17);
		
		
		//  Load the absolute direction coords.
		ABSOLUTE_DIRECTION_COORDS = new ArrayList<HexCoord>();
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,0,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,1,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,0,1));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(-1,0,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,-1,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,0,-1));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(1,0,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,2,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,1,1));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,0,2));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(-1,0,1));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(-2,0,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(-1,-1,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,-2,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,-1,-1));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,0,-2));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(1,0,-1));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(2,0,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(1,1,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,3,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,2,1));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,1,2));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,0,3));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(-1,0,2));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(-2,0,1));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(-3,0,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(-2,-1,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(-1,-2,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,-3,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,-2,-1));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,-1,-2));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(0,0,-3));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(1,0,-2));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(2,0,-1));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(3,0,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(2,1,0));
		ABSOLUTE_DIRECTION_COORDS.add(new HexCoord(1,2,0));
		
		
		this.remainingTeams = numberOfTeams;	
		if (matchOptions != null){
			this.piecesPerTeam = matchOptions.getNumScouts() + matchOptions.getNumSnipers() + matchOptions.getNumTanks();		
		}
		else {
			this.piecesPerTeam = 3;
		}
		this.display = display;		
		this.loggerManager = new Logger(numberOfTeams);		
		this.matchOptions = matchOptions;		
		this.pieceCoords = new HexCoord[numberOfTeams*this.getPiecesPerTeam()];	
		this.currentPiece = 0;	
		this.initialized = false;
		
		this.teams = new Team[numberOfTeams];
		
		for (int i=0; i < numberOfTeams; i++){
			
			//  Initialize the team.
			this.teams[i] = new Team(this.getTeamFromOffset(i), this.matchOptions.getPlayers()[i].isAI(), this.matchOptions.getPlayers()[i], this);
			this.teams[i].setPlayerName(matchOptions.getPlayers()[i].getName());
		}
		
		//  Assign the size of the board.
		//  EXTENSION - this can be modified for multiple types of maps.
		switch (matchOptions.getMapName()){
			case "default":
				if (numberOfTeams == 2 || numberOfTeams == 3){
					sideLength = SMALLSIDELENGTH;
					break;
				}
				else {
					sideLength = LARGESIDELENGTH;
					break;
				}
			case "small":
				sideLength = SMALLSIDELENGTH;
				break;
			case "large":
				sideLength = LARGESIDELENGTH;
				break;
			default:
				throw new InvalidActivityException("Need to pick a small or large side length to construct a board.");
		}
		
		//  Depending on if it is a 2-player, 3-player, or 6-player game, initialize the starting position of each piece.
		//  EXTENSION - if new map types are defined, new cases may need to be defined.
		switch (sideLength){
		case 5:
			for (int i = 0; i < numberOfTeams*this.piecesPerTeam; i++){
				this.pieceCoords[i] = new HexCoord( SMALL_MAP_START_POSITIONS[this.getTeamFromOffset(i/piecesPerTeam).ordinal()].getX(),
													SMALL_MAP_START_POSITIONS[this.getTeamFromOffset(i/piecesPerTeam).ordinal()].getY(),
													SMALL_MAP_START_POSITIONS[this.getTeamFromOffset(i/piecesPerTeam).ordinal()].getZ());
				
			}
			break;
		case 7:
			for (int i = 0; i < numberOfTeams*this.piecesPerTeam; i++){
				this.pieceCoords[i] = new HexCoord( LARGE_MAP_START_POSITIONS[this.getTeamFromOffset(i/piecesPerTeam).ordinal()].getX(),
													LARGE_MAP_START_POSITIONS[this.getTeamFromOffset(i/piecesPerTeam).ordinal()].getY(),
													LARGE_MAP_START_POSITIONS[this.getTeamFromOffset(i/piecesPerTeam).ordinal()].getZ());
			}
			break;
		default:
			throw new InvalidActivityException("Error in Board: unknown side length.");
		}
		
		//  Set the initial facing direction of each piece for each team.
		//  The absolute direction conveniently matches the TeamEnum's ordinal.
		for (int i = 0; i < numberOfTeams; i++){
			int dir = this.getTeamFromOffset(i).ordinal();
			if (dir >= 0 && dir < 6){
				for (int j = 0; j < this.piecesPerTeam; j++){
					this.getTeams()[i].getPiece(j).rotate(dir);
				}
			}
			else {
				System.out.println("Illegal team ordinal in board initialization.");
			
			}
		}
		

	}
	
	public boolean getInitialized(){
		return this.initialized;
	}
	
	/** Initializes the team's AI programs, if any exist.
	 * @postcondition The AI programs have been initialized.
	 */
	public void initialize(){
		for (int i = 0; i < this.getTeams().length; i++){
			this.getTeams()[i].initAI();
		}
		this.initialized = true;
	}
	
	/**
	 * Method for handling death flags for each team, for animation purposes.
	 * @postcondition The processDeathFlags() method has been called for each team.
	 */
	public void processDeathFlags(){
		for (int i = 0; i < this.getTeams().length; i++){
			this.getTeams()[i].processDeathFlags();
		}
	}
	
	
	/** Moves the current playing piece to the specified coordinate
	 * @param piece
	 * @param coord
	 * @precondition The specified coordinate must exist in the range. 
	 */
	public void movePiece(HexCoord coord){
		
		//  Get the current piece's current coordinate
		HexCoord currentCoord = this.getPieceCoords()[this.getCurrentPiece()];

		//  Get the current piece's Piece values
		Piece movingPiece = this.getPieceFromOffset(this.getCurrentPiece());
		//  If you have the budget to move to the requested coordinate, move!
		if ( ! this.isValidAction(EntryEnum.MOVEMENT, coord)){
			return;
		}
		
		//  move - change coordinates
		this.getPieceCoords()[this.getCurrentPiece()] = coord;
		//  update the movement of the piece, and the direction.
		movingPiece.updateMove(this.shortestDistance(currentCoord, coord), movingPiece.getRelativeRotation(this.absoluteDirection(movingPiece, currentCoord, coord)));

		//  tell the logger about this.
		//  A list of all the witness piece indices.
		//  The witnesses who saw the departure.
		List<Integer> witnesses = this.getWitnesses(currentCoord);
		//  Create the log entry that is going to go to all the witnesses
		Entry entryDeparture = new Entry(EntryEnum.MOVEMENT);
		//  For each witness, add the entry into their team's log.
		entryDeparture.setMovementFrom(this.getTeamFromOffset(this.getCurrentPiece()/this.getPiecesPerTeam()), this.getPieceType(getCurrentPiece()), currentCoord);
		for (Integer witness : witnesses){
			this.getLoggerManager().getTeamLogs()[witness/this.getPiecesPerTeam()].addEntry(entryDeparture);
		}
		this.getLoggerManager().getTeamLogs()[this.getTeams().length].addEntry(entryDeparture);
		
		//  The witnesses who saw the arrival.
		witnesses = this.getWitnesses(coord);
		//  Create the log entries that are going to go to all the witnesses
		Entry entryArrival = new Entry(EntryEnum.MOVEMENT);
		//  For each witness, add the entry into their team's log.
		entryArrival.setMovementTo(this.getTeamFromOffset(this.getCurrentPiece()/this.getPiecesPerTeam()), this.getPieceType(getCurrentPiece()), coord);
		for (Integer witness : witnesses){
			this.getLoggerManager().getTeamLogs()[witness/this.getPiecesPerTeam()].addEntry(entryArrival);
		}
		this.getLoggerManager().getTeamLogs()[this.getTeams().length].addEntry(entryArrival);
		
		
		//  tell the display about this.
		this.display.getGameScreen().movePiece(this.getCurrentPiece(), currentCoord, this.displacement(currentCoord, coord), this.getPieceFromOffset(this.getCurrentPiece()).getRange(), this.getLivingTeamMembers(this.getCurrentPiece() / this.getPiecesPerTeam()), this.getVisibleRobotsForCurrentTeam());
		
	}

	/** Moves the current playing piece forward according to the direction it is facing.
	 * Called by the interpreter to move its pieces.
	 * @postcondition The current piece has moved forward one space if it is legal to do so.
	 */
	public void moveForward(){
		
		//  Get the current piece's current coordinate
		HexCoord currentCoord = this.getPieceCoords()[this.getCurrentPiece()];

		//  Get the current piece's Piece values
		Piece movingPiece = this.getPieceFromOffset(this.getCurrentPiece());
		
		Integer rotation = movingPiece.getAbsoluteRotation();
		
		HexCoord coord;
		
		//  Find the target HexCoord using the piece's rotation.
		switch (rotation){
		case 0:
			coord = new HexCoord(currentCoord.getX() + 1, currentCoord.getY(), currentCoord.getZ());
			break;
		case 1:
			coord = new HexCoord(currentCoord.getX(), currentCoord.getY() + 1, currentCoord.getZ());
			break;
		case 2:
			coord = new HexCoord(currentCoord.getX(), currentCoord.getY(), currentCoord.getZ() + 1);
			break;
		case 3:
			coord = new HexCoord(currentCoord.getX() - 1, currentCoord.getY(), currentCoord.getZ());
			break;
		case 4:
			coord = new HexCoord(currentCoord.getX(), currentCoord.getY() - 1, currentCoord.getZ());
			break;
		case 5:
			coord = new HexCoord(currentCoord.getX(), currentCoord.getY(), currentCoord.getZ() - 1);
			break;
		default:
			throw new RuntimeException("Invalid rotation in Board.moveForward().");
		
		
		}
		
		//  Move the piece to the specified coordinate.
		movePiece(coord);
		
	}
	
	/**
	 * This method determines whether a hex is a valid place to move for the active piece.
	 * 	/**This method determines whether a hex is a valid place to act for the active piece.
	 * @param action: The type of action requested.
	 * @param hex
	 * @return
	 */
	public boolean isValidAction(EntryEnum action, HexCoord hex) {
		
		Piece currentPiece = this.getPieceFromOffset(this.getCurrentPiece());
		
		if(!this.isInBounds(hex)){
			//System.out.println("Hex: " + hex.getX() + ", " + hex.getY() + ", " + hex.getZ());
			//This should never happen. Display takes care of this.
			System.out.println("Clicked hexagon is out of bounds.");
			return false;
		}
		else if(action.compareTo(EntryEnum.MOVEMENT) == 0){
			if (!hexIsWithinMovementRange(hex, this.getPieceFromOffset(this.getCurrentPiece()))){
				System.out.println("You don't have enough movement left.");
				return false;
			}
			if (!currentPiece.isAlive()){
				System.out.println("You are dead. You can't move, you zombie!");
				return false;
			}
		}
		else if (action.compareTo(EntryEnum.SHOOTING) == 0) {
			if (!this.hexIsWithinVisibleRange(hex, this.getPieceFromOffset(this.getCurrentPiece()))){
				System.out.println("You can't shoot where you can't see.");
				return false;
			}
			if (!currentPiece.isAlive()){
				System.out.println("You are dead. You can't shoot, you zombie!");
				return false;
			}
			if (currentPiece.getHasShot()){
				System.out.println("You have already had your turn to shoot.");
				return false;
			}
		}
		return true;
	}
	
	/** Method for checking if the hexagon is within the movement range of the current playing piece?
	 * @param hex: the target hexagon the player wishes to move to
	 * @param piece: the piece currently playing
	 * @return: true if the current piece can move to the intended hex coordinate.
	 */
	public boolean hexIsWithinMovementRange(HexCoord hex, Piece piece){
		HexCoord activePieceLocation = this.pieceCoords[this.getCurrentPiece()];
		return (this.shortestDistance(hex, activePieceLocation) <= piece.getCurrentMovement());
	}
	
	/**Calculates the shortest change in x,y, and z to get from one hexagon to the other.
	 * @param from: the starting hex coordinate.
	 * @param to: the ending hex coordinate.
	 * @return: A vector displacement hex coordinate.
	 */
	private HexCoord displacement(HexCoord from, HexCoord to){
		
		from.reduce();
		to.reduce();
		
		from.toVector();
		to.toVector();
		
		// vector is a vector for the displacement.
		HexCoord vector = new HexCoord(to.getX() - from.getX(), 
									  to.getY() - from.getY(),
									  to.getZ() - from.getZ());
		// Because +x + +z make one +y
		if (vector.getX()>0 && vector.getZ() >0){
			int minimumDifference = Math.min(vector.getX(), vector.getZ());
			vector.setX(vector.getX() - minimumDifference);
			vector.setY(vector.getY() + minimumDifference);
			vector.setZ(vector.getZ() - minimumDifference);
		}
		// Because -x and -z make one -y
		else if (vector.getX() < 0 && vector.getZ() < 0){
			int maximumDifference = Math.max(vector.getX(), vector.getZ());
			vector.setX(vector.getX() - maximumDifference);
			vector.setY(vector.getY() + maximumDifference);
			vector.setZ(vector.getZ() - maximumDifference);
		}
		else{
			//Should be done.
		}
		
		return vector;
		
	}
	
	/**
	 * Returns the absolute direction of a piece when it is at a hexagon and needs to face towards another hexagon
	 * @param piece: the piece whose direction is asked
	 * @param from: The starting position of the piece
	 * @param to: Where the piece needs to look.
	 * @return an integer signifying the direction: 0-17. 0 for East. and then clockwise for each subsequent number.
	 */
	private int absoluteDirection(Piece piece, HexCoord from, HexCoord to){
		//  provide the absolute minimum distance - vector form.
		HexCoord vector = this.displacement(from, to);
		
		int absoluteDir = 0;
		
		for (HexCoord key: this.ABSOLUTE_DIRECTION_OFFSETS.keySet()){
			if (vector.getX() == key.getX() && vector.getY() == key.getY() && vector.getZ() == key.getZ()){
				absoluteDir = this.ABSOLUTE_DIRECTION_OFFSETS.get(key);
			}
		}
		
		return absoluteDir;
		
	}
	
	/**
	 * Method for getting the relative direction value of a piece, called by the Interpreter so robots can scan properly.
	 * @param piece  - The piece calculating the direction
	 * @param from - The space the reference piece is occupying
	 * @param to - The space the reference piece wants the relative direction too.
	 * @return
	 */
	public int relativeDirection(Piece piece, HexCoord from, HexCoord to){
		int absolute = this.absoluteDirection(piece, from, to);
		
		int distance = this.shortestDistance(from, to);
		int relativeDistance = 0;
		switch (distance){
		case 0:
			return 0;
		case 1:
			relativeDistance = absolute - piece.getAbsoluteRotation();
			if (relativeDistance < 0){
				relativeDistance += 6;
			}
			return relativeDistance;
		case 2:
			relativeDistance = absolute - piece.getAbsoluteRotation() * 2;
			if (relativeDistance < 0){
				relativeDistance += 12;
			}
			return relativeDistance;
		case 3:
			relativeDistance = absolute - piece.getAbsoluteRotation() * 3;
			if (relativeDistance < 0){
				relativeDistance += 18;
			}
			return relativeDistance;
		default:
			System.out.println("Illegal distance supplied in board.relativeDirection()");
			return 0;
		}
		
	}
	
	
	/**This method is called when a player attempts to shoot at a hexagon space. 
	 * Reshooting in the same turn is not allowed, and shooting outside of the piece's range is not allowed.
	 * @param targetHex: the target hexagon that is intended to be shot
	 */
	public void shootSpace(HexCoord targetHex){
		//  Reduce the target hex coordinate.
		targetHex.reduce();
		
		//  Get the shooting piece from current piece
		Piece shootingPiece = this.getPieceFromOffset(this.getCurrentPiece());
		
		if (!this.isValidAction(EntryEnum.SHOOTING, targetHex)){
			return;
		}
		
		ArrayList<Integer> toHide = new ArrayList<Integer>();
		
		
		Integer totalDamageDealt = 0;
		List<Integer> numberEnemiesDefeated = new LinkedList<Integer>();
		
		
		//  Now that the piece's current position + the target position are less than the range, shoot!
		List<Integer> victims = this.scanSpace(targetHex);
		
		//  Code for hiding images, for animation purposes.
		toHide.addAll(victims);
		toHide.addAll(this.scanSpace(this.pieceCoords[this.getCurrentPiece()]));
		
		//  Deal damage to each of the victims.
		for (Integer victim : victims){	
			Piece victimPiece = this.getPieceFromOffset(victim);
			
			if (victimPiece.isAlive()){
				
				Integer damageTaken = Math.min(victimPiece.getCurrentHealth(), shootingPiece.getAttack());
				victimPiece.takeDamage(damageTaken);	
				totalDamageDealt += damageTaken;
				
				if(!victimPiece.isAlive()){
					numberEnemiesDefeated.add(victim);
					if (this.getTeams()[victim / this.piecesPerTeam].checkEliminated()){
						this.remainingTeams -= 1;
					}
				}
			}
		}
		// If a piece is destroyed, does it receive two logs? Damage taken and death?
		shootingPiece.updateShoot(totalDamageDealt, numberEnemiesDefeated.size());

		//  tell logger about this.
		
		//  A list of all the witness piece indices.
		//  The witnesses who saw the killer.
		List<Integer> witnesses = this.getWitnesses(pieceCoords[this.getCurrentPiece()]);
		
		//  Create the log entry that is going to go to all the witnesses
		Entry entryShooter = new Entry(EntryEnum.SHOOTING);
		//  For each witness, add the entry into their team's log.
		entryShooter.setShooting(this.getTeamFromOffset(this.getCurrentPiece()/this.getPiecesPerTeam()), this.getPieceType(getCurrentPiece()));
		
		for (Integer witness : witnesses){
			this.getLoggerManager().getTeamLogs()[witness/this.getPiecesPerTeam()].addEntry(entryShooter);
		}
		this.getLoggerManager().getTeamLogs()[this.getTeams().length].addEntry(entryShooter);
		
		//  The witnesses who saw the victims.
		witnesses = this.getWitnesses(targetHex);
		//  Create the log entries that are going to go to all the witnesses
		List<Entry> entriesInjured = new ArrayList<Entry>(victims.size());
		for (int i=0; i<entriesInjured.size(); i++){
			entriesInjured.add(i, new Entry(EntryEnum.DAMAGED));
			entriesInjured.get(i).setDamaged(this.getTeamFromOffset( victims.get(i) / this.getPiecesPerTeam()), this.getPieceType(victims.get(i)), shootingPiece.getAttack());
		}
		
		//Send the log entries to the witnesses
		for (Integer witness : witnesses){
			for (int i=0; i<entriesInjured.size(); i++){ 
				this.getLoggerManager().getTeamLogs()[witness.intValue()/this.getPiecesPerTeam()].addEntry(entriesInjured.get(i));
			}
		}


		for (int i=0; i<entriesInjured.size(); i++){
			this.getLoggerManager().getTeamLogs()[this.getTeams().length].addEntry(entriesInjured.get(i));
		}
		
		//tell the display about the shot.
		this.getDisplay().getGameScreen().hideForShot(toHide);
		this.getDisplay().getGameScreen().shootSpace(this.getPieceCoords()[this.getCurrentPiece()], targetHex);	
		
	}
	

	/**
	 * Shoot method called by the Interpreter using an AI program's distance and direction values for a space.
	 * @param distance - The shortest distance to the target space.
	 * @param direction - The relative direction to the target space.
	 * @postcondition The space has been shot, if the shot is legal.
	 */
	public void shootSpace(Integer distance, Integer direction){
		
		int index = 0;
		if (distance == 0){
			index = 0;
		}
		else if (distance == 1){
			index = 1 + direction;
		}
		else if (distance == 2){
			index = 7 + direction;
		}
		else {
			index = 19 + direction;
		}
		
		HexCoord current = this.getPieceCoords()[this.getCurrentPiece()];
		
		HexCoord targetHex = new HexCoord( current.getX() + this.ABSOLUTE_DIRECTION_COORDS.get(index).getX(), 
											current.getY() + this.ABSOLUTE_DIRECTION_COORDS.get(index).getY(),
											current.getZ() + this.ABSOLUTE_DIRECTION_COORDS.get(index).getZ());
		
		shootSpace(targetHex);
		
		
	}
	
	/**Receives the offset of a player, and returns which piece it is.
	 * @param order: the offset of a player
	 * @return the pieceEnum of that player
	*/
	private PieceEnum getPieceType(int order){
		order = order % this.getPiecesPerTeam();
		
		if (order < this.matchOptions.getNumScouts()){
			return PieceEnum.SCOUT;
		}
		// if order is between numScouts and numScout+numSniper;
		else if (order < (this.matchOptions.getNumScouts() + this.matchOptions.getNumSnipers())){
			return PieceEnum.SNIPER;
		}
		// if order is between numSniper and numScout+numSniper+numTanks;
		else if (order < (this.matchOptions.getNumScouts() + this.matchOptions.getNumSnipers() + this.matchOptions.getNumTanks())){
			return PieceEnum.TANK;
		}
		
		return PieceEnum.TANK;
	}
	
	/**Returns a list of all the pieces that can see a hexCoord.
	 * @param eventCoord: the coordinate of interest.
	 * @return list of all the pieces that can see the eventCoord hexCoord.
	 */
	private List<Integer> getWitnesses(HexCoord eventCoord){
		
		List<Integer> witnesses = new LinkedList<Integer>();
		
		// for each piece, if the piece is alive
		// and the piece can see the event hexCoord,
		// then the piece is a witness
		for (int i = 0; i < (this.getTeams().length * this.getPiecesPerTeam()); i++){
			Piece potentialWitness = this.getPieceFromOffset(i);
			if (potentialWitness.isAlive()){
				if (this.hexIsWithinVisibleRange(eventCoord, potentialWitness)){
					witnesses.add(i);
				}
			}
		}
				
		return witnesses;
	}
	
	
	/**
	 * Helper method for checking which robots a given piece can see.
	 * Called by the Display to implement the fog of war.
	 * @param offset The piece doing the visibility checking.
	 * @return A list of the offsets of visible robots.
	 */
	public ArrayList<Integer> getVisibleRobotsToPiece(int offset){
		ArrayList<Integer> visible = new ArrayList<Integer>();
		
		// for each piece, if the piece is alive
		// and the piece can see the event hexCoord,
		// then the piece is a witness
		for (int i = 0; i < (this.getTeams().length * this.getPiecesPerTeam()); i++){
			if (this.isVisibleToPiece(offset, i)){
				visible.add(i);
			}
		}

		return visible;
	}
	
	/**
	 * A method that gets the set of visible robots to a team.
	 * Called by the Display to implement the fog of war.
	 * @return A list of the offsets of visible robots.
	 */
	public ArrayList<Integer> getVisibleRobotsForCurrentTeam(){
		ArrayList<Integer> visible = new ArrayList<Integer>();
		
		for (int i = (this.getCurrentPiece() / this.piecesPerTeam) * this.piecesPerTeam; i < (this.getCurrentPiece() / this.piecesPerTeam)* this.piecesPerTeam + this.piecesPerTeam; i++){
			visible.addAll(this.getVisibleRobotsToPiece(i));
		}
		return visible;
		
	}
	
	/**
	 * Returns the Piece from the index, whether it is alive, or not.
	 * @param offset The offset to investigate
	 * @return The corresponding piece.
	 */
	public Piece getPieceFromOffset(int offset){
		if ((offset >= this.getPiecesPerTeam()*this.getTeams().length) || offset <0){
			throw new IndexOutOfBoundsException("offset is out of limit");
		}
		int teamNumber = offset/this.getPiecesPerTeam();
		int pieceNumber = offset % this.getPiecesPerTeam();
		return this.getTeams()[teamNumber].getPiece(pieceNumber);
	}
	
	/**
	 * Method called by the GameManager to check how many teams are still alive.
	 * @return The value of this.remainingTeams.
	 */
	public int getRemainingPlayers(){
		return this.remainingTeams;
	}
	
	
	/**Recursively calculates the index of the next alive piece bound to play.
	 * @param currentPieceIndex
	 * @return the index of the next player bound to play.
	 */
	private int nextPieceHelper(int currentPieceIndex){
		
		int nextPotential = (currentPieceIndex + this.getPiecesPerTeam()) % (this.getTeams().length * this.getPiecesPerTeam());
		
		//If the current piece is in the last team, change the next round's piece.
		if (currentPieceIndex > (this.getTeams().length * this.getPiecesPerTeam()) - (this.getPiecesPerTeam()+1)){
			nextPotential = (nextPotential+1)%this.piecesPerTeam;
			resetRound();
		}
		
		
		
		if (!this.getPieceFromOffset(nextPotential).isAvailableForTurn()){
			return nextPieceHelper(nextPotential);
		}
		else{
			return nextPotential;
		}
		
	}
	
	/**This method simply determines and returns the offset of next piece that is to play in the game.
	 * @param int pieceIndex: the index of the piece for which you want to know the next player index.
	 */
	public int nextPiece(int pieceIndex){
		return nextPieceHelper(pieceIndex);
	}
	
	/**
	 * Is the hex visible to the piece?
	 * @param hex: The intended hexagon
	 * @param piece: The piece who wants to check
	 * @return true if the hex is visible, false otherwise.
	 */
	public boolean hexIsWithinVisibleRange(HexCoord hex, Piece piece){
		HexCoord activePieceLocation = this.pieceCoords[this.getCurrentPiece()];	
		return (this.shortestDistance(hex, activePieceLocation) <= piece.getRange());
	}
	
	
	/**
	 * Method for checking whether one piece can see another.
	 * Used to implement the fog of war.
	 * @param current The current piece.
	 * @param offset The piece to investigate
	 * @return true if the piece is visible and alive, false otherwise.
	 */
	public boolean isVisibleToPiece(int current, int offset){
		
		HexCoord pieceLocation = this.pieceCoords[offset];
		return (this.shortestDistance(this.pieceCoords[current], pieceLocation) <= this.getPieceFromOffset(current).getRange()) && this.getPieceFromOffset(offset).isAlive();
	}
	
	/**This method takes in the enumeration that represents the piece that is performing the scan operation 
	 * and the target coordinate of the hexagon space, which must be a valid coordinate, that is being scanned and then 
	 * returns a list of HexCoord elements representing the coordinates that are determined to be occupied.
	 * @param scanningPieceType
	 * @param scanLocation
	 * @return a list of HexCoord elements representing the coordinates that are determined to be occupied.
	 * @throws InvalidParameterException if the location is outside of the board.
	 */
	public List<HexCoord> scanArea(PieceEnum scanningPieceType, HexCoord scanLocation){
		if (!isInBounds(scanLocation)){
			throw new InvalidParameterException("Coordinate is not in the bounds of the board.");
		}
		
		Piece tempPiece = new Piece(scanningPieceType);
		List<HexCoord> occupiedCoords = new LinkedList<HexCoord>();
		
		for (HexCoord coord: this.getPieceCoords()){
			if (this.hexIsWithinVisibleRange(scanLocation, tempPiece)){
				occupiedCoords.add(coord);
			}
		}
		return occupiedCoords;		
	}

	/** Scans the area around the current piece
	 * @return all the coordinates of pieces that it can see from its range.
	 */
	public List<HexCoord> scanCurrentArea(){
		// Find what type the current piece is - whether it is a scout, sniper or tank.
		// Call the scanArea function to do the work!
		return scanArea(this.getPieceType(this.getCurrentPiece()), this.getPieceCoords()[this.getCurrentPiece()]);
	}

	/**
	 * This method takes in one coordinate of a hexagon space, which must be valid coordinate, 
	 * and returns a list of pieces determined to be occupying the space.
	 * @param coord
	 * @return List of Pieces' offset that occupy the space
	 * @throws InvalidParameterException
	 */
	public LinkedList<Integer> scanSpace(HexCoord coord){
		if (!isInBounds(coord)){
			throw new InvalidParameterException("Coord is not in the bounds of the board");
		}

		// Create a new list called habitants to store all the pieces at the hexCoord
		LinkedList<Integer> habitants = new LinkedList<Integer>();
		//Loop through and add all the habitants
		for (int i = 0; i<this.getPieceCoords().length; i++){
			if (coord.isSameAs(this.getPieceCoords()[i])){
				Piece potentialHabitant = this.getPieceFromOffset(this.getCurrentPiece());
						if (potentialHabitant.isAlive()){
							habitants.add(i);
						}
			}
		}	
		return habitants;
	}
	
	/**
	 * An overloaded version of scanArea() for use in the Interpreter.
	 * @return
	 */
	public LinkedList<Integer> scanArea(){
		LinkedList<Integer> result = new LinkedList<Integer>();
		List<HexCoord> occupied = this.scanCurrentArea();
		
		for (HexCoord hex : occupied){
			result.addAll(scanSpace(hex));
		}
		return result;
	}
	
	/**
	 * A method used by the Interpreter to check the status of an adjacent space.
	 * @param relativeDir
	 * @return One of three values describing the state of a space.
	 */
	public String checkSpace(Integer relativeDir){
		String result = "";
		
		HexCoord currentCoord = this.getPieceCoords()[this.getCurrentPiece()];
		HexCoord coord;
		Integer rotation = this.getPieceFromOffset(this.getCurrentPiece()).getAbsoluteRotation(relativeDir);
		
		switch (rotation){
		case 0:
			coord = new HexCoord(currentCoord.getX() + 1, currentCoord.getY(), currentCoord.getZ());
			break;
		case 1:
			coord = new HexCoord(currentCoord.getX(), currentCoord.getY() + 1, currentCoord.getZ());
			break;
		case 2:
			coord = new HexCoord(currentCoord.getX(), currentCoord.getY(), currentCoord.getZ() + 1);
			break;
		case 3:
			coord = new HexCoord(currentCoord.getX() - 1, currentCoord.getY(), currentCoord.getZ());
			break;
		case 4:
			coord = new HexCoord(currentCoord.getX(), currentCoord.getY() - 1, currentCoord.getZ());
			break;
		case 5:
			coord = new HexCoord(currentCoord.getX(), currentCoord.getY(), currentCoord.getZ() - 1);
			break;
		default:
			throw new RuntimeException("Invalid rotation in Board.moveForward().");

		}
		
		if (!this.isInBounds(coord)){
			result = "OUT OF BOUNDS";
		}
		else {
			if (this.scanSpace(coord).size() == 0){
				result = "EMPTY";
			}
			else {
				result = "OCCUPIED";
			}
		}
		
		return result;
	}
	
	/**
	 * A method for getting piece info from an offset, used by the Interpreter.
	 * @return result A list containing the parameters for the interpreter: the piece's current health, its distance and direction, and its colour.
	 */
	public ArrayList<String> getPieceInfoFromOffset(Integer offset){
		Piece targetPiece = this.getPieceFromOffset(offset);
		ArrayList<String> result = new ArrayList<String>();
		
		result.add("" + targetPiece.getCurrentHealth());
		result.add("" + this.shortestDistance(this.getPieceCoords()[this.getCurrentPiece()], this.getPieceCoords()[offset]));
		
		result.add("" + this.relativeDirection(targetPiece, this.getPieceCoords()[this.getCurrentPiece()], this.getPieceCoords()[offset]));	
		result.add("" + this.getTeams()[offset/this.piecesPerTeam].getColour().toString());
		
		return result;
	}
	

	/**
	 * Give two hexCoords, this method figures out the shortestDistance (minimum number of hex-Coords) between the two hexCoords.
	 * @param coord1
	 * @param coord2
	 * @return an integer which represents the shortest hex-distance between the two coordinates.  
	 */
	public int shortestDistance(HexCoord coord1, HexCoord coord2){
		
		coord1.reduce();
		coord2.reduce();
		//  If the two coords are along the same x-axis or z-axis : Easy.
		if (coord1.getX() == coord2.getX()){
			return Math.abs(coord1.getZ() - coord2.getZ());
		}
		if (coord1.getZ() == coord2.getZ()){
			return Math.abs(coord1.getX() - coord2.getX());
		}
		
		HexCoord vector = this.displacement(coord1, coord2);

		//  Return the magnitude of this vector.
		return Math.abs(vector.getX()) + Math.abs(vector.getY()) + Math.abs(vector.getZ());
		
	}
	
	/**Checks whether a coordinate is within the bounds of the board coordinates
	 * @param coord
	 * @return boolean: whether the coordinate is within the bounds of the board
	 * @postcondition the coordinate is reduced to the standard form
	 */
	public boolean isInBounds(HexCoord coord){
	
		coord.reduce();
		
		//  Note: I could have reduced the following code to a few lines. But using switch cases is more readable and understandable than voodoo.
		if (this.getSideLength() == 5){
			switch (coord.getX()){

				case -4: 
					if (coord.getZ() >= -4 && coord.getZ() <= 0){
						return true;
					}
					break;
			
				case -3: 
					if (coord.getZ() >= -4 && coord.getZ() <= 1){
						return true;
					}
					break;
			
				case -2: 
					if (coord.getZ() >= -4 && coord.getZ() <= 2){
						return true;
					}
					break;
			
				case -1: 
					if (coord.getZ() >= -4 && coord.getZ() <= 3){
						return true;
					}
					break;
			
				case 0: 
					if (coord.getZ() >= -4 && coord.getZ() <= 4){
						return true;
					}
					break;
			
				case 1: 
					if (coord.getZ() >= -3 && coord.getZ() <= 4){
						return true;
					}
					break;
			
				case 2: 
					if (coord.getZ() >= -2 && coord.getZ() <= 4){
						return true;
					}
					break;
			
				case 3: 
					if (coord.getZ() >= -1 && coord.getZ() <= 4){
						return true;
					}
					break;
			
				case 4: 
					if (coord.getZ() >= 0 && coord.getZ() <= 4){
						return true;
					}
					break;
			
				default: 
					return false;

			}
		}
		else if (this.getSideLength() == 7){
			switch (coord.getX()){
				case -6: 
					if (coord.getZ() >= -6 && coord.getZ() <= 0){
					return true;
					}
					break;
	
				case -5: 
					if (coord.getZ() >= -6 && coord.getZ() <= 1){
					return true;
					}
					break;
	
				case -4: 
					if (coord.getZ() >= -6 && coord.getZ() <= 2){
						return true;
					}
					break;
		
				case -3: 
					if (coord.getZ() >= -6 && coord.getZ() <= 3){
						return true;
					}
					break;
		
				case -2: 
					if (coord.getZ() >= -6 && coord.getZ() <= 4){
						return true;
					}
					break;
		
				case -1: 
					if (coord.getZ() >= -6 && coord.getZ() <= 5){
						return true;
					}
					break;
		
				case 0: 
					if (coord.getZ() >= -6 && coord.getZ() <= 6){
						return true;
					}
					break;
		
				case 1: 
					if (coord.getZ() >= -5 && coord.getZ() <= 6){
						return true;
					}
					break;
		
				case 2: 
					if (coord.getZ() >= -4 && coord.getZ() <= 6){
						return true;
					}
					break;
		
				case 3: 
					if (coord.getZ() >= -3 && coord.getZ() <= 6){
						return true;
					}
					break;
		
				case 4: 
					if (coord.getZ() >= -2 && coord.getZ() <= 6){
						return true;
					}
					break;
					
				case 5: 
					if (coord.getZ() >= -1 && coord.getZ() <= 6){
						return true;
					}
					break;

				case 6: 
					if (coord.getZ() >= 0 && coord.getZ() <= 6){
						return true;
					}
					break;

				default: 
					return false;

			}

		}	
		return false;
	}

	public HexCoord[] getPieceCoords() {
		return pieceCoords;
	}

	public void setPieceCoords(HexCoord[] pieceCoords) {
		this.pieceCoords = pieceCoords;
	}

	
	//  EXTENSION - These parameters and functions can be used to implement dead spaces (holes, obstacles) on the map.
//	public HexCoord[] getBoardBounds() {
//		return boardBounds;
//	}
//
//	public void setBoardBounds(HexCoord[] boardBounds) {
//		this.boardBounds = boardBounds;
//	}

	public Team[] getTeams() {
		return teams;
	}


	public int getCurrentPiece() {
		//  If the value of currentPiece was modified to something illegal for array indexing, output an error message
		if (currentPiece >= this.getTeams().length * this.getPiecesPerTeam() || currentPiece < 0){
			System.out.println("Error in Board.getCurrentPiece(): illegal index for currentPiece: " + this.currentPiece);
		}
		return currentPiece;
	}

	public void setCurrentPiece(int currentPiece) {
		this.currentPiece = currentPiece % (this.getTeams().length * this.getPiecesPerTeam());
	}

	public Logger getLoggerManager() {
		return loggerManager;
	}


	public Display getDisplay() {
		return display;
	}

	public int getPiecesPerTeam() {
		return piecesPerTeam;
	}
	

	public int getSideLength() {
		return this.sideLength;
	}
	

	public void resetRound(){
		for (int i = 0; i < this.getTeams().length; i++){
			this.getTeams()[i].resetRound();
		}
	}
	
	
	
	/**
	 * Method that improves code modularity: gets the correct colour enum from a team offset integer.
	 * @param offset The nth team in this match.
	 * @return TeamEnum corresponding to the colour of the nth team.
	 */
	
	public TeamEnum getTeamFromOffset(int offset){
		if (offset >= this.teams.length || offset < 0){

			if (offset == this.getTeams().length){

				System.out.println("Error in Board: offset out of range for number of teams in getTeamFromOffset(). Possibly a modding problem. Offset:" + offset);

			}

			else{

				System.out.println("Error in Board: offset out of range for number of teams in getTeamFromOffset(). Offset:" + offset);

			}

		}
		switch (this.teams.length){
		case 2:
			return TeamEnum.values()[offset*3];
		case 3:
			return TeamEnum.values()[offset*2];
			
		case 6:
			return TeamEnum.values()[offset];
		default:
			throw new RuntimeException("Illegal number of teams on Board.");
		}
	}
	
	/**
	 * Method that improves code modularity: gets the correct team offset integer from a colour.
	 * @param colour The colour of the team to examine.
	 * @return Integer corresponding to the turn order of the team colour
	 */
	
	public int getOffsetFromTeam(TeamEnum colour){
		switch (colour){
		case GREEN:
			if (this.teams.length == 2){
				return 1;
			}
			else {
				return colour.ordinal();
			}
		case YELLOW:
			if (this.teams.length == 3){
				return 1;
			}
			else {
				return colour.ordinal();
			}
		case BLUE:
			if (this.teams.length == 3){
				return 2;
			}
			else {
				return colour.ordinal();
			}
		default:
			return colour.ordinal();
		}
	}
	
	public HashMap<Integer, Integer> getLivingTeamMembers(int teamOffset){
		HashMap<Integer, Integer> teamMembers = new HashMap<>();
		for (int i = (teamOffset * this.piecesPerTeam); i < (teamOffset * this.piecesPerTeam) + this.piecesPerTeam; i++){
			if (this.getPieceFromOffset(i).isAlive()){
				teamMembers.put(i, this.getPieceFromOffset(i).getRange());
			}
		}
		return teamMembers;
	}
	
	
	public MatchOptions getMatchOptions(){
		return this.matchOptions;
	}
	
	
	public static void main(String[] args) throws InvalidActivityException{
				
		//  Tests for the Board class.
		
		Board b = null;
		b = new Board(6, null, null);
		
		if (!b.isInBounds(new HexCoord (0, 0, 0))){
			System.out.println("Hexcoord isn't in bounds when it should be.");
		};
		
		if (!b.isInBounds(new HexCoord (2, 0, 1))){
			System.out.println("Hexcoord isn't in bounds when it should be.");
		};
		
		if (!b.isInBounds(new HexCoord (4, -1, 4))){
			System.out.println("Hexcoord isn't in bounds when it should be.");
		};
		
		if (b.isInBounds(new HexCoord (4, 0, 6))){
			System.out.println("Hexcoord is in bounds when it shouldn't be.");
		};

		if (b.isInBounds(new HexCoord (10, 10, 10))){
			System.out.println("Hexcoord is in bounds when it shouldn't be.");
		};
		
		if (b.isInBounds(new HexCoord (4, 1, 0))){
			System.out.println("Hexcoord is in bounds when it shouldn't be.");
		};
		
		
		if (b.isInBounds(new HexCoord (0, 0, -5))){
			System.out.println("Hexcoord is in bounds when it shouldn't be.");
		};
		
		if (b.isInBounds(new HexCoord (-5, 0, 0))){
			System.out.println("Hexcoord is in bounds when it shouldn't be.");
		};
		
		
		int minDistance = b.shortestDistance(new HexCoord(-1, 0, -2), new HexCoord(0, 0, -2));
		if (minDistance != 1 ){
			System.out.println("Incorrect shortest Distance. Calculated answer:" + minDistance);
		}
		
		minDistance = b.shortestDistance(new HexCoord(0, 0, -2), new HexCoord(1, 0, -1));
		if (minDistance != 1 ){
			System.out.println("Incorrect shortest Distance. Calculated answer:" + minDistance);
		}
		
		minDistance = b.shortestDistance(new HexCoord(-2, 0, 2), new HexCoord(2, 0, -2));
		if (minDistance != 8){
			System.out.println("Incorrect shortest Distance. Calculated answer:" + minDistance);
		}
		
		minDistance = b.shortestDistance(new HexCoord(-4, 0, -2), new HexCoord(3, 0, 2));
		if (minDistance != 7 ){
			System.out.println("Incorrect shortest Distance. Calculated answer:" + minDistance);
		}
		
		minDistance = b.shortestDistance(new HexCoord(0, 0, 0), new HexCoord(0, 0, 0));
		if (minDistance != 0 ){
			System.out.println("Incorrect shortest Distance. Calculated answer:" + minDistance);
		}
		
		minDistance = b.shortestDistance(new HexCoord(0, 0, -4), new HexCoord(0, 0, 4));
		if (minDistance != 8 ){
			System.out.println("Incorrect shortest Distance. Calculated answer:" + minDistance);
		}
		
		minDistance = b.shortestDistance(new HexCoord(-1, 0, -1), new HexCoord(1, 0, 2));
		if (minDistance != 3 ){
			System.out.println("Incorrect shortest Distance. Calculated answer:" + minDistance);
		}
		
		
		System.out.println("Tests complete.");
	}

}
