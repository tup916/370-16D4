/*  UserVariable.java
 *  Created by: Janelle
 *  Purpose: Class for holding user-defined values in user-defined names for the interpreter.
 *  Revision History:
 *  11/11/2016 - Janelle : Added an exception to the constructor.
 *  11/07/2016 - Janelle : Created the file, added stubs.
 * 
 */

package robowars.interpreter;

public class UserVariable {

	String varName;
	String varValue;
	
	public UserVariable(String name){
		if (name.equals("")){
			throw new RuntimeException("Cannot create a UserVariable with a blank name.");	
		}
		varName = name;
		varValue = "";
	}
	
	public String getName(){
		return varName;
	}
	
	public String getValue(){
		return varValue;
	}
	
	public void setValue(String value){
		varValue = value;
	}
	
}
