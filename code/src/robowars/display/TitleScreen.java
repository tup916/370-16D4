/*  TitleScreen.java
 *  Created by: Nickolas Gough
 *  Purpose: Models the title screen within the Display component. The title screen is the first screen shown to the user at startup.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  14/07/2016 - Nickolas Gough : Created the file and implemented the title screen.
 *  16/07/2016 - Nickolas Gough : Changed public specifiers that should have been private.
 */

package robowars.display;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TitleScreen extends Screen{
	
	/**
	 *  The default serial ID. 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 *  Store the Display object to enable the buttons to manipulate the Display.
	 */
	private Display display;
	
	
	/**
	 *  Store the desired size of the buttons.
	 */
	private final Dimension buttonSize = new Dimension(125, 30);

	
	/**
	 *  Store the desired padding.
	 */
	private final int padding = 50;
	
	
	/**
	 *  The constructor for the title Screen.
	 *  @postcondition A title Screen object is created as desired.
	 */
	public TitleScreen(Display display, Map<String, AbstractAction> titleActions){
		//  Assign this Screen to be the title.
		super(ScreenEnum.TITLE);

		//  Store the Display.
		this.display = display;

		//  Set the background, visibility, and the layout.
		this.setBackground(this.display.getBackground());
		this.setVisible(false);
		this.setLayout(new BorderLayout());
		
		//  Create the text panel and add it to the title Screen.
		JPanel textPanel = this.createTextPanel();
		this.add(textPanel, BorderLayout.NORTH);
		
		//  Create the button panel and add it to the Screen.
		JPanel buttonPanel = this.createButtonsPanel(titleActions);
		this.add(buttonPanel, BorderLayout.CENTER);
	}
	
	
	/**
	 *  Creates the new game button.
	 *  @return The new game button.
	 *  @postcondition Constructs the new game button.
	 */
	private JButton createNewGameButton(AbstractAction a){
		//  Create the new game button.
		JButton newGame = new JButton(a);
		newGame.setText("New Game");
		this.addButtonName(newGame.getText());
		newGame.setMinimumSize(this.buttonSize);
		newGame.setMaximumSize(this.buttonSize);
		newGame.setAlignmentX(CENTER_ALIGNMENT);

		return newGame;
	}
	

	/**
	 *  Creates the robot archive button.
	 *  @return The robot archive button.
	 *  @postcondition Constructs the robot archive button.
	 */
	private JButton createRobotArchiveButton(AbstractAction a){
		//  Create the robot archive button.
		JButton robotArchive = new JButton(a);
		robotArchive.setText("Robot Archive");
		this.addButtonName(robotArchive.getText());
		robotArchive.setMinimumSize(this.buttonSize);
		robotArchive.setMaximumSize(this.buttonSize);
		robotArchive.setAlignmentX(CENTER_ALIGNMENT);
		
		return robotArchive;
	}
	
	
	/**
	 *  Creates the settings button.
	 *  @return The settings button.
	 *  @postcondition Constructs the settings button.
	 */
	private JButton createSettingsButton(AbstractAction a){
		//  Create the settings button.
		JButton settings = new JButton(a);
		settings.setText("Settings");
		this.addButtonName(settings.getText());
		settings.setMinimumSize(this.buttonSize);
		settings.setMaximumSize(this.buttonSize);
		settings.setAlignmentX(CENTER_ALIGNMENT);

		
		
		return settings;
	}
	

	/**
	 *  Creates the exit button.
	 *  @return The exit button
	 *  @postcondition Constructs the exit button.
	 */
	private JButton createExitButton(AbstractAction a){
		//  Create the exit button.
		JButton exit = new JButton(a);
		exit.setText("Exit");
		this.addButtonName(exit.getText());
		exit.setMinimumSize(this.buttonSize);
		exit.setMaximumSize(this.buttonSize);
		exit.setAlignmentX(CENTER_ALIGNMENT);

		return exit;
	}


	/**
	 *  Creates the text panel to be placed at the top of the title screen.
	 *  @return The text panel.
	 *  @postcondition Constructs the text panel.
	 */
	private JPanel createTextPanel(){
		//  Create the label that will show the Robo-Wars text.
		JLabel roboWars = new JLabel("Robo-Wars");
		int fontSize = 250;
		roboWars.setFont(new Font(roboWars.getFont().getFontName(), Font.PLAIN, fontSize));

		//  Add the text to the title Screen.
		JPanel textPanel = new JPanel();
		textPanel.setBackground(this.display.getBackground());
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		int padding = 100;
		textPanel.add(Box.createVerticalStrut(padding));
		textPanel.add(roboWars);
		textPanel.setAlignmentX(CENTER_ALIGNMENT);
		roboWars.setAlignmentX(CENTER_ALIGNMENT);
		
		return textPanel;
	}
	
	
	/**
	 *  Construct the buttons panel.
	 *  @return The buttons panel.
	 *  @postcondition Constructs the buttons panel.
	 */
	private JPanel createButtonsPanel(Map<String, AbstractAction> titleActions){
		//  Add the buttons to the title Screen.
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(this.display.getBackground());
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(Box.createVerticalStrut(this.padding));

		//  Create the three buttons. 
		JButton newGame = this.createNewGameButton(titleActions.get("newGame"));
		JButton robotArchive = this.createRobotArchiveButton(titleActions.get("archive"));
		JButton settings = this.createSettingsButton(titleActions.get("settings"));
		JButton exit = this.createExitButton(titleActions.get("exit"));

		//  For demo only! Removing broken stuff.
		
		robotArchive.setVisible(false);
		settings.setVisible(false);
		
		
		//  Create the button panel and add the buttons to the panel.
		buttonPanel.add(Box.createVerticalStrut(this.padding));
		buttonPanel.add(newGame);
		buttonPanel.add(Box.createVerticalStrut(this.padding));
		buttonPanel.add(robotArchive);
		buttonPanel.add(Box.createVerticalStrut(this.padding));
		buttonPanel.add(settings);
		buttonPanel.add(Box.createVerticalStrut(this.padding));
		buttonPanel.add(exit);
		
		return buttonPanel;
	}

}
