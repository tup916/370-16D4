/*	MenuManager.java
 * 	Created by: Tushita Patel
 * 	Purpose: Handles the flow of inputs of the match and stores regular settings
 * 	Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 * 	11/11/2016	-	[Tushita] Create the class and set up all fields and methods 
 */

package robowars.flow;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import javax.activity.InvalidActivityException;
import javax.swing.AbstractAction;

import robowars.board.Board;
import robowars.board.HexCoord;
import robowars.display.Display;
import robowars.display.ScreenEnum;
import robowars.display.TurnTransitionScreen;
import robowars.pieces.Piece;
import robowars.pieces.Team;

public class GameManager {

	
  	@SuppressWarnings("unused")
	private EventCatcher catcher;
	
	private Display display;
	
	private Board board;
	
	
	public MatchOptions matchOptions;
	
	private Settings settings;
	
	
	private HexCoord activeHex;
	
	//	private Team currentTeam;
	
	public GameManager(EventCatcher ec, Display display, MatchOptions options, Settings settings){
		this.display = display;
		this.catcher = ec;
		this.matchOptions = options;
		this.settings = settings;
		
		activeHex = new HexCoord(0,0,0);
		this.createActions();
		createBoard(this.matchOptions);
	}
	
	private HashMap<String, AbstractAction> actions;
	
	/**
	 * Initialize the game board for the match.
	 * @param matchOptions The options for the match to be used in creating the board.
	 */
	public void createBoard(MatchOptions matchOptions) {
		
		try {
			
			//  Initialize the game board.
			board = new Board(matchOptions.getNumPlayers(), this.display, matchOptions);
			
			
		} catch (InvalidActivityException e) {
			System.out.println("GameManager can't initialize Board: " + e.getMessage());
			display.switchTo(ScreenEnum.TITLE);
			
		}
		
	}
	
	public HashMap<String, AbstractAction> getActions(){
		return this.actions;
	}
	
