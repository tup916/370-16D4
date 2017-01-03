/*	MenuManager.java
 * 	Created by: Tushita Patel
 * 	Purpose: Handles the input events and sends them to 
 * 	Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  11/30/2016 - Janelle : Integrating the flow component.
 * 	11/11/2016	-	[Tushita] Create the class and set up all fields and methods
 */

package robowars.flow;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import robowars.display.Display;

public class EventCatcher implements AWTEventListener {

	/** Reference to the application's Display component. */
	private Display display;

	/** The application's gameManager */
	private GameManager gameManager;

	/** The application's menuManager */
	private MenuManager menuManager;

	/** Constructor for use in the main application. */
	public EventCatcher(Display display){
		this.display = display;
		
		this.gameManager = null;
		this.menuManager = new MenuManager(this, display);
	}
	
	/** Constructor for testing purposes only. */
	public EventCatcher(Display display, GameManager gameManager, MenuManager menuManager){
		this.display = display;
		this.gameManager = gameManager;
		this.menuManager = menuManager;
	}

	public Display getDisplay() {
		return display;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}

	public GameManager getGameManager() {
		return gameManager;
	}

	public void setGameManager(GameManager gameManager) {
		this.gameManager = gameManager;
	}

	public MenuManager getMenuManager() {
		return menuManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void createGameManager(MatchOptions options){
		this.gameManager = new GameManager(this, this.display, options, menuManager.getSettings());
	}
	
	
	@Override
	//  Partway through code construction, this method of event handling became infeasible. Deprecated/vestigial code.
	public void eventDispatched(AWTEvent event) {
//		System.out.println("Event!");
//		// TODO Auto-generated method stub
//		switch (event.getID()){
//
//		case MouseEvent.MOUSE_DRAGGED:
//			//If the Mouse is dragged in the game screen
//			if (this.getDisplay().getCurrentScreen().getEnum() == ScreenEnum.GAME){
//
//			}
//			break;
//			//if the Mouse is dragged in the Settings screen - for the slider
//			
//			
//		case MouseEvent.MOUSE_CLICKED:
//			MouseEvent newEvent = (MouseEvent) event;
//			// If the click is a button
//			if (newEvent.getSource() instanceof JButton){
//				JButton button = (JButton) event.getSource();
//				String BName = button.getName();
//				if (this.getDisplay().getCurrentScreen().getEnum() == ScreenEnum.GAME){
//					this.getGameManager().clickedButton(BName);
//				}
//				else{
//					try {
//						this.getMenuManager().clickedButton(BName);
//					} catch (Throwable e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//			// If the click is on the checkbox
//			else if (newEvent.getSource() instanceof JCheckBox){
//				// What screen is it?
//				
//				JCheckBox checkBox = (JCheckBox) event.getSource();
//				
//				if(this.display.getCurrentScreen().getEnum() == ScreenEnum.PLAYERSELECTION){
//					PlayerSelectionScreen pss = (PlayerSelectionScreen) this.display.getCurrentScreen();
//					
//					for(int i = 0; i < pss.playerPanels.size(); i++){
//						JPanel panel = pss.playerPanels.get(i);
//						
//						if(panel.contains(newEvent.getX(), newEvent.getY())){
//							this.gameManager.matchOptions.getPlayers()[i].setAI(checkBox.isSelected());
//						}
//					}
//						
//				}
//				else if(this.display.getCurrentScreen().getEnum() == ScreenEnum.GAMEOPTIONS){
//					//GameOptionsScreen gos = (GameOptionsScreen) this.display.getCurrentScreen();
//					//gos.
//					this.menuManager.getSettings().setSaveLogsToDisk(checkBox.isSelected());
//				}
//					
//				
//			}
//			
//			else if (newEvent.getSource().equals(display.getGameScreen()))
//			{
//				HexCoord clickedHex = display.getGameScreen().determineClickedHex(newEvent.getPoint());
//				
//				this.gameManager.setActiveHex(clickedHex);
//			}
//			else{
//				throw new RuntimeException("Clicked on a checkbox that is unknown.");
//			}
//			break;
//			// If the click is on the radioButton
//		
//			
//			
//			
//			
//		case KeyEvent.KEY_PRESSED:
//			KeyEvent newKeyEvent = (KeyEvent) event;
//			String keyPressed = ((Character)newKeyEvent.getKeyChar()).toString().toLowerCase();
//			
//			if(this.gameManager.getSettings().getMovementKeyMappingKeys(keyPressed).contains(keyPressed) && 
//				!gameManager.getBoard().getPieceFromOffset(this.gameManager.getBoard().getCurrentPiece()).isAI()){
//				Integer mappedKey = this.gameManager.getSettings().getMovementKeyMappingKey(keyPressed);
//				
//				Integer confirmation = JOptionPane.showConfirmDialog(null, "Do you want to move " + getMovementDirectionName(mappedKey) + "?");
//				
//				if(confirmation.equals(0))
//				{
//					this.gameManager.getBoard().movePieceRelative(mappedKey);	
//				}
//			}
//				
//			break;
//		
//		default: 
//			break;
//		}
//
//		
	}
	
	@SuppressWarnings("unused")
	private String getMovementDirectionName(Integer mappedKey){
		switch (mappedKey){
		case 0: return "right";
		case 1: return "down and right";
		case 2: return "down and left";
		case 3: return "left";
		case 4: return "up and left";
		case 5: return "up and right";
		default: return "that way";
		}
	}
}
