/*  ResultsScreen.java
 *  Created by: Nickolas Gough
 *  Purpose: Models the settings screen that will be displayed by the Display component.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  15/07/2016 - Nickolas Gough : Created the file and began implementing the results screen.
 *  16/07/2016 - Nickolas Gough : Updated method specifiers and finished most of the class.
 */

package robowars.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class ResultsScreen extends Screen{

	
	/**
	 *  The default serial ID. 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 *  Store the Display component.
	 */
	private Display display;
	
	
	/**
	 *  Store the desired padding.
	 */
	private int padding = 100;
	
	
	/**
	 *  Store the desired dimensions of the buttons.
	 */
	private Dimension buttonDimensions = new Dimension(100, 30);
	
	
	/**
	 *  Store the tabbed pane.
	 */
	private JTabbedPane tabPane;
	
	
	/**
	 *  Store the preferredSize of the tabbed pane. 
	 */
	private Dimension preferredSizeTabbedPane;
	
	
	/**
	 *  Constructs the results screen.
	 *  @param display - The Display component that will display the Screen.
	 *  @postcondition The results screen is constructed.
	 */
	public ResultsScreen(Display display, HashMap<String, AbstractAction> actions){
		//  Assign the Screen to be the results Screen.
		super(ScreenEnum.RESULTS);

		//  Store the Display component.
		this.display = display;

		//  Determine the preferred size of the tabbed pane.
		this.preferredSizeTabbedPane = new Dimension(this.display.getWidth()/2, this.display.getHeight()/2);
		
		//  Instantiate the results Screen.
		this.setLayout(new BorderLayout());
		this.setBackground(this.display.getBackground());
		this.setVisible(false);
		
		//  Add the winner panel to the results Screen.
		JPanel winnerPanel = this.createWinnerPanel("Nick");
		this.add(winnerPanel, BorderLayout.NORTH);
		
		//  Add the tabbed pane to another panel to get the desired size.
		this.tabPane = this.createTabbedPane();
		JPanel tabbedPanel = new JPanel();
		tabbedPanel.setLayout(new BoxLayout(tabbedPanel, BoxLayout.Y_AXIS));
		tabbedPanel.setBackground(this.display.getBackground());
		tabbedPanel.add(Box.createVerticalStrut(this.padding));
		tabbedPanel.add(this.tabPane);
		
		//  Add the tabbed panel to the results Screen.
		this.add(tabbedPanel, BorderLayout.CENTER);
		
		//  Add the buttons panel to the results Screen.
		JPanel buttonsPanel = this.createButtonsPanel(actions);
		this.add(buttonsPanel, BorderLayout.SOUTH);
		
		//  Add fake results for testing.
		this.addResults("Red", "Scout", 0, 0, 0, 0, false);
		this.addResults("Red", "Sniper", 0, 0, 0, 0, false);
		this.addResults("Red", "Tank", 0, 0, 0, 0, false);
		this.addResults("Orange", "Scout", 0, 0, 0, 0, false);
		this.addResults("Orange", "Sniper", 0, 0, 0, 0, false);
		this.addResults("Orange", "Tank", 0, 0, 0, 0, false);
		this.addResults("Yellow", "Scout", 0, 0, 0, 0, false);
		this.addResults("Yellow", "Sniper", 0, 0, 0, 0, false);
		this.addResults("Yellow", "Tank", 0, 0, 0, 0, false);
		this.addResults("Green", "Scout", 0, 0, 0, 0, false);
		this.addResults("Green", "Sniper", 0, 0, 0, 0, false);
		this.addResults("Green", "Tank", 0, 0, 0, 0, false);
		this.addResults("Blue", "Scout", 0, 0, 0, 0, false);
		this.addResults("Blue", "Sniper", 0, 0, 0, 0, false);
		this.addResults("Blue", "Tank", 0, 0, 0, 0, false);
		this.addResults("Purple", "Scout", 0, 0, 0, 0, false);
		this.addResults("Purple", "Sniper", 0, 0, 0, 0, false);
		this.addResults("Purple", "Tank", 0, 0, 0, 0, false);
	}
	
	
	/**
	 *  Construct the winner panel to display the winner's name.
	 *  @param name - The name of the player that won the game.
	 *  @return The winner panel.
	 *  @postcondition The winner panel is constructed.
	 */
	private JPanel createWinnerPanel(String name){
		//  Create the Label that will display the winner.
		JLabel winnerLabel = new JLabel(name + " won the game!");
		int fontSize = 75;
		winnerLabel.setFont(new Font(winnerLabel.getFont().getFontName(), Font.PLAIN, fontSize));
		
		//  Add the winner label to the winner panel.
		JPanel winnerPanel = new JPanel();
		winnerPanel.setBackground(this.display.getBackground());
		winnerPanel.add(winnerLabel);
		winnerLabel.setAlignmentX(CENTER_ALIGNMENT);
		
		return winnerPanel;
	}
	
	
	/**
	 *  Constructs the panel that will store the results for the robots.
	 *  @param robot - A string representing the robot.
	 *  @param healthLeft - The remaining health of the robot.
	 *  @param damageDealt - The amount of damage dealt by the robot.
	 *  @param damageTaken - The amount of damage the robot has taken.
	 *  @param moved - The number of spaces the robot has moved.
	 *  @param alive - Indicates if the robot is still alive.
	 *  @return - The labels panel storing the results for the robot.
	 *  @postcondition The labels panel is constructed for the robot.
	 */
	private JPanel createLabelsPanel(Color color, String robot, int healthLeft, int damageDealt, int damageTaken, int moved, boolean alive){
		//  Construct the labels panel.
		JPanel labelsPanel = new JPanel();
		labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));
		labelsPanel.setBackground(color);
		
		//  Create the labels for displaying the results.
		JLabel robotLabel = new JLabel(robot);
		JLabel healthLeftLabel = new JLabel("Health Left: " + healthLeft);
		JLabel damageDealtLabel = new JLabel("Damage Dealt: " + damageDealt);
		JLabel damageTakenLabel = new JLabel("Damage Taken: " + damageTaken);
		JLabel movedLabel = new JLabel("Spaces Moved: " +moved);
		JLabel aliveLabel = new JLabel("Is alive: " + alive);
		
		//  Add the labels to the panel.
		labelsPanel.add(robotLabel);
		labelsPanel.add(Box.createVerticalStrut(2 * this.padding));
		labelsPanel.add(healthLeftLabel);
		labelsPanel.add(damageDealtLabel);
		labelsPanel.add(damageTakenLabel);
		labelsPanel.add(movedLabel);
		labelsPanel.add(aliveLabel);
		
		return labelsPanel;
	}
	
	
	/**
	 *  Constructs the tab panel.
	 *  @param color - The color assigned to the tab panel.
	 *  @return - The tab panel.
	 *  @postcondition The tab panel is constructed.
	 */
	private JPanel createTabPanel(Color color){
		//  Construct the tab panel.
		JPanel tabPanel = new JPanel();
		tabPanel.setLayout(new FlowLayout());
		tabPanel.setBackground(color);
		tabPanel.setPreferredSize(this.preferredSizeTabbedPane);
		
		return tabPanel;
	}

	/**
	 *  Constructs the tabbed pane.
	 *  @return The tabbed pane.
	 *  @postcondition The tabbed pane is constructed.
	 */
	private JTabbedPane createTabbedPane(){
		//  Construct the panels that will be inserted into the tabbed pane.
		JPanel redPanel = this.createTabPanel(Color.RED);
		JPanel orangePanel = this.createTabPanel(Color.ORANGE);
		JPanel yellowPanel = this.createTabPanel(Color.YELLOW);
		JPanel greenPanel = this.createTabPanel(Color.GREEN);
		JPanel bluePanel = this.createTabPanel(Color.BLUE);
		JPanel purplePanel = this.createTabPanel(Color.MAGENTA);

		//  Construct the tabbed pane.
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBackground(this.display.getBackground());
		tabbedPane.setMaximumSize(this.preferredSizeTabbedPane);
		tabbedPane.addTab("Red", redPanel);
		tabbedPane.addTab("Orange", orangePanel);
		tabbedPane.addTab("Yellow", yellowPanel);
		tabbedPane.addTab("Green", greenPanel);
		tabbedPane.addTab("Blue", bluePanel);
		tabbedPane.addTab("Purple", purplePanel);
		
		return tabbedPane;
	}

	
	/**
	 *  Adds the results of the robot owned by the team color.
	 *  @param teamColor - A string representing the color of the team being updated.
	 *  @param robotType - A string representing the type of the robot being updated.
	 *  @param healthLeft - The amount of remaining health.
	 *  @param damageDealt - The amount of damage dealt.
	 *  @param damageTaken - The amount of damage taken.
	 *  @param moved - The amount of spaces moved.
	 *  @param alive - The status of the robot's life.
	 *  @throws RuntimeException when the tabbed pane is not instantiated or the color or robot type does not exist.
	 *  @postcondition The results are added to the tabbed pane in the correct tab.
	 */
	public void addResults(String teamColor, String robotType, int healthLeft, int damageDealt, int damageTaken, int moved, boolean alive) throws RuntimeException{
		//  Check that the tabbed pane has beein instantiated.
		if (this.tabPane == null){
			throw new RuntimeException("Error: The tabbed pane must be instantiated before results can be added.");
		}
		
		//  Determine which panel to add the results to.
		JPanel addPanel = null;
		switch(teamColor){
		case "Red":
			addPanel = (JPanel) this.tabPane.getComponentAt(0);
			break;
		case "Orange":
			addPanel = (JPanel) this.tabPane.getComponentAt(1);
			break;
		case "Yellow":
			addPanel = (JPanel) this.tabPane.getComponentAt(2);
			break;
		case "Green":
			addPanel = (JPanel) this.tabPane.getComponentAt(3);
			break;
		case "Blue":
			addPanel = (JPanel) this.tabPane.getComponentAt(4);
			break;
		case "Purple":
			addPanel = (JPanel) this.tabPane.getComponentAt(5);
			break;
		default:
			throw new RuntimeException("The specified team does not exist.");
		}
		
		//  Add the results to the panel.
		addPanel.add(this.createLabelsPanel(addPanel.getBackground(), robotType, healthLeft, damageDealt, damageTaken, moved, alive));
	
		//  Add horizontal space if necessary.
		if (addPanel.getComponentCount()-1 < 3){
			addPanel.add(Box.createHorizontalStrut(this.padding));
		}
	}
	
	
	/**
	 *  Constructs the title button.
	 *  @return The title button.
	 *  @postcondition The title button is constructed.
	 */
	private JButton createTitleButton(AbstractAction a){
		//  Create the title button.
		JButton titleButton = new JButton(a);
		titleButton.setText("Title Page");
		this.addButtonName(titleButton.getText());
		titleButton.setPreferredSize(this.buttonDimensions);
//		titleButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event){
//				display.switchTo(ScreenEnum.TITLE);
//			}
//		});
		
		return titleButton;
	}
	

	/**
	 *  Create the view log button.
	 *  @return The view log button.
	 *  @postcondition The view log button is created.
	 */
	private JButton createViewLogButton(AbstractAction a){
		//  Create the title button.
		JButton viewLogButton = new JButton(a);
		viewLogButton.setText("View Log");
		this.addButtonName(viewLogButton.getText());
		viewLogButton.setPreferredSize(this.buttonDimensions);

		return viewLogButton;
	}
	

	/**
	 *  Constructs the rematch button.
	 *  @return The rematch button.
	 *  @postcondition The rematch button is constructed.
	 */
	private JButton createRematchButton(AbstractAction a){
		//  Create the title button.
		JButton rematchButton = new JButton(a);
		rematchButton.setText("Rematch");
		this.addButtonName(rematchButton.getText());
		rematchButton.setPreferredSize(this.buttonDimensions);

		return rematchButton;
	}
	
	
	/**
	 *  Constructs the buttons panel.
	 *  @return The buttons panel.
	 *  @postcondition The buttons panel is constructed.
	 */
	private JPanel createButtonsPanel(HashMap<String, AbstractAction> actions){
		//  Create the cancel and save buttons.
		JButton title = this.createTitleButton(actions.get("title"));
		JButton viewLog = this.createViewLogButton(actions.get("viewLog"));
		JButton rematch = this.createRematchButton(actions.get("rematch"));

		//  Create the buttons panel.
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout());
		buttonsPanel.setBackground(this.display.getBackground());
		buttonsPanel.add(title);
		buttonsPanel.add(Box.createHorizontalStrut(this.padding));
		buttonsPanel.add(viewLog);
		buttonsPanel.add(Box.createHorizontalStrut(this.padding));
		buttonsPanel.add(rematch);
		
		//  Add the buttons panel to another horizontal panel to create vertical space.
		JPanel horizontalPanel = new JPanel();
		horizontalPanel.setLayout(new BoxLayout(horizontalPanel, BoxLayout.Y_AXIS));
		horizontalPanel.setBackground(this.display.getBackground());
		horizontalPanel.add(buttonsPanel);
		horizontalPanel.add(Box.createVerticalStrut(this.padding));

		return horizontalPanel;
	}
}