	/**
	 * Method for creating the move/shoot/inspect/cancel button actions.
	 * @postcondition - The actions have been initialized.
	 */
	@SuppressWarnings("serial")	
	public void createActions(){
		actions = new HashMap<>();
		actions.put("endTurn", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				
				//  Hide stats from other teams.
				Team[] teams = board.getTeams();
				for (Team team : teams) {
					int teamIndex = board.getOffsetFromTeam(team.getColour());
					display.getGameScreen().hideStats(teamIndex);
				}

				//  Enable buttons at the beginning
				actions.get("move").setEnabled(true);
				actions.get("shoot").setEnabled(true);

				if (board.getInitialized() == true){
					System.out.println("GameScreen - End Turn");
					//  Set the piece's turn to have ended
					System.out.println("Ending piece: " + board.getCurrentPiece());
				
					display.getGameScreen().getBoardPanel().getContextMenu().setVisible(false);
					board.getPieceFromOffset(board.getCurrentPiece()).endTurn();
				}
				display.switchTo(ScreenEnum.TURNTRANSITION);
				

				
				if (board.getRemainingPlayers() == 0){
					((TurnTransitionScreen)display.getCurrentScreen()).showVictoryMessage("", true);
					//  EXTENSION - The logger's output can be passed to the results screen here.
					//board.getLoggerManager().printLogger();
					
					// TODO load the results into the results screen, then...
					
					//DEMO ONLY!! Go back to title instead of results screen.
					
					display.switchTo(ScreenEnum.TITLE);
					return;
					
				}
				else if (board.getRemainingPlayers() == 1){
					
					board.setCurrentPiece(board.nextPiece(board.getCurrentPiece()));
					
					((TurnTransitionScreen)display.getCurrentScreen()).showVictoryMessage(
							board.getTeams()[board.getCurrentPiece() / board.getPiecesPerTeam()].getPlayerName(), false);
					
					//  EXTENSION - The logger's output can be passed to the results screen here.
					//board.getLoggerManager().printLogger();
					
					// TODO load the results into the results screen, then...
					
					// DEMO ONLY!! Go back to title instead of results screen
					
					display.switchTo(ScreenEnum.TITLE);
					return;
				}
				
				if (board.getInitialized()){
					board.setCurrentPiece(board.nextPiece(board.getCurrentPiece()));
				}
				else {
					board.initialize();
				}
				
				display.getGameScreen().reloadForCurrentPiece(board.getCurrentPiece(), board.getPieceFromOffset(board.getCurrentPiece()).getRange(), board.getLivingTeamMembers(board.getCurrentPiece() / board.getPiecesPerTeam()), board.getVisibleRobotsForCurrentTeam());
				
				
				//  Show the stats of current piece.
				Piece piece = board.getPieceFromOffset(board.getCurrentPiece());
				Team currentTeam = board.getTeams()[board.getCurrentPiece() / board.getPiecesPerTeam()];
				int teamIndex = board.getOffsetFromTeam(currentTeam.getColour());
				
				display.getGameScreen().setCurrentStats(teamIndex, board.getCurrentPiece(), 
						currentTeam.getPlayerName(), piece.getAttack(), piece.getCurrentMovement(),
						piece.getCurrentHealth(), piece.getRange());
				
				
				
				
				boolean isAI = board.getTeams()[board.getCurrentPiece() / board.getPiecesPerTeam()].isAI();
				((TurnTransitionScreen)display.getCurrentScreen()).showMessagePlayerMessage(board.getTeams()[board.getCurrentPiece() / board.getPiecesPerTeam()].getPlayerName(), isAI);
				if (isAI){
					board.getTeams()[board.getCurrentPiece() / board.getPiecesPerTeam()].playAI(board.getCurrentPiece() % board.getPiecesPerTeam());
					this.actionPerformed(e);
				}
				else {
					display.switchTo(ScreenEnum.GAME);
				}
			}
			
		});
		actions.put("move", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("GameScreen - Move");
				display.getGameScreen().getBoardPanel().getContextMenu().setVisible(false);
				board.movePiece(display.getGameScreen().getBoardPanel().getActiveHex());
				
				display.getGameScreen().reloadForCurrentPiece(board.getCurrentPiece(), 
						board.getPieceFromOffset(board.getCurrentPiece()).getRange(), 
						board.getLivingTeamMembers(board.getCurrentPiece() / board.getPiecesPerTeam()), 
						board.getVisibleRobotsForCurrentTeam());
				
				//  Disable move button when no movement points left
				if (board.getPieceFromOffset(board.getCurrentPiece()).getCurrentMovement() == 0) {
					actions.get("move").setEnabled(false);
				}
							
			}
			
		});
		actions.put("shoot", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("GameScreen - Shoot");
				
				display.getGameScreen().getBoardPanel().getContextMenu().setVisible(false);
				board.shootSpace(display.getGameScreen().getBoardPanel().getActiveHex());
				try {
					Thread.sleep(1100);
				} catch (InterruptedException e1) {
				}
				board.processDeathFlags();
				display.getGameScreen().reloadForCurrentPiece(board.getCurrentPiece(), 
						board.getPieceFromOffset(board.getCurrentPiece()).getRange(), 
						board.getLivingTeamMembers(board.getCurrentPiece() / board.getPiecesPerTeam()), 
						board.getVisibleRobotsForCurrentTeam());
				
				//  Disable shoot button after one shot
				//  EXTENSION - Bug fix: if the shot was not successful, don't disable the button.
				actions.get("shoot").setEnabled(false);
				
				if (board.getTeams()[board.getCurrentPiece() / board.getPiecesPerTeam()].isEliminated()) {
					display.getGameScreen().getBoardPanel().showDefeatMessage(board.getTeams()
							[board.getCurrentPiece() / board.getPiecesPerTeam()].getPlayerName());
					actions.get("endTurn").actionPerformed(e);
				} else if (board.getRemainingPlayers() == 1) {
					actions.get("endTurn").actionPerformed(e);
				}
			}
			
		});
		actions.put("inspect", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("GameScreen - Inspect");
				//  EXTENSION - Inspect function has not been implemented. Would display the stats of all robots in a particular space.
				display.getGameScreen().getBoardPanel().getContextMenu().setVisible(false);

			}
			
		});
		actions.put("cancel", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("GameScreen - Cancel");
				//  cancels out the menu
				display.getGameScreen().getBoardPanel().getContextMenu().setVisible(false);
			}
			
		});
	}
	
	public Board getBoard(){
		return this.board;
	}
	
	public Settings getSettings(){
		return this.settings;
	}
	
	public HexCoord getActiveHex(){
		return this.activeHex;
	}
	
	public void setActiveHex(HexCoord hex){
		activeHex = hex;
	}
	
	/**
	 * Debug method used to test board functionality.
	 */
	@SuppressWarnings("unused")
	private void killAllPiecesOfActivePlayer() {
		Integer activeTeam = this.getBoard().getTeamFromOffset(this.getBoard().getCurrentPiece()).ordinal();
		Integer numberOfPiecesPerTeam = this.getBoard().getPiecesPerTeam();
		Integer startingPiece = activeTeam * numberOfPiecesPerTeam; 
		
		for(int i = startingPiece; i < (startingPiece + numberOfPiecesPerTeam); i++){
			Integer pieceOffset = i;
			Integer pieceHealthLeft = this.getBoard().getPieceFromOffset(pieceOffset).getCurrentHealth();
			this.getBoard().getPieceFromOffset(pieceOffset).takeDamage(pieceHealthLeft);
		}
		
	}
	

}
