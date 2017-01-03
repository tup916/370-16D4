/*  UserWord.java
 *  Created by: Janelle
 *  Purpose: Class for substituting user-defined Forth-like words with standard functions.
 *  Revision History:
 *  11/11/2016 - Janelle : Added an exception to the constructor.
 *  11/07/2016 - Janelle : Created the file, added stubs.
 * 
 */
package robowars.interpreter;

import java.util.List;


public class UserWord {

	String wordName;
	List<String> replaceValues;
	
	public UserWord(String name){
		if (name.equals("")){
			throw new RuntimeException("Cannot create a UserWord with a blank name.");	
		}
		wordName = name;
	}
	
	public String getName(){
		return wordName;
	}
	
	public List<String> getReplaceValues(){
		return replaceValues;
	}
	
	public void setReplaceValues(List<String> rValues){
		replaceValues = rValues;
	}
	
}
