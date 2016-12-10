/*  RecordLoader.java
 *  Created by: 
 *  Purpose: Class for retrieving JSON-encoded Robot Record files.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  11/27/2016 - Janelle : Created the file and moved Yige's code from RobotRecord.
 */

package robowars.flow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class RecordLoader {

	/**  Whether to get files online. */
	public Boolean isReadOnline;
	
	/** Constructor */
	public RecordLoader(Boolean isReadOnline){
		this.isReadOnline = isReadOnline;
		
		
	}

	/**
	 * Getter for the jsonFile field.
	 * @return a list of String indicates JSON file
	 */
	public List<RobotRecord> getJsonFile() {
		List<RobotRecord> results = new ArrayList<RobotRecord>();
		
		if (this.isRobotFolderEmpty() || this.isReadOnline) {
			results = this.getJSONFileOnline();

		} else {
			results = this.getJSONFileLocal();

		}
		return results;
	}
	
	/**
	 * Method stub for retrieving robots from the internet.
	 * Not implemented.
	 * @return A list of records from the server, based on the query made.
	 */
	public List<RobotRecord> getJSONFileOnline() {
		//  EXTENSION - This method is a stub for retrieving robots from the server.
		List<RobotRecord> records = new ArrayList<RobotRecord>();
		try {
			@SuppressWarnings("unused")
			URL url = new URL("http://this-is-a-place-holder.google.com");
			
			// TODO Get file from online
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return records;
	}
	
	/**
	 * Method for retrieving robot files from the local resources folder.
	 * @return A list of local robot records.
	 */
	public List<RobotRecord> getJSONFileLocal() {
		List<RobotRecord> records = new ArrayList<RobotRecord>();
		
		try {
			File files = new File("resources/ExampleRobots");
			
			for (String file : files.list()) {
				RobotRecord record = new RobotRecord();
				this.parseJSON(record, file);
				records.add(record);
			}
		} catch (Exception e) {
			
		} finally {
			
		}
		return records;
	}


	/**
	 * Method for parsing the robot file into JSON, and then into a RobotRecord.
	 * @param fileName The local filename of the robot to load and parse
	 */
	public void parseJSON(RobotRecord r, String fileName) {

		JSONObject object = getJSONObject(fileName);
		
		r.setAttributes(object);

	}
	
	/**
	 * Make one JSON into a JSONObject to later parse.
	 * @param fileName
	 * @return
	 */
	public JSONObject getJSONObject(String fileName) {

		FileReader reader = null;
		try {
			reader = new FileReader("resources/ExampleRobots/" + fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		JSONParser parser = new JSONParser();
		
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) parser.parse(reader);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return (JSONObject) jsonObject.get("script");
	}
	
	
	/**
	 * Method for checking if any robots are stored on the local machine
	 * @return true if no files in ExampleRobots folder
	 */
	public Boolean isRobotFolderEmpty() {
		
		File file = new File("resources/ExampleRobots");

		if (file.list() != null) {
			return file.list().length == 0;
		} else {
			return true;
		}

	}
	
	public static void main(String[] args) {
		// test place
		File file = new File("resources/ExampleRobots");
		System.out.println(file.list()[1]);
		FileReader reader = null;
		try {
			reader = new FileReader("resources/ExampleRobots/Creeper.jsn");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		JSONParser parser = new JSONParser();
		
		@SuppressWarnings("unused")
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) parser.parse(reader);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		

	}
	
	
}
