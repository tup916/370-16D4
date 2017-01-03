/*  BoardPanel.java
 *  Created by: Nickolas Gough
 *  Purpose: Models the board that will be displayed by the game Screen.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  20/07/2016 - Nickolas Gough : Created the file and began implementing the basics. 
 *  24/07/2016 - Nickolas Gough : Set the colors of the board and began loading in the robot images.
 */

package robowars.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import robowars.board.HexCoord;

public class BoardPanel extends JPanel implements MouseListener, MouseMotionListener{


	/**
	 *  The default serial ID.
	 */
	private static final long serialVersionUID = 1L;


	/** 
	 *  Store the collection of hexagons.
	 */
	private ArrayList<Hexagon>[] hexagons;


	/**
	 *  Store the size of the board.
	 */
	private int boardSize;


	/**
	 *  Store the factor by which to map hex coordinates.
	 */
	private int addFactor;


	/**
	 *  Store the maximum width.
	 */
	private int max;


	/**
	 *  Store the preferred dimension of the game board.
	 */
	private final Dimension preferredSize = new Dimension(700, 700);


	/**
	 *  Store the desired size of the buttons. 
	 */
	private final Dimension buttonSize = new Dimension(100, 30);


	/**
	 *  Store the dimension;
	 */
	private Point center;


	/**
	 *  Store the width of the hexagon.
	 */
	private final int hexWidth = 45;


	/**
	 *  Store the images to be used in the game Screen.
	 */
	private RobotImage[] robotImages;


	/**
	 *  Store the context menu.
	 */
	private JDialog contextMenu;
	
	
	private boolean canCallContextMenu;


	/**
	 *  Store the desired dimensions of the context menu.
	 */
	private final Dimension contextSize = new Dimension(90, 120);


	/**
	 *  Store the shot images and locations.
	 */
	private ShotImage shot;


	/**
	 *  Store the visibility of the bang to be drawn.
	 */
	private boolean bangVisible;


	/**
	 *  Store the visibility of the mushroom.
	 */
	private boolean mushroomVisible;


	/** 
	 *  Store the desired dimensions of the images.
	 */
	private final Dimension imageDimensions = new Dimension(50, 50);


	/**
	 *  Store the previous range.
	 */
	@SuppressWarnings("unused")
	private int previousRange;
	
	private HexCoord activeHex;

	/**
	 * Constructor for use in the main application
	 * @param numberPlayers How many players in the match
	 * @param boardSize How large is the board
	 * @param center The center of the board
	 * @param actions The actions used to create each Button.
	 */
	@SuppressWarnings("unchecked")
	public BoardPanel(int numberPlayers, int boardSize, Point center, HashMap<String, AbstractAction> actions) {
		
		//  Initialize the hexagons collection.
		if (boardSize != 7 && boardSize != 5){
			throw new RuntimeException("Board size not correctly set.");
		}
		this.boardSize = boardSize;
		this.addFactor = this.boardSize-1;
		this.center = center;

		//  Determine the maximum width of the board.
		this.max = (this.boardSize*2)-1;
		this.hexagons = (ArrayList<Hexagon>[]) new ArrayList[max];
		for(int n = 0; n < this.max; n += 1){
			this.hexagons[n] = new ArrayList<Hexagon>();
		}

		//  Setup the board panel.
		this.setBackground(Color.WHITE);
		this.setMaximumSize(this.preferredSize);
		this.setMinimumSize(this.preferredSize);
		this.setPreferredSize(this.preferredSize);
		this.robotImages = new RobotImage[numberPlayers*3];

		//  Initialize the game board.
		this.initializeHexagons(this.center, numberPlayers);
		this.initializeImages(numberPlayers);
		this.initializePositions(numberPlayers);
		
		//  Load in the mushroom cloud and the bang.
		Image mushroom = null;
		Image bang = null;
		try {
			mushroom = ImageIO.read(this.getClass().getResource("/images/mushroom_cloud.jpg"));
			bang = ImageIO.read(this.getClass().getResource("/images/bang.jpg"));
		} catch (IOException e) {
			System.out.println("Error loading the shot images.");
		}
		mushroom = mushroom.getScaledInstance(this.imageDimensions.width, this.imageDimensions.height, Image.SCALE_SMOOTH);
		bang = bang.getScaledInstance(this.imageDimensions.width, this.imageDimensions.height, Image.SCALE_SMOOTH);
		this.shot = new ShotImage(bang, mushroom);
		this.bangVisible = false;
		this.mushroomVisible = false;

		//  Add the context menu.
		this.contextMenu = this.createContextMenu(actions);
		this.contextMenu.setVisible(false);

		//  Indicate the panel is to listen to mouse events.
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
		this.activeHex = null;
		this.canCallContextMenu = true;
		
		
	}

