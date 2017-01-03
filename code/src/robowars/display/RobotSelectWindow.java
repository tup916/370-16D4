/*  RobotSelectWindow.java
 *  Created by: Nickolas Gough
 *  Purpose: Models the robot select window that will be displayed when the user is selecting the AI that will play.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  14/07/2016 - Nickolas Gough : Created the file and implemented the title screen.
 *  16/07/2016 - Nickolas Gough : Changed public specifiers that should have been private.
 */

package robowars.display;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RobotSelectWindow extends JFrame{

	
	/**
	 *  Default serial ID. 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 *  Store the desired dimensions for the menus.
	 */
	private final Dimension robotMenuDimensions = new Dimension(150, 50);

	
	/**
	 *  Store the padding.
	 */
	private final int padding = 50;
	
	
	/**
	 *  Store the display component. 
	 */
	private Display display;
	
	
	/**
	 *  The constructor the robot select window.
	 *  @postcondition The robot select window is constructed.
	 */
	public RobotSelectWindow(Display display){
		//  Store the display component.
		this.display = display;
		
		//  Create the window that will be displayed for selecting AIs.
		this.setLayout(new BorderLayout());
		Dimension windowSize = new Dimension(this.display.getWidth()/2, this.display.getHeight()/2);
		this.setSize(windowSize);
		this.setPreferredSize(windowSize);
		this.setLocationRelativeTo(null);
		this.setBackground(this.getBackground());
		this.setTitle("Robot Selection");
		this.setAlwaysOnTop(true);
		
		//  Add the robot and OK panel to the robot window.
		JPanel robotPanel = this.createRobotsPanel();
		JPanel okayPanel = this.createOKPanel();
		this.add(robotPanel, BorderLayout.CENTER);
		this.add(okayPanel, BorderLayout.SOUTH);
		this.pack();
	}
	
	
	/**
	 *  Constructs a robot panel.
	 *  @param robot - A string representing which robot panel to create. 
	 *  @return A robot panel.
	 *  @postcondition A robot panel is created.
	 */
	private JPanel createRobotPanel(String robot){
		//  Create the panel for the scout.
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		panel.setBackground(this.display.getBackground());
		JLabel label = new JLabel(robot);
		JComboBox<String> combo = new JComboBox<String>();
		combo.setPreferredSize(this.robotMenuDimensions);
		combo.addItem("Select " + robot);
		panel.add(label);
		panel.add(Box.createHorizontalStrut(this.padding));
		panel.add(combo);

		return panel;
	}
	
	
	/**
	 *  Constructs the robots panel.
	 *  @return The robots panel.
	 *  @postcondition The robots panel is constructed.
	 */
	private JPanel createRobotsPanel(){
		//  Create the three robot panels.
		JPanel scoutPanel = this.createRobotPanel("Scout");
		JPanel sniperPanel = this.createRobotPanel("Sniper");
		JPanel tankPanel = this.createRobotPanel("Tank");
		
		//  Create the panel that will store the robot panels.
		JPanel robotPanel = new JPanel();
		robotPanel.setLayout(new BoxLayout(robotPanel, BoxLayout.Y_AXIS));
		robotPanel.setBackground(this.display.getBackground());
		robotPanel.add(scoutPanel);
		robotPanel.add(sniperPanel);
		robotPanel.add(tankPanel);
		
		return robotPanel;
	}
	
	
	/**
	 *  Constructs the OK button.
	 *  @return The OK button.
	 *  @postcondition The OK button is constructed.
	 */
	private JButton createOKButton(){
		//  Create the bottom button.
		JButton okayButton = new JButton("OK");
		okayButton.setAlignmentX(CENTER_ALIGNMENT);
		okayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				setVisible(false);
			}
		});
		
		return okayButton;
	}
	

	/**
	 *  Constructs the OK panel.
	 *  @return The OK Panel.
	 *  @postcondition The OK panel is constructed.
	 */
	private JPanel createOKPanel(){
		//  Create the OK button.
		JButton okayButton = this.createOKButton();
		
		//  Create the panel that will house the okay button followed by some vertical space.
		JPanel okayPanel = new JPanel();
		okayPanel.setLayout(new BoxLayout(okayPanel, BoxLayout.Y_AXIS));
		okayPanel.setBackground(this.display.getBackground());
		okayPanel.add(okayButton);
		okayPanel.add(Box.createVerticalStrut(this.padding));
		
		return okayPanel;
	}
}
