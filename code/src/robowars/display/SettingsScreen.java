/*  SettingsScreen.java
 *  Created by: Nickolas Gough
 *  Purpose: Models the settings screen that will be displayed by the Display component.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  14/07/2016 - Nickolas Gough : Created the file and implemented the settings Screen.
 *  15/07/2016 - Nickolas Gough : Suppressed the warnings of the type cast for the menus.
 */

package robowars.display;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class SettingsScreen extends Screen{

	/**
	 *  The default serial ID. 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 *  Store the desired padding.
	 */
	private final int padding = 100;


	/**
	 *  Store the display component.
	 */
	private Display display;	
	
	
	/**
	 *  Store the desired dimensions for the menus (combo boxes).
	 */
	private final Dimension menuDimension = new Dimension(75, 25);
	
	
	/**
	 *  Store the desired button dimensions.
	 */
	private final Dimension buttonDimensions = new Dimension(100, 30);
	
	
	/**
	 *  Store the collection of comboBoxes.
	 */
	private JComboBox<String>[] menus;

	
	/**
	 *  Store the slider.
	 */
	private JSlider volumeSlider;
	

	@SuppressWarnings("unchecked")
	/**
	 *  Constructs the settings Screen.
	 *  @postcondition A settings Screen object is instantiated.
	 */
	public SettingsScreen(Display display, HashMap<String, AbstractAction> actions){
		//  Assign the Screen to be a settings Screen.
		super(ScreenEnum.SETTINGS);
		
		//  Store the Display component.
		this.display = display;
		
		//  Instantiate the menus field.
		this.menus = (JComboBox<String>[]) new JComboBox[6];

		//  Set the background, visibility, and the layout.
		this.setBackground(this.display.getBackground());
		this.setVisible(false);
		this.setLayout(new BorderLayout());

		//  Add the text panel to the settings Screen.
		JPanel textPanel = this.createTextPanel();
		this.add(textPanel, BorderLayout.NORTH);

		//  Create the center and bottom panels and add them.
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
	public JPanel createTextPanel(){
		//  Create the text that will be displayed at the top.
		JLabel text = new JLabel("Settings");
		int fontSize = 75;
		text.setFont(new Font(text.getFont().getFontName(), Font.PLAIN, fontSize));
		JPanel textPanel = new JPanel();
		textPanel.setBackground(this.display.getBackground());
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.add(text);
		textPanel.setAlignmentX(CENTER_ALIGNMENT);
		text.setAlignmentX(CENTER_ALIGNMENT);

		return textPanel;
	}


	/**
	 * Create the hexagon panel that will be placed in the center of the setting screen.
	 * @return The panel containing the drawn hexagon and the combo boxes.
	 * @postcondition The hexagon panel contains a hexagon and the six combo boxes.
	 */
	public HexagonPanel createHexagonPanel(){
		//  Instantiate the array that will store the points at which to position the dropdown menus.
		Point[] points = new Point[6];

		//  Declare the center points of the Screen and the dimensions of the hexagon.
		Dimension hexagonDimensions = new Dimension(75, 75);
		int xCenter = this.display.getWidth()/2;
		int yCenter = this.display.getHeight()/2 - hexagonDimensions.height - this.padding;

		//  Define the length of the line and the line end point.
		int xLength = 80;
		int yLength = 130;
		int x2;
		int y2;

		//  Store the end points of the first line.
		x2 = xCenter + xLength;
		y2 = yCenter - yLength;
		points[0] = new Point(x2, y2);

		//  Store the end points of the second line.
		x2 = xCenter + (2 * xLength);
		y2 = yCenter;
		points[1] = new Point(x2, y2);

		//  Store the the end points of the third line.
		x2 = xCenter + xLength;
		y2 = yCenter + yLength;
		points[2] = new Point(x2, y2);

		//  Store the end points of the fourth line.
		x2 = xCenter - xLength;
		y2 = yCenter + yLength;
		points[3] = new Point(x2, y2);

		//  Store the end points of the fifth line.
		x2 = xCenter - (2 * xLength);
		y2 = yCenter;
		points[4] = new Point(x2, y2);

		//  Store the end points of the sixth line.
		x2 = xCenter - xLength;
		y2 = yCenter - yLength;
		points[5] = new Point(x2, y2);

		//  Create the hexagon panel.
		HexagonPanel hexagonPanel = new HexagonPanel(hexagonDimensions, xCenter, yCenter, points);
		hexagonPanel.setLayout(null);
		
		//  Create and add the menus.
		this.menus[0] = this.createComboBox(points[0]);
		this.menus[1] = this.createComboBox(points[1]);
		this.menus[2] = this.createComboBox(points[2]);
		this.menus[3] = this.createComboBox(points[3]);
		this.menus[4] = this.createComboBox(points[4]);
		this.menus[5] = this.createComboBox(points[5]);
		hexagonPanel.add(this.menus[0]);
		hexagonPanel.add(this.menus[1]);
		hexagonPanel.add(this.menus[2]);
		hexagonPanel.add(this.menus[3]);
		hexagonPanel.add(this.menus[4]);
		hexagonPanel.add(this.menus[5]);
		
		return hexagonPanel;
	}


	/**
	 *  Construct a combo box that with items for selecting the key that will be used to make decisions.
	 *  @param point - The point at which to position the combo box.
	 *  @return A combo box with key items for determining which keys will be used.
	 *  @postcondition A combo box with items for selecting the keys to be used in the game is created.
	 */
	public JComboBox<String> createComboBox(Point point){
		//  Create the drop down menu. 
		JComboBox<String> menu = new JComboBox<String>();
		menu.setSize(this.menuDimension);
		menu.setLocation(point);
		menu.addItem("1");
		menu.addItem("3");
		menu.addItem("4");
		menu.addItem("6");
		menu.addItem("7");
		menu.addItem("9");
		
		return menu;
	}


	/**
	 *  Create the slider panel.
	 *  @return The slider panel.
	 *  @postcondition The slider panel is constructed.
	 */
	public JPanel createSliderPanel(){
		//  Create the slidebar and its label that will be used to adjust the volume.
		JLabel volumeLabel = new JLabel("Volume: ");
		int fontSize = 30;
		volumeLabel.setFont(new Font(volumeLabel.getFont().getFontName(), Font.PLAIN, fontSize));
		int volumeMin = 0;
		int volumeMax = 100;
		int volumeInit = 50;
		this.volumeSlider = new JSlider(JSlider.HORIZONTAL, volumeMin, volumeMax, volumeInit);

		//  Add the slider and its label to a JPanel.
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new FlowLayout());
		sliderPanel.setBackground(this.display.getBackground());
		sliderPanel.add(volumeLabel);
		sliderPanel.add(this.volumeSlider);

		//  Add the slider panel to another JPanel to create vertical space. 
		JPanel northSliderPanel = new JPanel();
		northSliderPanel.setLayout(new BoxLayout(northSliderPanel, BoxLayout.Y_AXIS));
		northSliderPanel.setBackground(this.getBackground());
		northSliderPanel.add(sliderPanel);
		northSliderPanel.add(Box.createVerticalStrut(this.padding));
		
		return northSliderPanel;
	}
	
	
	/**
	 *  Create the center panel.
	 *  @return The center panel.
	 *  @postcondition The center panel is constructed.
	 */ 
	public JPanel createCenterPanel(){
		//  Create the panel that will store the center of the settings Screen.
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setBackground(this.display.getBackground());
		
		//  Add the hexagon and slider panels to the center panel.
		JPanel hexagonPanel = this.createHexagonPanel();
		JPanel sliderPanel = this.createSliderPanel();
		centerPanel.add(hexagonPanel, BorderLayout.CENTER);
		centerPanel.add(sliderPanel, BorderLayout.SOUTH);
		
		return centerPanel;
	}
	
	
	/**
	 *  Construct the cancel button.
	 *  @return The cancel button.
	 *  @postcondition The cancel button is constructed.
	 */
	public JButton createCancelButton(AbstractAction a){
		//  Create the Cancel button.
		JButton cancelButton = new JButton(a);
		cancelButton.setText("Cancel");
		this.addButtonName(cancelButton.getText());
		cancelButton.setPreferredSize(this.buttonDimensions);
		
		return cancelButton;
	}
	
	
	/**
	 *  Construct the save button.
	 *  @return The save button.
	 *  @postcondition The save button is constructed.
	 */
	public JButton createSaveButton(AbstractAction a){
		//  Create the save button.
		JButton saveButton = new JButton(a);
		saveButton.setText("Save");
		this.addButtonName(saveButton.getText());
		saveButton.setPreferredSize(this.buttonDimensions);
		
		return saveButton;
	}
	
	
	/**
	 *  Construct the buttons panel.
	 *  @return The buttons panel.
	 *  @postcondition The buttons panel is constructed.
	 */
	public JPanel createButtonsPanel(HashMap<String, AbstractAction> actions){
		//  Create the panel that will store the buttons.
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.setBackground(this.display.getBackground());

		//  Add the cancel and save buttons to the button panel.
		JButton cancelButton = this.createCancelButton(actions.get("cancel"));
		JButton saveButton = this.createSaveButton(actions.get("save"));
		buttonPanel.add(cancelButton);
		buttonPanel.add(Box.createHorizontalStrut(this.padding));
		buttonPanel.add(saveButton);
		
		return buttonPanel;
	}
	
	
	/**
	 *  Creates the bottom panel.
	 *  @return The bottom panel.
	 *  @postcondition The bottom panel is constructed.
	 */
	public JPanel createBottomPanel(HashMap<String, AbstractAction> actions){
		//  Create the bottom panel that will store the buttons.
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.setBackground(this.display.getBackground());
		
		//  Add the buttons panel to the bottom panel and add some padding.
		JPanel buttonPanel = this.createButtonsPanel(actions);
		bottomPanel.add(buttonPanel);
		bottomPanel.add(Box.createVerticalStrut(this.padding));
		
		return bottomPanel;
	}
	
	public JComboBox<String>[] getMenus(){
		return this.menus;
	}
	
	public JSlider getVolumeSlider(){
		return this.volumeSlider;
	}
}
