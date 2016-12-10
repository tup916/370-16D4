/*  Interpreter.java
 *  Created by: Janelle
 *  Purpose: main Interpreter class, holding all the standard Interpreter methods and parser.
 *  Revision History:
 *  12/09/2016 - Janelle : Documentation sweep and final edits.
 *  12/04/2016 - Janelle : Adjustments to make sure Interpreter runs with real programs.
 *  11/26/2016 - Janelle : Integration with board, implementation of turn(), move() etc.
 *  11/18/2016 - Janelle and Yige: Pair programming session. Implemented ifBlock(), the message methods, and parse(); 200 tests now pass.
 *  11/14/2016 - Janelle : Implemented word interface excluding ifBlock(), whileBlock(), forBlock(), the message methods and parse().
 *  11/12/2016 - Janelle : Implemented all unit tests as described in the testing document.
 *  11/11/2016 - Janelle : Wrote all but 4 unit tests for the Interpreter class.
 *  11/07/2016 - Janelle : Created the file, added stubs.
 * 
 */

package robowars.interpreter;
import robowars.flow.PlayerSettings;
import robowars.pieces.Piece;
import robowars.pieces.Team;
import robowars.pieces.TeamEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

public class Interpreter {

	/**
	 *  Interface for implementing Forth-like words as elements in a function table.
	 */
	public interface Word{
		void execute();
	}
	
	/**
	 *  A TimerTask used to clock the time spent in an Interpreter loop (for or while blocks).
	 *  Prevents malicious or malformed user code from locking up the system.
	 */
	public class TurnTimerTask extends TimerTask {
		
		Interpreter parent;
		Thread t;
		
		public TurnTimerTask(Interpreter p){
			parent = p;
			t = new Thread();
		}
		
		@Override
		public void run(){
			System.out.println("Exceeded turn limit time. Aborting.");
			parent.triggerTimerInterrupt();

		}
		
	}
	
	//  Constant values for the Interpreter implementation
	
	/** How long a loop is permitted to run before aborting. */
	public static int TURN_LIMIT_MILLISECONDS = 5000;
	/** The maximum capacity of each piece's mailbox for message sending. */
	public static int MAILBOX_CAPACITY = 6;
	
	/** The number of pieces per team. */
	Integer numberOfPieces;
	/** The offset of the currently active piece. */
	Integer currentPiece;
	/** An array for storing the programs for each piece's AI, indexed by the piece offset. */
	AI[] ai;
	/** An array for storing the mailboxes for each piece, indexed by the piece offset. */
	Mailbox[] mailboxes;
	/** An array for storing the interpreter stacks for each piece, indexed by the piece offset. */
	protected Stack<String>[] stacks;
	/** An array of lists of the user-defined words for each piece's program, indexed by the piece offset. */
	List<UserWord>[] userWords;
	/** An array of lists of the user-defined variables for each piece's program, indexed by the piece offset. */
	List<UserVariable>[] userVars;
	/** A list of loopIterators for the currently running program. */
	List<Integer> loopIterators;
	/** A list of instructions for the currently running program. */
	List<String> currentInstructions;
	/** A mapping of piece names to integer offsets. */
	Map<String, Integer> idToIndex;
	/** A mapping of string words to indices for the methods array. */
	Map<String, Integer> functionTableMapping;
	/** A function table for the Interpreter's standard methods. */
	Word[] methods;
	/** The team this Interpreter belongs to. */
	Team team;
	/** Flag for whether parsing has hit a semicolon and the turn should end. */
	boolean hitSemicolon;
	/** Flag for whether a loop should be forced to quit running. */
	boolean forceLoopInterrupt;
	/** Flag for whether a "leave" word was parsed inside a for block. */
	boolean leaveForBlock;
	/** Flag to prevent malicious code from trying to run board-related methods outside of the play() method. */
	boolean isPlayMode;
	
	/** Defines which words are only allowed to be executed during play mode (eg. turn(), shoot()) */
	public List<String> restrictedWords;
	
	/** Timer for clocking the Interpreter's execution time during loops. */
	Timer turnTimer;
	/** Task to be scheduled by the turnTimer during loop execution. */
	TurnTimerTask timeoutTask;
	
	
	/** Constructor for the Interpreter for use in the main application. */
	@SuppressWarnings("unchecked")
	public Interpreter(Integer numPieces, Team t, PlayerSettings robots) throws RuntimeException{
		//  Check preconditions for creating the interpreter.
		if (numPieces <= 0){
			throw new RuntimeException("Error creating Interpreter: number of Pieces must be greater than 0.");
		}
		if (t == null){
			throw new RuntimeException("Team cannot be null when instantiating Interpreter.");
		}
		if (robots != null){
			if (robots.mapRobots.size() < numPieces){
				throw new RuntimeException("Insufficient number of Robot Records when instantiating Interpreter.");
			}
		}
		
		numberOfPieces = numPieces;
		team = t;
		currentPiece = 0;
		ai = new AI[numPieces];
		mailboxes = new Mailbox[numPieces];
		stacks = (Stack<String>[]) new Stack[numPieces];
		userWords = (ArrayList<UserWord>[]) new ArrayList[numPieces];
		userVars = (ArrayList<UserVariable>[]) new ArrayList[numPieces];
		idToIndex = new HashMap<String, Integer>();
		
		//  Instantiate the array elements for each piece.
		for (int i = 0; i < numPieces; i++){
			if (robots != null){
				ai[i] = new AI(robots.mapRobots.get(i));
				//System.out.println("Program " + i + ": " + ai[i].getFull());
			}
			
			//  EXTENSION: To make this code modular, adjust names to match types later? Eg. SNIPER2
			mailboxes[i] = new Mailbox(MAILBOX_CAPACITY, "piece" + i, this);
			idToIndex.put("piece" + i, i);
			stacks[i] = new Stack<String>();
			userWords[i] = new ArrayList<UserWord>();
			userVars[i] = new ArrayList<UserVariable>();
		}
		
		
		loopIterators = new ArrayList<Integer>();
		currentInstructions = new ArrayList<String>();
		
		hitSemicolon = false;
		forceLoopInterrupt = false;
		leaveForBlock = false;
		functionTableMapping = new HashMap<String, Integer>();
		isPlayMode = false;
		
		//  Define the words which are restricted to play mode.
		restrictedWords = new ArrayList<String>();
		restrictedWords.add("health");
		restrictedWords.add("healthLeft");
		restrictedWords.add("moves");
		restrictedWords.add("movesLeft");
		restrictedWords.add("attack");
		restrictedWords.add("range");
		restrictedWords.add("team");
		restrictedWords.add("type");
		restrictedWords.add("turn!");
		restrictedWords.add("move!");
		restrictedWords.add("shoot!");
		restrictedWords.add("check!");
		restrictedWords.add("scan!");
		restrictedWords.add("identify!");
		
		
		//  Define the function table mapping for the standard interpreter functions
		functionTableMapping.put("+", 0);
		functionTableMapping.put("-", 1);
		functionTableMapping.put("*", 2);
		functionTableMapping.put("/mod", 3);
		functionTableMapping.put("and", 4);
		functionTableMapping.put("or", 5);
		functionTableMapping.put("invert", 6);
		functionTableMapping.put("dup", 7);
		functionTableMapping.put("drop", 8);
		functionTableMapping.put("swap", 9);
		functionTableMapping.put("rot", 10);
		functionTableMapping.put(">", 11);
		functionTableMapping.put(">=", 12);
		functionTableMapping.put("<", 13);
		functionTableMapping.put("<=", 14);
		functionTableMapping.put("=", 15);
		functionTableMapping.put("<>", 16);
		functionTableMapping.put("if", 17);
		functionTableMapping.put("begin", 18);
		functionTableMapping.put("do", 19);
		functionTableMapping.put("variable", 20);
		functionTableMapping.put(":", 21);
		functionTableMapping.put("random", 22);
		functionTableMapping.put(".", 23);
		functionTableMapping.put("health", 24);
		functionTableMapping.put("healthLeft", 25);
		functionTableMapping.put("moves", 26);
		functionTableMapping.put("movesLeft", 27);
		functionTableMapping.put("attack", 28);
		functionTableMapping.put("range", 29);
		functionTableMapping.put("team", 30);
		functionTableMapping.put("type", 31);
		functionTableMapping.put("turn!", 32);
		functionTableMapping.put("move!", 33);
		functionTableMapping.put("shoot!", 34);
		functionTableMapping.put("check!", 35);
		functionTableMapping.put("scan!", 36);
		functionTableMapping.put("identify!", 37);
		functionTableMapping.put("send!", 38);
		functionTableMapping.put("mesg?", 39);
		functionTableMapping.put("recv!", 40);
		functionTableMapping.put("?", 41);
		functionTableMapping.put("!", 42);

		
		
		//  Define the methods that go in the function table.
		methods = new Word[] {
				new Word() {public void execute() {add();} },
				new Word() {public void execute() {subtract();} },
				new Word() {public void execute() {multiply();} },
				new Word() {public void execute() {divideRemain();} },
				new Word() {public void execute() {and();} },
				new Word() {public void execute() {or();} },
				new Word() {public void execute() {invert();} },
				new Word() {public void execute() {duplicate();} },
				new Word() {public void execute() {drop();} },
				new Word() {public void execute() {swap();} },
				new Word() {public void execute() {rotate();} },
				new Word() {public void execute() {greaterThan();} },
				new Word() {public void execute() {greaterThanEqual();} },
				new Word() {public void execute() {lessThan();} },
				new Word() {public void execute() {lessThanEqual();} },
				new Word() {public void execute() {equal();} },
				new Word() {public void execute() {notEqual();} },
				new Word() {public void execute() {ifBlock();} },
				new Word() {public void execute() {whileBlock();} },
				new Word() {public void execute() {forBlock();} },
				new Word() {public void execute() {declareVar();} },
				new Word() {public void execute() {declareWord();} },
				new Word() {public void execute() {random();} },
				new Word() {public void execute() {dotPrint();} },
				new Word() {public void execute() {qHealth();} },  		//  Restricted word
				new Word() {public void execute() {qHealthLeft();} },	//  Restricted word
				new Word() {public void execute() {qMoves();} },		//  Restricted word
				new Word() {public void execute() {qMovesLeft();} },	//  Restricted word
				new Word() {public void execute() {qAttack();} },		//  Restricted word
				new Word() {public void execute() {qRange();} },		//  Restricted word
				new Word() {public void execute() {qTeam();} },			//  Restricted word
				new Word() {public void execute() {qType();} },			//  Restricted word
				new Word() {public void execute() {turn();} },			//  Restricted word
				new Word() {public void execute() {move();} },			//  Restricted word
				new Word() {public void execute() {shoot();} },			//  Restricted word
				new Word() {public void execute() {check();} },			//  Restricted word
				new Word() {public void execute() {scan();} },			//  Restricted word
				new Word() {public void execute() {identify();} },		//  Restricted word
				new Word() {public void execute() {sendMessage();} },	
				new Word() {public void execute() {checkMessages();} },
				new Word() {public void execute() {receiveMessage();} },
				new Word() {public void execute() {retrieve();} },
				new Word() {public void execute() {store();} }
		};
		
	
		
	}
	
	
	/**
	 *  Public method used to play the AI for the pieceID given.
	 *  @param piece - An integer representing the index of the piece to play.
	 *  @postcondition The AI has taken its turn.
	 */
	public void play(Integer piece){
		this.currentPiece = piece;
		this.currentInstructions.add("play");
		
		this.isPlayMode = true;
		while (this.currentInstructions.size() > 0){
			this.parse(this.currentInstructions.remove(0));			
		}	
		
		this.isPlayMode = false;
		return;
	}
	
	/**
	 * Public method used to initialize the AI for the pieceID given.
	 * @param piece - An integer representing the index of the piece to initialize.
	 * @postcondition The AI has been initialized.
	 */
	public void initialize(Integer piece){
		this.currentPiece = piece;
		this.currentInstructions = this.ai[piece].getFull();
		while (this.currentInstructions.size() > 0){
			this.parse(this.currentInstructions.remove(0));			
		}
		//  Make sure that we have a play method defined; define an empty one otherwise.
		boolean hasPlay = false;
		System.out.println("Words: " + this.userWords[piece]);
		for (int i = 0; i < this.userWords[piece].size(); i++){
			if (this.userWords[piece].get(i).getName().equals("play")){
				hasPlay = true;
			}
		}
		if (!hasPlay){
			System.out.println("Error: no play word detected. Adding a blank one.");
			this.userWords[piece].add(new UserWord("play"));
			this.userWords[piece].get(this.userWords[piece].size() -1).setReplaceValues(new ArrayList<String>());
		}		
		return;
	}
	
