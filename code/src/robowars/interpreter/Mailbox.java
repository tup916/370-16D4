/*  Mailbox.java
 *  Created by: Janelle
 *  Purpose: Class for sending and receiving messages between AI programs.
 *  Revision History:
 *  11/13/2016 - Yige : Added comments and code, also adjusted string comparison from == to String.equals() in the tests.
 *  11/11/2016 - Janelle : Completed the test code.
 *  11/07/2016 - Janelle : Created the file, added stubs.
 */

package robowars.interpreter;

import java.util.ArrayList;
import java.util.List;

import robowars.pieces.Team;
import robowars.pieces.TeamEnum;

public class Mailbox {

	/**  The messages received from other pieces.
	*    Each message is a string, which contains the ID of the sender, a separation character, and then the message itself.
	*/
	private List<String> messages;
	/**  The limit on the number of messages that may be held in the mailbox at once. */
	private Integer mailboxSize;
	/**  ID of the piece that the Mailbox belongs to. */
	private String pieceID;
	/**  Reference to the interpreter holding the Mailbox. */
	private Interpreter interpreter;
	
	/**  Constructor for testing purposes only. */
	public Mailbox(Integer size, String id){
		mailboxSize = size;
		pieceID = id;
		//  Initialize the Interpreter using the test constructor
		interpreter = new Interpreter(size, new Team(TeamEnum.RED, true, 1, 1, 1), null);		
		messages = new ArrayList<String>();
	}
	
	/** Constructor for use in the application. */
	public Mailbox(Integer size, String id, Interpreter parent){
		mailboxSize = size;
		pieceID = id;
		interpreter = parent;		
		messages = new ArrayList<String>();
	}
	
	/**
	 * Getter for pieceID.
	 * @return ID of the piece that the Mailbox belongs to
	 */
	public String getID(){
		return this.pieceID;
	}
	
	/**
	 * Setter for pieceID.
	 * @param id ID of the piece that the Mailbox belongs to
	 */
	public void setID(String id){
		this.pieceID = id;
	}
	
	/**
	 * Check if the mailbox is empty.
	 * @return true if the mailbox is empty, false otherwise
	 */
	public boolean isEmpty(){
		return this.messages.size() == 0;
	}
	
	/**
	 * A method for checking whether this mailbox holds a message from the sender.
	 * @param sender pieceID - A string representing the ID of the sending mailbox.
	 * @return true if there is a message from sender currently held in the Mailbox, false otherwise
	 */
	public boolean hasMessage(String sender){
		for (String message : this.messages) {
			String pieceID = message.split("@")[0];
			if (pieceID.equalsIgnoreCase(sender)) {
				return true;
			}	
		}
		return false;
	}
	
	/**
	 * A method for retrieving a message from the given sender.
	 * @param sender pieceID - A string representing the ID of the sending mailbox.
	 * @postcondition The message, if it exists, has been removed from the mailbox.
	 * @return A string representing the oldest message received from the sender, or "" if there is no message.
	 */
	public String receiveMessage(String sender){
		for (int i = 0; i < this.messages.size(); i++) {
			String[] message = this.messages.get(i).split("@");
			String pieceID = message[0];
			String onlyMessage = message[1];
			//  Remove and return the earliest message if found.
			if (pieceID.equalsIgnoreCase(sender)) {
				this.messages.remove(i);
				return onlyMessage;
			}	
		}
		//  No message from sender.
		return "";
	}
	
	/**
	 * A method for sending messages to other Mailboxes.
	 * @param recipient - A string representing the ID of the Mailbox to send to.
	 * @param message - A string holding the message to be delivered.
	 * @postcondition The message has been delivered if the recipient ID is valid.
	 * @return True if the message is sent successfully, false otherwise.
	 */
	public boolean sendMessage(String recipient, String message){
		for (Mailbox mailbox : this.interpreter.mailboxes) {
			if (mailbox.getID().equals(recipient)) {
				if (mailbox.messages.size() < mailboxSize){
					mailbox.addMessage(this.pieceID + "@" + message);
					return true;
				}
				else {
					return false;
				}
			}
		}
		return false;
	}
	
	/**
	 * Public method to add a message to the messages field. 
	 * @param message - A string holding a new message.
	 */
	public void addMessage(String message){
		this.messages.add(message);
	}
	
	/**
	 * Public method to remove all messages from the messages list.
	 * @postcondition The messages field has been cleared.
	 */
	public void clear(){
		this.messages.clear();
	}
	
