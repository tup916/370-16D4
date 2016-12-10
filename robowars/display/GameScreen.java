/*  GameScreen.java
 *  Created by: Nickolas Gough
 *  Purpose: Models the game Screen.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  20/07/2016 - Nickolas Gough : Created the file and began implementing the basics. 
 */

package robowars.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import robowars.board.HexCoord;
import robowars.flow.GameManager;

public class GameScreen extends Screen{


	/**
	 *  The default serial ID. 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 *  Store the desired dimensions of the player panels.
	 */
	@SuppressWarnings("unused")
	private final Dimension playerPanelDimension = new Dimension(150, 150);
	
	
	/**
	 *  Store the desired dimensions of the active player panel.
	 */
	@SuppressWarnings("unused")
	private final Dimension activePlayerDimensions = new Dimension(200, 200);


	/**
	 *  Store the desired dimnesions of the button.
	 */
	private final Dimension buttonDimensions = new Dimension(100, 30);


	/**
	 *  Store the top players panel.
	 */
	private JPanel playersPanel;


	/**
	 *  Store the display component.
	 */
	private Display display;


	/**
	 *  Store the number of players.
	 */
	private int numberPlayers;


	/**
	 *  Store the board.
	 */
	private BoardPanel board;


	/**
	 *  Store the desired padding for the game Screen.
	 */
	private final int padding = 50;

	
	/**
	 *  Store the desired dimensions of the image.
	 */
	@SuppressWarnings("unused")
	private final Dimension imageDimensions = new Dimension(50, 50);
	
	
	/**
	 *  Store the label that will display the health of the active piece.
	 */
	JLabel healthLabel;
	
	
	/** 
	 *  Store the label that will display the mobility points of the active piece.
	 */
	JLabel mobilityLabel;

	
	/**
	 *  Construct the game Screen.
	 *  @param display - The display component.
	 *  @postcondition The game screen is constructed.
	 */
	public GameScreen(Display display, GameManager gm){//HashMap<String, AbstractAction> actions, int numberPlayers, int boardSize){
		//  Assign to be the game Screen.
		super(ScreenEnum.GAME);

		//  Store the Display.
		this.display = display;
		this.numberPlayers = gm.matchOptions.getNumPlayers();

		//  Set the layout of the game screen.
		this.setLayout(new BorderLayout());
		this.setBackground(this.display.getBackground());
		this.setVisible(false);

		//  Add the board to the game screen.
		BoardPanel board = this.createBoard(numberPlayers, gm.matchOptions.getBoardSize(), new Point(this.display.getWidth()/2, (this.display.getHeight()/2)), gm.getActions());
		this.add(board, BorderLayout.CENTER);

		//  Add the top panel to the game Screen.
		this.playersPanel = this.createTopPanel(numberPlayers);
		this.add(this.playersPanel, BorderLayout.WEST);

		//  Add the bottom panel to the game Screen.
		JPanel bottomPanel = this.createBottomPanel(gm.getActions());
		this.add(bottomPanel, BorderLayout.EAST);
				
	}


	/**
	 *  Constructs the board.
	 *  @param numberPlayers - The number of players playing.
	 *  @param position - The position at which to place the board.
	 *  @return The JScrollPane containing the board.
	 *  @postcondition The board is constructed.
	 */
	private BoardPanel createBoard(int numberPlayers, int boardSize, Point position, HashMap<String, AbstractAction> actions){
		//  Construct the board pane.
		this.board = new BoardPanel(numberPlayers, boardSize, position, actions);

		return this.board;
	}
	
	
	/**
	 *  Create a player panel.
	 *  @param color - The desired color of the panel.
	 *  @return - The player panel.
	 *  @postcondition The player panel is constructed.
	 */
	private PlayerPanel createPlayerPanel(Color color){
		//  Create the player panel.
		PlayerPanel playerPanel = new PlayerPanel(color);
		
		return playerPanel;
	}