	/**
	 *  Constructs the board panel.
	 *  @throws RuntimeException when board size not 5 or 7.
	 */
//	@SuppressWarnings("unchecked")
//	public BoardPanel(int numberPlayers, int boardSize, Point center) throws RuntimeException{
//		//  Initialize the hexagons collection.
//		if (boardSize != 7 && boardSize != 5){
//			throw new RuntimeException("Board size not correctly set.");
//		}
//		this.boardSize = boardSize;
//		this.addFactor = this.boardSize-1;
//		this.center = center;
//
//		//  Determine the maximum width of the board.
//		this.max = (this.boardSize*2)-1;
//		this.hexagons = (ArrayList<Hexagon>[]) new ArrayList[max];
//		for(int n = 0; n < this.max; n += 1){
//			this.hexagons[n] = new ArrayList<Hexagon>();
//		}
//
//		//  Setup the board panel.
//		this.setBackground(Color.WHITE);
//		this.setMaximumSize(this.preferredSize);
//		this.setMinimumSize(this.preferredSize);
//		this.setPreferredSize(this.preferredSize);
//		this.robotImages = new RobotImage[numberPlayers*3];
//
//		//  Initialize the game board.
//		this.initializeHexagons(this.center, numberPlayers);
//		this.initializeImages(numberPlayers);
//		this.initializePositions(numberPlayers);
//		
//		//  Load in the mushroom cloud and the bang.
//		Image mushroom = null;
//		Image bang = null;
//		try {
//			mushroom = ImageIO.read(this.getClass().getResource("/images/mushroom_cloud.jpg"));
//			bang = ImageIO.read(this.getClass().getResource("/images/bang.jpg"));
//		} catch (IOException e) {
//			System.out.println("Error loading the shot images.");
//		}
//		mushroom = mushroom.getScaledInstance(this.imageDimensions.width, this.imageDimensions.height, Image.SCALE_SMOOTH);
//		bang = bang.getScaledInstance(this.imageDimensions.width, this.imageDimensions.height, Image.SCALE_SMOOTH);
//		this.shot = new ShotImage(bang, mushroom);
//		this.bangVisible = false;
//		this.mushroomVisible = false;
//
//		//  Add the context menu.
//		this.contextMenu = this.createContextMenu(actions);
//		this.contextMenu.setVisible(false);
//
//		//  Indicate the panel is to listen to mouse events.
//		this.addMouseListener(this);
//		this.addMouseMotionListener(this);
//		
//		// Initialize the game manager
//		
//		
//	}


	/**
	 *  Initializes the hexagons of the game board.
	 *  @param center - The center point at which to center the board.
	 *  @postcondition The game board consisting of the hexagons is constructed. 
	 */
	private void initializeHexagons(Point center, int numberPlayers){
		//  Determine the starting drawing point.
		double x = center.x - (this.addFactor*this.hexWidth*(Math.sqrt(3.0)));
		double y = center.y;

		//  Store away the center x coordinates and the y coordinates.
		double tempX = x;
		double tempY = y;

		// Instantiate each hexagon.
		int centerLayer = (this.addFactor);
		Color color = Color.LIGHT_GRAY;
		for(int n = 0; n <= centerLayer; n += 1){
			for (int k = 0; k < (this.boardSize+n); k += 1){
				color = Color.LIGHT_GRAY;
				if (n == 0 && k == 0){
					color = Color.RED;
				}
				else if (n == 0 && k == this.addFactor && numberPlayers == 6){
					color = Color.MAGENTA;
				}
				else if (n == this.addFactor && k == 0 && numberPlayers == 6){
					color = Color.ORANGE;
				}
				else if (n == this.addFactor && k == (2*this.addFactor) && (numberPlayers == 3 || numberPlayers == 6)){
					color = Color.BLUE;
				}
				this.hexagons[n].add(new Hexagon(tempX, tempY, this.hexWidth, color));
				tempX += this.hexWidth*(Math.sqrt(3.0)/2);
				tempY += 1.5*this.hexWidth;
			}
			tempX = x + (n+1)*(this.hexWidth*(Math.sqrt(3.0)/2));
			tempY = y - (n+1)*(1.5*this.hexWidth);
		}

		tempX += (this.hexWidth*(Math.sqrt(3.0)/2)); 
		tempY += (1.5*this.hexWidth);
		for(int n = (centerLayer+1); n < this.max; n += 1){
			for (int k = this.hexagons[n-1].size()-1; k > 0; k -= 1){
				color = Color.LIGHT_GRAY;
				if (n == (2*this.addFactor) && k == (this.addFactor+1) && (numberPlayers == 3 || numberPlayers == 6)){
					color = Color.YELLOW;
				}
				else if (n == (2*this.addFactor) && k == 1 && (numberPlayers == 2 || numberPlayers == 6)){
					color = Color.GREEN;
				}
				this.hexagons[n].add(new Hexagon(tempX, tempY, this.hexWidth, color));
				tempX += this.hexWidth*(Math.sqrt(3.0)/2);
				tempY += 1.5*this.hexWidth;
			}
			if (this.boardSize == 5){
				tempX = x + (n-1)*(this.hexWidth*(Math.sqrt(3.0)));
				tempY = y - (this.addFactor)*(1.5*this.hexWidth);
			}
			else {
				tempX = x + (n-2)*(this.hexWidth*(Math.sqrt(3.0)));
				tempY = y - (this.addFactor)*(1.5*this.hexWidth);
			}
		}
	}


