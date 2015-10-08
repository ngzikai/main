/*
 * Class: ArgParser
 *  Utility Class to Parse user input into <action> , <arguments>
 *  Example use :
 *  				String action = <ArgParser>.parseAction(userinput);
 *  				String arguments = <ArgParser>.parseArguments(userinput);
 */
package Tempo;

import java.util.ArrayList;
import java.util.HashMap;

public class ArgParser {
	private HashMap<ArrayList<String>, String> keywords;

	public ArgParser() {
		keywords = new HashMap<ArrayList<String>, String>();
		ArrayList<String> add = new ArrayList<String>();
		add.add("add");
		add.add("create");
		add.add("new");
		keywords.put(add, "add");

		ArrayList<String> display = new ArrayList<String>();
		display.add("display");
		display.add("all");
		display.add("view");
		display.add("list");
		keywords.put(display, "display");

		ArrayList<String> update = new ArrayList<String>();
		update.add("update");
		update.add("edit");
		update.add("change");
		keywords.put(update, "update");

		ArrayList<String> remove = new ArrayList<String>();
		remove.add("remove");
		remove.add("cancel");
		remove.add("delete");
		keywords.put(remove, "remove");

		ArrayList<String> exit = new ArrayList<String>();
		exit.add("exit");
		exit.add("quit");
		keywords.put(exit, "exit");

	}

	/**
	 * @param userInput
	 * @return arguments
	 **/
	public String[] parseArguments(String userInput) {
		return getArguments(userInput);
	}

	/**
	 * @param userInput
	 * @return action
	 **/
	public String parseAction(String userInput) {
		return getAction(userInput);
	}

	/**
	 * @param userInput
	 * @return arguments
	 * @throws IllegalArugmentException
	 **/
	private String[] getArguments(String message) throws IllegalArgumentException {
		if (!isValidInput(message)) {
			throw new IllegalArgumentException();
		}
		String[] tokenizedInput = message.split(" ");
		String[] argument = extractSubArray(tokenizedInput, 1, tokenizedInput.length);
		return argument;
	}

	/**
	 * @param full
	 *            array
	 * @param lowerbound
	 * @param upper
	 *            bound
	 * @throws IllegalArgumentException
	 * @return subarray
	 **/
	private String[] extractSubArray(String[] tokenizedInput, int start, int end) throws IllegalArgumentException {
		if (!isValidInput(tokenizedInput)) {
			throw new IllegalArgumentException();
		}
		String[] subArray = new String[end - start];
		System.arraycopy(tokenizedInput, 1, subArray, 0, end - start);
		return subArray;
	}

	private String getAction(String message) throws IllegalArgumentException {
		if (!isValidInput(message)) {
			throw new IllegalArgumentException();
		}
		String[] tokenizedInput = message.split(" ");
		String action = tokenizedInput[0];
		String keyword = getValidCommand(action);

		return keyword;
	}

	private String getValidCommand(String action) {
		for (ArrayList<String> currActionKey : keywords.keySet()) {
			{
				if (existsInArray(currActionKey, action)) {
					return currActionKey.get(0);
				}
			}
		}
		return null;
	}

	private boolean existsInArray(ArrayList<String> actionKey, String action) {
		for (int i = 0; i < actionKey.size(); i++) {
			if (actionKey.get(i).equals(action)) {
				return true;
			}
		}
		return false;
	}

	private boolean isEmpty(String message) {
		return message.length() == 0;
	}
	
	private boolean isEmpty(String[] tokenizedInput) {
		return tokenizedInput.length == 0;
	}

	private boolean isNull(String message) {
		return message == null;
	}
	
	private boolean isNull(String[] tokenizedInput) {
		return tokenizedInput == null;
	}
	
	private boolean isValidInput(String[] tokenizedInput) {
		if (isEmpty(tokenizedInput) || isNull(tokenizedInput)) {
			return false;
		}
		return true;
	}

	private boolean isValidInput(String message) {
		if (isNull(message) || isEmpty(message)) {
			return false;
		}
		return true;
	}
}
