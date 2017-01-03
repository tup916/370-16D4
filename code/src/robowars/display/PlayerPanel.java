/*  PlayerPanel.java
 *  Created by: Nickolas Gough
 *  Purpose: Models the player panel used to display the stats of the player.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  28/11/2016 - Nickolas Gough : Created the file and began implementing the basics.
 */

package robowars.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PlayerPanel extends JPanel{

	
	/**
	 *  The default serial ID.
	 */
	private static final long serialVersionUID = 1L;


	/**
	 *  Store the image label.
	 */
	private JLabel imageLabel;
	
	
	/**
	 *  Store the player name label.
	 */
	private JLabel playerNameLabel;
	
	
	/**
	 *  Store the health label.
	 */
	private JLabel healthLabel;
	
	
	/**
	 *  Store the mobility label.
	 */
	private JLabel mobilityLabel;
	
	
	/**
	 *  Store the range label.
	 */
	private JLabel rangeLabel;
	
	
	/**
	 *  Store the attack label.
	 */
	private JLabel attackLabel;
	
	
	/**
	 *  Store the inner panel.
	 */
	private JPanel innerPanel;
	
	
	/**
	 *  Store the desired dimension of the player panel.
	 */
	private final Dimension playerPanelDimension = new Dimension(150, 150);
	
	
	/** 
	 *  Constructs a player panel.
	 *  @param color - The background color of the player panel.
	 *  @postcondition The player panel is constructed with the specified background.
	 */
	public PlayerPanel(Color color){
		//  Stats panel.
		this.innerPanel = new JPanel();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setPreferredSize(this.playerPanelDimension);
		this.setPreferredSize(this.playerPanelDimension);
		this.setMaximumSize(this.playerPanelDimension);
		this.setBackground(color);
		this.innerPanel.setLayout(new BoxLayout(this.innerPanel, BoxLayout.Y_AXIS));
		this.innerPanel.setBackground(color);
		this.innerPanel.setVisible(false);

		//  The name to display.
		this.playerNameLabel = new JLabel();
		this.playerNameLabel.setAlignmentX(CENTER_ALIGNMENT);
		this.add(this.playerNameLabel);

		//  The active image.
		JPanel imagePanel = new JPanel();
		imagePanel.setBackground(color);
		JLabel activeLabel = new JLabel("Active: ");
		this.imageLabel = new JLabel();
		imagePanel.add(activeLabel);
		imagePanel.add(this.imageLabel);
		this.innerPanel.add(imagePanel);

		//  The stats.
		JPanel panelOne = new JPanel();
		panelOne.setBackground(color);
		this.attackLabel = new JLabel("A: ");
		this.mobilityLabel = new JLabel("M: "); 
		panelOne.add(this.attackLabel);
		panelOne.add(this.mobilityLabel);
		this.innerPanel.add(panelOne);

		JPanel panelTwo = new JPanel();
		panelTwo.setBackground(color);
		this.healthLabel = new JLabel("H: ");
		this.rangeLabel = new JLabel("R: ");
		panelTwo.add(this.healthLabel);
		panelTwo.add(this.rangeLabel);
		this.innerPanel.add(panelTwo);
		
		this.add(this.innerPanel);
	}
	
	
	/**
	 *  Sets the stats to be displayed.
	 *  @param image - The image of the active piece to display.
	 *  @param name - The name of the player to display.
	 *  @param attack - The attack points of the active piece.
	 *  @param health - The remaining health of the active piece.
	 *  @param mobility - The mobility of the active piece.
	 *  @param range - The range of the active piece.
	 */
	public void setStats(Image image, String name, int attack, int health, int mobility, int range){
		//  Set the correct stats.
		this.imageLabel.setIcon(new ImageIcon(image));
		this.playerNameLabel.setText(name);
		this.mobilityLabel.setText("M: " + mobility);
		this.rangeLabel.setText("R:" + range);
		this.attackLabel.setText("A: " + attack);
		this.healthLabel.setText("H: " + health);
	}
	
	public void setStats(String name, int attack, int health, int mobility, int range){

		this.playerNameLabel.setText(name);
		this.mobilityLabel.setText("M: " + mobility);
		this.rangeLabel.setText("R:" + range);
		this.attackLabel.setText("A: " + attack);
		this.healthLabel.setText("H: " + health);
		
		System.out.println("set stat of the initial one");
	}
	
	
	/**
	 *  Shows the stats of the player.
	 *  @postcondition The inner panel is revealed so the stats can be seen.
	 */
	public void showStats(){
		//  Show the stats.
		this.innerPanel.setVisible(true);
		System.out.println("Player Panel - show stat");
	}
	
	
	/**
	 *  Hides the stats of the player.
	 *  @postcondition The stats of the player are hidden.
	 */
	public void hideStats(){
		//  Hide the stats.
		this.innerPanel.setVisible(false);
	}
}
