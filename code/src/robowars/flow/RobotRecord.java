/*  RobotRecord.java
 *  Created by: 
 *  Purpose: Class for storing the JSON file.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  12/03/2016 - Janelle : Adjusted to work with the JSON input.
 *  11/16/2016 - Yige : Added comments according to the design doc.
 */

package robowars.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class RobotRecord {

	/** The name of the Robot */	
	private String name;
	/** The team that created the Robot */
	private String team;
	/** The type of robot this is (Scout/Sniper/Tank/All?) */
	private String type;
	/** The program code for the AI as a string list. */
	private List<String> code;
	/** The statistics for this robot */
	private Map<String, Integer> statistics;


	/**
	 * constructor 
	 */
	public RobotRecord(){
		this.statistics = new HashMap<String, Integer>();
		this.statistics.put("matches", 0);
		this.statistics.put("wins", 0);
		this.statistics.put("losses", 0);
		this.statistics.put("executions", 0);
		this.statistics.put("lived", 0);
		this.statistics.put("died", 0);
		this.statistics.put("absorbed", 0);
		this.statistics.put("killed", 0);
		this.statistics.put("moved", 0);
		
		this.name = "";
		this.team = "";
		this.type = "";
		this.code = new ArrayList<String>();
	}
	
	public List<String> getCode(){

		return this.code;
	}
	
	private void setCode(JSONObject object) {

		JSONArray codeArray = (JSONArray) object.get("code");
		@SuppressWarnings("rawtypes")
		Iterator itr = codeArray.iterator();
		while (itr.hasNext()) {
			this.code.add(itr.next().toString());
		}
			
	}
	
	public Map<String, Integer> getStatistics(JSONObject object) {
		return this.statistics;
	}
	
	
	public void setAttributes(JSONObject object) {
		for (Object key : object.keySet()) {
			//  This looks stupid, but put them together in put, they don't work.
			String keyToString = key.toString();
			String valueToString = object.get(key).toString();
			
			if (keyToString.equals("code")) {
				this.setCode(object);
				
			} 
			else if (keyToString.equals("team")){
				this.setTeam(valueToString);
			}
			else if (keyToString.equals("name")){
				this.setName(valueToString);
			}
			else if (keyToString.equals("class")){
				this.setType(valueToString);
			}
			else {
				try {
					Integer intValue = Integer.parseInt(valueToString);
					this.statistics.put(keyToString, intValue);
				}
				catch (NumberFormatException e) {
					System.out.println("Error when loading RobotRecord: statistic can't be parsed as an integer.");
					this.statistics.put(keyToString, 0);
				}
				finally {}
				
				
			}
		}
	}
	
	public String getName(){
		return this.name;
	}

	public void setName(String name){
		this.name = name;
	}
	
	
	public String getTeam(){
		return this.team;
	}
	
	public void setTeam(String team){
		this.team = team;
	}
	
	public String getType(){
		return this.type;
	}

	public void setType(String type){
		this.type = type;
	}
	
}