	/**
	 *  Initialize the images for the game Screen.
	 *  @param numberPlayers - The number of players.
	 *  @postcondition The correct images are loaded for the correct number of pieces.
	 */
	private void initializeImages(int numberPlayers){
		
		//  Determine the correct colours. 
		String[] colors = new String[numberPlayers];
		switch(numberPlayers){
		case 2:
			colors[0] = "red";
			colors[1] = "green";
			break;
		case 3:
			colors[0] = "red";
			colors[1] = "yellow";
			colors[2] = "blue";
			break;
		case 6:
			colors[0] = "red";
			colors[1] = "orange";
			colors[2] = "yellow";
			colors[3] = "green";
			colors[4] = "blue";
			colors[5] = "purple";
		}
		
		//  Load the robot images.
		try {
			String teamColor = "red";
			for (int n = 0; n < numberPlayers; n += 1){
				//  Determine the robot's color.
				int tempNumber = n*3;
				switch(n){
				case 0:
					teamColor = colors[n];
					break;
				case 1:
					teamColor = colors[n];
					break;
				case 2:
					teamColor = colors[n];
					break;
				case 3:
					teamColor = colors[n];
					break;
				case 4:
					teamColor = colors[n];
					break;
				case 5:
					teamColor = colors[n];
					break;
				default:
					// Do nothing.
				}

				this.robotImages[tempNumber] = new RobotImage(ImageIO.read(this.getClass().getResource("/images/" + teamColor + "_scout.jpg")));
				this.robotImages[tempNumber+1] = new RobotImage(ImageIO.read(this.getClass().getResource("/images/" + teamColor + "_sniper.jpg")));
				this.robotImages[tempNumber+2] = new RobotImage(ImageIO.read(this.getClass().getResource("/images/" + teamColor + "_tank.jpg")));
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 *  Initialize the positions of the robot images.
	 *  @param numberPlayers - The number of players for which to initialize the game.
	 *  @postcondition The game Screen is initialized for the correct number of players.
	 */
	private void initializePositions(int numberPlayers){
		//  Position the robot images in their initial starting positions.
		//  EXTENSION - This method must flex for a variable number of pieces
		for (int n = 0; n < numberPlayers; n += 1){
			for (ArrayList<Hexagon> hexList : this.hexagons){
				for (Hexagon hexagon : hexList){
					Color currentColor = hexagon.getColor();
					int position = 0;
					if (currentColor == Color.RED){
						this.robotImages[position].setXPosition(hexagon.getXCenter());
						this.robotImages[position].setYPosition(hexagon.getYCenter());
						this.robotImages[position+1].setXPosition(hexagon.getXCenter());
						this.robotImages[position+1].setYPosition(hexagon.getYCenter());
						this.robotImages[position+2].setXPosition(hexagon.getXCenter());
						this.robotImages[position+2].setYPosition(hexagon.getYCenter());
					}
					else if (currentColor == Color.ORANGE && numberPlayers == 6){
						this.robotImages[position+3].setXPosition(hexagon.getXCenter());
						this.robotImages[position+3].setYPosition(hexagon.getYCenter());
						this.robotImages[position+4].setXPosition(hexagon.getXCenter());
						this.robotImages[position+4].setYPosition(hexagon.getYCenter());
						this.robotImages[position+5].setXPosition(hexagon.getXCenter());
						this.robotImages[position+5].setYPosition(hexagon.getYCenter());
					}
					else if (currentColor == Color.YELLOW && (numberPlayers == 3 || numberPlayers == 6)){
						if (numberPlayers == 3){
							position = 3;
						}
						else {
							position = 6;
						}
						this.robotImages[position].setXPosition(hexagon.getXCenter());
						this.robotImages[position].setYPosition(hexagon.getYCenter());
						this.robotImages[position+1].setXPosition(hexagon.getXCenter());
						this.robotImages[position+1].setYPosition(hexagon.getYCenter());
						this.robotImages[position+2].setXPosition(hexagon.getXCenter());
						this.robotImages[position+2].setYPosition(hexagon.getYCenter());
					}
					else if (currentColor == Color.GREEN && (numberPlayers == 2 || numberPlayers == 6)){
						if (numberPlayers == 2){
							position = 3;
						}
						else {
							position = 9;
						}
						this.robotImages[position].setXPosition(hexagon.getXCenter());
						this.robotImages[position].setYPosition(hexagon.getYCenter());
						this.robotImages[position+1].setXPosition(hexagon.getXCenter());
						this.robotImages[position+1].setYPosition(hexagon.getYCenter());
						this.robotImages[position+2].setXPosition(hexagon.getXCenter());
						this.robotImages[position+2].setYPosition(hexagon.getYCenter());
					}
					else if (currentColor == Color.BLUE && (numberPlayers == 3 || numberPlayers == 6)){
						if (numberPlayers == 3){
							position = 6;
						}
						else {
							position = 12;
						}
						this.robotImages[position].setXPosition(hexagon.getXCenter());
						this.robotImages[position].setYPosition(hexagon.getYCenter());
						this.robotImages[position+1].setXPosition(hexagon.getXCenter());
						this.robotImages[position+1].setYPosition(hexagon.getYCenter());
						this.robotImages[position+2].setXPosition(hexagon.getXCenter());
						this.robotImages[position+2].setYPosition(hexagon.getYCenter());
					}
					else if (currentColor == Color.MAGENTA && numberPlayers == 6){
						this.robotImages[15].setXPosition(hexagon.getXCenter());
						this.robotImages[15].setYPosition(hexagon.getYCenter());
						this.robotImages[16].setXPosition(hexagon.getXCenter());
						this.robotImages[16].setYPosition(hexagon.getYCenter());
						this.robotImages[17].setXPosition(hexagon.getXCenter());
						this.robotImages[17].setYPosition(hexagon.getYCenter());
					}
				}
			}
		}
	}

	/**
	 * Method for hiding robot images which are being shot.
	 * @param hidden Offsets of the images to hide.
	 */
	public void hideForShot(ArrayList<Integer> hidden){
		for (Integer i: hidden){
			this.robotImages[i].setVisible(false);
			System.out.println("Hidden!");
			this.repaint();
		}
	}
	

	/**
	 *  Transforms the hexagon coordinate into indices of a hexagon.
	 *  @param coord - The hexagon coordinate to transform into indices.
	 *  @return The indices of the hexagon as a point.
	 *  @postcondition The hexagon coordinates are transformed into the indices of the hexagon. 
	 */
	private Point reducedHexToIndices(HexCoord coord){
		//  Transform the hex coordinate to indices.
		try {
			Point newPoint = new Point(coord.getX()+this.addFactor, coord.getZ()+this.addFactor);
			if (newPoint.x > this.addFactor) {
				newPoint.setLocation(newPoint.getX(), coord.getZ()+this.addFactor-(newPoint.x-this.addFactor));
			} else {
				
			}
			
			return newPoint;
			
		} catch (NullPointerException e) {
			System.out.println("It's outside of the board!!!");
		} finally {
			
		}
		
		return null;
	}


	/**
	 *  Transforms the coordinates of a hexagon into the hexagon coordinates of the hexagon.
	 *  @param point - The indices of the hexagon to transform to a hexagon coordinate.
	 *  @return - The indices of the hexagon coordinate.
	 *  @postcondition The indices of the hexagon are transformed into a reduced hexagon coordinate.
	 */
	@SuppressWarnings("unused")
	private HexCoord indicesToReducedHex(Point point){
		//  Transform the hex coordinate to indices.
		HexCoord newPoint = null;
		if (point.x > this.addFactor){
			newPoint = new HexCoord(point.x-this.addFactor, 0, point.y-this.addFactor+(point.x-this.addFactor));
		}
		else{
			newPoint = new HexCoord(point.x-this.addFactor, 0, point.y-this.addFactor);
		}

		return newPoint;
	}


	/**
	 *  Move the specified piece to the specified hexagon coordinate.
	 *  @param pieceOffset - The piece to move.
	 *  @param source - The coordinate to which to move the piece.
	 */
	public void showMove(int pieceOffset, HexCoord source){
		//  Determine the index of the hexagon to which to move the piece.
		Point indices = this.reducedHexToIndices(source);
		try {
			Hexagon hexagon = this.hexagons[indices.x].get(indices.y);
			double ratioY = Math.abs(hexagon.getYCenter()-this.robotImages[pieceOffset].getYPosition());
			double ratioX = Math.abs(hexagon.getXCenter()-this.robotImages[pieceOffset].getXPosition());
			double angle = Math.atan2(ratioY, ratioX);
			this.robotImages[pieceOffset].setRotationAngle(angle);
			this.robotImages[pieceOffset].setXPosition(hexagon.getXCenter());
			this.robotImages[pieceOffset].setYPosition(hexagon.getYCenter());
	
//			this.repaint();
		} 
		catch (NullPointerException e) {
			// Do nothing.
		} 
		finally {
			// Do nothing.
		}
	}


	/** 
	 *  Show the shot beginning from the shot to the target.
	 *  @param source - The starting point of the shot. 
	 *  @param target - The ending point of the shot.
	 */
	public void showShot(HexCoord source, HexCoord target){
		//  Position and show the bang.
		Point indices = this.reducedHexToIndices(source);
		Hexagon sourceHex = this.hexagons[indices.x].get(indices.y);
		this.shot.setBangXPosition(sourceHex.getXCenter());
		this.shot.setBangYPosition(sourceHex.getYCenter());
		this.bangVisible = true;

		this.repaint(0);
		System.out.println("BANG.");
		try {
			Thread.sleep(500);
		} 
		catch (InterruptedException e) {
			//  Do nothing.
		}
		this.bangVisible = false;

		// Position and show the mushroom cloud.
		indices = this.reducedHexToIndices(target);
		Hexagon targetHex = this.hexagons[indices.x].get(indices.y);
		this.shot.setMushroomXPosition(targetHex.getXCenter());
		this.shot.setMushroomYPosition(targetHex.getYCenter());
		this.mushroomVisible = true;
		this.repaint(0);
		System.out.println("POOF.");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			//  Do nothing.
		}
		this.mushroomVisible = false;

		this.repaint(0);
	}


	/**
	 *  Paints the contents of the board.
	 *  @postcodition The hexagons and the pieces are drawn.
	 */
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
		//  Cast the graphics to a graphics 2D.
		Graphics2D g2 = (Graphics2D) g;
		
		
//		//  Whiten the hexagons.
//		for (ArrayList<Hexagon> hexList : this.hexagons){
//			for (Hexagon hexagon : hexList){
//				hexagon.whiten();
//			}
//		}

		//  Draw the hexagons.
		for(ArrayList<Hexagon> hexList : this.hexagons){
			for (Hexagon hexagon : hexList){
				hexagon.drawComponent(g2);
			}
		}

	//  Draw the bang or mushroom if either is visible.
			if (this.bangVisible){
				this.shot.drawBang(g2);
			}
			if (this.mushroomVisible){
				this.shot.drawMushroom(g2);
			}
		
		//  Draw the visible robots.
		for(ArrayList<Hexagon> hexList : this.hexagons){
			for (Hexagon hexagon : hexList){
				for (RobotImage robotImage : this.robotImages){
					if (this.isOccupying(robotImage.getXPosition(), robotImage.getYPosition(), hexagon.getXCenter(), hexagon.getYCenter()) && hexagon.isVisible() && robotImage.isVisible()){
						if ((this.bangVisible && this.shot.bXPosition == hexagon.getXCenter() && this.shot.bYPosition == hexagon.getYCenter())){
							System.out.println("Shot visible.");
						}
						else {
							
							robotImage.draw(g2);
						}
					}
				}
			}
		}

		
	}


	/**
	 *  Determines if a robot is occupying a given hex.
	 *  @param robot - The points (location on screen) of the robot.
	 *  @param hex - The points (location on screen) of the hexagon.
	 *  @return True if the robot is occupying the hexagon.
	 */
	private boolean isOccupying(double robotX, double robotY, double hexX, double hexY){
		//  Determine the distance from the robot and the hexagon.
		double deltaY = Math.abs(robotY-hexY);
		double deltaX = Math.abs(robotX-hexX);
		double distance = Math.sqrt(Math.pow(deltaY, 2.0)+Math.pow(deltaX, 2.0));
		boolean isOccupyingHex = false;
		if (distance < this.hexWidth-10){
			isOccupyingHex = true;
		}

		return isOccupyingHex;
	}


	/**
	 *  Retrieve the hexagon that is closest to the position of the mouse.
	 *  @param mousePoint - The point of the mouse click on the screen.
	 *  @return - The hex coord of the clicked hexagon.
	 */
	public HexCoord getClickedHexagonCoords(Point mousePoint){
		//  Set the initial distance.
		double deltaX = Math.abs(mousePoint.getX()-this.center.x);
		double deltaY = Math.abs(mousePoint.getY()-this.center.y);
		double distance = Math.sqrt(Math.pow(deltaX, 2)+Math.pow(deltaY, 2));

		//  Determine which hexagon is clicked.
		HexCoord hexCoord = null;
		for (int n = 0; n < this.hexagons.length; n += 1){
			for (int k = 0; k < this.hexagons[n].size(); k += 1){
				Hexagon hexagon = this.hexagons[n].get(k);
				deltaX = Math.abs(hexagon.getXCenter()-mousePoint.getX());
				deltaY = Math.abs(hexagon.getYCenter()-mousePoint.getY());
				distance = Math.sqrt((Math.pow(deltaX, 2)+Math.pow(deltaY, 2)));
				if (distance < this.hexWidth){
					if (n > this.addFactor){
						hexCoord = new HexCoord(n-this.addFactor, 0, k-this.addFactor+(n-this.addFactor));
					}
					else{
						hexCoord = new HexCoord(n-this.addFactor, 0, k-this.addFactor);
					}
				}
			}
		}

		return hexCoord;
	}


	/**
	 *  Retrieve the indices of the hexagon that was clicked.
	 *  @param mousePoint - The point representing the point at which the mouse was clicked.
	 *  @return The indices of the hexagon that was clicked.
	 */
	public Point getClickedHexagonIndices(Point mousePoint){
		//  Set the initial distance.
		double deltaX = Math.abs(mousePoint.getX()-this.center.x);
		double deltaY = Math.abs(mousePoint.getY()-this.center.y);
		double distance = Math.sqrt(Math.pow(deltaX, 2)+Math.pow(deltaY, 2));

		//  Determine which hexagon is clicked.
		Point hexClicked = null;
		for (int n = 0; n < this.hexagons.length; n += 1){
			for (int k = 0; k < this.hexagons[n].size(); k += 1){
				Hexagon hexagon = this.hexagons[n].get(k);
				deltaX = Math.abs(hexagon.getXCenter()-mousePoint.getX());
				deltaY = Math.abs(hexagon.getYCenter()-mousePoint.getY());
				distance = Math.sqrt((Math.pow(deltaX, 2)+Math.pow(deltaY, 2)));
				if (distance < this.hexWidth){
					hexClicked = new Point(n, k);
				}
			}
		}

		return hexClicked;
	}


	/**
	 *  Shade the hexagons around the indicated piece within the specified range.
	 *  @param pieceOffset - The offset of the piece.
	 *  @param range - The range in which to shade.
	 */
	public void highlightRobot(int pieceOffset, int range){
		//  Determine the location of the specified piece.
		RobotImage robot = this.robotImages[pieceOffset];
		Point position = new Point(0, 0);
		position.setLocation(robot.getXPosition(), robot.getYPosition());

		//  Determine the hexagon.
		Point indices = this.getClickedHexagonIndices(position);

		//  Shade the hexagons.
		this.shadeInRangeOf(indices, range);
	}

	/**
	 *  Highlights the hexagons in within the specified range of the specified hexagon.
	 *  @param indices - The indices of the hexagon hexagon from which to radiate outwards.
	 *  @param range - The range of the shaded region.
	 *  @postcondition The hexagons within the specified range of the specified hexagon are highlighted.
	 */
	@SuppressWarnings("unused")
	private void highlightInRangeOf(Point indices, int range){
		
		ArrayList<Hexagon> hexagons = this.getHexagonsInRange(indices, range);
		
		try {
			for (Hexagon hexagon : hexagons) {
				hexagon.shade();
			}
			
		} catch (NullPointerException e) {
			
		} finally {
			
		}

	}

	/**
	 *  Shades the hexagons in within the specified range of the specified hexagon.
	 *  @param indices - The indices of the hexagon hexagon from which to radiate outwards.
	 *  @param range - The range of the shaded region.
	 *  @postcondition The hexagons within the specified range of the specified hexagon are shaded.
	 */
	private void shadeInRangeOf(Point indices, int range){
		//  Determine the hexagons in range.
		ArrayList<Hexagon> hexagons = this.getHexagonsInRange(indices, range);
		try {
			for (Hexagon hexagon : hexagons){
				hexagon.shade();
			}
		} catch (NullPointerException e) {
			
		} finally {
			
		}

//		this.repaint();
	}
	
	
	/**
	 *  Set the visibilty of the hexagons around the piece in the specified range.
	 *  @param pieceOffset - The offset of the piece.
	 *  @param range - The of hexagons to be made visible.
	 */
	public void setVisibilityInRange(int pieceOffset, int range, HashMap<Integer, Integer> teamMembers, ArrayList<Integer> visibleRobots){
		//  Determine the indices of the hexagon.
		
		for (Integer piece: teamMembers.keySet()){
			RobotImage robot = this.robotImages[piece];
			Point position = new Point(0, 0);
			position.setLocation(robot.getXPosition(), robot.getYPosition());
			
			//  Determine the indices of the hexagon.
			Point indices = this.getClickedHexagonIndices(position);
			
			//  Make the hexagons visible.
			this.setVisibilityInRangeOf(indices, teamMembers.get(piece));
		}
		
		//  Make the robots visible
		for (int i= 0; i < visibleRobots.size(); i++){
			this.robotImages[visibleRobots.get(i)].setVisible(true);
		}
		
//		this.repaint();
	}
	
	
	/**
	 *  Make the hexagons visible within the specified range of the specified center.
	 *  @param indices - The indices of the center hexagon.
	 *  @param range - The range.
	 */
	private void setVisibilityInRangeOf(Point indices, int range){
		//  Determine the hexagons in range.
		ArrayList<Hexagon> hexagons = this.getHexagonsInRange(indices, range);
		for (Hexagon hexagon : hexagons){
			hexagon.setVisible(true);
		}
	}
	
	
	
	/**
	 *  Resets the visibility of the collection of hexagons.
	 */
	public void resetVisibility(){
		//  Reset the visibility of each hexagon.
		for (ArrayList<Hexagon> hexList : this.hexagons){
			for (Hexagon hexagon : hexList){
				hexagon.setVisible(false);
			}
		}
	}
	
	
	/**
	 *  Resets the colors of the hexagons.
	 */
	public void resetColors(){
		//  Reset the colors of the hexagons.
		for (ArrayList<Hexagon> hexList : this.hexagons){
			for (Hexagon hexagon : hexList){
				hexagon.reset();
			}
		}
	}
	
	
	/**
	 *  Shades an area of the hexagons.
	 */
	public void shadeArea() {

		for (ArrayList<Hexagon> hexList : this.hexagons) {
			for (Hexagon hexagon : hexList){
				hexagon.shade();
			}
		}
		
	}

	/**
	 * Resets the robot visibility in order to draw objects within the fog of war properly.
	 */
	public void resetRobotVisibility(){
		for (int i = 0; i < this.robotImages.length; i++){
			this.robotImages[i].setVisible(false);
		}
		this.repaint();
	}
	
	

	/**
	 *  Determines the hexagons within range of a hexagon specified by its indices.
	 *  @param indices - The indices of the center hexagon.
	 *  @param range - The range.
	 *  @return The collection of hexagons within range of the center.
	 */
	private ArrayList<Hexagon> getHexagonsInRange(Point indices, int range){
		//  Don't shade if the indices is null.
		if (indices == null){
			return null;
		}

		//  The list of hexagons.
		ArrayList<Hexagon> hexagons = new ArrayList<Hexagon>();
		
		//  Shade the hexagons in range of the indicated hexagon.
		int n = indices.x;
		int left = indices.y-range;
		int right = indices.y+range;

		//  Shade the upper portion that is in range of the indicated hexagon.
		for(int i = n; i <= (n+range); i += 1){
			for (int j = left; j <= right; j += 1){
				if (i >= 0 && i < this.hexagons.length){
					if (j >= 0 && j < this.hexagons[i].size()){
						hexagons.add(this.hexagons[i].get(j));
					}
				}
			}
			if (i >= this.addFactor){
				right -= 1;
			}
			if (i < this.addFactor){
				left += 1;
			}
		}

		//  Shade the lower portion in range of the indicated hexagon.
		n = indices.x;
		left = indices.y-range;
		right = indices.y+range;
		if (n > this.addFactor){
			left += 1;
		}
		if (n <= this.addFactor){
			right -= 1;
		}
		for(int i = (n-1); i >= (n-range); i -= 1){
			for (int j = left; j <= right; j += 1){
				if (i >= 0 && i < this.hexagons.length){
					if (j >= 0 && j < this.hexagons[i].size()){
						hexagons.add(this.hexagons[i].get(j));
					}
				}
			}
			if (i > this.addFactor){
				left += 1;
			}
			if (i <= this.addFactor){
				right -= 1;
			}
		}
		
		return hexagons;
	}


	/**
	 *  Clears all the hexagons back to white.
	 *  @postcondition The hexagons are all colored their original color.
	 */
	public void clearShades(){
		//  Clear the shades.
		for (ArrayList<Hexagon> hexList : this.hexagons){
			for (Hexagon hexagon : hexList){
				hexagon.reset();
			}
		}

//		this.repaint();
	}


	/**
	 *  Construct the move button.
	 *  @return The move button.
	 *  @postcondition The move button is constructed.
	 */
	
	
	//pass in AbstractAction a
	private JButton createMoveButton(AbstractAction a){
		//  Construct the move button.
		JButton moveButton = new JButton(a);
		moveButton.setName("Move");
		moveButton.setText("Move");
		moveButton.setPreferredSize(this.buttonSize);
		moveButton.setMinimumSize(this.buttonSize);
		moveButton.setMaximumSize(this.buttonSize);
		moveButton.setAlignmentX(CENTER_ALIGNMENT);
//		moveButton.addActionListener(new ActionListener(){
//			public void actionPerformed(ActionEvent event){
//				
//			}
//		});

		return moveButton;
	}


	/**
	 *  Construct the shoot button.
	 *  @return The shoot button.
	 *  @postcondition The shoot button is constructed.
	 */
	private JButton createShootButton(AbstractAction a){
		//  Construct the shoot button.
		JButton shootButton = new JButton(a);
		shootButton.setName("Shoot");
		shootButton.setText("Shoot");
		shootButton.setPreferredSize(this.buttonSize);
		shootButton.setMinimumSize(this.buttonSize);
		shootButton.setMaximumSize(this.buttonSize);
		shootButton.setAlignmentX(CENTER_ALIGNMENT);
//		shootButton.addActionListener(new ActionListener(){
//			public void actionPerformed(ActionEvent event){
//			}
//		});

		return shootButton;
	}


	/**
	 *  Constructs the inspect button.
	 *  @return The inspect button.
	 *  @postcondition The inspect button is constructed.
	 */
	private JButton createInspectButton(AbstractAction a){
		//  Construct the inspect button.
		JButton inspectButton = new JButton(a);
		inspectButton.setName("Inspect");
		inspectButton.setText("Inspect");
		inspectButton.setPreferredSize(this.buttonSize);
		inspectButton.setMinimumSize(this.buttonSize);
		inspectButton.setMaximumSize(this.buttonSize);
		inspectButton.setAlignmentX(CENTER_ALIGNMENT);
//		inspectButton.addActionListener(new ActionListener(){
//			public void actionPerformed(ActionEvent event){
//				System.out.println("I click on the inspect!!");
//			}
//		});

		return inspectButton;
	}


	/**
	 *  Construct the cancel button.
	 *  @return The cancel button.
	 *  @postcondition The cancel button is constructed.
	 */
	private JButton createCancelButton(AbstractAction a){
		// Construct the cancel button.
		JButton cancelButton = new JButton(a);
		cancelButton.setName("Cancel");
		cancelButton.setText("Cancel");
		cancelButton.setPreferredSize(this.buttonSize);
		cancelButton.setMinimumSize(this.buttonSize);
		cancelButton.setMaximumSize(this.buttonSize);
		cancelButton.setAlignmentX(CENTER_ALIGNMENT);
		/*cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				contextMenu.setVisible(false);
			}
		});*/

		return cancelButton;
	}


	/**
	 *  Construct the context menu.
	 *  @return The context menu.
	 *  @postcondition The context menu is constructed.
	 */
	private JDialog createContextMenu(HashMap<String, AbstractAction> actions){
		//  Construct the context menu.
		JDialog contextMenu = new JDialog();
		contextMenu.setBackground(Color.WHITE);
		contextMenu.setAlwaysOnTop(true);
		contextMenu.setPreferredSize(this.contextSize);
		contextMenu.setUndecorated(true);

		//  Add the buttons to the context menu.
		JPanel contextPanel = new JPanel();
		contextPanel.setLayout(new BoxLayout(contextPanel, BoxLayout.Y_AXIS));
		JButton moveButton = this.createMoveButton(actions.get("move"));
		JButton shootButton = this.createShootButton(actions.get("shoot"));
		JButton inspectButton = this.createInspectButton(actions.get("inspect"));
		JButton cancelButton = this.createCancelButton(actions.get("cancel"));
		contextPanel.add(moveButton);
		contextPanel.add(shootButton);
		contextPanel.add(inspectButton);
		contextPanel.add(cancelButton);
		contextMenu.add(contextPanel);

		contextMenu.pack();

		return contextMenu;
	}
	
	
	/**
	 *  Retrieve the piece image.
	 *  @param pieceOffset - The offset of the piece.
	 *  @return The image of the piece corresponding to the piece offset.
	 */
	public Image getPieceImage(int pieceOffset){
		return this.robotImages[pieceOffset].getImage();
	}


	/**
	 *  Shows the context menu at the specified location.
	 *  @param location - The location at which to show the context menu.
	 *  @postcondition The context menu is displayed at the specified location.
	 */
	public void showContextMenu(Point location){
		//  Show the context menu at the specified location.
		this.contextMenu.setLocation(location);
		this.contextMenu.setVisible(true);
	}
	
	public JDialog getContextMenu(){
		return this.contextMenu;
	}
	
	public HexCoord getActiveHex(){
		return this.activeHex;
	}
	
	public boolean getCanCallContext(){
		return this.canCallContextMenu;
	}

	public void setCanCallContext(boolean b){
		this.canCallContextMenu = b;
		if (this.canCallContextMenu == false){
			this.contextMenu.setVisible(false);
		}
	}

	public void showDefeatMessage(String playerName){
		JOptionPane.showMessageDialog(null, "You have been eliminated!");
	}
	
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
//		this.clearShades();
		
		if (canCallContextMenu){
			
			
			// Only show buttons when you left click the mouse and the location is inside of the board
			if (SwingUtilities.isLeftMouseButton(e)) {
				HexCoord coord = this.getClickedHexagonCoords(e.getPoint());
				
				this.activeHex = coord;
				
				if (this.reducedHexToIndices(coord) != null) {
					//this.showMove(0, coord);
					this.showContextMenu(e.getLocationOnScreen());
				}
				
			// And then hide it when you right click somewhere else
			} else if (SwingUtilities.isRightMouseButton(e)) {
				this.contextMenu.setVisible(false);
			}
		}
	}


	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}


	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}


	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}


	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}


	@Override
	public void mouseMoved(MouseEvent e) {
//		this.clearShades();
		@SuppressWarnings("unused")
		Point center = this.getClickedHexagonIndices(e.getPoint());
		// TODO shade the current moused-over hex some other colour that is not gray.
		// Also don't get rid of other shading
//		this.shadeInRangeOf(center, 1);
	}


	/**
	 * The class that will be used for displaying the robots. 
	 * @author nvg081
	 */
	private class RobotImage{


		/*
		 *  Store the image of the robot.
		 */
		private Image image;


		/*
		 *  Store is the robot is visible.
		 */
		private boolean isVisible;


		/*
		 *  Store the xPosition of the robot image.
		 */
		private double xPosition;


		/*
		 *  Store the y position of the robot image.
		 */
		private double yPosition;


		/*
		 *  Store the angle of the image.
		 */
		@SuppressWarnings("unused")
		private double rotationAngle;


		/**
		 *  Constructs a robot image.
		 *  @param image - The image of the robot.
		 */
		public RobotImage(Image image){
			this.image = image;
			this.rotationAngle = 0.0;
		}


		/**
		 *  Draw the robot image.
		 *  @param g2 - The graphics2D object.
		 */
		public void draw(Graphics2D g2){
			//  Set up the coordinates at which to draw the image.
			g2.drawImage(this.image, (int) this.xPosition, (int) this.yPosition, null);
		}


		/**
		 *  Retrieve the image of the robot.
		 *  @return - The robot image.
		 */
		public Image getImage(){
			return this.image;
		}


		/**
		 *  Set the x position of the robot image.
		 *  @param newPosition - The new x position of the robot.
		 *  @postcondition The x position of the robot is updated.
		 */
		public void setXPosition(double newPosition){
			this.xPosition = newPosition-(this.image.getWidth(null)/2);
		}


		/** 
		 *  Retrieve the x position of the robot image.
		 *  @return - The robot image's x position.
		 */
		public double getXPosition(){
			return this.xPosition+(this.image.getWidth(null)/2);
		}


		/** 
		 *  Retrieve the y position of the robot image.
		 *  @return - The robot image's y position.
		 */
		public double getYPosition(){
			return this.yPosition+(this.image.getHeight(null)/2);
		}


		/**
		 *  Set the 7 position of the robot image.
		 *  @param newPosition - The new y position of the robot.
		 *  @postcondition The y position of the robot is updated.
		 */
		public void setYPosition(double newPosition){
			this.yPosition = newPosition-(this.image.getHeight(null)/2);
		}


		/**
		 *  Set the rotation angle of the robot image.
		 *  @param newRotation - The new rotation angle.
		 *  @postcondition The robot image's rotation angle is adjusted.
		 */
		public void setRotationAngle(double newRotation){
			this.rotationAngle = newRotation;
		}


		/** 
		 *  Determines if the robot is visible.
		 *  @return - True if the robot is visible, false otherwise.
		 */
		public boolean isVisible(){
			return this.isVisible;
		}


		/**
		 *  Sets the visibility of the robot image.
		 *  @param isVisible - The new visibility of the robot.
		 *  @postcondition The visibility of the robot image is updated.
		 */
		public void setVisible(boolean isVisible){
			this.isVisible = isVisible;
		}
	}


	/**
	 * The class that will be used for displaying the shot. 
	 * @author nvg081
	 */
	private class ShotImage{


		private Image mushroomCloud;


		private Image bang;


		private double mXPosition;


		private double mYPosition;


		private double bXPosition;


		private double bYPosition;


		public ShotImage(Image bangImage, Image mushroomCloudImage){
			this.bang = bangImage;
			this.mushroomCloud = mushroomCloudImage;
		}


		public void drawBang(Graphics2D g2){
			//  Set up the coordinates at which to draw the image.
			g2.drawImage(this.bang, (int) this.bXPosition-(this.bang.getWidth(null)/2), (int) this.bYPosition-(this.bang.getHeight(null)/2), null);
		}


		public void drawMushroom(Graphics2D g2){
			//  Set up the coordinates at which to draw the image.
			g2.drawImage(this.mushroomCloud, (int) this.mXPosition-(this.mushroomCloud.getWidth(null)/2), (int) this.mYPosition-(this.mushroomCloud.getHeight(null)/2), null);
		}


		public void setBangXPosition(double newPosition){
			this.bXPosition = newPosition;
		}


		public void setBangYPosition(double newPosition){
			this.bYPosition = newPosition;
		}


		public void setMushroomXPosition(double newPosition){
			this.mXPosition = newPosition;
		}


		public void setMushroomYPosition(double newPosition){
			this.mYPosition = newPosition;
		}
	}
}
