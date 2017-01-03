/*  GameOptionsScreen.java
 *  Created by: Nickolas Gough
 *  Purpose: Models the game options Screen, the Screen that will display the options of the game to be set.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  14/07/2016 - Nickolas Gough : Created the file and implemented the game options Screen.
 *  16/07/2016 - Nickolas Gough : Changed public specifiers that should have been private.
 */

package robowars.display;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GameOptionsScreen extends Screen{

	
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
	private final int padding = 100;
	
	
	/**
	 *  Store the desired button dimensions.
	 */
	private final Dimension buttonDimensions = new Dimension(100, 30);
	
//	private saveStatsCheckbox
	
	JComboBox<String> rulesCombo;
	JCheckBox saveStats;
	
	
	/**
	 *  Construct the game options Screen.
	 *  @param display - The Display component.
	 *  @postconditions The game options Screen is constructed.
	 */
	public GameOptionsScreen(Display display, HashMap<String, AbstractAction> actions){
		//  Assign the Screen to be the game options Screen.
		super(ScreenEnum.GAMEOPTIONS);

		//  Store the Display component.
		this.display = display;
		
		//  Initialize the game options screen.
		this.setBackground(this.display.getBackground());
		this.setVisible(false);
		this.setLayout(new BorderLayout());
		
		//  Add the text panel to the Screen.
		JPanel textPanel = this.createTextPanel();
		this.add(textPanel, BorderLayout.NORTH);
		
		//  Add the center and south panels to the game options Screen.
		JPanel centerPanel = this.createCenterPanel();
		JPanel bottomPanel = this.createBottomPanel(actions);
		this.add(centerPanel, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);
	}
	
	
	/**
	 *  Constructs the text panel.
	 *  @return The text panel.
	 *  @postcondition The text panel is constructed.
	 */
	private JPanel createTextPanel(){
		//  Create the label that will contain the top text.
		JLabel text = new JLabel("Game Options");
		int fontSize = 75;
		text.setFont(new Font(text.getFont().getFontName(), Font.PLAIN, fontSize));

		//  Place the text into a panel and add it to the game options screen.
		JPanel textPanel = new JPanel();
		textPanel.setBackground(this.display.getBackground());
		textPanel.add(text);
		textPanel.setAlignmentX(CENTER_ALIGNMENT);

		return textPanel;
	}
	
	
	/**"
	 *  Creates the save stats checkbox.
	 *  @return The save stats checkbox.
	 *  @postcondition The save stats checkbox is constructed.
	 */
	private JCheckBox createSaveStatsCheck(){
		//  Create the save robot statistics checkbox.
		JCheckBox saveStats = new JCheckBox("Save Robot Statistics: ");
		saveStats.setAlignmentX(CENTER_ALIGNMENT);
		saveStats.setSelected(true);
		return saveStats;
	}
	
	
	/**
	 *  Creates the rules combo box.
	 *  @return The rules combo box.
	 *  @postcondition The rules combo box is constructed.
	 */
	private JComboBox<String> createRulesCombo(){
		//  Create the rules combo box.
		JComboBox<String> rulesCombo = new JComboBox<String>();
		rulesCombo.addItem("Normal");
		rulesCombo.addItem("Advanced");
		
		return rulesCombo;
	}


	/** 
	 *  Creates the rules panel.
	 *  @return The rules panel.
	 *  @postcondition The rules panel is constructed.
	 */
	private JPanel createRulesPanel(){
		//  Create the rules label.
		JLabel rulesLabel = new JLabel("Rules: ");
		int fontSize = 30;
		rulesLabel.setFont(new Font(rulesLabel.getFont().getName(), Font.PLAIN, fontSize));
		
		//  Create the rules combo box.
		JComboBox<String> rulesCombo = this.createRulesCombo();
		this.rulesCombo = rulesCombo;
		//  Create the rules combo box.
		JPanel rulesPanel = new JPanel();
		rulesPanel.setLayout(new FlowLayout());
		rulesPanel.setBackground(this.display.getBackground());
		rulesPanel.add(rulesLabel);
		rulesPanel.add(rulesCombo);
		
		return rulesPanel;
	}


	/**
	 *  Creates the center panel.
	 *  @return The center panel.
	 *  @postcondition The center panel is constructed.
	 */
	private JPanel createCenterPanel(){
		//  Create the save stats checkbox and the rules panel.
		JPanel rulesPanel = this.createRulesPanel();
		JCheckBox saveStats = this.createSaveStatsCheck();
		this.saveStats = saveStats;
		//  Create the panel that will store the check box and the rules panel.
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBackground(this.display.getBackground());
		centerPanel.add(Box.createVerticalStrut(this.padding));
		centerPanel.add(saveStats);
		centerPanel.add(Box.createVerticalStrut(this.padding));
		centerPanel.add(rulesPanel);
		
		return centerPanel;
	}


	/**
	 *  Creates the cancel button.
	 *  @return The cancel button.
	 *  @postcondition The cancel button is constructed.
	 */
	private JButton createCancelButton(AbstractAction a){
		//  Create the cancel and save buttons and their panel. 
		JButton cancel = new JButton(a);
		cancel.setText("Cancel");
		this.addButtonName(cancel.getText());
		cancel.setPreferredSize(this.buttonDimensions);

		
		return cancel;
	}
	
	
	/**
	 *  Creates the save button.
	 *  @return The save button.
	 *  @postcondition The save button is constructed.
	 */
	private JButton createSaveButton(AbstractAction a){
		//  Create the save button.
		JButton save = new JButton(a);
		save.setText("Save");
		this.addButtonName(save.getText());
		save.setPreferredSize(this.buttonDimensions);
		
		return save;
	}
	
	
	/**
	 *  Creates the buttons panel.
	 *  @return The buttons panel.
	 *  @postcondition The buttons panel is constructed.
	 */
	private JPanel createButtonsPanel(HashMap<String, AbstractAction> actions){
		//  Create the cancel and save buttons.
		JButton cancel = this.createCancelButton(actions.get("cancel"));
		JButton save = this.createSaveButton(actions.get("save"));
		
		//  Create the buttons panel.
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout());
		buttonsPanel.setBackground(this.display.getBackground());
		buttonsPanel.add(cancel);
		buttonsPanel.add(Box.createHorizontalStrut(this.padding));
		buttonsPanel.add(save);
		
		return buttonsPanel;
	}
	

	/**
	 *  Creates the bottom panel.
	 *  @return The bottom panel.
	 *  @postcondition The bottom panel is constructed.
	 */
	private JPanel createBottomPanel(HashMap<String, AbstractAction> actions){
		//  Create the buttons panel.
		JPanel buttonsPanel = this.createButtonsPanel(actions);
		
		//  Create the bottom panel.
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.setBackground(this.getBackground());
		bottomPanel.add(buttonsPanel);
		bottomPanel.add(Box.createVerticalStrut(this.padding));
		
		return bottomPanel;
	}
	
	
	public String getRulesComboValue(){
		return (String)rulesCombo.getSelectedItem();
	}
	
	public boolean getSaveCheckValue(){
		return this.saveStats.isSelected();
	}
	
	public void setRulesComboValue(String s){
		rulesCombo.setSelectedItem(s);
	}
	
	public void setSaveCheckValue(boolean b){
		saveStats.setSelected(b);
	}
	
}