	/**
	 *  Constructs the top panel.
	 *  @return The top panel.
	 *  @postcondition The top panel is constructed.
	 */
	private JPanel createTopPanel(int numberPlayers){
		//  Create the top layer panel.
		JPanel firstPanel = new JPanel();
		firstPanel.setLayout(new BoxLayout(firstPanel, BoxLayout.Y_AXIS));
		firstPanel.setBackground(this.display.getBackground());

		//  Create the individual player panels and add them if necessary.
		PlayerPanel redPanel = this.createPlayerPanel(Color.RED);
		firstPanel.add(redPanel);
		if (numberPlayers == 6){
			PlayerPanel orangePanel = this.createPlayerPanel(Color.ORANGE);
			firstPanel.add(orangePanel);
		}
		if (numberPlayers == 3 || numberPlayers == 6){
			PlayerPanel yellowPanel = this.createPlayerPanel(Color.YELLOW);
			firstPanel.add(yellowPanel);
		}
		if (numberPlayers == 2 || numberPlayers == 6){
			PlayerPanel greenPanel = this.createPlayerPanel(Color.GREEN);
			if (numberPlayers == 2){
				firstPanel.add(greenPanel);
			}
			else {
				firstPanel.add(greenPanel);
			}
		}
		if (numberPlayers == 3 || numberPlayers == 6){
			PlayerPanel bluePanel = this.createPlayerPanel(Color.BLUE);
			if (numberPlayers == 3){
				firstPanel.add(bluePanel);
			}
			else {
				firstPanel.add(bluePanel);
			}
		}
		if (numberPlayers == 6){
			PlayerPanel purplePanel = this.createPlayerPanel(Color.MAGENTA);
			firstPanel.add(purplePanel);
		}

		return firstPanel;
	}
	
	
	/**
	 *  Set the current stats being displayed.
	 *  @param pieceOffset - The offset of the piece.
	 *  @param playerName - The name of the player.
	 *  @param attack - The attack points of the piece.
	 *  @param mobility - The mobility points of the piece.
	 *  @param health - The health points of the piece.
	 *  @param range - The range points of the piece.
	 */
	public void setCurrentStats(int pieceOffset, String playerName, int attack, int mobility, int health, int range){
		// Update the new first panel.
		PlayerPanel currentPanel = (PlayerPanel) this.playersPanel.getComponent(0);
		Image robotImage = this.board.getPieceImage(pieceOffset);
		currentPanel.setStats(robotImage, playerName, attack, health, mobility, range);
		currentPanel.showStats();

		//  Upadate the game Screen.
		this.repaint();
	}
	

	/**
	 * Method for setting the visual representation of the current stats.
	 * @param teamIndex The color of the team
	 * @param pieceOffset  The offset of the piece
	 * @param playerName The name of the player
	 * @param attack The attack fo the piece
	 * @param mobility The mobility of the piece
	 * @param health The health of the piece
	 * @param range The range of the piece
	 */
	public void setCurrentStats(int teamIndex, int pieceOffset, String playerName, int attack, int mobility, int health, int range){
		// Update the new first panel.
		PlayerPanel currentPanel = (PlayerPanel) this.playersPanel.getComponent(teamIndex);
		Image robotImage = this.board.getPieceImage(pieceOffset);
		currentPanel.setStats(robotImage, playerName, attack, health, mobility, range);
		currentPanel.showStats();

		//  Update the game Screen.
		this.repaint();
	}
	
	/**
	 * Method for hiding the stats for a player.
	 * @param playerIndex
	 */
	public void hideStats(int playerIndex) {
		PlayerPanel panel = (PlayerPanel) this.playersPanel.getComponent(playerIndex);
		panel.hideStats();
		this.repaint();
	}


	/**
	 *  Rotate the side players panel so that the next player in line is at the front.
	 */
	public void rotatePlayer(int pieceOffset, String playerName, int attack, int mobility, int health, int range){
		//  Create the attributes.
		PlayerPanel tempPanel = (PlayerPanel) this.playersPanel.getComponent(0);
		
		//  Re-add the last component to rotate the top players panel.
		this.playersPanel.remove(0);
		this.playersPanel.add(tempPanel);
		tempPanel.hideStats();
		
		// Update the new first panel.
		this.setCurrentStats(pieceOffset, playerName, attack, mobility, health, range);
		
		//  Upadate the game Screen.
		this.repaint();
	}


	/**
	 *  Construct the end turn button.
	 *  @return The end turn button.
	 *  @postcondition The end turn button is constructed.
	 */
	private JButton createEndTurnButton(AbstractAction a){
		//  Construct the end turn button.
		JButton endTurn = new JButton(a);
		endTurn.setText("End Turn");
		endTurn.setName("EndTurnButton");
		endTurn.setPreferredSize(this.buttonDimensions);
//		endTurn.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event){
//			}
//		});

		return endTurn;
	}


	/**
	 *  Construct the button panel.
	 *  @return The button panel.
	 *  @postcondition The button panel is constructed.
	 */
	private JPanel createButtonPanel(HashMap<String, AbstractAction> actions){
		//  Construct the button panel.
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.setBackground(this.display.getBackground());

		//  Add the end turn button to the button panel.
		JButton endTurn = this.createEndTurnButton(actions.get("endTurn"));
		buttonPanel.add(endTurn);

		return buttonPanel;
	}


	/**
	 *  Construct the bottom panel.
	 *  @return The bottom panel.
	 *  @postcondition The bottom panel is constructed.
	 */
	private JPanel createBottomPanel(HashMap<String, AbstractAction> actions){
		//  Construct the bottom panel.
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.setBackground(this.display.getBackground());

		//  Add the button panel to the bottom panel.
		JPanel buttonPanel = this.createButtonPanel(actions);
		bottomPanel.add(Box.createVerticalStrut(this.display.getHeight()-(2*this.padding)));
		bottomPanel.add(buttonPanel);

		return bottomPanel;
	}