	public static void main(String[] args){
		
		//  Main class providing testing for the Mailbox Class

		Mailbox test1 = new Mailbox(3, "Batman");
		
		boolean bResult;
		String sResult;

		/*hasMessage(String pieceID) :
		o Test case: The messages field of the Mailbox is empty. Expected result: hasMessage() returns false.
		o Test case: The messages field of the Mailbox is not empty, and hasMessage() is passed an empty string. Expected result: hasMessage() returns false.
		o Test case: The messages field of the Mailbox is not empty, and hasMessage() is passed a string which matches none of the pieceID s of the messages it contains. Expected result: hasMessage() returns false.
		o Test case: The messages field of the Mailbox is not empty, and hasMessage() is passed a string which matches with at least one of the pieceID s of the messages it contains. Expected result: hasMessage() returns true.
		*/
		
		bResult = test1.hasMessage("");
		if (bResult != false){
			System.out.println("FAILED: Mailbox hasMessage() Test 1. Result: " + bResult);
			return;
		}
		else {
			System.out.println("PASSED: Mailbox hasMessage() Test 1.");
		}
		
		bResult = test1.hasMessage("Superman");
		if (bResult != false){
			System.out.println("FAILED: Mailbox hasMessage() Test 2. Result: " + bResult);
			return;
		}
		else {
			System.out.println("PASSED: Mailbox hasMessage() Test 2.");
		}
		
		test1.addMessage("Catwoman@Hi");
		
		bResult = test1.hasMessage("");
		if (bResult != false){
			System.out.println("FAILED: Mailbox hasMessage() Test 3. Result: " + bResult);
			return;
		}
		else {
			System.out.println("PASSED: Mailbox hasMessage() Test 3.");
		}
		
		bResult = test1.hasMessage("Superman");
		if (bResult != false){
			System.out.println("FAILED: Mailbox hasMessage() Test 4. Result: " + bResult);
			return;
		}
		else {
			System.out.println("PASSED: Mailbox hasMessage() Test 4.");
		}
		
		test1.addMessage("Superman@Heyo");
		
		bResult = test1.hasMessage("Superman");
		if (bResult != true){
			System.out.println("FAILED: Mailbox hasMessage() Test 5. Result: " + bResult);
			return;
		}
		else {
			System.out.println("PASSED: Mailbox hasMessage() Test 5.");
		}
			
		test1.clear();
		
		/*receiveMessage(String pieceID):
		o Test case: The messages field of the Mailbox is empty. Expected result: receiveMessage() returns an empty string.
		o Test case: The messages field of the Mailbox is not empty, and receiveMessage() is passed an empty string. Expected result: receiveMessage() returns an empty string.
		o Test case: The messages field of the Mailbox is not empty, and receiveMessage() is passed a string which matches none of the pieceID s of the messages it contains. Expected result: receiveMessage() returns an empty string.
		o Test case: The messages field of the Mailbox is not empty, and receiveMessage() is passed a string which matches with exactly one of the pieceID s of the messages it contains. Expected result: receiveMessage() returns the message matching the pieceID, with the pieceID removed from the string, and removes the message from the Mailbox.
		o Test case: The messages field of the Mailbox is not empty, and receiveMessage() is passed a string which matches with more than one of the pieceIDs of the messages it contains. Expected result: receiveMessage() returns the message matching the pieceID which was chronologically received first, with the pieceID removed from the string, and removes the message from the Mailbox.
		*/
		
		sResult = test1.receiveMessage("Superman");
		if (!sResult.equals("")){
			System.out.println("FAILED: Mailbox receiveMessage() Test 6. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Mailbox receiveMessage() Test 6.");
		}
		
		sResult = test1.receiveMessage("");
		if (!sResult.equals("")){
			System.out.println("FAILED: Mailbox receiveMessage() Test 7. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Mailbox receiveMessage() Test 7.");
		}
		
		test1.addMessage("Aquaman@Glubglub");
		
		sResult = test1.receiveMessage("");
		if (!sResult.equals("")){
			System.out.println("FAILED: Mailbox receiveMessage() Test 7. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Mailbox receiveMessage() Test 7.");
		}
		
		sResult = test1.receiveMessage("Superman");
		if (!sResult.equals("")){
			System.out.println("FAILED: Mailbox receiveMessage() Test 8. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Mailbox receiveMessage() Test 8.");
		}
		
		sResult = test1.receiveMessage("Aquaman");
		if (!sResult.equals("Glubglub")) {
			System.out.println("FAILED: Mailbox receiveMessage() Test 9. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Mailbox receiveMessage() Test 9.");
		}
		
		test1.addMessage("Superman@Super");
		test1.addMessage("Superman@Duper");
		
		sResult = test1.receiveMessage("Superman");
		if (!sResult.equals("Super")){
			System.out.println("FAILED: Mailbox receiveMessage() Test 10. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Mailbox receiveMessage() Test 10.");
		}
		
		sResult = test1.receiveMessage("Superman");
		if (!sResult.equals("Duper")){
			System.out.println("FAILED: Mailbox receiveMessage() Test 11. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Mailbox receiveMessage() Test 11.");
		}
		
		/*
		sendMessage(String pieceID, String message):
		o Test case: sendMessage() is passed an empty string as either of its parameters. Expected result: sendMessage() delivers no messages and returns false.
		o Test case: sendMessage() is passed an invalid pieceID string. Expected result: sendMessage() delivers no messages and returns false.
		o Test case: sendMessage() is passed the pieceID of a Mailbox that is full. Expected result: sendMessage() delivers no messages and returns false.
		o Test case: sendMessage() is passed the pieceID of a Mailbox that is not full. Expected result: sendMessage() delivers the message using the matching Mailbox�s addMessage() method, and returns true.
		*/

		bResult = test1.sendMessage("", "");
		if (bResult != false){
			System.out.println("FAILED: Mailbox hasMessage() Test 12. Result: " + bResult);
			return;
		}
		else {
			System.out.println("PASSED: Mailbox hasMessage() Test 12.");
		}
		
		bResult = test1.sendMessage("asdf", "");
		if (bResult != false){
			System.out.println("FAILED: Mailbox hasMessage() Test 13. Result: " + bResult);
			return;
		}
		else {
			System.out.println("PASSED: Mailbox hasMessage() Test 13.");
		}
		
		bResult = test1.sendMessage("", "asdf");
		if (bResult != false){
			System.out.println("FAILED: Mailbox hasMessage() Test 14. Result: " + bResult);
			return;
		}
		else {
			System.out.println("PASSED: Mailbox hasMessage() Test 14.");
		}
		
		//  The remaining sendMessage() test cases must occur in the Interpreter class tests.
		
		System.out.println("Mailbox Class: all tests passed.");
	}
	
}
