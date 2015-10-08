/*
 * Class: ArgParser
 *  Utility Class to Parse user input into <action> , <arguments>
 *  Example use :
 *  				String action = <ArgParser>.parseAction(userinput);
 *  				String arguments = <ArgParser>.parseArguments(userinput);
 */
package Tempo;

import java.util.*;

public class ArgParser {
	private HashMap<ArrayList<String>, String> keywords;

	public ArgParser() {
		keywords = new HashMap<ArrayList<String>, String>();
		initialiseKeywords();
	}
	
	public void initialiseKeywords() {
		addAddKeywords();
		addUpdateKeywords();
		addDisplayKeywords();
		addRemoveKeywords();
	}
	
	private void addAddKeywords() {
		ArrayList<String> add = new ArrayList<String>();
		add.add("add");
		add.add("create");
		add.add("new");
		keywords.put(add, "add");
	}
	
	private void addUpdateKeywords() {
		ArrayList<String> update = new ArrayList<String>();
		update.add("update");
		update.add("edit");
		update.add("change");
		keywords.put(update, "update");
	}
	
	private void addDisplayKeywords() {
		ArrayList<String> display = new ArrayList<String>();
		display.add("display");
		display.add("all");
		display.add("view");
		display.add("list");
		keywords.put(display, "display");
	}
	
	private void addRemoveKeywords() {
		ArrayList<String> remove = new ArrayList<String>();
		remove.add("remove");
		remove.add("cancel");
		remove.add("delete");
		keywords.put(remove, "remove");
	}

	public String getCommand(String message) {
		return getFirstWord(message);
	}
	
	public String getArguments(String message) throws IllegalArgumentException {
		return removeFirstWord(message);
	}
	
	public int getId(String arguments) {
		return Integer.parseInt(getFirstWord(arguments));
	}
	
	public String getName(String arguments) {
		return getFirstWord(arguments);
	}
	
//	from <start date> at <start time> to <end date> at <end time>
	public String getEventStartDate(String arguments) {
		String[] parameters1 = arguments.split("from");
		String[] parameters2 = parameters1[1].trim().split("at");
		return parameters2[0].trim();
	}
	
	public String getEventStartTime(String arguments) {
		String[] parts = arguments.split(" ");
		return parts[3]; //DONE
	}
	
	public String getEventEndDate(String arguments) {
		String[] parts = arguments.split(" ");
		return parts[5]; //DONE
	}
	
	public String getEventEndTime(String arguments) {
		String[] parts = arguments.split(" ");
		return parts[7]; //DONE
	}
	
	public String getTaskDueDate(String arguments) {
		String[] parts = arguments.split(" ");
		return parts[3]; // DONE
	}

	//event <id> <name> from <start date> at <start time> to <end date> at <end time>
	//task <id> <name> due <date>
	//task <id> <field name>: <new value>
	public ArrayList<String> getFieldsList(String arguments) {
		ArrayList<String> returnList = new ArrayList<String>();
		
		if(arguments.contains(":")){
			//one argument only
			String[] split1 = arguments.split(":");
			String[] split2 = split1[0].split(" ");
			returnList.add(split2[2]);
		}else if (arguments.contains("due")){
			//updating task
			returnList.add("due");
		}else if (arguments.contains("from")){
			//updating event
			returnList.add("start date");
			returnList.add("start time");
			returnList.add("end date");
			returnList.add("end time");
		}
		return returnList; //DONE
	}
	
	//event <id> <name> from <start date> at <start time> to <end date> at <end time>
	//task <id> <name> due <date>
	//task <id> <field name>: <new value>
	public ArrayList<String> getNewValuesList(String arguments) {
		ArrayList<String> returnList = new ArrayList<String>();
		
		if(arguments.contains(":")){
			//one argument only
			String[] split1 = arguments.split(":");
			String[] split2 = split1[0].split(" ");
			returnList.add(split2[1].trim());
		}else if (arguments.contains("due")){
			//updating task
			String[] split = arguments.split(" ");
			returnList.add(split[5]);
		}else if (arguments.contains("from")){
			//updating event
			String[] split = arguments.split(" ");
		
			returnList.add(split[4]);
			returnList.add(split[6]);
			returnList.add(split[8]);
			returnList.add(split[10]);
		}
		return returnList; //DONE
	}
	
	private String removeFirstWord(String message) {   
		return message.replace(getFirstWord(message), "").trim();  
	}  
	
	private String getFirstWord(String message) {   
		String commandTypeString = message.trim().split("\\s+")[0];   
		return commandTypeString;  
	}
	
	public boolean isEvent(String message) {
		message = removeFirstWord(message);
		if (getFirstWord(message).equals("event")) {
			return true;
		}
		return false;
	}
	
	public boolean isFloatingTask(String message) {
		if (message.contains("due")) {
			return true;
		}
		return false;
	}
	
	// --------------------------------------------------- //
//
//	private String getValidCommand(String action) {
//		for (ArrayList<String> currActionKey : keywords.keySet()) {
//			{
//				if (isInArray(currActionKey, action)) {
//					return currActionKey.get(0);
//				}
//			}
//		}
//		return null;
//	}
//
//	private boolean isInArray(ArrayList<String> actionKey, String action) {
//		for (int i = 0; i < actionKey.size(); i++) {
//			if (actionKey.get(i).equals(action)) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	private boolean isEmpty(String message) {
//		return message.length() == 0;
//	}
//	
//	private boolean isEmpty(String[] tokenizedInput) {
//		return tokenizedInput.length == 0;
//	}
//
//	private boolean isNull(String message) {
//		return message == null;
//	}
//	
//	private boolean isNull(String[] tokenizedInput) {
//		return tokenizedInput == null;
//	}
//	
//	private boolean isValidInput(String[] tokenizedInput) {
//		if (isEmpty(tokenizedInput) || isNull(tokenizedInput)) {
//			return false;
//		}
//		return true;
//	}
//
//	private boolean isValidInput(String message) {
//		if (isNull(message) || isEmpty(message)) {
//			return false;
//		}
//		return true;
//	}
}