	/**
	 *  Move the piece in the specified direction.
	 *  @param pieceOffset - The offset of the piece that is to be moved.
	 *  @param vector - The vector indicating the spaces the piece must be moved.
	 */
	public void movePiece(int pieceOffset, HexCoord source, HexCoord vector, int range, HashMap<Integer, Integer> teamMembers, ArrayList<Integer> visibleRobots){
		//  Store the required moves.
		ArrayList<HexCoord> moves = new ArrayList<HexCoord>();

		//  Move along the x-axis.
		if (vector.getX() > 0){
			while (vector.getX() > 0){
				moves.add(new HexCoord(source.getX()+1, source.getY(), source.getZ()));
				vector.setX(vector.getX()-1);
				source.setX(source.getX()+1);
				source.reduce();
			}
		}
		else if (vector.getX() < 0){
			while (vector.getX() < 0){
				moves.add(new HexCoord(source.getX()-1, source.getY(), source.getZ()));
				vector.setX(vector.getX()+1);
				source.setX(source.getX()-1);
				source.reduce();
			}
		}

		//  Move along the y-axis.
		if (vector.getY() > 0){
			while (vector.getY() > 0){
				moves.add(new HexCoord(source.getX(), source.getY()+1, source.getZ()));
				vector.setY(vector.getY()-1);
				source.setY(source.getY()+1);
				source.reduce();
			}
		}
		else if (vector.getY() < 0){
			while (vector.getY() < 0){
				moves.add(new HexCoord(source.getX(), source.getY()-1, source.getZ()));
				vector.setY(vector.getY()+1);
				source.setY(source.getY()-1);
				source.reduce();
			}
		}

		//  Move along the z-axis.
		if (vector.getZ() > 0){
			while (vector.getZ() > 0){
				moves.add(new HexCoord(source.getX(), source.getY(), source.getZ()+1));
				vector.setZ(vector.getZ()-1);
				source.setZ(source.getZ()+1);
				source.reduce();
			}
		}
		else if (vector.getZ() < 0){
			while (vector.getZ() < 0){
				moves.add(new HexCoord(source.getX(), source.getY(), source.getZ()-1));
				vector.setZ(vector.getZ()+1);
				source.setZ(source.getZ()-1);
				source.reduce();
			}
		}

		// Show the animation.
		Thread moveThread = new Thread() {
			public void run(){
				//  Move the piece.
				for (HexCoord movement : moves){
					movement.reduce();
					board.resetVisibility();
					board.resetColors();
					board.showMove(pieceOffset, movement);
					board.highlightRobot(pieceOffset, range);

					//			this.board.setVisibilityInRange(pieceOffset, 2, visible);
					reloadForCurrentPiece(pieceOffset, range, teamMembers, visibleRobots);
					try {
						Thread.sleep(500);
					} 
					catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		moveThread.start();
//		this.reloadForCurrentPiece(pieceOffset, range, teamMembers, visibleRobots);
	}


	/**
	 *  Shows a shot.
	 *  @param source - The source of the shot.
	 *  @param target - The target of the shot.
	 */
	public void shootSpace(HexCoord source, HexCoord target){
		// Show the shot animation.
		Thread shootThread = new Thread() {
			public void run() {
				board.showShot(source, target);
			}
		};
		shootThread.start();
	}
	
	
	/**
	 *  Shade the pieces in the specified range of the specified piece.
	 *  @param pieceOffset - The offset of the piece.
	 *  @param range - The desired range in which to shade.
	 */
	public void shadeHexesInRange(int pieceOffset, int range){
		this.board.highlightRobot(pieceOffset, range);
	}
	
	
	/**
	 *  Show the robots within the specified range of the specified piece.
	 *  @param pieceOffset - The offset of the piece.
	 *  @param range - The range of the piece.
	 */
	public void showVisibleRobots(int pieceOffset, int range, HashMap<Integer, Integer> teamMembers, ArrayList<Integer> visibleRobots){
		this.board.resetRobotVisibility();
		this.board.setVisibilityInRange(pieceOffset, range, teamMembers, visibleRobots);
	}


	/**
	 *  Determines the hexagon coordinate given the point that was clicked.
	 *  @param point - The location of the mouse.
	 *  @return The hexagon coordinate of the clicked hexagon.
	 */
	public HexCoord determineClickedHex(Point point) {
		return this.board.getClickedHexagonCoords(point);
	}
	
	/**
	 * Sets different colours to fog and visible area.
	 * @param pieceOffset
	 * @param range
	 * @param teamMembers
	 * @param visibleRobots
	 */
	public void reloadForCurrentPiece(int pieceOffset, int range, HashMap<Integer, Integer> teamMembers, ArrayList<Integer> visibleRobots){
		this.board.resetColors();
		//  Shade fog area.
		//this.board.shadeArea();
		
		//  Show robot and their area.
		this.showVisibleRobots(pieceOffset, range, teamMembers, visibleRobots);
		for(Integer piece : teamMembers.keySet()){
			this.shadeHexesInRange(piece, teamMembers.get(piece));
		}

		this.repaint();
	}
	
	public BoardPanel getBoardPanel() {
		return this.board;
	}
	
	/** Method for hiding pieces when a shot is animating. */
	public void hideForShot(ArrayList<Integer> hide){
		this.getBoardPanel().hideForShot(hide);
		
	}
	
}
