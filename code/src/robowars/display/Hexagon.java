/*  Display.java
 *  Created by: Nickolas Gough
 *  Purpose: Models the hexagons of the game Screen.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  20/07/2016 - Nickolas Gough : Created the file and began implementing the basics. 
 */

package robowars.display;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import javax.swing.JComponent;


public class Hexagon extends JComponent{
	
	
	/**
	 *  The default serial ID.
	 */
	private static final long serialVersionUID = 1L;


	/**
	 *  Store the dimensions of the hexagon.
	 */
	private int hexagonWidth;
	
	
	/**
	 *  Store the center x coordinate of the hexagon.
	 */
	private double xCenter;
	
	
	/**
	 *  Store the center y coordinate of the hexagon.
	 */
	private double yCenter;
	
	
	/**
	 *  Store the color to draw the hexagon.
	 */
	private Color color;
	
	
	/**
	 *  Store the original color of the hexagon.
	 */
	private Color originalColor;
	
	
	/**
	 *  Store the visibility of the hexagon.
	 */
	private boolean isVisible;
	
	
	/**
	 *  Construct the hexagon.
	 *  @param xCenter - The x coordinate of the hexagon center.
	 *  @param yCenter - The y coordinate of the hexagon center.
	 *  @param coords - The hexagon coordinates assigned to this hexagon.
	 */
	public Hexagon(double xCenter, double yCenter, int width, Color color){
		//  Store the center of the hexagon.
		this.color = color;
		this.originalColor = color;
		this.xCenter = xCenter;
		this.yCenter = yCenter;
		this.hexagonWidth = width;
	}
	

	/**
	 *  Paints the hexagon.
	 *  @postcondition The hexagon is painted.
	 */
	public void drawComponent(Graphics2D g2){
		// The hexagon to draw.
		Polygon hexagon = new Polygon();
		
		//  Set up the drawing environment.
		g2.setStroke(new BasicStroke(7.0f));

		//  Initialize the hexagon.
		double a;
		for (int i=0; i < 7; i++){
			a = Math.PI / 3.0 * i;
			hexagon.addPoint((int) (Math.ceil(this.xCenter + Math.sin(a) * this.hexagonWidth)), ((int) Math.ceil((this.yCenter + Math.cos(a) * this.hexagonWidth))));
		}
		
		//  Draw the hexagon.
		g2.setColor(Color.BLACK);
		g2.drawPolygon(hexagon);
		g2.setColor(this.color);
		g2.fillPolygon(hexagon);
	}
	
	
	/**
	 *  Get the width of the hexagon.
	 */
	public int getWidth(){
		return this.hexagonWidth;
	}
	
	
	/**
	 *  Retrieve the center x coordinate of the hexagon.
	 */
	public double getXCenter(){
		return this.xCenter;
	}
	
	
	/**
	 *  Retrieve the center y coordinate of the hexagon.
	 */
	public double getYCenter(){
		return this.yCenter;
	}
	
	
	/**
	 *  Changes the color of the hexagon to be shaded.
	 */
	public void shade(){
		this.color = Color.WHITE;
	}
	
	
	/**
	 *  Determines if the hexagon is shaded.
	 *  @return True if the hexagon is shaded and false otherwise.
	 */
	public boolean isShaded(){
		return this.color == Color.WHITE;
	}
	
	
	/**
	 *  Is the hexagon visible?
	 *  @return True if the hexagon is set to be visible.
	 */
	public boolean isVisible(){
		return this.isVisible;
	}
	
	
	/**
	 *  Set the visibility of the hexagon.
	 */
	public void setVisible(boolean visibilty){
		this.isVisible = visibilty;
	}
	
	
	/**
	 *  Resets the original color of the hexagon.
	 */
	public void reset(){
		this.color = this.originalColor;
	}
	
	/**
	 *  Highlights the hexagon to be white.
	 */
	public void highlight(){
		this.color = Color.WHITE;
	}
	
	//  EXTENSION -  Need functions for different types of shading?
	//  Dark for fog of war, highlighted for spaces you can move to/shoot, etc.?
	
	/**
	 *  Retrieve the color of the hexagon.
	 *  @return The color of the hexagon.
	 *  @postcondition The color of the hexagon is retrieved.
	 */
	public Color getColor(){
		return this.color;
	}
}