	/**
	 * Method called by the turnTimer's task to stop a loop from executing.
	 */
	private void triggerTimerInterrupt(){
		forceLoopInterrupt = true;
	}
	
	
	/**
	 *  A method for adding the top two elements of the current stack.
	 *  @precondition The top two elements of the stack represent integers.
	 *  @postcondition The top two elements have been popped from the stack, added, and the result pushed to the stack.
	 */
	public void add(){
		if (this.stacks[this.currentPiece].size() < 2){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation add().");
			return;
		}
		try {
			Integer arg1 = Integer.parseInt(this.stacks[this.currentPiece].pop());
			Integer arg2 = Integer.parseInt(this.stacks[this.currentPiece].pop());
			this.stacks[this.currentPiece].push("" + (arg1 + arg2));
			return;
		}
		catch (NumberFormatException e) {
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": incorrect argument types for stack operation add().");
			return;
		}
		finally {}
		
	}
	
	/**
	 *  A method for subtracting the top two elements of the current stack.
	 *  @precondition The top two elements of the stack represent integers.
	 *  @postcondition The top two elements have been popped from the stack, subtracted, and the result pushed to the stack.
	 */
	public void subtract(){
		if (this.stacks[this.currentPiece].size() < 2){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation subtract().");
			return;
		}
		try {
			Integer arg1 = Integer.parseInt(this.stacks[this.currentPiece].pop());
			Integer arg2 = Integer.parseInt(this.stacks[this.currentPiece].pop());
			this.stacks[this.currentPiece].push("" + (arg1 - arg2));
			return;
		}
		catch (NumberFormatException e) {
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": incorrect argument types for stack operation subtract().");
			return;
		}
		finally {}
	}

	/**
	 *  A method for multiplying the top two elements of the current stack.
	 *  @precondition The top two elements of the stack represent integers.
	 *  @postcondition The top two elements have been popped from the stack, multiplied, and the result pushed to the stack.
	 */
	public void multiply(){
		if (this.stacks[this.currentPiece].size() < 2){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation multiply().");
			return;
		}
		try {
			Integer arg1 = Integer.parseInt(this.stacks[this.currentPiece].pop());
			Integer arg2 = Integer.parseInt(this.stacks[this.currentPiece].pop());
			this.stacks[this.currentPiece].push("" + (arg1 * arg2));
			return;
		}
		catch (NumberFormatException e) {
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": incorrect argument types for stack operation multiply().");
			return;
		}
		finally {}
	}
	
	/**
	 *  A method for dividing the top two elements of the current stack.
	 *  @precondition The top two elements of the stack represent integers.
	 *  @postcondition The top two elements have been popped from the stack, divided, and the remainder and quotient pushed to the stack in that order.
	 */
	public void divideRemain(){
		if (this.stacks[this.currentPiece].size() < 2){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation divideRemain().");
			return;
		}
		try {
			Integer arg1 = Integer.parseInt(this.stacks[this.currentPiece].pop());
			Integer arg2 = Integer.parseInt(this.stacks[this.currentPiece].pop());
			this.stacks[this.currentPiece].push("" + (arg1 % arg2));
			this.stacks[this.currentPiece].push("" + (arg1 / arg2));
			return;
		}
		catch (NumberFormatException e) {
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": incorrect argument types for stack operation divideRemain().");
			return;
		}
		finally {}
	}
	
	/**
	 *  A method for performing the boolean and operation on the top two elements of the current stack.
	 *  @precondition The top two elements of the stack represent boolean values.
	 *  @postcondition The top two elements have been popped from the stack, the and operation performed, and the result pushed to the stack.
	 */
	public void and(){
		if (this.stacks[this.currentPiece].size() < 2){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation and().");
			return;
		}
		
		//  We cannot use a try...catch block here because parseBoolean() will convert any string not equal to "true" to false and throws no exception.
		
		boolean bool1, bool2;
		String arg1 = this.stacks[this.currentPiece].pop();
		String arg2 = this.stacks[this.currentPiece].pop();
		if (arg1.equals("true") || arg1.equals("false")){
			bool1 = Boolean.parseBoolean(arg1);
		}
		else {
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": incorrect argument types for stack operation and().");
			return;
		}
		
		if (arg2.equals("true") || arg2.equals("false")){
			bool2 = Boolean.parseBoolean(arg2);
		}
		else {
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": incorrect argument types for stack operation and().");
			return;
		}
		
		this.stacks[this.currentPiece].push("" + (bool1 && bool2));
		return;

	}
	
	/**
	 *  A method for performing the boolean or operation on the top two elements of the current stack.
	 *  @precondition The top two elements of the stack represent boolean values.
	 *  @postcondition The top two elements have been popped from the stack, the or operation performed, and the result pushed to the stack.
	 */
	public void or() {
		if (this.stacks[this.currentPiece].size() < 2){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation or().");
			return;
		}
		
		//  We cannot use a try...catch block here because parseBoolean() will convert any string not equal to "true" to false and throws no exception.
		
		boolean bool1, bool2;
		String arg1 = this.stacks[this.currentPiece].pop();
		String arg2 = this.stacks[this.currentPiece].pop();
		if (arg1.equals("true") || arg1.equals("false")){
			bool1 = Boolean.parseBoolean(arg1);
		}
		else {
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": incorrect argument types for stack operation or().");
			return;
		}
		
		if (arg2.equals("true") || arg2.equals("false")){
			bool2 = Boolean.parseBoolean(arg2);
		}
		else {
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": incorrect argument types for stack operation or().");
			return;
		}
		
		this.stacks[this.currentPiece].push("" + (bool1 || bool2));
		return;
		
	}
	
	/**
	 *  A method for performing the boolean not operation on the top element of the current stack.
	 *  @precondition The top element of the stack represents a boolean value.
	 *  @postcondition The top element has been popped from the stack, the value inverted, and the result pushed to the stack.
	 */
	public void invert() {
		if (this.stacks[this.currentPiece].size() < 1){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation invert().");
			return;
		}
		
		//  We cannot use a try...catch block here because parseBoolean() will convert any string not equal to "true" to false and throws no exception.
		
		boolean bool1;
		String arg1 = this.stacks[this.currentPiece].pop();
		if (arg1.equals("true") || arg1.equals("false")){
			bool1 = Boolean.parseBoolean(arg1);
		}
		else {
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": incorrect argument types for stack operation invert().");
			return;
		}		
		
		this.stacks[this.currentPiece].push("" + (!bool1));
		return;
	}
	
	/**
	 *  A method for duplicating the top element of the current stack.
	 *  @precondition The stack contains at least one element.
	 *  @postcondition The top element has been popped from the stack, and the value pushed to the stack twice.
	 */
	public void duplicate() {
		if (this.stacks[this.currentPiece].size() < 1){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation duplicate().");
			return;
		}
		String value = this.stacks[this.currentPiece].pop();
		this.stacks[this.currentPiece].push(value);
		this.stacks[this.currentPiece].push(value);
		return;
	}
	
	/**
	 *  A method for discarding the top element of the current stack.
	 *  @precondition The stack contains at least one element.
	 *  @postcondition The top element has been popped from the stack and discarded.
	 */
	public void drop(){
		if (this.stacks[this.currentPiece].size() < 1){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation drop().");
			return;
		}
		this.stacks[this.currentPiece].pop();
		return;
	}
	
	/**
	 *  A method for changing the positions of the top two elements of the current stack.
	 *  @precondition The stack contains at least one element.
	 *  @postcondition The top two elements have been popped from the stack, and pushed in the same order to reverse their positions.
	 *  @postcondition If the stack contains only one element, that element has been popped from and then pushed to the stack.
	 */
	public void swap(){
		if (this.stacks[this.currentPiece].size() < 1){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation swap().");
			return;
		}
		
		if (this.stacks[this.currentPiece].size() == 1){
			return;
		}
		
		String value1 = this.stacks[this.currentPiece].pop();
		String value2 = this.stacks[this.currentPiece].pop();
		this.stacks[this.currentPiece].push(value1);
		this.stacks[this.currentPiece].push(value2);
		return;
	}
	
	/**
	 *  A method for changing the positions of the top three elements of the current stack.
	 *  @precondition The stack contains at least one element.
	 *  @postcondition The top three elements have been popped from the stack, and pushed so that the third value is the new top value.
	 *  @postcondition If the stack contains one or two elements, those elements have not changed positions.
	 */
	public void rotate(){
		if (this.stacks[this.currentPiece].size() < 1){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation rotate().");
			return;
		}
		
		if (this.stacks[this.currentPiece].size() < 3){
			return;
		}
			
		String value1 = this.stacks[this.currentPiece].pop();
		String value2 = this.stacks[this.currentPiece].pop();
		String value3 = this.stacks[this.currentPiece].pop();
		this.stacks[this.currentPiece].push(value2);
		this.stacks[this.currentPiece].push(value1);
		this.stacks[this.currentPiece].push(value3);
		return;
	}
	
	/**
	 *  A method for performing a greater-than comparison of the top two elements of the current stack.
	 *  @precondition The stack contains at least two comparable elements of identical types.
	 *  @postcondition The top two elements have been popped from the stack, their greater-than relationship compared, and the boolean result pushed to the stack.
	 */
	public void greaterThan(){
		if (this.stacks[this.currentPiece].size() < 2){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation greaterThan().");
			return;
		}
		
		String value1 = this.stacks[this.currentPiece].pop();
		String value2 = this.stacks[this.currentPiece].pop();
		boolean error = false;
		
		if (value1.equals("true") || value1.equals("false") || value2.equals("true") || value2.equals("false")){
			//  At least one value is boolean and not comparable.
			error = true;
		}
		if (value1.contains("#") || value2.contains("#")){
			//  At least one value is a variable location and not comparable.
			error = true;
		}
		
		//  If one value is an integer, the other must also be an integer.
		Integer numberOfInts = 0;
		Integer int1 = Integer.MAX_VALUE; 
		Integer int2 = Integer.MAX_VALUE;
		try {
			int1 = Integer.parseInt(value1);
			numberOfInts += 1;
			int2 = Integer.parseInt(value2);
			numberOfInts += 1;
		}
		catch (NumberFormatException e){
			if (numberOfInts == 1){
				//  One value is an integer and the other is not
				error = true;
			}
			else {
				//  One value could still be an integer
				
				try {
					int2 = Integer.parseInt(value2);
					numberOfInts += 1;
				}
				catch (NumberFormatException e2){
					if (numberOfInts == 1){
						//  One value is an integer and the other is not.
						error = true;
					}
				}
				finally {}				
			}
		}
		finally {}
			
		if (error){
			System.out.println("Error in program execution for piece" + this.currentPiece + ": the two arguments for stack operation greaterThan() are not comparable.");
			return;
		}
		
		boolean result;
		if (numberOfInts == 2){
			//  Perform integer comparison.
			result = (int1 > int2);
		}
		else {
			//  Perform string comparison.
			result = (value1.compareTo(value2) == 1);
		}
		
		//  Push the final result.
		this.stacks[this.currentPiece].push("" + result);
		
	}
	
	/**
	 *  A method for performing a greater-than-equal comparison of the top two elements of the current stack.
	 *  @precondition The stack contains at least two comparable elements of identical types.
	 *  @postcondition The top two elements have been popped from the stack, their greater-than-equal relationship compared, and the boolean result pushed to the stack.
	 */
	public void greaterThanEqual(){
		if (this.stacks[this.currentPiece].size() < 2){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation greaterThanEqual().");
			return;
		}
		
		String value1 = this.stacks[this.currentPiece].pop();
		String value2 = this.stacks[this.currentPiece].pop();
		boolean error = false;
		
		if (value1.equals("true") || value1.equals("false") || value2.equals("true") || value2.equals("false")){
			//  At least one value is boolean and not comparable.
			error = true;
		}
		if (value1.contains("#") || value2.contains("#")){
			//  At least one value is a variable location and not comparable.
			error = true;
		}
		
		//  If one value is an integer, the other must also be an integer.
		Integer numberOfInts = 0;
		Integer int1 = Integer.MAX_VALUE; 
		Integer int2 = Integer.MAX_VALUE;
		try {
			int1 = Integer.parseInt(value1);
			numberOfInts += 1;
			int2 = Integer.parseInt(value2);
			numberOfInts += 1;
		}
		catch (NumberFormatException e){
			if (numberOfInts == 1){
				//  One value is an integer and the other is not
				error = true;
			}
			else {
				//  One value could still be an integer
				
				try {
					int2 = Integer.parseInt(value2);
					numberOfInts += 1;
				}
				catch (NumberFormatException e2){
					if (numberOfInts == 1){
						//  One value is an integer and the other is not.
						error = true;
					}
				}
				finally {}				
			}
		}
		finally {}
			
		if (error){
			System.out.println("Error in program execution for piece" + this.currentPiece + ": the two arguments for stack operation greaterThanEqual() are not comparable.");
			return;
		}
		
		boolean result;
		if (numberOfInts == 2){
			//  Perform integer comparison.
			result = (int1 >= int2);
		}
		else {
			//  Perform string comparison.
			result = (value1.compareTo(value2) >= 0);
		}
		
		//  Push the final result.
		this.stacks[this.currentPiece].push("" + result);
	}
	
	/**
	 *  A method for performing a less-than comparison of the top two elements of the current stack.
	 *  @precondition The stack contains at least two comparable elements of identical types.
	 *  @postcondition The top two elements have been popped from the stack, their less-than relationship compared, and the boolean result pushed to the stack.
	 */
	public void lessThan(){
		if (this.stacks[this.currentPiece].size() < 2){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation lessThan().");
			return;
		}
		
		String value1 = this.stacks[this.currentPiece].pop();
		String value2 = this.stacks[this.currentPiece].pop();
		boolean error = false;
		
		if (value1.equals("true") || value1.equals("false") || value2.equals("true") || value2.equals("false")){
			//  At least one value is boolean and not comparable.
			error = true;
		}
		if (value1.contains("#") || value2.contains("#")){
			//  At least one value is a variable location and not comparable.
			error = true;
		}
		
		//  If one value is an integer, the other must also be an integer.
		Integer numberOfInts = 0;
		Integer int1 = Integer.MAX_VALUE; 
		Integer int2 = Integer.MAX_VALUE;
		try {
			int1 = Integer.parseInt(value1);
			numberOfInts += 1;
			int2 = Integer.parseInt(value2);
			numberOfInts += 1;
		}
		catch (NumberFormatException e){
			if (numberOfInts == 1){
				//  One value is an integer and the other is not
				error = true;
			}
			else {
				//  One value could still be an integer
				
				try {
					int2 = Integer.parseInt(value2);
					numberOfInts += 1;
				}
				catch (NumberFormatException e2){
					if (numberOfInts == 1){
						//  One value is an integer and the other is not.
						error = true;
					}
				}
				finally {}				
			}
		}
		finally {}
			
		if (error){
			System.out.println("Error in program execution for piece" + this.currentPiece + ": the two arguments for stack operation lessThan() are not comparable.");
			return;
		}
		
		boolean result;
		if (numberOfInts == 2){
			//  Perform integer comparison.
			result = (int1 < int2);
		}
		else {
			//  Perform string comparison.
			result = (value1.compareTo(value2) == -1);
		}
		
		//  Push the final result.
		this.stacks[this.currentPiece].push("" + result);
	}
	
	/**
	 *  A method for performing a less-than-equal comparison of the top two elements of the current stack.
	 *  @precondition The stack contains at least two comparable elements of identical types.
	 *  @postcondition The top two elements have been popped from the stack, their less-than-equal relationship compared, and the boolean result pushed to the stack.
	 */
	public void lessThanEqual(){
		if (this.stacks[this.currentPiece].size() < 2){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation lessThanEqual().");
			return;
		}
		
		String value1 = this.stacks[this.currentPiece].pop();
		String value2 = this.stacks[this.currentPiece].pop();
		boolean error = false;
		
		if (value1.equals("true") || value1.equals("false") || value2.equals("true") || value2.equals("false")){
			//  At least one value is boolean and not comparable.
			error = true;
		}
		if (value1.contains("#") || value2.contains("#")){
			//  At least one value is a variable location and not comparable.
			error = true;
		}
		
		//  If one value is an integer, the other must also be an integer.
		Integer numberOfInts = 0;
		Integer int1 = Integer.MAX_VALUE; 
		Integer int2 = Integer.MAX_VALUE;
		try {
			int1 = Integer.parseInt(value1);
			numberOfInts += 1;
			int2 = Integer.parseInt(value2);
			numberOfInts += 1;
		}
		catch (NumberFormatException e){
			if (numberOfInts == 1){
				//  One value is an integer and the other is not
				error = true;
			}
			else {
				//  One value could still be an integer
				
				try {
					int2 = Integer.parseInt(value2);
					numberOfInts += 1;
				}
				catch (NumberFormatException e2){
					if (numberOfInts == 1){
						//  One value is an integer and the other is not.
						error = true;
					}
				}
				finally {}				
			}
		}
		finally {}
			
		if (error){
			System.out.println("Error in program execution for piece" + this.currentPiece + ": the two arguments for stack operation lessThanEqual() are not comparable.");
			return;
		}
		
		boolean result;
		if (numberOfInts == 2){
			//  Perform integer comparison.
			result = (int1 <= int2);
		}
		else {
			//  Perform string comparison.
			result = (value1.compareTo(value2) <= 0);
		}
		
		//  Push the final result.
		this.stacks[this.currentPiece].push("" + result);
	}
	
	/**
	 *  A method for performing an equality comparison of the top two elements of the current stack.
	 *  @precondition The stack contains at least two comparable elements of identical types.
	 *  @postcondition The top two elements have been popped from the stack, their equality compared, and the boolean result pushed to the stack.
	 */
	public void equal(){
		if (this.stacks[this.currentPiece].size() < 2){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation equal().");
			return;
		}
		
		String value1 = this.stacks[this.currentPiece].pop();
		String value2 = this.stacks[this.currentPiece].pop();

		if (value1.contains("#") || value2.contains("#")){
			//  At least one value is a variable location and not comparable.
			System.out.println("Error in program execution for piece" + this.currentPiece + ": the two arguments for stack operation equal() are not comparable.");
			return;
		}
		
		boolean result = (value1.equals(value2));
		
		//  Push the final result.
		this.stacks[this.currentPiece].push("" + result);
	}
	
	/**
	 *  A method for performing an inequality comparison of the top two elements of the current stack.
	 *  @precondition The stack contains at least two comparable elements of identical types.
	 *  @postcondition The top two elements have been popped from the stack, their inequality compared, and the boolean result pushed to the stack.
	 */
	public void notEqual(){
		if (this.stacks[this.currentPiece].size() < 2){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation notEqual().");
			return;
		}
		
		String value1 = this.stacks[this.currentPiece].pop();
		String value2 = this.stacks[this.currentPiece].pop();

		if (value1.contains("#") || value2.contains("#")){
			//  At least one value is a variable location and not comparable.
			System.out.println("Error in program execution for piece" + this.currentPiece + ": the two arguments for stack operation notEqual() are not comparable.");
			return;
		}
		
		boolean result = !value1.equals(value2);
		
		//  Push the final result.
		this.stacks[this.currentPiece].push("" + result);
	}
	
	/** 
	 *  A method for running an if statement within a set of instructions.
	 *  @postcondition The if Block has been executed and the instructions between "if" and "then" have been removed from the current instructions.
	 */
	public void ifBlock(){	
		if (this.stacks[this.currentPiece].size() < 1){
			//  No values to execute test block, execute only statements after "then"
			
			String nextTerm = "";
			
			while (!nextTerm.equals("then") && !nextTerm.equals(";") && this.currentInstructions.size() != 0){
				nextTerm = this.currentInstructions.remove(0);
			}
			return;
			
		}
		
		String testValue = this.stacks[this.currentPiece].pop();
		if (!testValue.equals("true") && !testValue.equals("false")){
			//  No boolean to execute test block, execute only statements after "then"
			String nextTerm = "";
			
			while (!nextTerm.equals("then") && !nextTerm.equals(";") && this.currentInstructions.size() != 0){
				nextTerm = this.currentInstructions.remove(0);
			}
			return;
		}
		
		if (testValue.equals("true")){
			String nextTerm = "";
			if (this.currentInstructions.size() != 0){
				nextTerm = this.currentInstructions.remove(0);
				
				//  Parse all terms after "if" and before "else"
				//  Stop if the instruction is malformed
				while (!nextTerm.equals("else") && !nextTerm.equals(";") && !nextTerm.equals("then") && this.currentInstructions.size() != 0){
					this.parse(nextTerm);
					nextTerm = this.currentInstructions.remove(0);
				}
				
				//  When we reach "else" throw everything away between "else" and "then"
				//  Stop if instruction is malformed
				if (nextTerm.equals("else")){
					while (!nextTerm.equals(";") && !nextTerm.equals("then") && this.currentInstructions.size() != 0){					
						nextTerm = this.currentInstructions.remove(0);
					}
				}
				
				//  nextTerm must be either "then" or ";"
				//  Return to parser.
				if (nextTerm.equals(";")){
					hitSemicolon = true;
				}
				return;
			}
			else {
				return;
			}
		}
		else {
			String nextTerm = "";
			if (this.currentInstructions.size() != 0){
				//  Throw away everything until "else"
				//  Stop if the instruction is malformed
				while (!nextTerm.equals("else") && !nextTerm.equals(";") && !nextTerm.equals("then") && this.currentInstructions.size() != 0){					
					nextTerm = this.currentInstructions.remove(0);
				}
				
				//  When we reach "else", execute everything between "else" and "then"
				//  Stop if instruction is malformed
				if (nextTerm.equals("else")){
					if (this.currentInstructions.size() !=0 ){
						nextTerm = this.currentInstructions.remove(0);
						while (!nextTerm.equals(";") && !nextTerm.equals("then") && this.currentInstructions.size() != 0){
							this.parse(nextTerm);
							nextTerm = this.currentInstructions.remove(0);
						}
					}
					else {
						return;
					}
				}
				
				if (nextTerm.equals(";")){
					hitSemicolon = true;
				}
				return;
			}
			else {
				return;
			}
		}

		
	}
	
	/** 
	 *  A method for running a while loop within a set of instructions.
	 *  @postcondition The while loop has been executed and the instructions between "begin" and "until" have been removed from the current instructions.
	 */
	public void whileBlock(){
		if (this.currentInstructions.size() == 0) { 
			return;
		}
		
		List<String> bodyBlock = new ArrayList<String>();
		String nextTerm = this.currentInstructions.remove(0);
		while (!nextTerm.equals("until") && !nextTerm.equals(";") && this.currentInstructions.size() > 0) {
			bodyBlock.add(nextTerm);	
			nextTerm = this.currentInstructions.remove(0);
		}
		
		
		if (bodyBlock.size() == 0){
			System.out.println("Error in whileBlock(): empty body. Ending whileBlock().");
			return;
			
		}
		
		bodyBlock.add("until");
		
		if (nextTerm.equals(";")) {
			this.hitSemicolon = true;
		}
		boolean finished = false;
		turnTimer = new Timer();
		turnTimer.schedule(new TurnTimerTask(this), 10000);
		while(!finished && !this.forceLoopInterrupt){
			this.currentInstructions.addAll(0, bodyBlock);
			if (this.currentInstructions.size() == 0){
				finished = true;
				turnTimer.cancel();
				break;
			}
			
			while (this.currentInstructions.size() > 0){
				nextTerm = this.currentInstructions.remove(0);
				if (nextTerm.equals("until")){
					//  Finished 
					if (this.stacks[this.currentPiece].size() == 0){
						System.out.println("Error in whileBlock: no value on stack to test finished condition. Aborting turn.");
						this.currentInstructions.clear();
						finished = true;
						turnTimer.cancel();
						return;
					}
					String testCondition = this.stacks[this.currentPiece].pop();
					if (testCondition.equals("true") || testCondition.equals("false"))
					{
						if (testCondition.equals("true")){
							finished = true;
							turnTimer.cancel();
							return;
						}
					}
					else {
						System.out.println("Error in whileBlock: no boolean  value on stack to test finished condition. Aborting turn.");
						this.currentInstructions.clear();
						finished = true;
						turnTimer.cancel();
						return;
					}
				}
				else {
					this.parse(nextTerm);
				}
			}
		}
		if (forceLoopInterrupt){
			forceLoopInterrupt = false;
			System.out.println("Error in execution of whileBlock(): timed out. Ending turn.");
			this.currentInstructions.clear();
			this.timeoutTask.cancel();
			return;
		}
	}
	
	/** 
	 *  A method for running a for loop within a set of instructions.
	 *  @postcondition The for loop has been executed and the instructions between "do" and "loop" have been removed from the current instructions.
	 */
	public void forBlock(){
		if (this.currentInstructions.size() == 0) { 
			return;
		}
		
		Integer start;
		Integer end;
		
		List<String> bodyBlock = new ArrayList<String>();
		String nextTerm = this.currentInstructions.remove(0);
		while (!nextTerm.equals("loop") && !nextTerm.equals(";") && this.currentInstructions.size() > 0) {
			bodyBlock.add(nextTerm);	
			nextTerm = this.currentInstructions.remove(0);
		}
		
		
		if (bodyBlock.size() == 0){
			System.out.println("Error in forBlock(): empty body. Ending forBlock().");
			return;			
		}
		
		bodyBlock.add("loop");
		System.out.println(bodyBlock);
		
		if (nextTerm.equals(";")) {
			this.hitSemicolon = true;
		}
		
		if (this.stacks[this.currentPiece].size() < 2){
			//  There are not enough arguments on the stack, so we only need to execute the body once.
			start = 0;
			end = 0;
			this.stacks[this.currentPiece].clear();
		}
		else {
			try {
				start = Integer.parseInt(this.stacks[this.currentPiece].pop());
				end = Integer.parseInt(this.stacks[this.currentPiece].pop());
				
				if (end <= start){
					start = 0;
					end = 0;
				}
			}
			catch (NumberFormatException e) {
				//  One or both of the arguments are not integers, so we ignore then and only need to execute the body once.
				start = 0;
				end = 0;
			}
			finally {}
		}
		
		this.loopIterators.add(start);
		this.leaveForBlock = false;
		turnTimer = new Timer();
		turnTimer.schedule(new TurnTimerTask(this), 10000);
		for(this.loopIterators.get(this.loopIterators.size() -1); this.loopIterators.get(this.loopIterators.size() -1) <= end; this.loopIterators.set(this.loopIterators.size() - 1, this.loopIterators.get(this.loopIterators.size() -1) + 1)){
			this.currentInstructions.addAll(0, bodyBlock);
			if (this.currentInstructions.size() == 0){
				turnTimer.cancel();
				break;
			}
			
			while (this.currentInstructions.size() > 0){
				nextTerm = this.currentInstructions.remove(0);
				if (nextTerm.equals("loop")){
					break;
				}
				else {
					this.parse(nextTerm);
				}
			}
			
			if (this.leaveForBlock){
				this.leaveForBlock = false;
				turnTimer.cancel();
				break;		
			}
			
			if (forceLoopInterrupt){
				forceLoopInterrupt = false;
				System.out.println("Error in execution of forBlock(): timed out. Ending turn.");
				this.currentInstructions.clear();
				this.timeoutTask.cancel();
				return;
			}
			
		}
		
		if (this.loopIterators.size() > 0){
			this.loopIterators.remove(this.loopIterators.size() -1);
		}
		turnTimer.cancel();
		
		return;
	}
	
	/**
	 *  A method for declaring a new UserVariable.
	 *  A new UserVariable with the matching name of the following term has been created if it does not already exist.
	 */
	public void declareVar(){
		
		if (this.currentInstructions.size() < 1){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough instructions for operation declareVar().");
			return;
		}
		
		String value = this.currentInstructions.remove(0);
		
		try {
			Integer q = Integer.parseInt(value);
			q += 1;
			System.out.println("Error in program execution for piece" + this.currentPiece + ": integer argument as name in declareVar().");
			return;
		}
		catch (NumberFormatException e){
			if (value.equals("true") || value.equals("false")){
				System.out.println("Error in program execution for piece" + this.currentPiece + ": boolean argument as name in declareVar().");
				return;
			}
			if (value.contains("#")){
				System.out.println("Error in program execution for piece" + this.currentPiece + ": variable location as name in declareVar().");
				return;
			}
		}
		finally {}
		
		boolean found = false;
		for (int i = 0; i < this.userVars[this.currentPiece].size(); i++){
			if (this.userVars[this.currentPiece].get(i).getName() == value){
				found = true;
				break;
			}
		}
		if (!found){
			try{
				this.userVars[this.currentPiece].add(new UserVariable(value));
			}
			catch (Exception e){
				System.out.println("Error in declareVar(): name of new variable should never ben an empty string.");
				this.stacks[this.currentPiece].clear();
				return;
			}
			finally {}
		}
		
	}
	
	/**
	 *  A method for declaring a new UserWord.
	 *  @precondition The stack contains at least one element.
	 *  @postcondition The string has been popped from the stack, and a new UserWord with the matching name has been created if it does not already exist.
	 */
	public void declareWord(){
		
		System.out.println("declareWord current piece: " + this.currentPiece);
		
		if (this.currentInstructions.size() < 1){
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough instructions for operation declareWord().");
			return;
		}
		
		String value = this.currentInstructions.remove(0);
		
		try {
			Integer q = Integer.parseInt(value);
			q += 1;
			System.out.println("Error in program execution for piece" + this.currentPiece + ": integer argument as word name in declareWord().");
			return;
		}
		catch (NumberFormatException e){
			if (value.equals("true") || value.equals("false")){
				this.stacks[this.currentPiece].clear();
				System.out.println("Error in program execution for piece" + this.currentPiece + ": boolean argument as word name in declareWord().");
				return;
			}
			if (value.contains("#")){
				this.stacks[this.currentPiece].clear();
				System.out.println("Error in program execution for piece" + this.currentPiece + ": variable location as word name in declareWord().");
				return;
			}
		}
		finally {}
		
		boolean found = false;
		for (int i = 0; i < this.userWords[this.currentPiece].size(); i++){
			if (this.userWords[this.currentPiece].get(i).getName() == value){
				found = true;
				break;
			}
		}
		if (!found){
			
			try{
				this.userWords[this.currentPiece].add(new UserWord(value));
				System.out.println(value);
				if (this.currentInstructions.size() == 0){
					this.userWords[this.currentPiece].get(this.userWords[this.currentPiece].size() -1).setReplaceValues(new ArrayList<String>());
				}
				else {
					//  Get the replace values for the word from the current instruction list.
					
					String next = this.currentInstructions.remove(0);
					ArrayList<String> replace = new ArrayList<>();
					replace.add(next);
					while (this.currentInstructions.size() != 0){
						next = this.currentInstructions.remove(0);
						if (!next.equals(";")){
							replace.add(next);
						}
						else {
							System.out.println("Hit a semicolon in declareWord()!");
							this.hitSemicolon = true;
							break;
						}
						
					}
					this.userWords[this.currentPiece].get(this.userWords[this.currentPiece].size() -1).setReplaceValues(replace);
				}
			}
			catch (Exception e){
				//  The name of the word to be created was empty
				System.out.println("Error in declareWord(): name of new word should never be an empty string.");
				this.stacks[this.currentPiece].clear();
				return;
			}
			finally {}
			if (this.hitSemicolon == true){
				this.hitSemicolon = false;
			}
			System.out.println("Replace: " + this.userWords[this.currentPiece].get(this.userWords[this.currentPiece].size() -1).getReplaceValues());
		}
	}
	
	/**
	 *  A method for generating a random integer.
	 *  @precondition The stack has an integer value on top.
	 *  @postcondition The integer has been popped from the stack, a random integer between 0 and that value generated, and the result pushed to the stack.
	 */
	public void random(){
		if (this.stacks[this.currentPiece].size() < 1){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation random().");
			return;
		}
		try {
			Integer arg1 = Integer.parseInt(this.stacks[this.currentPiece].pop());
			if (arg1 <= 0){
				this.stacks[this.currentPiece].push("0");
			}
			else {
				Random r = new Random();
				this.stacks[this.currentPiece].push("" + r.nextInt(arg1));
			}
			return;
		}
		catch (NumberFormatException e) {
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": incorrect argument types for stack operation random().");
			return;
		}
		finally {}
	}
	
	/**
	 *  A debug method for printing a value on the stack.
	 *  @precondition The stack contains at least one element.
	 *  @postcondition The top value has been popped from the stack and printed to the console.
	 */
	public void dotPrint(){
		if (this.stacks[this.currentPiece].size() < 1){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation random().");
			return;
		}
		
		String value = this.stacks[this.currentPiece].pop();
		System.out.println("dotPrint(): " + value);
	}
	
	/**
	 *  A method for querying the board about the maximum health of the current piece.
	 *  @postcondition The maximum health of the current piece has been pushed to the stack.
	 */
	public void qHealth(){
		try {
			Piece p = this.team.getPiece(this.currentPiece);
			this.stacks[this.currentPiece].push("" + p.getHealth());
		}
		catch (Exception e){
			System.out.println("Error in program execution for piece" + this.currentPiece + ": invalid piece index when querying in qHealth().");
			this.stacks[this.currentPiece].clear();
			return;
		}
		finally {}
	}
	
	/**
	 *  A method for querying the board about the current health of the current piece.
	 *  @postcondition The current health of the current piece has been pushed to the stack.
	 */
	public void qHealthLeft(){
		try {
			Piece p = this.team.getPiece(this.currentPiece);
			this.stacks[this.currentPiece].push("" + p.getCurrentHealth());
		}
		catch (Exception e){
			System.out.println("Error in program execution for piece" + this.currentPiece + ": invalid piece index when querying in qHealthLeft().");
			this.stacks[this.currentPiece].clear();
			return;
		}
		finally {}
	}
	
	/**
	 *  A method for querying the board about the maximum moves of the current piece.
	 *  @postcondition The maximum moves of the current piece has been pushed to the stack.
	 */
	public void qMoves(){
		try {
			Piece p = this.team.getPiece(this.currentPiece);
			this.stacks[this.currentPiece].push("" + p.getMovement());
		}
		catch (Exception e){
			System.out.println("Error in program execution for piece" + this.currentPiece + ": invalid piece index when querying in qMoves().");
			this.stacks[this.currentPiece].clear();
			return;
		}
		finally {}
	}
	
	/**
	 *  A method for querying the board about the remaining moves of the current piece.
	 *  @postcondition The remaining moves of the current piece has been pushed to the stack.
	 */
	public void qMovesLeft(){
		try {
			Piece p = this.team.getPiece(this.currentPiece);
			this.stacks[this.currentPiece].push("" + p.getCurrentMovement());
		}
		catch (Exception e){
			System.out.println("Error in program execution for piece" + this.currentPiece + ": invalid piece index when querying in qMovesLeft().");
			this.stacks[this.currentPiece].clear();
			return;
		}
		finally {}
	}
	
	/**
	 *  A method for querying the board about the attack of the current piece.
	 *  @postcondition The attack of the current piece has been pushed to the stack.
	 */
	public void qAttack(){
		try {
			Piece p = this.team.getPiece(this.currentPiece);
			this.stacks[this.currentPiece].push("" + p.getAttack());
		}
		catch (Exception e){
			System.out.println("Error in program execution for piece" + this.currentPiece + ": invalid piece index when querying in qAttack().");
			this.stacks[this.currentPiece].clear();
			return;
		}
		finally {}
	}
	
	/**
	 *  A method for querying the board about the range of the current piece.
	 *  @postcondition The range of the current piece has been pushed to the stack.
	 */
	public void qRange(){
		try {
			Piece p = this.team.getPiece(this.currentPiece);
			this.stacks[this.currentPiece].push("" + p.getRange());
		}
		catch (Exception e){
			System.out.println("Error in program execution for piece" + this.currentPiece + ": invalid piece index when querying in qRange().");
			this.stacks[this.currentPiece].clear();
			return;
		}
		finally {}
	}
	
	/**
	 *  A method for querying the board about the team of the current piece.
	 *  @postcondition The team of the current piece has been pushed to the stack.
	 */
	public void qTeam(){
		this.stacks[this.currentPiece].push("" + this.team.getColour().toString());
	}
	
	/**
	 *  A method for querying the board about the unit type (Scout, Sniper or Tank) of the current piece.
	 *  @postcondition The type of the current piece has been pushed to the stack.
	 */
	public void qType(){
		try {
			Piece p = this.team.getPiece(this.currentPiece);
			this.stacks[this.currentPiece].push("" + p.getType().toString());
		}
		catch (Exception e){
			System.out.println("Error in program execution for piece" + this.currentPiece + ": invalid piece index when querying in qType().");
			this.stacks[this.currentPiece].clear();
			return;
		}
		finally {}
	}
	
	/**
	 *  A method for turning the current piece on the board (changing the rotation).
	 *  @precondition The top of the stack contains an integer.
	 *  @postcondition The integer has been popped from the stack and the current piece has been rotated the corresponding amount.
	 */
	public void turn(){
		if (this.stacks[this.currentPiece].size() < 1){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments on stack for robot operation turn().");
			return;
		}
		
		String arg1 = this.stacks[this.currentPiece].pop();
		Integer rotation;
		try {
			rotation = Integer.parseInt(arg1);
			this.team.getPiece(this.currentPiece).rotate(rotation);
			
		}
		catch (NumberFormatException e){
			//  The top of the stack is not an integer.
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": need integer on stack for robot operation turn().");
			return;
		}
		finally {}
		
	}
	
	/**
	 *  A method for moving the current piece on the board.
	 *  @postcondition The current piece has moved one space forward on the board.
	 */
	public void move(){
		try{
			this.team.getBoard().moveForward();
		}
		catch (RuntimeException e) {
			System.out.println(e);
			this.stacks[this.currentPiece].clear();
			this.currentInstructions.clear();
			return;
		}
		finally {}
	}
	
	/**
	 *  A method for using the current piece on the board to shoot a space on the board.
	 *  @precondition The top of the stack contains two integer values.
	 *  @postcondition The two integers, m and n, have been popped from the stack, and the space at m distance in direction n has been shot.
	 */
	public void shoot(){
		if (this.stacks[this.currentPiece].size() < 2){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments on stack for robot operation shoot().");
			return;
		}
		
		String arg1 = this.stacks[this.currentPiece].pop();
		String arg2 = this.stacks[this.currentPiece].pop();
		Integer distance;
		Integer direction;
		try {
			distance = Integer.parseInt(arg1);
			direction = Integer.parseInt(arg2);
			this.team.getBoard().shootSpace(distance, direction);
			
		}
		catch (NumberFormatException e){
			//  The top of the stack is not an integer.
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": need integer on stack for robot operation shoot().");
			return;
		}
		finally {}
		
		
	}

	/**
	 *  A method for checking the contents of a space on the board.
	 *  @precondition The top of the stack contains an integer.
	 *  @postcondition The integer has been popped from the stack, and a string representing the contents of the space (EMPTY, OCCUPIED or OUT OF BOUNDS) has been pushed to the stack.
	 */
	public void check(){
		if (this.stacks[this.currentPiece].size() < 1){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments on stack for robot operation check().");
			return;
		}
		
		String arg1 = this.stacks[this.currentPiece].pop();
		Integer space;
		try {
			space = Integer.parseInt(arg1);
			this.stacks[this.currentPiece].push(this.team.getBoard().checkSpace(space));
			
		}
		catch (NumberFormatException e){
			//  The top of the stack is not an integer.
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": need integer on stack for robot operation check().");
			return;
		}
		finally {}
	}
	
	
	/**
	 *  A method for counting the number of visible pieces, based on the current piece.
	 *  @postcondition An integer representing the number of visible pieces (0 or greater) has been pushed to the stack.
	 */
	public void scan(){
		this.stacks[this.currentPiece].push("" + this.team.getBoard().scanArea().size());
	}
	
	/**
	 *  A method for querying information about a visible piece.
	 *  @precondition The top of the stack contains an integer.
	 *  @postcondition The integer has been popped from the stack, and values representing the remaining health, direction, range and team colour of the robot have been pushed to the stack in that order.
	 */	
	public void identify(){
		if (this.stacks[this.currentPiece].size() < 1){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments on stack for robot operation identify().");
			return;
		}
		
		String arg1 = this.stacks[this.currentPiece].pop();
		Integer nthPiece;
		try {
			nthPiece = Integer.parseInt(arg1);
			List<Integer> inRangePieces = this.team.getBoard().scanArea();
			
			if (nthPiece >= inRangePieces.size()){
				this.stacks[this.currentPiece].clear();
				System.out.println("Error in program execution for piece" + this.currentPiece + ": piece index out of range for robot operation identify().");
				return;
			}
			
			
			ArrayList<String> info = this.team.getBoard().getPieceInfoFromOffset(inRangePieces.get(nthPiece));
			
			if (info.size() != 4){
				this.stacks[this.currentPiece].clear();
				System.out.println("Error in program execution for piece" + this.currentPiece + ": malformed info returned for robot operation identify().");
				return;
			}
			
			//  Push the four pieces of information returned to the stack.
			this.stacks[this.currentPiece].push(info.remove(0));
			this.stacks[this.currentPiece].push(info.remove(0));
			this.stacks[this.currentPiece].push(info.remove(0));
			this.stacks[this.currentPiece].push(info.remove(0));
			return;
		}
		catch (NumberFormatException e){
			//  The top of the stack is not an integer.
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": need integer on stack for robot operation identify().");
			return;
		}
		finally {}
		
		
	}

	/**
	 *  A method for sending a message from one piece to another.
	 *  @precondition The top of the stack contains at least two values, with a string at the top, and any value below it.
	 *  @postcondition The two values have been popped from the stack, the message sent if possible, and a boolean true or false value has been pushed to the stack.
	 */
	public void sendMessage(){
		if (this.stacks[this.currentPiece].size() < 2){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation sendMessage().");
			return;
		}
		
		String recipient = this.stacks[this.currentPiece].pop();
		String message = this.stacks[this.currentPiece].pop();
		
		if (recipient.equals("true") || recipient.equals("false") || recipient.contains("#")){
			System.out.println("Error in program execution for piece" + this.currentPiece + ": wrong argument type as recipient in sendMessage().");
			return;
		}
		
		try{
			Integer q = Integer.parseInt(recipient);
			q +=1;
			System.out.println("Error in program execution for piece" + this.currentPiece + ": integer argument type as recipient in sendMessage().");
			this.stacks[this.currentPiece].clear();
			return;
		}
		catch (NumberFormatException e){
			//  The value is a string and can be used to address a piece.
			if (!this.team.getPiece(this.idToIndex.get(recipient)).isAlive()){
				this.stacks[this.currentPiece].push("false");
			}
			else {
				boolean success = this.mailboxes[this.currentPiece].sendMessage(recipient, message);
				if (success){
					this.stacks[this.currentPiece].push("true");
				}
				else {
					this.stacks[this.currentPiece].push("false");
				}
			}
		}
		finally{}
		
	}
	
	/**
	 *  A method for checking the current piece's mailbox for messages from a sender.
	 *  @precondition The top of the stack contains a string.
	 *  @postcondition The string has been popped from the stack, the mailbox checked, and a boolean true or false value has been pushed to the stack.
	 */
	public void checkMessages(){
		if (this.stacks[this.currentPiece].size() < 1){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation checkMessages().");
			return;
		}
		String sender = this.stacks[this.currentPiece].pop();
		
		if (sender.equals("true") || sender.equals("false") || sender.contains("#")){
			System.out.println("Error in program execution for piece" + this.currentPiece + ": wrong argument type as recipient in checkMessages().");
			return;
		}
		
		try{
			Integer q = Integer.parseInt(sender);
			q +=1;
			System.out.println("Error in program execution for piece" + this.currentPiece + ": integer argument type as recipient in checkMessages().");
			this.stacks[this.currentPiece].clear();
			return;
		}
		catch (NumberFormatException e){
			//  The value is a string and can be used to address a piece.
			boolean hasMessage = this.mailboxes[this.currentPiece].hasMessage(sender);
			if (hasMessage){
				this.stacks[this.currentPiece].push("true");
			}
			else {
				this.stacks[this.currentPiece].push("false");
			}
		}
		finally{}
	}
	
	/**
	 *  A method for retrieving messages from a sender from the current piece's mailbox.
	 *  @precondition The top of the stack contains a string, and the mailbox contains a message from the sender.
	 *  @postcondition The string has been popped from the stack, the mailbox checked, and the value of the message has been pushed to the stack.
	 */
	public void receiveMessage(){
		if (this.stacks[this.currentPiece].size() < 1){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation receiveMessage().");
			return;
		}
		
		String sender = this.stacks[this.currentPiece].pop();
		
		if (sender.equals("true") || sender.equals("false") || sender.contains("#")){
			System.out.println("Error in program execution for piece" + this.currentPiece + ": wrong argument type as recipient in receiveMessage().");
			return;
		}
		
		try{
			Integer q = Integer.parseInt(sender);
			q +=1;
			System.out.println("Error in program execution for piece" + this.currentPiece + ": integer argument type as recipient in receiveMessage().");
			this.stacks[this.currentPiece].clear();
			return;
		}
		catch (NumberFormatException e){
			//  The value is a string and can be used to address a piece.
			String message = this.mailboxes[this.currentPiece].receiveMessage(sender);
			if (!message.equals("")){
				System.out.println(message);
				this.stacks[this.currentPiece].push(message);
			}
		}
		finally{}
	}
	
	
	/**
	 * A method for storing a value within a user-defined variable.
	 * @precondition The top element of the stack is an address and the element below that is some value.
	 * @postcondition The top two elements of the stack have been popped, and the value has been stored in the location.
	 * 
	 */
	public void store(){
		if (this.stacks[this.currentPiece].size() < 2){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation store().");
			return;
		}
		
		//  The top of the stack must be a variable location.
		String arg1 = this.stacks[this.currentPiece].pop();
		if (arg1.contains("#") == false){
			System.out.println(arg1);
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": argument for stack operation store() not a variable location." );
			return;
		}
		
		UserVariable matchingVar = null;
		//  Search and try to find the matching variable.
		for (int i = 0; i < this.userVars[this.currentPiece].size(); i++){
			if (this.userVars[this.currentPiece].get(i).getName().equals(arg1.substring(1))){
				matchingVar = this.userVars[this.currentPiece].get(i);
				break;
			}
		}
		
		if (matchingVar == null){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": no such variable location " + arg1 + " for store()." );
			return;
		}
		
		String arg2 = this.stacks[this.currentPiece].pop();
		matchingVar.setValue(arg2);
		
		return;
		
		
	}
	
	/**
	 * A method for retrieving a value from a user-defined variable.
	 * @precondition The top element of the stack is an address.
	 * @postcondition The address has been popped, and the value stored at the address has been pushed to the stack.
	 */
	public void retrieve(){
		if (this.stacks[this.currentPiece].size() < 1){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": not enough arguments for stack operation retrieve().");
			return;
		}
		
		//  The top of the stack must be a variable location.
		String arg1 = this.stacks[this.currentPiece].pop();
		if (arg1.contains("#") == false){
			System.out.println(arg1);
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": argument for stack operation retrieve() not a variable location.");
			return;
		}
		
		String value = "";
		UserVariable matchingVar = null;
		//  Search and try to find the matching variable.
		for (int i = 0; i < this.userVars[this.currentPiece].size(); i++){
			if (this.userVars[this.currentPiece].get(i).getName().equals(arg1.substring(1))){
				matchingVar = this.userVars[this.currentPiece].get(i);
				value = matchingVar.getValue();
				break;
			}
		}
		
		if (matchingVar == null){
			this.stacks[this.currentPiece].clear();
			System.out.println("Error in program execution for piece" + this.currentPiece + ": no such variable location " + arg1 + " for retrieve()." );
			return;
		}
		
		this.stacks[this.currentPiece].push(value);
		return;
		
	}
	
	/**
	 * A method used for parsing each term of a program and taking the appropriate action.
	 * @param term - A string representing the term to parse.
	 * @postcondition The value has been parsed and the appropriate action has been taken.
	 */
	public void parse(String term){
		
		//  System.out.println("Parsing " + term);
		
		//   Check if the term is a semicolon; if it is, we're finished.
		if (term.equals("")){
			return;
		}
		
		
		if (term.equals(";")){
//			this.currentInstructions.clear();
			this.loopIterators.clear();
			this.hitSemicolon = false;
			return;
		}
		
		//  Check if the term stands in for a loop iterator; if it is, get the value and push to the stack
		if (term.equals("I")){
			if (this.loopIterators.size() == 0){
				//  If there are no iterators, throw an error.
				
				System.out.println("Error in parse(): no loop iterator value to push for term 'I'. Aborting.");
				this.currentInstructions.clear();
				return;
			}
			
			this.stacks[this.currentPiece].push("" + this.loopIterators.get(this.loopIterators.size()-1));
			return;
		}
		
		//  Check if the term is "leave"; if it is, we need to end a for block somewhere.
		if (term.equals("leave")){
			this.leaveForBlock = true;
			return;
		}
		
		//  Check if the term is one of the built-in methods; if it is, execute it.
		if (this.functionTableMapping.containsKey(term)) {
			
			if (!this.restrictedWords.contains(term) || isPlayMode == true){
			
				int index = this.functionTableMapping.get(term);
				this.methods[index].execute();
				if (this.hitSemicolon == true){
					this.currentInstructions.clear();
					this.loopIterators.clear();
					this.hitSemicolon = false;
				}
			}
			else{
				System.out.println("Error in parse(): restricted word cannot be parsed outside of play mode. Aborting.");
				this.currentInstructions.clear();
			}
			return;
		}
		
		//  Check if the term is a user word; if it is, add the replacement values to currentInstructions.
		for (UserWord word : this.userWords[this.currentPiece]) {
			if (word.getName().equals(term)) {
				ArrayList<String> values = new ArrayList<>(word.getReplaceValues());
				this.currentInstructions.addAll(0, values);
				return;
			}
		}
		
		//  Check if the term is a user variable; if it is, push the location trigger to the stack.
		for (UserVariable variable : this.userVars[this.currentPiece]) {
			if (variable.getName().equals(term)) {
				String address = "#" + variable.varName;
				this.stacks[this.currentPiece].push(address);
				return;
			}
		}
		
		//  Check if the term is a string; if it is, strip the quotation marks and leading dot and push to the stack.
		if (term.startsWith(".")) {
			this.stacks[this.currentPiece].push(term.split("\"")[1]);

		}
		
		//  Else the term is a boolean or integer; push it to the stack.
		else {
			this.stacks[this.currentPiece].push(term);
		}
		
	}
	
	public static void main(String[] args){
		
		//  Main class providing testing for the Interpreter Class
		
		
		//  Use the Team test constructor.
		Team t = new Team(TeamEnum.RED, true, 1, 1, 1);
		//PlayerSettings playerS = new PlayerSettings();
		
		Interpreter testI = new Interpreter(3, t, null);
		
		String sResult;
		
		System.out.println("Beginning tests for Interpreter class.");
		
		/*
		  add() :
		o Test case: The stack contains fewer than two values. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values where one or both are not integers. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two integers on top. Expected result: The top of the stack contains the correct result of the addition.
		 */

		testI.add();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter add() Test 1. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter add() Test 1.");
		}
		
		testI.stacks[0].push("cow");
		
		testI.add();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter add() Test 2. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter add() Test 2.");
		}
		
		testI.stacks[0].push("cow");
		testI.stacks[0].push("chicken");
		
		testI.add();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter add() Test 3. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter add() Test 3.");
		}
		
		testI.stacks[0].push("cow");
		testI.stacks[0].push("42");
		
		testI.add();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter add() Test 4. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter add() Test 4.");
		}
		
		testI.stacks[0].push("8");
		testI.stacks[0].push("42");
		
		testI.add();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter add() Test 5. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter add() Test 5.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("50")){
			System.out.println("FAILED: Interpreter add() Test 6. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter add() Test 6.");
		}
		
		testI.stacks[0].push("1");
		testI.stacks[0].push("2");
		testI.stacks[0].push("3");
		
		testI.add();
		
		if (testI.stacks[0].size() != 2){
			System.out.println("FAILED: Interpreter add() Test 7. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter add() Test 7.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("5")){
			System.out.println("FAILED: Interpreter add() Test 8. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter add() Test 8.");
		}
		
		testI.stacks[0].push("5");
		testI.add();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter add() Test 9. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter add() Test 9.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("6")){
			System.out.println("FAILED: Interpreter add() Test 10. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter add() Test 10.");
		}
		

		/*
		   subtract() :
		o Test case: The stack contains fewer than two values. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values where one or both are not integers. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two integers on top. Expected result: The top of the stack contains the correct result of the subtraction
		*/
		
		testI.subtract();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter subtract() Test 11. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter subtract() Test 11.");
		}
		
		testI.stacks[0].push("cow");
		
		testI.subtract();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter subtract() Test 12. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter subtract() Test 12.");
		}
		
		testI.stacks[0].push("cow");
		testI.stacks[0].push("chicken");
		
		testI.subtract();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter subtract() Test 13. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter subtract() Test 13.");
		}
		
		testI.stacks[0].push("cow");
		testI.stacks[0].push("42");
		
		testI.subtract();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter subtract() Test 14. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter subtract() Test 14.");
		}
		
		testI.stacks[0].push("42");
		testI.stacks[0].push("8");
		
		testI.subtract();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter subtract() Test 15. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter subtract() Test 15.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("-34")){
			System.out.println("FAILED: Interpreter subtract() Test 16. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter subtract() Test 16.");
		}
		
		testI.stacks[0].push("1");
		testI.stacks[0].push("2");
		testI.stacks[0].push("3");
		
		testI.subtract();
		
		if (testI.stacks[0].size() != 2){
			System.out.println("FAILED: Interpreter subtract() Test 17. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter subtract() Test 17.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("1")){
			System.out.println("FAILED: Interpreter subtract() Test 18. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter subtract() Test 18.");
		}
		
		testI.stacks[0].push("1");
		testI.subtract();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter subtract() Test 19. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter subtract() Test 19.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("0")){
			System.out.println("FAILED: Interpreter subtract() Test 20. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter subtract() Test 20.");
		}
		
		/*multiply() :
		o Test case: The stack contains fewer than two values. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values where one or both are not integers. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two integers on top. Expected result: The top of the stack contains the correct result of the multiplication.
		*/
		
		testI.multiply();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter multiply() Test 21. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter multiply() Test 21.");
		}
		
		testI.stacks[0].push("cow");
		
		testI.multiply();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter multiply() Test 22. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter multiply() Test 22.");
		}
		
		testI.stacks[0].push("cow");
		testI.stacks[0].push("chicken");
		
		testI.multiply();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter multiply() Test 23. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter multiply() Test 23.");
		}
		
		testI.stacks[0].push("cow");
		testI.stacks[0].push("42");
		
		testI.multiply();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter multiply() Test 24. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter multiply() Test 24.");
		}
		
		testI.stacks[0].push("6");
		testI.stacks[0].push("4");
		
		testI.multiply();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter multiply() Test 25. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter multiply() Test 25.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("24")){
			System.out.println("FAILED: Interpreter multiply() Test 26. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter multiply() Test 26.");
		}
		
		testI.stacks[0].push("1");
		testI.stacks[0].push("2");
		testI.stacks[0].push("3");
		
		testI.multiply();
		
		if (testI.stacks[0].size() != 2){
			System.out.println("FAILED: Interpreter multiply() Test 27. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter multiply() Test 27.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("6")){
			System.out.println("FAILED: Interpreter multiply() Test 28. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter multiply() Test 28.");
		}
		
		testI.stacks[0].push("6");
		testI.multiply();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter multiply() Test 29. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter multiply() Test 29.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("6")){
			System.out.println("FAILED: Interpreter multiply() Test 30. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter multiply() Test 30.");
		}
		
		/* divideRemain() :
		o Test case: The stack contains fewer than two values. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values where one or both are not integers. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two integers on top that divide evenly. Expected result: The top of the stack contains the correct result of the division, and the second value from the top is 0.
		o Test case: The stack contains two integers on top that do not divide evenly. Expected result: The top of the stack contains the correct result of the division, and the second value from the top is the remainder.
		*/
		
		
		testI.divideRemain();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter divideRemain() Test 31. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 31.");
		}
		
		testI.stacks[0].push("cow");
		
		testI.divideRemain();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter divideRemain() Test 32. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 32.");
		}
		
		testI.stacks[0].push("cow");
		testI.stacks[0].push("chicken");
		
		testI.divideRemain();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter divideRemain() Test 33. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 33.");
		}
		
		testI.stacks[0].push("cow");
		testI.stacks[0].push("42");
		
		testI.divideRemain();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter divideRemain() Test 34. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 34.");
		}
		
		testI.stacks[0].push("3");
		testI.stacks[0].push("12");
		
		testI.divideRemain();
		
		if (testI.stacks[0].size() != 2){
			System.out.println("FAILED: Interpreter divideRemain() Test 35. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 35.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("4")){
			System.out.println("FAILED: Interpreter divideRemain() Test 36. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 36.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("0")){
			System.out.println("FAILED: Interpreter divideRemain() Test 37. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 37.");
		}
		
		testI.stacks[0].push("5");
		testI.stacks[0].push("12");
		
		testI.divideRemain();
		
		if (testI.stacks[0].size() != 2){
			System.out.println("FAILED: Interpreter divideRemain() Test 38. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 38.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("2")){
			System.out.println("FAILED: Interpreter divideRemain() Test 39. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 39.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("2")){
			System.out.println("FAILED: Interpreter divideRemain() Test 40. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 40.");
		}
		
		testI.stacks[0].push("7");
		testI.stacks[0].push("58");
		testI.stacks[0].push("100");
		
		testI.divideRemain();
		
		if (testI.stacks[0].size() != 3){
			System.out.println("FAILED: Interpreter divideRemain() Test 41. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 41.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("1")){
			System.out.println("FAILED: Interpreter divideRemain() Test 42. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 42.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("42")){
			System.out.println("FAILED: Interpreter divideRemain() Test 43. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 43.");
		}
		
		testI.stacks[0].push("42");
		testI.divideRemain();
		
		if (testI.stacks[0].size() != 2){
			System.out.println("FAILED: Interpreter divideRemain() Test 44. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 44.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("6")){
			System.out.println("FAILED: Interpreter divideRemain() Test 45. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 45.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("0")){
			System.out.println("FAILED: Interpreter divideRemain() Test 46. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter divideRemain() Test 46.");
		}
		
		
		/*and() :
		o Test case: The stack contains fewer than two values. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values where one or both are not boolean values. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two boolean values on top. Expected result: The top of the stack contains the correct result of the and() operation as a boolean, true or false.
		*/
		
		testI.and();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter and() Test 47. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter and() Test 47.");
		}
		
		testI.stacks[0].push("cow");
		
		testI.and();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter and() Test 48. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter and() Test 48.");
		}
		
		testI.stacks[0].push("123");
		testI.stacks[0].push("456");
		
		testI.and();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter and() Test 49. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter and() Test 49.");
		}
		
		testI.stacks[0].push("false");
		testI.stacks[0].push("456");
		
		testI.and();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter and() Test 50. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter and() Test 50.");
		}
		
		testI.stacks[0].push("false");
		testI.stacks[0].push("false");
		
		testI.and();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter and() Test 50. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter and() Test 50.");
		}
		
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter and() Test 51. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter and() Test 51.");
		}
		
		testI.stacks[0].push("true");
		testI.stacks[0].push("false");
		
		testI.and();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter and() Test 52. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter and() Test 52.");
		}
		
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter and() Test 53. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter and() Test 53.");
		}
		
		testI.stacks[0].push("true");
		testI.stacks[0].push("true");
		
		testI.and();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter and() Test 54. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter and() Test 54.");
		}
		
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter and() Test 55. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter and() Test 55.");
		}
		
		/*
		or() :
		o Test case: The stack contains fewer than two values. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values where one or both are not boolean values. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two boolean values on top. Expected result: The top of the stack contains the correct result of the or() operation as a boolean, true or false.
		*/
		
		testI.or();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter or() Test 56. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter or() Test 56.");
		}
		
		testI.stacks[0].push("cow");
		
		testI.or();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter or() Test 57. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter or() Test 57.");
		}
		
		testI.stacks[0].push("123");
		testI.stacks[0].push("456");
		
		testI.or();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter or() Test 58. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter or() Test 58.");
		}
		
		testI.stacks[0].push("false");
		testI.stacks[0].push("456");
		
		testI.or();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter or() Test 59. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter or() Test 59.");
		}
		
		testI.stacks[0].push("false");
		testI.stacks[0].push("false");
		
		testI.or();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter or() Test 60. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter or() Test 60.");
		}
		
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter or() Test 61. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter or() Test 61.");
		}
		
		testI.stacks[0].push("true");
		testI.stacks[0].push("false");
		
		testI.or();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter or() Test 62. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter or() Test 62.");
		}
		
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter or() Test 63. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter or() Test 63.");
		}
		
		testI.stacks[0].push("true");
		testI.stacks[0].push("true");
		
		testI.or();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter or() Test 64. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter or() Test 64.");
		}
		
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter or() Test 65. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter or() Test 65.");
		}
		
		
		/* invert() :
		o Test case: The stack contains one non-boolean value on top.
		Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains one boolean value on top. Expected result: The top of the stack contains the opposite boolean value.
		*/
		
		testI.invert();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter invert() Test 66. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter invert() Test 66.");
		}
		
		testI.stacks[0].push("cow");
		
		testI.invert();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter invert() Test 67. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter invert() Test 67.");
		}
		
		testI.stacks[0].push("123");
		
		testI.invert();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter invert() Test 68. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter invert() Test 68.");
		}
		
		
		testI.stacks[0].push("false");
		
		testI.invert();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter invert() Test 69. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter invert() Test 69.");
		}
		
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter invert() Test 70. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter invert() Test 70.");
		}
		
		testI.stacks[0].push("true");
		
		testI.invert();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter invert() Test 71. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter invert() Test 71.");
		}
		
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter invert() Test 72. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter invert() Test 72.");
		}
		
		
		
		/*
		 duplicate() :
		o Test case: The stack contains one value of any type on top. Expected result: The top of the stack contains two of the original value.
		*/
	
		testI.duplicate();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter duplicate() Test 73. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter duplicate() Test 73.");
		}
		
		testI.stacks[0].push("hello");
		
		testI.duplicate();
		
		if (testI.stacks[0].size() != 2){
			System.out.println("FAILED: Interpreter duplicate() Test 74. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter duplicate() Test 74.");
		}
		
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("hello")){
			System.out.println("FAILED: Interpreter duplicate() Test 75. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter duplicate() Test 75.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("hello")){
			System.out.println("FAILED: Interpreter duplicate() Test 76. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter duplicate() Test 76.");
		}
		
		testI.stacks[0].push("127");
		
		testI.duplicate();
		
		if (testI.stacks[0].size() != 2){
			System.out.println("FAILED: Interpreter duplicate() Test 77. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter duplicate() Test 77.");
		}
		
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("127")){
			System.out.println("FAILED: Interpreter duplicate() Test 78. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter duplicate() Test 78.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("127")){
			System.out.println("FAILED: Interpreter duplicate() Test 79. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter duplicate() Test 79.");
		}
		
		
		/* drop() :
		o Test case: The stack contains one value of any type on top. Expected result: The value has been removed from the stack, and nothing has been pushed to the stack.
		
		 */
		
		
		testI.drop();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter drop() Test 80. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter drop() Test 80.");
		}
		
		testI.stacks[0].push("hello");
		
		testI.drop();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter drop() Test 81. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter drop() Test 81.");
		}
		
		testI.stacks[0].push("red");
		testI.stacks[0].push("blue");
		testI.stacks[0].push("green");
		
		testI.drop();
		
		if (testI.stacks[0].size() != 2){
			System.out.println("FAILED: Interpreter drop() Test 82. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter drop() Test 82.");
		}
		
		testI.drop();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter drop() Test 83. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter drop() Test 83.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("red")){
			System.out.println("FAILED: Interpreter drop() Test 84. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter drop() Test 84.");
		}
		
		
		/* swap() :
		o Test case: The stack contains one value of any type on top. Expected result: The value has been replaced at the top of the stack.
		o Test case: The stack contains two values of any type on top. Expected result: The second value is on the top of the stack, and the first is just below it.
		*/
		
		testI.swap();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter swap() Test 85. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter swap() Test 85.");
		}
		
		testI.stacks[0].push("moo");
		
		testI.swap();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter swap() Test 86. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter swap() Test 86.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("moo")){
			System.out.println("FAILED: Interpreter swap() Test 87. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter swap() Test 87.");
		}
		
		testI.stacks[0].push("black");
		testI.stacks[0].push("jack");
		
		testI.swap();
		
		if (testI.stacks[0].size() != 2){
			System.out.println("FAILED: Interpreter swap() Test 88. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter swap() Test 88.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("black")){
			System.out.println("FAILED: Interpreter swap() Test 89. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter swap() Test 89.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("jack")){
			System.out.println("FAILED: Interpreter swap() Test 90. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter swap() Test 90.");
		}
		
		testI.stacks[0].push("black");
		testI.stacks[0].push("jack");
		testI.stacks[0].push("stack");
		
		testI.swap();
		
		if (testI.stacks[0].size() != 3){
			System.out.println("FAILED: Interpreter swap() Test 91. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter swap() Test 92.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("jack")){
			System.out.println("FAILED: Interpreter swap() Test 93. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter swap() Test 93.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("stack")){
			System.out.println("FAILED: Interpreter swap() Test 94. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter swap() Test 94.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "black"){
			System.out.println("FAILED: Interpreter swap() Test 95. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter swap() Test 95.");
		}
		
		
		
		/*rotate() :
		o Test case: The stack contains one value of any type on top. Expected result: the value has been replaced at the top of the stack
		o Test case: The stack contains two values of any type on top. Expected result: The two values have been replaced at the top of the stack in their original order.
		o Test case: The stack contains three values of any type on top. Expected result: The three values will be replaced on the stack, with the second at the bottom, the first in the middle, and the third at the top.
		*/
		
		testI.rotate();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter rotate() Test 96. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter rotate() Test 96.");
		}
		
		testI.stacks[0].push("moo");
		
		testI.rotate();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter rotate() Test 97. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter rotate() Test 97.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "moo"){
			System.out.println("FAILED: Interpreter rotate() Test 98. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter rotate() Test 98.");
		}
		
		testI.stacks[0].push("black");
		testI.stacks[0].push("jack");
		
		testI.rotate();
		
		if (testI.stacks[0].size() != 2){
			System.out.println("FAILED: Interpreter rotate() Test 99. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter rotate() Test 99.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "jack"){
			System.out.println("FAILED: Interpreter rotate() Test 100. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter rotate() Test 100.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "black"){
			System.out.println("FAILED: Interpreter rotate() Test 101. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter rotate() Test 101.");
		}
		
		testI.stacks[0].push("black");
		testI.stacks[0].push("jack");
		testI.stacks[0].push("stack");
		
		testI.rotate();
		
		if (testI.stacks[0].size() != 3){
			System.out.println("FAILED: Interpreter rotate() Test 102. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter rotate() Test 102.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "black"){
			System.out.println("FAILED: Interpreter rotate() Test 103. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter rotate() Test 103.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "stack"){
			System.out.println("FAILED: Interpreter rotate() Test 104. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter rotate() Test 104.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "jack"){
			System.out.println("FAILED: Interpreter rotate() Test 105. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter rotate() Test 105.");
		}
		
		testI.stacks[0].push("1");
		testI.stacks[0].push("2");
		testI.stacks[0].push("3");
		testI.stacks[0].push("4");
		
		testI.rotate();
		
		if (testI.stacks[0].size() != 4){
			System.out.println("FAILED: Interpreter rotate() Test 106. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter rotate() Test 106.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "2"){
			System.out.println("FAILED: Interpreter rotate() Test 107. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter rotate() Test 107.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "4"){
			System.out.println("FAILED: Interpreter rotate() Test 108. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter rotate() Test 108.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "3"){
			System.out.println("FAILED: Interpreter rotate() Test 109. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter rotate() Test 109.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "1"){
			System.out.println("FAILED: Interpreter rotate() Test 110. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter rotate() Test 110.");
		}
		
		
		/*greaterThan() :
		o Test case: The stack contains fewer than two values. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on top where both are not the same type. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on top where both are the same type but not comparable. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on the stack of the same type and are comparable Expected result: The top of the stack contains the correct result of the greaterThan() operation as a boolean, true or false.
		*/
		
		testI.greaterThan();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter greaterThan() Test 111. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 111.");
		}
		
		testI.stacks[0].push("foo");
		
		testI.greaterThan();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter greaterThan() Test 112. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 112.");
		}
		
		testI.stacks[0].push("foo");
		testI.stacks[0].push("48");
		
		testI.greaterThan();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter greaterThan() Test 113. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 113.");
		}
		
		testI.stacks[0].push("true");
		testI.stacks[0].push("true");
		
		testI.greaterThan();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter greaterThan() Test 114. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 114.");
		}
		
		
		testI.stacks[0].push("18");
		testI.stacks[0].push("15");
		
		testI.greaterThan();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter greaterThan() Test 115. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 115.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter greaterThan() Test 116. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 116.");
		}
		
		testI.stacks[0].push("18");
		testI.stacks[0].push("18");
		
		testI.greaterThan();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter greaterThan() Test 117. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 117.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter greaterThan() Test 118. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 118.");
		}
		
		
		testI.stacks[0].push("15");
		testI.stacks[0].push("18");
		
		testI.greaterThan();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter greaterThan() Test 119. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 119.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter greaterThan() Test 120. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 120.");
		}
		
		
		testI.stacks[0].push("abc");
		testI.stacks[0].push("acb");
		
		testI.greaterThan();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter greaterThan() Test 121. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 121.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter greaterThan() Test 122. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 122.");
		}
		
		testI.stacks[0].push("acb");
		testI.stacks[0].push("acb");
		
		testI.greaterThan();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter greaterThan() Test 123. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 123.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter greaterThan() Test 124. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 124.");
		}
		
		
		testI.stacks[0].push("acb");
		testI.stacks[0].push("abc");
		
		testI.greaterThan();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter greaterThan() Test 125. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 125.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter greaterThan() Test 126. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThan() Test 126.");
		}
		
		/*greaterThanEqual() :
		o Test case: The stack contains fewer than two values. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on top where both are not the same type. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on top where both are the same type but not comparable. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on the stack of the same type and are comparable Expected result: The top of the stack contains the correct result of the greaterThanEqual() operation as a boolean, true or false.
		*/
		
		testI.greaterThanEqual();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 127. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 127.");
		}
		
		testI.stacks[0].push("foo");
		
		testI.greaterThanEqual();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 128. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 128.");
		}
		
		testI.stacks[0].push("foo");
		testI.stacks[0].push("48");
		
		testI.greaterThanEqual();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 129. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 129.");
		}
		
		testI.stacks[0].push("true");
		testI.stacks[0].push("true");
		
		testI.greaterThanEqual();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 130. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 130.");
		}
		
		
		testI.stacks[0].push("18");
		testI.stacks[0].push("15");
		
		testI.greaterThanEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 131. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 131.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 132. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 132.");
		}
		
		testI.stacks[0].push("18");
		testI.stacks[0].push("18");
		
		testI.greaterThanEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 133. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 133.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 134. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 134.");
		}
		
		
		testI.stacks[0].push("15");
		testI.stacks[0].push("18");
		
		testI.greaterThanEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 135. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 135.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 136. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 136.");
		}
		
		
		testI.stacks[0].push("abc");
		testI.stacks[0].push("acb");
		
		testI.greaterThanEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 137. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 137.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 138. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 138.");
		}
		
		testI.stacks[0].push("acb");
		testI.stacks[0].push("acb");
		
		testI.greaterThanEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 139. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 139.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 140. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 140.");
		}
		
		
		testI.stacks[0].push("acb");
		testI.stacks[0].push("abc");
		
		testI.greaterThanEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 141. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 141.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter greaterThanEqual() Test 142. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter greaterThanEqual() Test 142.");
		}
		
		
		
		/*
		lessThan() :
		o Test case: The stack contains fewer than two values. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on top where both are not the same type. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on top where both are the same type but not comparable. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on the stack of the same type and are comparable Expected result: The top of the stack contains the correct result of the lessThan() operation as a boolean, true or false.
		*/
		
		testI.lessThan();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter lessThan() Test 143. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 143.");
		}
		
		testI.stacks[0].push("foo");
		
		testI.lessThan();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter lessThan() Test 144. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 144.");
		}
		
		testI.stacks[0].push("foo");
		testI.stacks[0].push("48");
		
		testI.lessThan();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter lessThan() Test 145. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 145.");
		}
		
		testI.stacks[0].push("true");
		testI.stacks[0].push("true");
		
		testI.lessThan();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter lessThan() Test 146. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 146.");
		}
		
		
		testI.stacks[0].push("18");
		testI.stacks[0].push("15");
		
		testI.lessThan();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter lessThan() Test 147. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 147.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter lessThan() Test 148. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 148.");
		}
		
		testI.stacks[0].push("18");
		testI.stacks[0].push("18");
		
		testI.lessThan();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter lessThan() Test 149. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 149.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter lessThan() Test 150. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 150.");
		}
		
		
		testI.stacks[0].push("15");
		testI.stacks[0].push("18");
		
		testI.lessThan();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter lessThan() Test 151. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 151.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter lessThan() Test 152. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 152.");
		}
		
		
		testI.stacks[0].push("abc");
		testI.stacks[0].push("acb");
		
		testI.lessThan();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter lessThan() Test 153. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 153.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter lessThan() Test 154. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 154.");
		}
		
		testI.stacks[0].push("acb");
		testI.stacks[0].push("acb");
		
		testI.lessThan();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter lessThan() Test 155. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 155.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter lessThan() Test 156. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 156.");
		}
		
		
		testI.stacks[0].push("acb");
		testI.stacks[0].push("abc");
		
		testI.lessThan();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter lessThan() Test 157. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 157.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter lessThan() Test 158. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThan() Test 158.");
		}
		
		
		/*lessThanEqual() :
		o Test case: The stack contains fewer than two values. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on top where both are not the same type. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on top where both are the same type but not comparable. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on the stack of the same type and are comparable.
		Expected result: The top of the stack contains the correct result of the lessThanEqual() operation as a boolean, true or false.
		*/
		
		
		testI.lessThanEqual();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 159. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 159.");
		}
		
		testI.stacks[0].push("foo");
		
		testI.lessThanEqual();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 160. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 160.");
		}
		
		testI.stacks[0].push("foo");
		testI.stacks[0].push("48");
		
		testI.lessThanEqual();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 161. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 161.");
		}
		
		testI.stacks[0].push("true");
		testI.stacks[0].push("true");
		
		testI.lessThanEqual();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 162. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 162.");
		}
		
		
		testI.stacks[0].push("15");
		testI.stacks[0].push("18");
		
		testI.lessThanEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 163. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 163.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 164. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 164.");
		}
		
		testI.stacks[0].push("18");
		testI.stacks[0].push("18");
		
		testI.lessThanEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 165. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 165.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 166. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 166.");
		}
		
		
		testI.stacks[0].push("18");
		testI.stacks[0].push("15");
		
		testI.lessThanEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 167. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 167.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 168. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 168.");
		}
		
		
		testI.stacks[0].push("abc");
		testI.stacks[0].push("acb");
		
		testI.lessThanEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 169. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 169.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 170. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 170.");
		}
		
		testI.stacks[0].push("acb");
		testI.stacks[0].push("acb");
		
		testI.lessThanEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 171. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 171.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 172. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 172.");
		}
		
		
		testI.stacks[0].push("acb");
		testI.stacks[0].push("abc");
		
		testI.lessThanEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 173. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 173.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter lessThanEqual() Test 174. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter lessThanEqual() Test 174.");
		}
		
		
		
		/*
		equal() :
		o Test case: The stack contains fewer than two values. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on top where both are not the same type. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on top where both are the same type but not comparable. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on the stack of the same type and are comparable. Expected result: The top of the stack contains the correct result of the equal() operation as a boolean, true or false.
		*/
		
		testI.equal();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter equal() Test 175. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 175.");
		}
		
		testI.stacks[0].push("foo");
		
		testI.equal();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter equal() Test 176. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 176.");
		}
		
		testI.stacks[0].push("foo");
		testI.stacks[0].push("48");
		
		testI.equal();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter equal() Test 177. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 177.");
		}
		
		testI.stacks[0].pop();
		
		testI.stacks[0].push("true");
		testI.stacks[0].push("true");
		
		testI.equal();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter equal() Test 178. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 178.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter equal() Test 179. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 179.");
		}
		
		testI.stacks[0].push("false");
		testI.stacks[0].push("false");
		
		testI.equal();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter equal() Test 180. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 180.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter equal() Test 181. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 181.");
		}
		
		testI.stacks[0].push("false");
		testI.stacks[0].push("true");
		
		testI.equal();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter equal() Test 182. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 182.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter equal() Test 183. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 183.");
		}
		
		testI.stacks[0].push("15");
		testI.stacks[0].push("18");
		
		testI.equal();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter equal() Test 184. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 184.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter equal() Test 185. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 185.");
		}
		
		testI.stacks[0].push("18");
		testI.stacks[0].push("18");
		
		testI.equal();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter equal() Test 186. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 186.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter equal() Test 187. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 187.");
		}
		
		testI.stacks[0].push("abc");
		testI.stacks[0].push("acb");
		
		testI.equal();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter equal() Test 188. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 188.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter equal() Test 189. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 189.");
		}
		
		testI.stacks[0].push("acb");
		testI.stacks[0].push("acb");
		
		testI.equal();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter equal() Test 190. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 190.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter equal() Test 191. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter equal() Test 191.");
		}
		
		
		
		
		
		/*notEqual() :
		o Test case: The stack contains fewer than two values. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on top where both are not the same type. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on top where both are the same type but not comparable. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: The stack contains two values on the stack of the same type and are comparable. Expected result: The top of the stack contains the correct result of the notEqual() operation as a boolean, true or false.
		
		*/
		
		
		testI.notEqual();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter notEqual() Test 192. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter notEqual() Test 192.");
		}
		
		testI.stacks[0].push("foo");
		
		testI.notEqual();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter notEqual() Test 193. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter notEqual() Test 193.");
		}
		
		testI.stacks[0].push("foo");
		testI.stacks[0].push("48");
		
		testI.notEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter notEqual() Test 194. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter notEqual() Test 194.");
		}
		
		testI.stacks[0].pop();
		
		testI.stacks[0].push("true");
		testI.stacks[0].push("true");
		
		testI.notEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter notEqual() Test 195. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter notEqual() Test 195.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter notEqual() Test 196. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter notEqual() Test 196.");
		}
		
		testI.stacks[0].push("true");
		testI.stacks[0].push("false");
		
		testI.notEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter notEqual() Test 197. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter notEqual() Test 197.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter notEqual() Test 198. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter notEqual() Test 198.");
		}
		
		
		testI.stacks[0].push("15");
		testI.stacks[0].push("18");
		
		testI.notEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter notEqual() Test 199. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter notEqual() Test 199.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter notEqual() Test 200. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter notEqual() Test 200.");
		}
		
		testI.stacks[0].push("18");
		testI.stacks[0].push("18");
		
		testI.notEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter notEqual() Test 201. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter notEqual() Test 201.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter notEqual() Test 202. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter notEqual() Test 202.");
		}
		
		testI.stacks[0].push("abc");
		testI.stacks[0].push("acb");
		
		testI.notEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter notEqual() Test 203. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter notEqual() Test 203.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter notEqual() Test 204. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter notEqual() Test 204.");
		}
		
		testI.stacks[0].push("acb");
		testI.stacks[0].push("acb");
		
		testI.notEqual();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter notEqual() Test 205. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter notEqual() Test 205.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter notEqual() Test 206. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter notEqual() Test 206.");
		}
		
		
		/*ifBlock() :
		o Test case: The test block in the program is empty. Expected result: The if and else blocks are ignored, and the then block is executed.
		o Test case: The stack does not contain values to execute the test block properly. Expected result: The if and else blocks are ignored, and the then block is executed.
		o Test case: The stack contains values to execute the test block properly, and the value returned is not a boolean. Expected result: The if and else blocks are ignored, and the then block is executed.
		o Test case: The stack contains values to execute the test block properly, and the result returned is a true value boolean. Expected result: The code in the if block is executed, followed by the code in the then block.
		o Test case: The stack contains values to execute the test block properly, and the result returned is a false value boolean. Expected result: The code in the else block is executed, followed by the code in the then block.
		*/
		
		testI.currentInstructions.add("");
		
		testI.ifBlock();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter ifBlock() Test 207. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter ifBlock() Test 207.");
		}
		
		testI.currentInstructions.clear();
		
		//testI.currentInstructions.add("if");
		//  Assume an "if" was at the front.
		testI.currentInstructions.add("1");
		testI.currentInstructions.add("else");
		testI.currentInstructions.add("2");
		testI.currentInstructions.add("then");
		
		testI.ifBlock();
		
		if (testI.stacks[0].size() != 0){
			System.out.println("FAILED: Interpreter ifBlock() Test 208. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter ifBlock() Test 208.");
		}		
		
		testI.currentInstructions.clear();
		
		testI.stacks[0].push("10");
		//testI.currentInstructions.add("if");
		//Assume an "if" was at the front.
		testI.currentInstructions.add("1");
		testI.currentInstructions.add("else");
		testI.currentInstructions.add("2");
		testI.currentInstructions.add("then");
		
		testI.ifBlock();
		
		if (testI.stacks[0].size() != 0){
			System.out.println("FAILED: Interpreter ifBlock() Test 210. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter ifBlock() Test 210.");
		}		
		
		
		testI.currentInstructions.clear();
		
		testI.stacks[0].push("true");
		//  Assume an "if" was at the front.
		testI.currentInstructions.add("1");
		testI.currentInstructions.add("else");
		testI.currentInstructions.add("2");
		testI.currentInstructions.add("then");
		
		testI.ifBlock();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter ifBlock() Test 212. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter ifBlock() Test 212.");
		}		
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "1"){
			System.out.println("FAILED: Interpreter ifBlock() Test 214. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter ifBlock() Test 214.");
		}
			
		testI.currentInstructions.clear();
		
		testI.stacks[0].push("false");
		//  Assum an "if" was at the front.
		testI.currentInstructions.add("1");
		testI.currentInstructions.add("else");
		testI.currentInstructions.add("2");
		testI.currentInstructions.add("then");
		
		testI.ifBlock();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter ifBlock() Test 215. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter ifBlock() Test 215.");
		}		
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "2"){
			System.out.println("FAILED: Interpreter ifBlock() Test 217. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter ifBlock() Test 217.");
		}
		

		
		
		/*whileBlock() :
		o Test case: The finished block of the program is empty. Expected result: The body is ignored, and the until block is executed.
		o Test case: The stack does not contain values to execute the finished block properly. Expected result: The body is ignored, and the until block is executed.
		o Test case: The stack contains values to execute the finished block properly, and the value returned is not a boolean. Expected result: The body is ignored, and the until block is executed.
		o Test case: The stack contains values to execute the finished block properly, and the value returned is a true value boolean. Expected result: The code in the body is ignored, and the until block is executed.
		o Test case: The stack contains values to execute the finished block properly, and the value returned is a false value boolean, and the finished block will never return true. Expected result: The code in the body is executed repeatedly, until the Interpreter detects that the time limit has been exceeded, and then the Interpreter ends the piece’s turn.
		o Test case: The stack contains values to execute the finished block properly, and the value returned is a false value boolean, and the finished block will eventually return true. Expected result: The code in the body is executed as many times as the finished block evaluates to false, and then the until block is executed.
		*/
		testI.currentInstructions.clear();
		testI.currentInstructions.add("");
		
		testI.whileBlock();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter whileBlock() Test 218. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter whileBlock() Test 218.");
		}
		
		testI.currentInstructions.clear();
		
		// Assume a "begin" was at the front
		testI.currentInstructions.add("until");
		
		testI.whileBlock();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter whileBlock() Test 219. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter whileBlock() Test 219.");
		}		
		
		
		testI.currentInstructions.clear();
		
		// Assume a "begin" was at the front
		testI.currentInstructions.add("5");
		testI.currentInstructions.add(">");
		testI.currentInstructions.add("until");
		
		testI.whileBlock();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter whileBlock() Test 221. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter whileBlock() Test 221.");
		}		
		
		testI.currentInstructions.clear();
		
		// Assume a "begin" was at the front
		testI.currentInstructions.add("5");
		testI.currentInstructions.add("5");
		testI.currentInstructions.add("+");
		testI.currentInstructions.add("until");
		
		testI.whileBlock();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter whileBlock() Test 223. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter whileBlock() Test 223.");
		}		
		
		
		testI.currentInstructions.clear();
		
		// Assume a "begin" was at the front
		testI.currentInstructions.add("foo");
		testI.currentInstructions.add("5");
		testI.currentInstructions.add("5");
		testI.currentInstructions.add("+");
		testI.currentInstructions.add("10");
		testI.currentInstructions.add("=");
		testI.currentInstructions.add("until");
		
		testI.whileBlock();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter whileBlock() Test 225. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter whileBlock() Test 225.");
		}		
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("foo")){
			System.out.println("FAILED: Interpreter whileBlock() Test 227. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter whileBlock() Test 227.");
		}
		
		testI.currentInstructions.clear();
		
		testI.stacks[0].push("0");
		// Assume a "begin" was at the front
		testI.currentInstructions.add("foo");
		testI.currentInstructions.add("swap");
		testI.currentInstructions.add("1");
		testI.currentInstructions.add("+");
		testI.currentInstructions.add("dup");
		testI.currentInstructions.add("3");
		testI.currentInstructions.add("=");
		testI.currentInstructions.add("until");
		
		testI.whileBlock();
		
		if (testI.stacks[0].size() != 4){
			System.out.println("FAILED: Interpreter whileBlock() Test 228. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter whileBlock() Test 228.");
		}		
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("3")){
			System.out.println("FAILED: Interpreter whileBlock() Test 230. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter whileBlock() Test 230.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("foo")){
			System.out.println("FAILED: Interpreter whileBlock() Test 230. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter whileBlock() Test 230.");
		}
		
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("foo")){
			System.out.println("FAILED: Interpreter whileBlock() Test 231. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter whileBlock() Test 231.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (!sResult.equals("foo")){
			System.out.println("FAILED: Interpreter whileBlock() Test 232. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter whileBlock() Test 232.");
		}
		
		//  This test should time out.
		
		testI.currentInstructions.clear();
		
		testI.stacks[0].push("0");
		// Assume a "begin" was at the front
		testI.currentInstructions.add("foo");
		testI.currentInstructions.add("swap");
		testI.currentInstructions.add("1");
		testI.currentInstructions.add("+");
		testI.currentInstructions.add("dup");
		testI.currentInstructions.add("0");
		testI.currentInstructions.add("=");
		testI.currentInstructions.add("until");
		
		try {
			testI.whileBlock();
			System.out.println("FAILED: Interpreter whileBlock() Test 233. Result should have timed out.");
			return;
		}
		catch (Exception e){
			System.out.println("PASSED: Interpreter whileBlock() Test 233. Result timed out.");
			
		}
		finally {
			
		}
		
		testI.stacks[0].clear();

		
		
		/*forBlock() :
		o Test case: The end or start expressions of the program are missing. Expected result: The body will only be executed once.
		o Test case: The value of the end expression is less than or equal to the start expression. Expected result: The body will only be executed once.
		o Test case: the value of the start expression is less than or equal to the end expression Expected result: The body will be executed a number of times, incrementing the iterator, until the start expression is greater than the end expression, and the iterator will be destroyed.
		o Test case: The value of the start expression is less than or equal to the end expression, and the body code of the program contains the leave statement.
		Expected result: The body will be executed a number of times, incrementing the iterator, until the start expression is greater than the end expression or the leave statement in the body code is reached, and the iterator will be destroyed.
		*/

		testI.currentInstructions.clear();
		testI.currentInstructions.add("");
		
		testI.forBlock();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter forBlock() Test 234. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter forBlock() Test 234.");
		}
		
		testI.currentInstructions.clear();
		
		// Assume a "do" was at the front.
		testI.currentInstructions.add("hello");
		testI.currentInstructions.add("loop");
		
		testI.forBlock();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter forBlock() Test 235. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter forBlock() Test 235.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "hello"){
			System.out.println("FAILED: Interpreter forBlock() Test 236. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter forBlock() Test 236.");
		}
		
		testI.currentInstructions.clear();
		
		testI.stacks[0].push("5");
		// Assume a "do" was at the front.
		testI.currentInstructions.add("hello");
		testI.currentInstructions.add("loop");
		
		testI.forBlock();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter forBlock() Test 237. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter forBlock() Test 237.");
		}
		sResult = testI.stacks[0].pop();
		
		if (sResult != "hello"){
			System.out.println("FAILED: Interpreter forBlock() Test 238. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter forBlock() Test 238.");
		}
		
		testI.currentInstructions.clear();
		
		testI.stacks[0].push("3");
		testI.stacks[0].push("5");
		// Assume a "do" was at the front.
		testI.currentInstructions.add("hello");
		testI.currentInstructions.add("loop");
		
		testI.forBlock();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter forBlock() Test 2xx. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter forBlock() Test 2xx.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "hello"){
			System.out.println("FAILED: Interpreter forBlock() Test 2yy. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter forBlock() Test 2yy.");
		}
		
		testI.currentInstructions.clear();
		
		testI.stacks[0].push("5");
		testI.stacks[0].push("4");
		// Assume a "do" was at the front.
		testI.currentInstructions.add("hello");
		testI.currentInstructions.add("loop");
		
		testI.forBlock();
		
		if (testI.stacks[0].size() != 2){
			System.out.println("FAILED: Interpreter forBlock() Test 239. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter forBlock() Test 239.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "hello"){
			System.out.println("FAILED: Interpreter forBlock() Test 240. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter forBlock() Test 240.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "hello"){
			System.out.println("FAILED: Interpreter forBlock() Test 241. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter forBlock() Test 241.");
		}
		
		testI.currentInstructions.clear();
		
		testI.stacks[0].push("5");
		testI.stacks[0].push("0");
		// Assume a "do" was at the front.
		testI.currentInstructions.add("I");
		testI.currentInstructions.add("3");
		testI.currentInstructions.add("=");
		testI.currentInstructions.add("if");
		testI.currentInstructions.add("leave");
		testI.currentInstructions.add("else");
		testI.currentInstructions.add("hello");
		testI.currentInstructions.add("then");
		testI.currentInstructions.add("loop");
		
		testI.forBlock();
		
		if (testI.stacks[0].size() != 3){
			System.out.println(testI.stacks[0]);
			System.out.println("FAILED: Interpreter forBlock() Test 242. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter forBlock() Test 242.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "hello"){
			System.out.println("FAILED: Interpreter forBlock() Test 243. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter forBlock() Test 243.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "hello"){
			System.out.println("FAILED: Interpreter forBlock() Test 244. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter forBlock() Test 244.");
		}
		
		sResult = testI.stacks[0].pop();
		
		if (sResult != "hello"){
			System.out.println("FAILED: Interpreter forBlock() Test 245. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter forBlock() Test 245.");
		}
		
		
		/* declareVar() :
		o Test case: declareVar() is passed the name of a variable that does not currently exist. Expected result: A new UserVariable has been created in the Interpreter with the given name.
		o Test case: declareVar() is passed the name of a variable that currently exists. Expected result: No new UserVariables are added to the Interpreter.
		*/
		
		testI.userVars[0].clear();
		
		testI.currentInstructions.clear();
		
		testI.currentInstructions.add("");
		
		testI.declareVar();
		
		
		testI.currentInstructions.clear();
		
		if (testI.userVars[0].size() > 0){
			System.out.println("FAILED: Interpreter declareVar() Test 247. Result: " + testI.userVars[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter declareVar() Test 247.");
		}
		
		
		testI.currentInstructions.add("foo");
		
		testI.declareVar();
		
		if (testI.userVars[0].size() != 1){
			System.out.println("FAILED: Interpreter declareVar() Test 248. Result: " + testI.userVars[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter declareVar() Test 248.");
		}
		
		if (testI.userVars[0].get(0).getName() != "foo"){
			System.out.println("FAILED: Interpreter declareVar() Test 249. Result: " + testI.userVars[0].get(0).getName());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter declareVar() Test 249.");
		}
		
		testI.currentInstructions.clear();
		testI.currentInstructions.clear();
		testI.currentInstructions.add("foo");
		
		testI.declareVar();
		
		if (testI.userVars[0].size() != 1){
			System.out.println("FAILED: Interpreter declareVar() Test 250. Result: " + testI.userVars[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter declareVar() Test 250.");
		}
		
		testI.currentInstructions.clear();
		testI.currentInstructions.add("bar");
		
		testI.declareVar();
		
		if (testI.userVars[0].size() != 2){
			System.out.println("FAILED: Interpreter declareVar() Test 251. Result: " + testI.userVars[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter declareVar() Test 251.");
		}
		
		if (testI.userVars[0].get(0).getName() != "foo"){
			System.out.println("FAILED: Interpreter declareVar() Test 252. Result: " + testI.userVars[0].get(0).getName());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter declareVar() Test 252.");
		}
		
		if (testI.userVars[0].get(1).getName() != "bar"){
			System.out.println("FAILED: Interpreter declareVar() Test 253. Result: " + testI.userVars[0].get(1).getName());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter declareVar() Test 253.");
		}
		
		/*declareWord() :
		o Test case: declareWord() is passed the name of a word that does not currently exist. Expected result: A new UserWord has been created in the Interpreter with the given name and replacement values (even if the replacement values are empty).
		o Test case: declareWord() is passed the name of a word that currently exists. Expected result: The existing UserWord is updated to use the given replacement values (even if the replacement values are empty).
		*/
		
		testI.userWords[0].clear();
		testI.stacks[0].clear();
		testI.currentInstructions.clear();
		
		
		testI.currentInstructions.add("");
		
		testI.declareWord();
		
		if (testI.userWords[0].size() > 0){
			System.out.println("FAILED: Interpreter declareWord() Test 255. Result: " + testI.userWords[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter declareWord() Test 255.");
		}
		
		
		testI.currentInstructions.add("foo");
		
		testI.declareWord();
		
		if (testI.userWords[0].size() != 1){
			System.out.println("FAILED: Interpreter declareWord() Test 256. Result: " + testI.userWords[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter declareWord() Test 256.");
		}
		
		if (testI.userWords[0].get(0).getName() != "foo"){
			System.out.println("FAILED: Interpreter declareWord() Test 257. Result: " + testI.userWords[0].get(0).getName());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter declareWord() Test 257.");
		}
		
		testI.currentInstructions.clear();
		testI.currentInstructions.add("foo");
		
		testI.declareWord();
		
		if (testI.userWords[0].size() != 1){
			System.out.println("FAILED: Interpreter declareWord() Test 258. Result: " + testI.userWords[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter declareWord() Test 258.");
		}
		
		testI.currentInstructions.clear();
		testI.currentInstructions.add("bar");
		testI.currentInstructions.add("+");
		testI.currentInstructions.add(";");
		
		testI.declareWord();
		
		if (testI.userWords[0].size() != 2){
			System.out.println("FAILED: Interpreter declareWord() Test 259. Result: " + testI.userWords[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter declareWord() Test 259.");
		}
		
		if (testI.userWords[0].get(0).getName() != "foo"){
			System.out.println("FAILED: Interpreter declareWord() Test 260. Result: " + testI.userWords[0].get(0).getName());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter declareWord() Test 260.");
		}
		
		if (testI.userWords[0].get(1).getName() != "bar"){
			System.out.println("FAILED: Interpreter declareWord() Test 261. Result: " + testI.userWords[0].get(1).getName());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter declareWord() Test 261.");
		}
		
		
		if (testI.userWords[0].get(1).getReplaceValues().size() != 1 || !testI.userWords[0].get(1).getReplaceValues().get(0).equals("+")){
			System.out.println("FAILED: Interpreter declareWord() Test 262a. Result: " + testI.userWords[0].get(1).getReplaceValues());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter declareWord() Test 262a.");
		}
		
		
		/*random() :
		o Test case: There is one value on the stack which is not an integer. Expected result: The stack will be cleared, and an error will be logged.
		o Test case: There is one value on the stack which is an integer. Expected result: A random integer between 0 and the popped value (exclusive) has been pushed to the stack.
		*/
		
		testI.currentInstructions.clear();
		testI.stacks[0].clear();
		testI.random();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter random() Test 262. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter random() Test 262.");
		}
		
		testI.stacks[0].push("foo");
		
		testI.random();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter random() Test 263. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter random() Test 263.");
		}
		
		testI.stacks[0].push("10");
		
		testI.random();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter random() Test 264. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter random() Test 264.");
		}
		
		sResult = testI.stacks[0].pop();
		
		int iResult = -1;
		
		try {
			iResult = Integer.parseInt(sResult);
			System.out.println("PASSED: Interpreter random() Test 265.");
		}
		catch (Exception e){
			System.out.println("FAILED: Interpreter random() Test 265. Exception on converting return value from random to integer.");
			return;		
		}
		finally {
			
		}
		
		if (iResult < 0 || iResult > 10){
			System.out.println("FAILED: Interpreter random() Test 266. Result: " + iResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter random() Test 266.");
		}
		

		
		
		/*dotPrint() :
		o Test case: There is one value on the stack. Expected result: A string representation of the value has been printed to the console.
				 
		 */
		
		testI.dotPrint();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter dotPrint() Test 267. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter dotPrint() Test 267.");
		}
		
		testI.stacks[0].push("thingToPrint");
		
		testI.dotPrint();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter dotPrint() Test 268. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter dotPrint() Test 268.");
		}
		
		testI.stacks[0].push("thingToNotPrint");
		testI.stacks[0].push("thingToPrint");
		
		testI.dotPrint();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter dotPrint() Test 269. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter dotPrint() Test 269.");
		}
		
		
		sResult = testI.stacks[0].pop();
		if (sResult != "thingToNotPrint"){
			System.out.println("FAILED: Interpreter dotPrint() Test 270. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter dotPrint() Test 270.");
		}
		
		
		/*  sendMessage()
		   (s v -- b)
		   o Test case: There are fewer than two values on the stack. Expected result: The stack will be cleared and the error will be logged.
		   o Test case: There are two values on the stack, and the second is not a string. Expected result: The stack will be cleared and the error will be logged.
		   o Test case: There are two correct values on the stack, and the given mailbox is not full. Expected result: The boolean value true will be pushed to the stack.
		   o Test case: There are two correct values on the stack, and the given mailbox is full. Expected result: The boolean value false will be pushed to the stack.
		*/
		
		//  EXTENSION - If the way of identifying pieces by name changes (eg. SNIPER2 instead of piece3), these message-passing tests will need to be re-written.
		testI.sendMessage();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter sendMessage() Test 271. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter sendMessage() Test 271.");
		}
		
		testI.stacks[0].clear();
		
		
		testI.stacks[0].push("hello");
		testI.stacks[0].push("false");
		testI.sendMessage();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter sendMessage() Test 272. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter sendMessage() Test 272.");
		}
		
		
		testI.stacks[0].push("hello");
		testI.stacks[0].push("piece0");
		testI.sendMessage();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter sendMessage() Test 273. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter sendMessage() Test 273.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter sendMessage() Test 274. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter sendMessage() Test 274.");
		}
		
		for (int i = 1; i < MAILBOX_CAPACITY; i++){
			
			testI.stacks[0].push("hello");
			testI.stacks[0].push("piece0");
			testI.sendMessage();
			sResult = testI.stacks[0].pop();
		}
		
		
		testI.stacks[0].push("hello");
		testI.stacks[0].push("piece0");
		testI.sendMessage();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter sendMessage() Test 275. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter sendMessage() Test 275.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter sendMessage() Test 276. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter sendMessage() Test 276.");
		}
		
		/*  checkMessage()
		    (s -- b)
		    o Test case: There are no values on the stack. Expected result: The stack will be cleared and the error will be logged.
		    o Test case: There is one non-string value on the stack. Expected result: The stack will be cleared and the error will be logged.
		    o Test case: There is one string value on the stack, and the given mailbox is empty. Expected result: The boolean value false will be pushed to the stack.
		    o Test case: There is one string value on the stack, and the given mailbox has no messages from the matching piece. Expected result: The boolean value false will be pushed to the stack.
		    o Test case: There is one string value on the stack, and the given mailbox has at least one message from the matching piece. Expected result: The boolean value false will be pushed to the stack.
		*/
		testI.mailboxes[0].clear();
		testI.checkMessages();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter checkMessages() Test 277. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter checkMessages() Test 277.");
		}
		
		testI.stacks[0].push("365");
		testI.checkMessages();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter checkMessages() Test 278. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter checkMessages() Test 278.");
		}
		
		testI.stacks[0].push("piece0");
		testI.checkMessages();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter checkMessages() Test 279. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter checkMessages() Test 279.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter checkMessages() Test 280. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter checkMessages() Test 280.");
		}
		
		
		testI.stacks[0].push("hi");
		testI.stacks[0].push("piece0");
		testI.sendMessage();
		
		testI.stacks[0].clear();
		
		testI.stacks[0].push("piece2");
		testI.checkMessages();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter checkMessages() Test 281. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter checkMessages() Test 281.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("false")){
			System.out.println("FAILED: Interpreter checkMessages() Test 282. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter checkMessages() Test 282.");
		}
		
		testI.stacks[0].push("piece0");
		testI.checkMessages();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter checkMessages() Test 283. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter checkMessages() Test 283.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter checkMessages() Test 284. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter checkMessages() Test 284.");
		}
		
		/*  receiveMessage()
		    (s -- v)
		    o Test case: There are no values on the stack. Expected result: The stack will be cleared and the error will be logged.
		    o Test case: There is one non-string value on the stack. Expected result: The stack will be cleared and the error will be logged.
		    o Test case: There is one string value on the stack, and the given mailbox is empty. Expected result: No value is pushed to the stack.
		    o Test case: There is one string value on the stack, and the given mailbox has no messages from the matching piece. Expected result: No value is pushed to the stack.
		    o Test case: There is one string value on the stack, and the given mailbox has one message from the matching piece. Expected result: The value from the message will be pushed to the stack.
			o Test case: There is one string value on the stack, and the given mailbox has more than one message from the matching piece. Expected result: The value from the earliest message will be pushed to the stack.
		*/
		
		testI.mailboxes[0].clear();
		testI.receiveMessage();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter receiveMessage() Test 285. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter receiveMessage() Test 285.");
		}
		
		testI.stacks[0].push("365");
		testI.receiveMessage();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter receiveMessage() Test 286. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter receiveMessage() Test 286.");
		}
		
		testI.stacks[0].push("piece0");
		testI.receiveMessage();
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter receiveMessage() Test 287. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter receiveMessage() Test 287.");
		}
		
		
		testI.stacks[0].push("hi");
		testI.stacks[0].push("piece0");
		testI.sendMessage();
		
		testI.stacks[0].clear();
		
		testI.stacks[0].push("piece2");
		testI.receiveMessage();
		
		System.out.println(testI.stacks[0]);
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter receiveMessage() Test 288. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter receiveMessage() Test 288.");
		}
		
		
		testI.stacks[0].push("piece0");
		testI.receiveMessage();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter receiveMessage() Test 289. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter receiveMessage() Test 289.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("hi")){
			System.out.println("FAILED: Interpreter receiveMessage() Test 290. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter receiveMessage() Test 290.");
		}
		
		testI.stacks[0].push("first");
		testI.stacks[0].push("piece0");
		testI.sendMessage();
		testI.stacks[0].pop();
		
		testI.stacks[0].push("second");
		testI.stacks[0].push("piece0");
		testI.sendMessage();
		testI.stacks[0].pop();
		
		testI.stacks[0].push("third");
		testI.stacks[0].push("piece0");
		testI.sendMessage();
		testI.stacks[0].pop();
		
		testI.stacks[0].push("piece0");
		testI.receiveMessage();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter receiveMessage() Test 291. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter receiveMessage() Test 291.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("first")){
			System.out.println("FAILED: Interpreter receiveMessage() Test 292. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter receiveMessage() Test 292.");
		}
		
		testI.stacks[0].push("piece0");
		testI.receiveMessage();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter receiveMessage() Test 293. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter receiveMessage() Test 293.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("second")){
			System.out.println("FAILED: Interpreter receiveMessage() Test 294. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter receiveMessage() Test 294.");
		}
		
		testI.stacks[0].push("piece0");
		testI.receiveMessage();
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter receiveMessage() Test 295. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter receiveMessage() Test 295.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("third")){
			System.out.println("FAILED: Interpreter receiveMessage() Test 296. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter receiveMessage() Test 296.");
		}
		
		
		/*  parse(String line) :
			o Test case: parse() is passed an empty string. Expected result: parse() will perform no action.
			o Test case: parse() is passed a string which evaluates to an integer. Expected result: parse() will correctly convert the string to an integer, and perform the appropriate operation with it.
			o Test case: parse() is passed a string which evaluates to a boolean value true or false. Expected result: parse() will correctly convert the string to a boolean value, and perform the appropriate operation with it.
			o Test case: parse() is passed a string which begins with a . character. Expected result: parse() will correctly convert the string into a cleaned string, without the leading character, and perform the appropriate operation with it.
			o Test case: parse() is passed a string which evaluates to the name of an existing UserWord. Expected result: parse() will retrieve the correct UserWord and perform the appropriate operation with it.
			o Test case: parse() is passed a string which evaluates to the name of an existing UserVariable. Expected result: parse() will retrieve the correct UserVariable and perform the appropriate operation with it.
			o Test case: parse() is passed a string which evaluates to the name of an existing method in the Word Interface. Expected result: parse() will call the correct method.
		 */
		
		testI.stacks[0].clear();
		testI.mailboxes[0].clear();
		testI.userVars[0].clear();
		testI.userWords[0].clear();
		
		testI.parse("");
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter parse() Test 297. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 297.");
		}
		
		if (testI.userWords[0].size() > 0){
			System.out.println("FAILED: Interpreter parse() Test 298. Result: " + testI.userWords[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 298.");
		}
		
		if (testI.userVars[0].size() > 0){
			System.out.println("FAILED: Interpreter parse() Test 299. Result: " + testI.userVars[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 299.");
		}
		
		
		testI.parse("true");
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 300. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 300.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("true")){
			System.out.println("FAILED: Interpreter parse() Test 301. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 301.");
		}
		
		if (testI.userWords[0].size() > 0){
			System.out.println("FAILED: Interpreter parse() Test 302. Result: " + testI.userWords[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 302.");
		}
		
		if (testI.userVars[0].size() > 0){
			System.out.println("FAILED: Interpreter parse() Test 303. Result: " + testI.userVars[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 303.");
		}
		
		testI.parse("42");
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 304. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 304.");
		}
		
		sResult = testI.stacks[0].pop();
		if (sResult != "42"){
			System.out.println("FAILED: Interpreter parse() Test 305. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 305.");
		}
		
		if (testI.userWords[0].size() > 0){
			System.out.println("FAILED: Interpreter parse() Test 306. Result: " + testI.userWords[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 306.");
		}
		
		if (testI.userVars[0].size() > 0){
			System.out.println("FAILED: Interpreter parse() Test 307. Result: " + testI.userVars[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 307.");
		}
		
		testI.parse(".\"porkchop\"");
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 308. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 308.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("porkchop")){
			System.out.println("FAILED: Interpreter parse() Test 309. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 309.");
		}
		
		if (testI.userWords[0].size() > 0){
			System.out.println("FAILED: Interpreter parse() Test 310. Result: " + testI.userWords[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 310.");
		}
		
		if (testI.userVars[0].size() > 0){
			System.out.println("FAILED: Interpreter parse() Test 311. Result: " + testI.userVars[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 311.");
		}
		
		testI.parse("shotsFired");
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 312. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 312.");
		}
		
		if (testI.userWords[0].size() > 0){
			System.out.println("FAILED: Interpreter parse() Test 313. Result: " + testI.userWords[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 313.");
		}
		
		if (testI.userVars[0].size() > 0){
			System.out.println("FAILED: Interpreter parse() Test 314. Result: " + testI.userVars[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 314.");
		}
		testI.stacks[0].clear();
		
		testI.currentInstructions.clear();
		testI.currentInstructions.add("shotsFired");
		testI.declareVar();
		
		testI.parse("shotsFired");
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 315. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 315.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("#shotsFired")){
			System.out.println("FAILED: Interpreter parse() Test 316. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 316.");
		}
		
		if (testI.userWords[0].size() > 0){
			System.out.println("FAILED: Interpreter parse() Test 317. Result: " + testI.userWords[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 317.");
		}
		
		if (testI.userVars[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 318. Result: " + testI.userVars[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 318.");
		}
		
		testI.stacks[0].clear();
		testI.currentInstructions.clear();
		testI.currentInstructions.add("double");
		testI.currentInstructions.add("dup");
		testI.currentInstructions.add("+");
		testI.declareWord();
		
		testI.parse("double");
		
		if (!testI.currentInstructions.get(0).equals("dup")){
			System.out.println("FAILED: Interpreter parse() Test 319. Result: " + testI.currentInstructions.get(0));
			return;
		}
		else{
			System.out.println("PASSED: Interpreter parse() Test 319.");
		}
		
		if (testI.currentInstructions.get(1) != "+"){
			System.out.println("FAILED: Interpreter parse() Test 320. Result: " + testI.currentInstructions.get(1));
			return;
		}
		else{
			System.out.println("PASSED: Interpreter parse() Test 320.");
		}
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter parse() Test 321. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 321.");
		}
		
		if (testI.userWords[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 322. Result: " + testI.userWords[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 322.");
		}
		
		if (testI.userVars[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 323. Result: " + testI.userVars[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 323.");
		}
		
		testI.currentInstructions.clear();
		
		testI.stacks[0].clear();
		
		
		//  Test for established words in the Word Interface
		
		testI.parse("3");
		testI.parse("3");
		testI.parse("+");
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 324. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 324.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("6")){
			System.out.println("FAILED: Interpreter parse() Test 325. Result: " + sResult);
			return;
		}
		else{
			System.out.println("PASSED: Interpreter parse() Test 325.");
		}
		
		if (testI.userWords[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 326. Result: " + testI.userWords[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 326.");
		}
		
		if (testI.userVars[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 327. Result: " + testI.userVars[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 327.");
		}
		
		testI.parse("moo");
		testI.parse("dup");
		
		if (testI.stacks[0].size() != 2){
			System.out.println("FAILED: Interpreter parse() Test 328. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 328.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("moo")){
			System.out.println("FAILED: Interpreter parse() Test 329. Result: " + sResult);
			return;
		}
		else{
			System.out.println("PASSED: Interpreter parse() Test 329.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("moo")){
			System.out.println("FAILED: Interpreter parse() Test 330. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 330.");
		}
		
		if (testI.userWords[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 331. Result: " + testI.userWords[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 331.");
		}
		
		if (testI.userVars[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 332. Result: " + testI.userVars[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 332.");
		}
		
		testI.stacks[0].clear();
		testI.currentInstructions.clear();
		
		testI.stacks[0].push("so many shots");
		testI.stacks[0].push("#shotsFired");

		testI.parse("!");
		
		if (testI.stacks[0].size() > 0){
			System.out.println("FAILED: Interpreter parse() Test 333. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 333.");
		}
		
		
		if (testI.userWords[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 334. Result: " + testI.userWords[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 334.");
		}
		
		if (testI.userVars[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 335. Result: " + testI.userVars[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 335.");
		}
		
		if (!testI.userVars[0].get(0).getValue().equals("so many shots")){
			System.out.println("FAILED: Interpreter parse() Test 336. Result: " + testI.userVars[0].get(0).getValue());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 336.");
		}
		
		testI.stacks[0].clear();
		testI.currentInstructions.clear();
		
		testI.stacks[0].push("#shotsFired");

		testI.parse("?");
		
		if (testI.stacks[0].size() != 1){
			System.out.println("FAILED: Interpreter parse() Test 337. Result: " + testI.stacks[0].size());
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 337.");
		}
		
		sResult = testI.stacks[0].pop();
		if (!sResult.equals("so many shots")){
			System.out.println("FAILED: Interpreter parse() Test 338. Result: " + sResult);
			return;
		}
		else {
			System.out.println("PASSED: Interpreter parse() Test 338.");
		}
		

		//  Reached the end of the unit tests
		System.out.println("Interpreter class: all unit tests passed.");
	}
	
}