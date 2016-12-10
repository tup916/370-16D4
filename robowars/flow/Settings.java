/*  Setting.java
 *  Created by: 
 *  Purpose: 
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits/
 *  11/16/2016 - Yige : Added comments according to the design doc.
 */

package robowars.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings {
	
	//  EXTENSION - None of these values have been integrated into the main application, but can be set in the Settings screen.
	
	/**  The loudness of the background sound on a scale from 0 to 100. */
	private int volume;
	
	/**  Whether the logs will be stored to the disk for future reference. */
	private boolean saveLogsToDisk;
	
	
	/**  The mapping of the keyboard key inputs to their functionalities. */
	public Map<String, Integer> keyMapping;
	

	/**
	 * Constructor
	 */
	public Settings(){
		this.volume = 50;
		this.saveLogsToDisk = false;
		this.keyMapping = new HashMap<String, Integer>();
	}
	
	/**
	 * Getter for volume field.
	 * @return the loudness of the background sound on a scale from 1 to 100
	 */
	public int getVolume() {
		return this.volume;
	}

	/**
	 * Setter for volume field.
	 * @param Volume the loudness of the background sound on a scale from 1 to 100
	 */
	public void setVolume(int inVolume) {
		this.volume = inVolume;
	}

	/**
	 * Getter for saveLogsToDisk field.
	 * @return true if the logs will be stored to the disk for future reference, false otherwise
	 */
	public boolean isSaveLogsToDisk() {
		return this.saveLogsToDisk;
	}

	/**
	 * Setter for saveLogsToDisk field.
	 * @param saveLogsToDisk whether the logs will be stored to the disk for future reference
	 */
	public void setSaveLogsToDisk(boolean saveLogsToDisk) {
		this.saveLogsToDisk = saveLogsToDisk;
	}

	/**
	 * 
	 * @param keyPressed 
	 * @return
	 */
	public List<String> getMovementKeyMappingKeys(String keyPressed) {
		return new ArrayList<String>(this.keyMapping.keySet());
	}
	
	/**
	 * 
	 * @param keyPressed 
	 * @return
	 */
	public Integer getMovementKeyMappingKey(String keyPressed) {
		for (String key : this.keyMapping.keySet()) {
			if (key.equals(keyPressed.toLowerCase())) {
				return this.keyMapping.get(key);
			}
		}
		return -1;
	}

}
