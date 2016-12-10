/*  AI.java
 *  Created by: Janelle
 *  Purpose: Class for holding user-defined programs at the start of a match.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  11/30/2016 - Janelle : Implemented the program parsing now that the JSON parser was implemented.
 *  11/07/2016 - Janelle : Created the file, added stubs.
 * 
 */

package robowars.interpreter;

import java.util.ArrayList;
import java.util.List;

import robowars.flow.RobotRecord;

public class AI {

	/** A list of the lines contained in a clean full program. */
	List<String> fullProgram;

	/** Constructor for use in the main application, taking a RobotRecord. */
	public AI (RobotRecord r){
		
		fullProgram = new ArrayList<String>();
		
		//  Remove comments from each line, remove empty whitespace entries, and add to the full program.
		if (r != null){

			List<String> raw = r.getCode();
			
			for (String line : raw){
				line = stripComments(line);
				String[] splitLine = line.split(" ");
				for (int i = 0; i < splitLine.length; i++){
					splitLine[i] = splitLine[i].trim();
					if (!splitLine[i].equals("")){
						fullProgram.add(splitLine[i]);
					}
				}
			}
		}
		
	}
	
	/** Constructor for test purposes only. */
	public AI (List<String> raw){
		fullProgram = raw;
	}
	
	public List<String> getFull(){
		return fullProgram;
	}
	
	public void setFull(List<String> program){
		fullProgram = program;
	}
	
	
	/**
	 * Remove comments from a RobotRecord's code.
	 * @param line The line to strip of comments.
	 * @return The cleaned line.
	 */
	
	public String stripComments(String line){
		//  If the line doesn't contain any parentheses, we are finished.
		while (line.contains("(") || line.contains(")")){
			if (line.contains("(")){
				if (line.contains(")")){
					//  Line contains at least one complete comment
					int openCount = 1;
					int closeCount = 0;
					int firstOpenIndex = line.indexOf("(");
					int lastClosedIndex = line.indexOf("(");
					for (int i = firstOpenIndex + 1; i < line.length(); i++){
						if (("" + line.charAt(i)).equals("(")){
							openCount += 1;
						}
						else if (("" + line.charAt(i)).equals(")")){
							closeCount += 1;
							lastClosedIndex = i;
							if (closeCount == openCount){
								//  We've matched the outer pair of braces.
								StringBuilder text = new StringBuilder(line);
								System.out.println(text);
								if (i+1 < line.length()){
									text.replace(firstOpenIndex, i + 1, "");
								}
								else {
									text.replace(firstOpenIndex, i, "");
								}
								line = text.toString();
								System.out.println(line);
								break;
							}
						}
					}
					//  If some parentheses are unmatched, replace the outermost complete pair.
					if (closeCount != openCount){
						StringBuilder text = new StringBuilder(line);
						System.out.println(text);
						text.replace(firstOpenIndex, lastClosedIndex, "");
						line = text.toString();
						System.out.println(line);
					}
					
				}
				else {
					//  Hanging parentheses; strip out the opening parentheses.
					line = line.replace("(", "");
				}
			}
			else {
				//  Hanging parentheses; strip out the closing parentheses.
				line = line.replace(")", "");
			}
		}
		return line;
	}
	
	public static void main(String[] args){
		
		//  Main class providing testing for the AI Class
		RobotRecord r = new RobotRecord();
		AI test = new AI(r);
		String result;

		System.out.println("Beginning tests for AI class");
		
		/*
			stripComments(String line) :
			o Test case: stripComments() is passed a string which contains no �(� and �)� characters. 
				Expected result: stripComments() returns the unmodified string.
			o Test case: stripComments() is passed a string which contains a �(� character with no matching �)� character. 
				Expected result: stripComments() removes only the stray �(� character, and returns the modified string.
			o Test case: stripComments() is passed a string which contains a �)� character with no matching �(� character. 
				Expected result: stripComments() removes only the stray �)� character, and returns the modified string.
			o Test case: stripComments() is passed a string containing sets of matched �(� and �)� characters. 
				Expected result: stripComments() removes the �(� and �)� characters, as well as all the characters between them, and returns the modified string.
		*/
		
		result = test.stripComments("");
		if (!result.equals("")){
			System.out.println("FAILED: AI stripComments() Test 1. Result: " + result);
			return;
		}
		else {
			System.out.println("PASSED: AI stripComments() Test 1.");
		}
		
		result = test.stripComments("asdf");
		if (!result.equals("asdf")){
			System.out.println("FAILED: AI stripComments() Test 2. Result: " + result);
			return;
		}
		else {
			System.out.println("PASSED: AI stripComments() Test 2.");
		}
		
		result = test.stripComments("The (quick brown fox");
		if (!result.equals("The quick brown fox")){
			System.out.println("FAILED: AI stripComments() Test 3. Result: " + result);
			return;
		}
		else {
			System.out.println("PASSED: AI stripComments() Test 3.");
		}
		
		result = test.stripComments("The quick bro)wn fox");
		if (!result.equals("The quick brown fox")){
			System.out.println("FAILED: AI stripComments() Test 4. Result: " + result);
			return;
		}
		else {
			System.out.println("PASSED: AI stripComments() Test 4.");
		}
		
		result = test.stripComments("The quick (purple )brown fox");
		if (!result.equals("The quick brown fox")){
			System.out.println("FAILED: AI stripComments() Test 5. Result: " + result);
			return;
		}
		else {
			System.out.println("PASSED: AI stripComments() Test 5.");
		}
		
		result = test.stripComments("The (slow )quick (purple )brown fox");
		if (!result.equals("The quick brown fox")){
			System.out.println("FAILED: AI stripComments() Test 6. Result: " + result);
			return;
		}
		else {
			System.out.println("PASSED: AI stripComments() Test 6.");
		}
		
		result = test.stripComments("The (slow (but not too slow) gross )quick (purple )brown fox");
		if (!result.equals("The quick brown fox")){
			System.out.println("FAILED: AI stripComments() Test 7. Result: " + result);
			return;
		}
		else {
			System.out.println("PASSED: AI stripComments() Test 7.");
		}
		
		
		result = test.stripComments("The (())quick (purple )brown fo(()x");
		if (!result.equals("The quick brown fox")){
			System.out.println("FAILED: AI stripComments() Test 8. Result: " + result);
			return;
		}
		else {
			System.out.println("PASSED: AI stripComments() Test 8.");
		}
		
		System.out.println("AI Class: all tests passed.");
	}
	
}
