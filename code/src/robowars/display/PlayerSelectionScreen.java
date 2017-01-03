/*  PlayerSelectionScreen.java
 *  Created by: Nickolas Gough
 *  Purpose: Models the player selection screen that will be shown by the Display.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  14/07/2016 - Nickolas Gough : Created the file and implemented the title screen.
 *  16/07/2016 - Nickolas Gough : Changed public specifiers that should have been private.
 */

package robowars.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class PlayerSelectionScreen extends Screen{

	/**
	 *  The default serial ID.
	 */
	private static final long serialVersionUID = 1L;


	/**
	 *  Store the padding.
	 */
	private final int padding = 50;

	
	/**
	 *  Store the Display component.
	 */
	private Display display;
	
	
	/**
	 *  Store the desired dimensions of the text.
	 */
	private final Dimension textDimensions = new Dimension(100, 30);
	
	
	/**
	 *  Store the desired dimensions of the buttons.
	 */
	private final Dimension buttonDimensions = new Dimension(100, 30);
	
	
	/**
	 *  Store the buttons that will display the robot select window.
	 */
	private JButton[] playerSelectAIs;
	
	
	/**
	 *  Store the robot select window.
	 */
	private RobotSelectWindow robotWindow;
	
	
	/**
	 *  Store the radio buttons.
	 */
	private JRadioButton two;
	private JRadioButton three;
	private JRadioButton six;
	
	
	/**
	 *  Store the player select buttons.
	 */
	public ArrayList<JPanel> playerPanels;
	
	
	/**
	 *  Store the comboBox for selecting the sizes of the board. 
	 */
	private JComboBox<Integer> boardSizes;

	
	private JButton startGameButton;
	

	/**
	 *  Constructs the player selection Screen.
	 *  @postcondition Constructs the player selection screen.
	 */
	public PlayerSelectionScreen(Display display, HashMap<String, AbstractAction> actions){
		//  Assign the Screen to be the player selection Screen.
		super(ScreenEnum.PLAYERSELECTION);
		
		//  Store the Display component.
		this.display = display;
		
		//  Instantiate the buttons.
		this.playerSelectAIs = new JButton[6];
		this.playerPanels = new ArrayList<JPanel>();
		
		//  initialize the player selection Screen.
		this.setBackground(this.display.getBackground());
		this.setVisible(false);
		this.setLayout(new BorderLayout());

		// Create and add the text panel.
		JPanel textPanel = this.createTextPanel();
		this.add(textPanel, BorderLayout.NORTH);

		//  Add the center panel to the player selection Screen.
		JPanel centerPanel = this.createCenterPanel();
		JPanel bottomPanel = this.createBottomPanel(actions);
		this.add(centerPanel, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);
		
		//  Instantiate the robot select window.
		this.robotWindow = new RobotSelectWindow(this.display);

		//  Enable the window to be displayed when one of the select AI buttons are pushed.
		for (JButton AISelectButton : this.playerSelectAIs){
			AISelectButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event){
					robotWindow.setVisible(true);
					robotWindow.setLocationRelativeTo(null);
				}
			});
			
			//  DEMO ONLY!! Disabling AI selectTLE
			AISelectButton.setEnabled(false);
		}
		
		
	}

	
	/**
	 *  Constructs the text panel.
	 *  @return The text panel.
	 *  @postcondition The text panel is constructed.
	 */
	private JPanel createTextPanel(){
		//  Create the text label for the player selection Screen.
		JLabel text = new JLabel("Player Selection");
		int fontSize = 75;
		text.setFont(new Font(text.getFont().getFontName(), Font.PLAIN, fontSize));

		//  Place the top label into a text panel and add it to the player selection Screen.
		JPanel textPanel = new JPanel();
		textPanel.setBackground(this.display.getBackground());
		textPanel.add(text);
		textPanel.setAlignmentX(CENTER_ALIGNMENT);

		return textPanel;
	}


	/**
	 *  Creates the players panel.
	 *  @return The players panel.
	 *  @postcondition The players panel is constructed.
	 */
	private JPanel createPlayersPanel(){
		//  Create the radio buttons label.
		JLabel playersLabel = new JLabel("Number of Players: ");
		int fontSize = 30;
		playersLabel.setFont(new Font(playersLabel.getFont().getName(), Font.PLAIN, fontSize));
		playersLabel.setAlignmentX(CENTER_ALIGNMENT);

		//  Create the three radio buttons.
		ButtonGroup radioGroup = new ButtonGroup();
		this.two = new JRadioButton("Two");
		this.two.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				showPlayers();
			}
		});
		this.three = new JRadioButton("Three");
		this.three.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				showPlayers();
			}
		});
		this.six = new JRadioButton("Six");
		this.six.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				showPlayers();
			}
		});
		radioGroup.add(this.two);
		radioGroup.add(this.three);
		radioGroup.add(this.six);

		//  Place the radio buttons into a panel.
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new FlowLayout());
		radioPanel.setBackground(this.display.getBackground());
		radioPanel.add(this.two);
		radioPanel.add(Box.createHorizontalStrut(this.padding));
		radioPanel.add(this.three);
		radioPanel.add(Box.createHorizontalStrut(this.padding));
		radioPanel.add(this.six);
		
		//  Add the JComboBox.
		JLabel boardSize = new JLabel("BoardSize:");
		boardSize.setAlignmentX(CENTER_ALIGNMENT);
		this.boardSizes = new JComboBox<Integer>();
		this.boardSizes.setVisible(false);
		this.boardSizes.setPreferredSize(buttonDimensions);
		this.boardSizes.setMinimumSize(buttonDimensions);
		this.boardSizes.setMaximumSize(buttonDimensions);
		this.boardSizes.addItem(5);
		this.boardSizes.addItem(7);
		this.boardSizes.setSelectedItem(5);

		//  Place the players label and the radio buttons into another panel together.
		JPanel playersPanel = new JPanel();
		playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
		playersPanel.setBackground(this.getBackground());
		playersPanel.add(Box.createVerticalStrut(this.padding));
		playersPanel.add(playersLabel);
		playersPanel.add(Box.createVerticalStrut(this.padding));
		playersPanel.add(radioPanel);
		playersPanel.add(boardSize);
		playersPanel.add(this.boardSizes);
		playersPanel.add(Box.createVerticalStrut(this.padding));
		
		return playersPanel;
	}
	
	
	/**
	 *  Determine which player panels should be visible and show them.
	 *  @postcondition The necessary player panels are set to be visible.
	 */
	private void showPlayers(){
		//  Determine the number of players selected and then make the necessary number of player panels visible.
		int numberPlayers = 0;
		startGameButton.setEnabled(true);
		if (this.two.isSelected()){
			numberPlayers = 2;
			this.boardSizes.setVisible(false);
			this.boardSizes.setSelectedItem(5);
		}
		else if (this.three.isSelected()){
			numberPlayers = 3;
			this.boardSizes.setVisible(true);
		}
		else if (this.six.isSelected()){
			numberPlayers = 6;
			this.boardSizes.setVisible(false);
			this.boardSizes.setSelectedItem(7);
		}
		else {
			startGameButton.setEnabled(false);
		}
		
		//  Make the player panels visible.
		for (int n = 0; n < this.playerPanels.size(); n += 1){
			if (n < numberPlayers){
				this.playerPanels.get(n).setVisible(true);
			}
			else {
				this.playerPanels.get(n).setVisible(false);
			}
		}
		
		
		this.repaint();
	}
	

	/**
	 *  Create a player name panel.
	 *  @return A player name panel.
	 *  @postcondition A player name panel is constructed.
	 */
	private JPanel createPlayerNamePanel(int player){
		//  Create the first player's panel.
		JPanel playerPanel = new JPanel();
		playerPanel.setLayout(new FlowLayout());
		playerPanel.setBackground(this.display.getBackground());
		JTextField playerName = new JTextField("Player " + player);
		playerName.setName("NameField");
		JCheckBox playerAI = new JCheckBox("AI");
		playerAI.setName("AIField");
		//TODO Convert this to the new button style later?
		this.playerSelectAIs[player-1] = new JButton("Select AI");
		this.playerSelectAIs[player-1].setName("AIButton");
		this.playerSelectAIs[player-1].setBackground(Color.BLUE);
		this.playerSelectAIs[player-1].setForeground(Color.WHITE);
		playerPanel.add(playerName);
		playerPanel.add(playerAI);
		playerPanel.add(this.playerSelectAIs[player-1]);
		playerName.setPreferredSize(this.textDimensions);
		this.playerSelectAIs[player-1].setPreferredSize(this.buttonDimensions);
		
		return playerPanel;
	}
	

	/**
	 *  Constructs the names panel.
	 *  @return The names panel.
	 *  @postcondition The names panel is constructed.
	 */
	private JPanel createNamesPanel(){
		//  Create the left side panel for the player names.
		JPanel namesPanel = new JPanel();
		namesPanel.setLayout(new BoxLayout(namesPanel, BoxLayout.Y_AXIS));
		namesPanel.setBackground(this.display.getBackground());
		
		return namesPanel;
	}


	/**
	 *  Constructs the middle panel.
	 *  @return The middle panel.
	 *  @postcondition The middle panel is constructed.
	 */
	private JPanel createMiddlePanel(){
		//  Create the player panels for each of the players. 
		JPanel playerOnePanel = this.createPlayerNamePanel(1);
		this.playerPanels.add(playerOnePanel);
		playerOnePanel.setVisible(false);
		JPanel playerTwoPanel = this.createPlayerNamePanel(2);
		this.playerPanels.add(playerTwoPanel);
		playerTwoPanel.setVisible(false);
		JPanel playerThreePanel = this.createPlayerNamePanel(3);
		this.playerPanels.add(playerThreePanel);
		playerThreePanel.setVisible(false);
		JPanel playerFourPanel = this.createPlayerNamePanel(4);
		this.playerPanels.add(playerFourPanel);
		playerFourPanel.setVisible(false);
		JPanel playerFivePanel = this.createPlayerNamePanel(5);
		this.playerPanels.add(playerFivePanel);
		playerFivePanel.setVisible(false);
		JPanel playerSixPanel = this.createPlayerNamePanel(6);
		this.playerPanels.add(playerSixPanel);
		playerSixPanel.setVisible(false);
		
		//  Create the left side panel.
		JPanel leftNames = this.createNamesPanel();
		leftNames.add(playerOnePanel);
		leftNames.add(playerTwoPanel);
		leftNames.add(playerThreePanel);
		
		//  Create the right side panel.
		JPanel rightNames = this.createNamesPanel();
		rightNames.add(playerFourPanel);
		rightNames.add(playerFivePanel);
		rightNames.add(playerSixPanel);

		//  Create the panel that will store the left and right panels for player names.
		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new FlowLayout());
		middlePanel.setBackground(this.display.getBackground());
		middlePanel.add(leftNames);
		middlePanel.add(Box.createHorizontalStrut(this.padding));
		middlePanel.add(rightNames);
		
		return middlePanel;
	}
	

	/**
	 *  Constructs the player names panel.
	 *  @return The player names panel.
	 *  @postcondition The player names panel is constructed.
	 */
	private JPanel createPlayerNamesPanel(){
		//  Create the player names label.
		JLabel namesLabel = new JLabel("Player Names: ");
		int fontSize = 30;
		namesLabel.setFont(new Font(namesLabel.getFont().getName(), Font.PLAIN, fontSize));
		namesLabel.setAlignmentX(CENTER_ALIGNMENT);
		
		//  Create the middle panel.
		JPanel middlePanel = this.createMiddlePanel();

		//  Create the names panel.
		JPanel namesPanel = new JPanel();
		namesPanel.setLayout(new BoxLayout(namesPanel, BoxLayout.Y_AXIS));
		namesPanel.setBackground(this.display.getBackground());
		namesPanel.add(namesLabel);
		namesPanel.add(Box.createVerticalStrut(this.padding));
		namesPanel.add(middlePanel);
		
		return namesPanel;
	}


	/**
	 *  Constructs the center panel.
	 *  @return The center panel.
	 *  @postcondition The center panel is constructed.
	 */
	private JPanel createCenterPanel(){
		//  Create the player names panel and the names
		JPanel playersPanel = this.createPlayersPanel();
		JPanel namesPanel = this.createPlayerNamesPanel();
		
		//  Create the center panel to store the main contents of the player selection Screen.
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBackground(this.display.getBackground());
		centerPanel.add(playersPanel);
		centerPanel.add(namesPanel);
		
		return centerPanel;
	}


	/**
	 *  Constructs the back button.
	 *  @return The back button.
	 *  @postcondition The back button is constructed.
	 */
	private JButton createBackButton(AbstractAction a){
		//  Create the back button.
		JButton back = new JButton(a);
		back.setText("Back");
		this.addButtonName(back.getText());
		back.setPreferredSize(this.buttonDimensions);
		
		return back;
	}
	
	
	/**
	 *  Constructs the options button.
	 *  @return The options button.
	 *  @postcondition The options button is constructed.
	 */
	private JButton createOptionsButton(AbstractAction a){
		JButton options = new JButton(a);
		options.setText("Game Options");
		this.addButtonName(options.getText());
		options.setPreferredSize(this.buttonDimensions);
		
		return options;
	}
	
	
	/**
	 *  Constructs the start game button.
	 *  @return The start game button.
	 *  @postcondition The start game button is constructed.
	 */
	private JButton createStartGameButton(AbstractAction a){
		//  Create the start game button.
		JButton startGame = new JButton(a);
		startGame.setText("Start Game");
		this.addButtonName(startGame.getText());
		startGame.setPreferredSize(this.buttonDimensions);
		
		return startGame;
	}
	
	
	/**
	 *  Constructs the button panel.
	 *  @return The button panel.
	 *  @postcondition The buttons panel is constructed.
	 */
	private JPanel createButtonsPanel(HashMap<String, AbstractAction> actions){
		//  Create the three buttons.
		JButton back = this.createBackButton(actions.get("back"));
		JButton options = this.createOptionsButton(actions.get("options"));
		JButton startGame = this.createStartGameButton(actions.get("start"));
		startGame.setEnabled(false);
		this.startGameButton = startGame;
		
		//  DEMO ONLY!! Disabling options
		options.setEnabled(true);
		
		//  Place the three bottom buttons into a panel.
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.setBackground(this.display.getBackground());
		buttonPanel.add(back);
		buttonPanel.add(Box.createHorizontalStrut(this.padding));
		buttonPanel.add(options);
		buttonPanel.add(Box.createHorizontalStrut(this.padding));
		buttonPanel.add(startGame);
		
		return buttonPanel;
	}


	/**
	 *  Constructs the bottom panel.
	 *  @return The bottom panel.
	 *  @postcondition The bottom panel is constructed.
	 */
	private JPanel createBottomPanel(HashMap<String, AbstractAction> actions){
		//  Create the buttons panel.
		JPanel buttonPanel = this.createButtonsPanel(actions);
		
		//  Create the bottom panel.
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.setBackground(this.display.getBackground());
		bottomPanel.add(buttonPanel);
		bottomPanel.add(Box.createVerticalStrut(this.padding));
		
		return bottomPanel;
	}
	
	public int getNumberOfPlayers(){
		if (two.isSelected()){
			return 2;
		}
		if (three.isSelected()){
			return 3;
		}
		if (six.isSelected()){
			return 6;
		}
		return 0;
		
	}
	
	public ArrayList<JPanel> getPlayerPanels(){
		return this.playerPanels;
	}
	
	public int getBoardSize(){
		return (Integer)this.boardSizes.getSelectedItem();
	}
	
}
