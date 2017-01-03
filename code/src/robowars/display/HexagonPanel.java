/*  HexagonPanel.java
 *  Created by: Nickolas Gough
 *  Purpose: Draws a hexagon at the center and places combo boxes at the end points of the lines extending from the hexagon.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  14/07/2016 - Nickolas Gough : Created the file and implemented the hexagon panel.
 */

package robowars.display;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;

import javax.swing.JPanel;

public class HexagonPanel extends JPanel{
	
	
	/**
	 *   Default serial ID.
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 *  Store the desired dimensions for the hexagon.
	 */
	private Dimension hexagonDimensions;
	
	
	/**
	 *  Store the center where the hexagon should be drawn.
	 */
	private int xCenter;
	
	
	/**
	 *  Store the y center where the hexagon should be drawn.
	 */
	private int yCenter;
	
	
	/**
	 *  Store the points of where the menus should be placed.
	 */
	private Point[] points;
	
	
	/**
	 *  The default constructor.
	 *  @postcondition The hexagonPanel is constructed.
	 */
	public HexagonPanel(Dimension hexagonDimensions, int xCenter, int yCenter, Point[] points){
		//  Store the center coordinates.
		this.xCenter = xCenter;
		this.yCenter = yCenter;
		
		//  Store the points of the menus.
		this.points = points;
		
		//  Store the desired hexagon dimensions.
		this.hexagonDimensions = hexagonDimensions;
	}
	

	/**
	 *  Paints the hexagon and the lines extending from the hexagon.
	 *  @param g - The Java graphics.
	 *  @postcondition The hexagon in the center is drawn.
	 */
	public void paintComponent(Graphics g){		
		//  Recast the graphics to a graphics 2D.
		Graphics2D g2 = (Graphics2D) g;

		//  Declare the hexagon.
		Polygon hexagon = new Polygon();

		//  Define a thicker stroke.
		g2.setStroke(new BasicStroke(3.0f));

		// Draw the first line.
		g2.drawLine(this.xCenter, this.yCenter, (int) this.points[0].getX(), (int) this.points[0].getY());

		// Draw the second line.
		g2.drawLine(this.xCenter, this.yCenter, (int) this.points[1].getX(), (int) this.points[1].getY());

		// Draw the third line.
		g2.drawLine(this.xCenter, this.yCenter, (int) this.points[2].getX(), (int) this.points[2].getY());

		// Draw the fourth line and store the end point.
		g2.drawLine(this.xCenter, this.yCenter, (int) this.points[3].getX(), (int) this.points[3].getY());

		// Draw the fifth line and store the end point.
		g2.drawLine(this.xCenter, this.yCenter, (int) this.points[4].getX(), (int) this.points[4].getY());

		// Draw the sixth line and store the end point.
		g2.drawLine(this.xCenter, this.yCenter, (int) this.points[5].getX(), (int) this.points[5].getY());

		// Define an even thicker stroke.
		g2.setStroke(new BasicStroke(5.0f));

		//  Initialize the hexagon.
		double a;
		for (int i=0; i < 7; i++){
			a = Math.PI / 3.0 * i;
			hexagon.addPoint((int)(Math.round(this.xCenter + Math.sin(a) * this.hexagonDimensions.getHeight())), (int)(Math.round(this.yCenter + Math.cos(a) * this.hexagonDimensions.getHeight())));
		}

		// Draw and color the hexagon.
		g2.setColor(Color.BLACK);
		g2.drawPolygon(hexagon);
		g2.setColor(Color.WHITE);
		g2.fillPolygon(hexagon);
	}
}
