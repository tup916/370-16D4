/*  RobotArchiveScreen.java
 *  Created by: Nickolas Gough
 *  Purpose: Models the settings screen that will be displayed by the Display component.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  16/07/2016 - Nickolas Gough : Created the file and began implementing the class.
 *  16/07/2016 - Nickolas Gough : Continue working on the file. Creating the left side of the screen.
 *  20/07/2016 - Nickolas Gough : Mostly completed the Robot archive screen. Need to figure out how to get the files and what to do with them.
 */

package robowars.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class RobotArchiveScreen extends Screen{
	
	
	/**
	 *  The default serial ID.
	 */
	private static final long serialVersionUID = 1L;


	/**
	 *  Store the desired padding.
	 */
	private final int padding = 100;
	
	
	/**
	 *  Store the Display component.
	 */
	private Display display;
	
	
	/**
	 *  Store the labels that will display the statistics of the robots.
	 */
	private JLabel[] statsLabels;
	
	
	/**
	 *  Store the text field that will be used to perform a search.
	 */
	private JTextField textField;
	
	
	/**
	 *  Store the text pane that will enumerate the robot files.
	 */
	private JPanel textPane;
	
	
	/**
	 *  Store the dimensions for the text pane.
	 */
	private final Dimension textPaneDimensions = new Dimension(200, 200);

	
	/**
	 *  Store the dimensions for the stats panel.
	 */
	private final Dimension statsPanelDimensions = new Dimension(200, 400);
	
	
	/**
	 *  Store the desired button dimensions.
	 */
	private final Dimension buttonDimensions = new Dimension(100, 30);
	
	
	/**
	 *  Store the desired dimensions for the text field.
	 */
	private final Dimension textFieldDimensions = new Dimension(150, 30);
	
	
	/**
	 *  Store the currently enumerated robot label.
	 */
	private RobotLabel currentLabel;
	
	
	/**
	 *  Store the file chooser for registering robots. 
	 */
	private final JFileChooser chooser = new JFileChooser();
	
	
	/**
	 *  Constructs the robot archive Screen.
	 *  @param display - The Display component that will display all the Screens.
	 *  @postcondition The robot archive Screen is constructed.
	 */
	public RobotArchiveScreen(Display display, HashMap<String, AbstractAction> actions){
		//  Assign the Screen to be the game options Screen.
		super(ScreenEnum.ROBOTARCHIVE);

		//  Store the Display component.
		this.display = display;
		
		//  Instantiate the collection of labels.
		this.statsLabels = new JLabel[7];

		//  Initialize the game options screen.
		this.setBackground(this.display.getBackground());
		this.setVisible(false);
		this.setLayout(new BorderLayout());
		
		//  Add the text panel to the top of the Screen.
		JPanel textPanel = this.createTextPanel();
		this.add(textPanel, BorderLayout.NORTH);
		
		//  Add the right and left panels.
		JPanel splitPane = new JPanel();
		splitPane.setLayout(new FlowLayout());
		splitPane.setBackground(this.display.getBackground());
		JPanel rightPanel = this.createRightPanel(actions);
		JPanel leftPanel = this.createLeftPanel(actions);
		splitPane.add(leftPanel);
		splitPane.add(Box.createHorizontalStrut(this.padding));
		splitPane.add(rightPanel);
		
		//  Add the bottom panel.
		JPanel bottomPanel = this.createBottomPanel(actions);
		this.add(bottomPanel, BorderLayout.SOUTH);
		
		//  Add some text for testing.
		this.addEnumeration("nickolas");
		this.addEnumeration("nickolas");
		this.addEnumeration("nickolas");
		this.addEnumeration("nickolas");
		this.addEnumeration("nickolas");
		this.addEnumeration("nickolas");
		this.addEnumeration("nickolas");
		this.addEnumeration("nickolas");
		this.addEnumeration("nickolas");
		this.addEnumeration("nickolas");
		this.addEnumeration("nickolas");
		this.addEnumeration("nickolas");
		this.addEnumeration("nickolas");
		
		this.add(splitPane, BorderLayout.CENTER);
	}


	/**
	 *  Construct the text panel that will display the top text.
	 *  @return The text panel
	 *  @postcondition The text panel is constructed.
	 */
	private JPanel createTextPanel(){
		//  Create the label that will contain the top text.
		JLabel text = new JLabel("Robot Archive");
		int fontSize = 75;
		text.setFont(new Font(text.getFont().getFontName(), Font.PLAIN, fontSize));

		//  Place the text into a panel and add it to the game options screen.
		JPanel textPanel = new JPanel();
		textPanel.setBackground(this.display.getBackground());
		textPanel.add(text);
		textPanel.setAlignmentX(CENTER_ALIGNMENT);

		return textPanel;
	}


	/**
	 *  Constructs the robot stats panel.
	 *  @return The robot stats panel.
	 *  @postcondition The robot stats panel is constructed.
	 */
	private JPanel createRobotStatsLabelPanel(){
		//  Construct the panel that will store the robot statistics label.
		JPanel statsPanel = new JPanel();
		statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
		statsPanel.setBackground(Color.LIGHT_GRAY);
		
		//  Add the robot statistics label to the stats panel.
		JLabel statsLabel = new JLabel("Robot Statistics");
		int fontSize = 25;
		statsLabel.setFont(new Font(statsLabel.getFont().getName(), Font.BOLD, fontSize));
		statsPanel.add(statsLabel);

		return statsPanel;
	}
	
	
	/**
	 *  Constructs the panel containing all the statistics of the robot file.
	 *  @return The panel containing the statistics of the robot.
	 *  @postcondition The panel containing the statistics of the robot is constructed.
	 */
	private JPanel createRobotStatsPanel(){
		//  Construct the stats panel.
		JPanel statsPanel = new JPanel();
		statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
		statsPanel.setBackground(Color.LIGHT_GRAY);
		
		//  Add the labels containing the data.
		this.statsLabels[0] = new JLabel("Team: ");
		this.statsLabels[1] = new JLabel("Type: ");
		this.statsLabels[2] = new JLabel("Wins: ");
		this.statsLabels[3] = new JLabel("Damage Dealt: ");
		this.statsLabels[4] = new JLabel("Damage Taken: ");
		this.statsLabels[5] = new JLabel("Spaces Moved: ");
		this.statsLabels[6] = new JLabel("Deaths: ");
		statsPanel.add(this.statsLabels[0]);
		statsPanel.add(this.statsLabels[1]);
		statsPanel.add(this.statsLabels[2]);
		statsPanel.add(this.statsLabels[3]);
		statsPanel.add(this.statsLabels[4]);
		statsPanel.add(this.statsLabels[5]);
		statsPanel.add(this.statsLabels[6]);
		
		return statsPanel;
	}
	
	/**
	 *  Sets the text containing all the statistics of the robot file.
	 *  @param team - The team of the robot.
	 *  @param type - The type of the robot.
	 *  @param wins - The number of wins of the robot.
	 *  @param damageDealt - The amount of damage the robot has dealt.
	 *  @param damageTaken - The amount of damage the robot has dealt.
	 *  @param spacesMoved - The number of spaces the robot has moved.
	 *  @param deaths - The number of times the robot has died.
	 *  @return The panel containing the statistics of the robot.
	 *  @postcondition The statistics being displayed are set.
	 */
	public void setStats(String team, String type, int wins, int damageDealt, int damageTaken, int spacesMoved, int deaths){
		this.statsLabels[0] = new JLabel("Team: " + team);
		this.statsLabels[1].setText("Type: " + type);
		this.statsLabels[2].setText("Wins: " + wins);
		this.statsLabels[3].setText("Damage Dealt: " + damageDealt);
		this.statsLabels[4].setText("Damage Taken: " + damageTaken);
		this.statsLabels[5].setText("Spaces Moved: " + spacesMoved);
		this.statsLabels[6].setText("Deaths: " + deaths);
	}
	
	
	/**
	 *  Constructs the robots panel that will display the robot stats.
	 *  @return The robot statistics panel.
	 *  @postcondition The robot statistics panel is constructed.
	 */
	private JPanel createRobotsPanel(){
		//  Construct the robot stats panel.
		JPanel statsPanel = new JPanel();
		statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
		statsPanel.setBackground(Color.LIGHT_GRAY);
		statsPanel.setPreferredSize(this.statsPanelDimensions);
		
		//  Add the label panel and the results panel.
		JPanel labelPanel = this.createRobotStatsLabelPanel();
		JPanel resultsPanel = this.createRobotStatsPanel();
		statsPanel.add(labelPanel);
		statsPanel.add(Box.createVerticalStrut(this.padding/2));
		statsPanel.add(resultsPanel);
		
		return statsPanel;
	}
	
	
	/**
	 *  Constructs the register button.
	 *  @return The register button.
	 *  @postcondition The register button is constructed.
	 */
	private JButton createRegisterButton(AbstractAction a){
		//  Create the register button.
		JButton register = new JButton(a);
		register.setText("Register");
		this.addButtonName(register.getText());
		register.setPreferredSize(new Dimension(this.buttonDimensions.width*2, this.buttonDimensions.height));
//		register.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event){
//				int returnValue = chooser.showOpenDialog(null);
//				if (0 == returnValue){
//					String name = JOptionPane.showInputDialog("Please enter the name to assign the Robot");
//				}
//			}
//		});
		
		return register;
	}
	
	
	/**
	 *  Parses the robot file to determine it is okay to register.
	 *  @return True if the robot file is okay to return, false otherwise.
	 */
	@SuppressWarnings("unused")
	private boolean parse(){
		return true;
	}
	
	
	/**
	 *  Register the robot file. 
	 *  @postcondition The robot file is registered if it is okay to register.
	 */
	public void register(){
		
	}
	
	
	/**
	 *  Constructs the revise button.
	 *  @return The revise button.
	 *  @postcondition The revise button is constructed.
	 */
	private JButton createReviseButton(AbstractAction a){
		//  Create the revise button.
		JButton revise = new JButton(a);
		revise.setText("Revise");
		this.addButtonName(revise.getText());
		revise.setPreferredSize(new Dimension(this.buttonDimensions.width*2, this.buttonDimensions.height));
//		revise.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event){
//				try {
//					File file = new File(System.getProperty("user.dir") + "newfile.txt");
//					file.createNewFile();
//					Desktop.getDesktop().edit(file);
//					file.delete();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		});
		
		return revise;
	}
	
	
	/**
	 *  Constructs the retire button.
	 *  @return The retire button.
	 *  @postcondition The retire button is constructed.
	 */
	private JButton createRetireButton(AbstractAction a){
		//  Create the register button.
		JButton retire = new JButton(a);
		retire.setText("Retire");
		this.addButtonName(retire.getText());
		retire.setPreferredSize(new Dimension(this.buttonDimensions.width*2, this.buttonDimensions.height));
//		retire.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event){
//				//  Don't do anything if a robot is not selected.
//				if (currentLabel == null){
//					return;
//				}
//				
//				// Confirm the decisision.
//				int returnValue = JOptionPane.showConfirmDialog(null, "Are you sure you want to retire: " + currentLabel.getText() + "?");
//				if (returnValue == 0){
//					retire();
//				}
//			}
//		});
		
		return retire;
	}
	
	
	/**
	 *  Retires the robot.
	 *  @postcondition The robot is retired and its name is able to be recycled.
	 */
	public void retire(){
		
	}


	/**
	 *  Constructs the back button.
	 *  @return The back button.
	 *  @postcondition The back button is constructed.
	 */
	private JButton createBackButton(AbstractAction a){
		//  Create the register button.
		JButton back = new JButton(a);
		back.setText("Back");
		this.addButtonName(back.getText());
		back.setPreferredSize(this.buttonDimensions);
		back.setAlignmentX(CENTER_ALIGNMENT);
//		back.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event){
//				display.switchTo(ScreenEnum.TITLE);
//			}
//		});

		return back;
	}
	
	
	/**
	 *  Constructs the buttons panel.
	 *  @return The buttons panel.
	 *  @postcondition The buttons panel is constructed.
	 */
	private JPanel createButtonsPanel(HashMap<String, AbstractAction> actions){
		//  Create the buttons panel.
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
		buttonsPanel.setBackground(this.display.getBackground());
		
		//  Add the buttons to the buttons panel.
		JButton register = this.createRegisterButton(actions.get("register"));
		JButton revise = this.createReviseButton(actions.get("revise"));
		JButton retire = this.createRetireButton(actions.get("retire"));
		buttonsPanel.add(register);
		buttonsPanel.add(Box.createVerticalStrut(this.padding/3));
		buttonsPanel.add(revise);
		buttonsPanel.add(Box.createVerticalStrut(this.padding/3));
		buttonsPanel.add(retire);
		
		return buttonsPanel;
	}
	
	
	/**
	 *  Constructs the panel that will be displayed on the right of the Screen.
	 *  @return The right panel;
	 *  @postcondition The right panel is constructed.
	 */
	private JPanel createRightPanel(HashMap<String, AbstractAction> actions){
		//  Create the right panel.
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setBackground(this.display.getBackground());
		
		//  Add the buttons and the robot statistics panel.
		JPanel statsPanel = this.createRobotsPanel();
		JPanel buttonsPanel = this.createButtonsPanel(actions);
		rightPanel.add(Box.createVerticalStrut(2*this.padding));
		rightPanel.add(statsPanel);
		rightPanel.add(Box.createVerticalStrut(this.padding));
		rightPanel.add(buttonsPanel);
		
		return rightPanel;
	}
	
	
	/**
	 *  Constructs the search button.
	 *  @return The search button.
	 *  @postcondition The search button is constructed.
	 */
	private JButton createSearchButton(AbstractAction a){
		//  Construct the search button.
		JButton searchButton = new JButton(a);
		searchButton.setText("Search");
		this.addButtonName(searchButton.getText());
		searchButton.setPreferredSize(this.buttonDimensions);
//		searchButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event){
//				search();
//			}
//		});
		
		return searchButton;
	}
	
	
	/**
	 *  Constructs the search panel.
	 *  @return The search panel.
	 *  @postcondition The search panel is constructed.
	 */
	private JPanel createSearchPanel(HashMap<String, AbstractAction> actions){
		//  Construct the panel that will store the search text field and the search button.
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new FlowLayout());
		searchPanel.setBackground(this.display.getBackground());
		
		//  Add the search text field and the search button.
		this.textField = new JTextField("Search");
		this.textField.setPreferredSize(this.textFieldDimensions);
		JButton searchButton = this.createSearchButton(actions.get("search"));
		searchPanel.add(this.textField);
		searchPanel.add(searchButton);
		
		return searchPanel;
	}
	
	
	/**
	 *  Performs a search with the specified text and condition.
	 *  @postcondition A search is performed and the approproate results are displayed.
	 */
	@SuppressWarnings("unused")
	private void search(){
		
	}
	
	
	/**
	 *  Constructs the combo box that will specify the condition by which to oder the robots. 
	 *  @return The combo box used to select the ordering of robots.
	 *  @postcondition The combo box for selecting the search conditions is constructed.
	 */
	private JComboBox<String> createSearchComboBox(){
		//  Construct the combo box that will be used to select the condition by which to search.
		JComboBox<String> searchCondition = new JComboBox<String>();
		searchCondition.setPreferredSize(this.buttonDimensions);
		searchCondition.addItem("Sort By");
		searchCondition.addItem("Teams");
		searchCondition.addItem("Wins");
		searchCondition.addItem("Type");
		
		return searchCondition;
	}
	
	
	/**
	 *  Construct the text pane that will enumerate the robot files. 
	 *  @return The text pane that will store the robot files.
	 *  @postcondition The text pane that will enumerate the robot files is constructed.
	 */
	private JScrollPane createScrollPane(){
		//  Construct the text pane that will enumerate the robot files.
		JScrollPane textPane = new JScrollPane();
		textPane.setPreferredSize(this.textPaneDimensions);
		textPane.setBackground(this.display.getBackground());
		textPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	
		return textPane;
	}
	
	
	/**
	 *  Constructs the panel that will be used to enumerate the robots.
	 *  @return The enumeration panel.
	 *  @postcondition The enumeration panel is constructed.
	 */
	private JPanel createEnumerationPanel(){
		//  Create the panel to store the enumerated robots. 
		JPanel enumerationPanel = new JPanel();
		enumerationPanel.setLayout(new BoxLayout(enumerationPanel, BoxLayout.Y_AXIS));
		enumerationPanel.setBackground(this.display.getBackground());
		
		return enumerationPanel;
	}
	
	
	/**
	 * 	The specified robot name is added to the list of enumerated robots.
	 *  @param robotName - The name of the robot file that is to be added to the total enumeration.
	 *  @postcondition The robot file name is added to the list of enumerated robot files. 
	 */
	public void addEnumeration(String robotName){
		//  Add the robot name to the text within the text field.
		RobotLabel robot = new RobotLabel(robotName, this);
		this.textPane.add(robot);
	}
	
	
	/**
	 *  Constructs the enumeration panel.
	 *  @return The enumeration panel.
	 *  @postcondition The enumeration panel is constructed.
	 */
	private JPanel createEnumDisplayPanel(){
		//  Create the enumeration panel.
		JPanel enumPanel = new JPanel();
		enumPanel.setLayout(new BoxLayout(enumPanel, BoxLayout.Y_AXIS));
		enumPanel.setBackground(this.display.getBackground());
		
		//  Add the elements to the enum panel.
		this.textPane = this.createEnumerationPanel();
		JScrollPane scrollPane = this.createScrollPane();
		scrollPane.setViewportView(this.textPane);
		JLabel scrollTitle = new JLabel("Robots");
		int fontSize = 25;
		scrollTitle.setFont(new Font(scrollTitle.getFont().getFontName(), Font.PLAIN, fontSize));
		enumPanel.add(scrollTitle);
		enumPanel.add(scrollPane);
		
		return enumPanel;
	}
	
	
	/**
	 *  Constructs the left panel of the Screen.
	 *  @return The left panel of the Screen.
	 *  @postcondition The left panel of the Screen is constructed.
	 */
	private JPanel createLeftPanel(HashMap<String, AbstractAction> actions){
		//  Construct the left panel.
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setBackground(this.display.getBackground());
		
		//  Add the elements to the left panel.
		JPanel searchPanel = this.createSearchPanel(actions);
		JComboBox<String> searchCondition = this.createSearchComboBox();
		JPanel scrollPanel = this.createEnumDisplayPanel();
		leftPanel.add(searchPanel);
		leftPanel.add(Box.createVerticalStrut(this.padding/2));
		leftPanel.add(searchCondition);
		leftPanel.add(Box.createVerticalStrut(this.padding/2));
		leftPanel.add(scrollPanel);
		
		return leftPanel;
	}
	
	
	/**
	 *  Constructs the bottom panel.
	 *  @return The bottom panel.
	 *  @postcondition The bottom panel is constructed.
	 */
	private JPanel createBottomPanel(HashMap<String, AbstractAction> actions){
		//  Create the bottom panel.
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.setBackground(this.display.getBackground());
		
		//  Add the elements to the bottom panel.
		JButton back = this.createBackButton(actions.get("back"));
		bottomPanel.add(back);
		bottomPanel.add(Box.createVerticalStrut(this.padding));
		
		return bottomPanel;
	}
	
	
	/**
	 *  Accessor to retrieve the current label.
	 *  @return The current label.
	 */
	public RobotLabel getCurrentLabel(){
		return this.currentLabel;
	}
	
	
	/**
	 *  Mutator to set the current robot label.
	 *  @param label - The new current robot label.
	 *  @postcondition The current robot level is reassigned accordingly.
	 */
	public void setCurrentLabel(RobotLabel label){
		this.currentLabel = label;
	}
	
	public JFileChooser getChooser(){
		return this.chooser;
	}
}
