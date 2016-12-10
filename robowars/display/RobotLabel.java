/*  RobotLabel.java
 *  Created by: Nickolas Gough
 *  Purpose: Models a label that can be clicked to retrieve a robot file.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  20/07/2016 - Nickolas Gough : Created the file and began implementing the basics. 
 */

package robowars.display;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

public class RobotLabel extends JLabel{
	
	
	/**
	 *  The default serial ID.
	 */
	private static final long serialVersionUID = 1L;


	/**
	 *  Store the current robot label.
	 */
	private RobotArchiveScreen parent;
	
	
	/**
	 *  Store itself to refer to in the mouse listener.
	 */
	private RobotLabel thisLabel = this;

	
	/**
	 *  Store the preferred size of the labels.
	 */
	private final Dimension preferredDimension = new Dimension(300, 30);
	
	
	/**
	 *  Constructs an interactive label.
	 *  @param labelName - The name to be given to the label.
	 *  @postcondition The robot label is constructed.
	 */
	public RobotLabel(String labelName, RobotArchiveScreen parentScreen){
		// Create the label.
		super(labelName);
		
		//  Store the current robot label.
		this.parent = parentScreen;
		this.setBackground(this.parent.getBackground());
		this.setMinimumSize(this.preferredDimension);
		this.setMaximumSize(this.preferredDimension);
		
		//  Set the font size of the label.
		int fontSize = 20;
		this.setFont(new Font(this.getFont().getFontName(), Font.PLAIN, fontSize));
		
		//  Ensure the change in the robot label can be seen.
		this.setOpaque(true);
		
		/*
		 *  Make the label interactive.
		 */
		this.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent event){
				//  Don't try to set the background of a null label.
				if (parent.getCurrentLabel() != null){
					parent.getCurrentLabel().setBackground(Color.WHITE);
				}
				
				//  Set the current background of the label and store the current label.
				setBackground(Color.CYAN);
				parent.setCurrentLabel(thisLabel);
			}
			

			@Override
			public void mousePressed(MouseEvent e) {};

			
			@Override
			public void mouseReleased(MouseEvent e) {};

			
			@Override
			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			};

			
			@Override
			public void mouseExited(MouseEvent e) {};
		});
	}
}
