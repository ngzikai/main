package Tempo.Logic;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import Tempo.CalendarObjects.CalendarObject;
import Tempo.CalendarObjects.Event;
import Tempo.CalendarObjects.FloatingTask;
import Tempo.CalendarObjects.Task;
import Tempo.Commands.Result;
import Tempo.Storage.CalendarExporter;
import Tempo.Storage.CalendarImporter;

public class Calendar {

	private static Calendar instance = new Calendar();
	private static IndexStore indexStore;
	private static CalendarImporter importer;

	private static final String COMMAND_ADD = "add";
	private static final String COMMAND_ADD_EVENT = "add event %1$s";
	private static final String COMMAND_ADD_TASK = "add task %1$s";
	private static final String COMMAND_ADD_FLOATING = "add floating task %1$s";
	private static final String COMMAND_REMOVE = "remove";
	private static final String COMMAND_REMOVE_EVENT = "remove event %1$s";
	private static final String COMMAND_REMOVE_TASK = "remove task %1$s";
	private static final String COMMAND_REMOVE_FLOATING = "remove floating task %1$s";
	private static final String COMMAND_UPDATE = "update";
	private static final String COMMAND_UPDATE_EVENT = "update event %1$s";
	private static final String COMMAND_UPDATE_TASK = "update task %1$s";
	private static final String COMMAND_UPDATE_FLOATING = "update floating task %1$s";
	private static final String COMMAND_DONE = "done";
	private static final String COMMAND_DONE_TASK = "done task %1$s";
	private static final String COMMAND_DONE_FLOATING = "done floating task %1$s";
	private static final String COMMAND_UNDO = "undo ";
	private static final String COMMAND_INVALID_UNDO = "invalid undo";
	private static final String COMMAND_SEARCH = "search %1$s";

	private static final String KEY_EVENTS = "events";
	private static final String KEY_TASKS = "tasks";
	private static final String KEY_FLOATING = "floating tasks";
	private static final String KEY_DAILY = "daily";
	private static final String KEY_WEEKLY = "weekly";
	private static final String KEY_MONTHLY = "monthly";
	private static final String KEY_ANNUALLY = "annually";

	public static final long MILLISECONDS_A_DAY = 86400000;
	public static final long MILLISECONDS_A_WEEK = 604800000;
	public static final String DATE_DELIMETER = "/";

	private static final int INDEX_INVALID = -1;

	private String _fileName;

	private int prevModIndex = INDEX_INVALID;
	private Event prevModEvent = null;
	private Task prevModTask = null;
	private FloatingTask prevModFloatingTask = null;
	private String prevCommand = COMMAND_INVALID_UNDO;

	private ArrayList<CalendarObject> eventsList;
	private ArrayList<CalendarObject> tasksList;
	private ArrayList<CalendarObject> floatingTasksList;

	private Calendar() {
		eventsList = new ArrayList<CalendarObject>();
		tasksList = new ArrayList<CalendarObject>();
		floatingTasksList = new ArrayList<CalendarObject>();
		indexStore = IndexStore.getInstance();
		importer = CalendarImporter.getInstance();
	}

	public static Calendar getInstance() {
		return instance;
	}

	public void createFile(String fileName) {
		_fileName = fileName;
		File file = new File(_fileName);
		if (file.exists()) {
			importFromFile();
			indexStore.initialiseStore(eventsList, tasksList, floatingTasksList);
		}
	}

	/***** ADD COMMAND EXECUTION ******/

	public Result addEvent(Event newEvent) {
		eventsList.add(newEvent);
		indexStore.addEvent(newEvent.getIndex(), newEvent);
		Collections.sort(eventsList);

		String name = newEvent.getName();
		String cmd = String.format(COMMAND_ADD_EVENT, name);

		return new Result(cmd, true, putInHashMap(KEY_EVENTS, eventsList));
	}

	public Result addEvent(String name, String start, String end) {
		int newEventIndex = indexStore.getNewId();
		Event newEvent = new Event(newEventIndex, name, start, end);
		eventsList.add(newEvent);
		indexStore.addEvent(newEventIndex, newEvent);
		Collections.sort(eventsList);
		exportToFile();

		savePrevCmd(newEventIndex, newEvent, null, null, COMMAND_ADD);

		String cmd = String.format(COMMAND_ADD_EVENT, name);

		return new Result(cmd, true, putInHashMap(KEY_EVENTS, eventsList));
	}

	//Szeying, can help me format this into a result and also the extra index from the index store...
	public ArrayList<String> addRecurringEvent(String name, String start, String end, String recurringType, String recurringEnd) {
		int newEventIndex = indexStore.getNewId();
		Event newEvent = new Event(newEventIndex, name, start, end);
		eventsList.add(newEvent);
		indexStore.addEvent(newEventIndex, newEvent);

		// Gets the list of recurring dates;
		ArrayList<String> recurringDates = processRecurringDates(start, recurringEnd, recurringType);
		
		for(String dates: recurringDates){
			newEventIndex = indexStore.getNewId();
			newEvent = new Event(newEventIndex, name, start, end);
			eventsList.add(newEvent);
			indexStore.addEvent(newEventIndex, newEvent);
		}

		Collections.sort(eventsList);
		exportToFile();

		// TODO: UNDO METHOD
	}

	public Result addTask(Task newTask) {
		tasksList.add(newTask);
		indexStore.addTask(newTask.getIndex(), newTask);
		Collections.sort(tasksList);

		String name = newTask.getName();
		String cmd = String.format(COMMAND_ADD_TASK, name);

		return new Result(cmd, true, putInHashMap(KEY_TASKS, tasksList));
	}

	public Result addTask(String name, String dueDate) {
		int newTaskIndex = indexStore.getNewId();
		Task newTask = new Task(newTaskIndex, name, dueDate);
		tasksList.add(newTask);
		indexStore.addTask(newTaskIndex, newTask);
		Collections.sort(tasksList);
		exportToFile();

		savePrevCmd(newTaskIndex, null, newTask, null, COMMAND_ADD);

		String cmd = String.format(COMMAND_ADD_TASK, name);
		return new Result(cmd, true, putInHashMap(KEY_TASKS, tasksList));
	}
	
	public ArrayList<String> addRecurringTask(String name, String dueDate, String recurringType,String recurringEnd) {
		int newTaskIndex = indexStore.getNewId();
		Task newTask = new Task(newTaskIndex, name, dueDate);
		tasksList.add(newTask);
		indexStore.addTask(newTaskIndex, newTask);

		// Gets the list of recurring dates;
		ArrayList<String> recurringDates = processRecurringDates(dueDate, recurringEnd, recurringType);
		
		for(String dates: recurringDates){
			newTaskIndex = indexStore.getNewId();
			newTask = new Task(newTaskIndex, name, dueDate);
			tasksList.add(newTask);
			indexStore.addTask(newTaskIndex, newTask);
		}

		Collections.sort(tasksList);
		exportToFile();

		// TODO: UNDO METHOD
	}

	public Result addFloatingTask(FloatingTask newTask) {
		floatingTasksList.add(newTask);
		indexStore.addTask(newTask.getIndex(), newTask);

		String name = newTask.getName();
		String cmd = String.format(COMMAND_ADD_FLOATING, name);

		return new Result(cmd, true, putInHashMap(KEY_FLOATING, floatingTasksList));
	}

	public Result addFloatingTask(String name) {
		int newTaskIndex = indexStore.getNewId();
		FloatingTask newFloatingTask = new FloatingTask(newTaskIndex, name);
		indexStore.addTask(newTaskIndex, newFloatingTask);
		floatingTasksList.add(newFloatingTask);
		exportToFile();

		savePrevCmd(newTaskIndex, null, null, newFloatingTask, COMMAND_ADD);

		String cmd = String.format(COMMAND_ADD_FLOATING, name);
		return new Result(cmd, true, putInHashMap(KEY_FLOATING, floatingTasksList));
	}

	/***** REMOVE COMMAND EXECUTION ******/

	public Result removeEvent(int idx) {
		String eventName = new String();

		for (int i = 0; i < eventsList.size(); i++) {
			if (eventsList.get(i).getIndex() == idx) {
				savePrevCmd(idx, eventsList.get(i), null, null, COMMAND_REMOVE);
				eventName = eventsList.get(i).getName();
				indexStore.removeEvent(eventsList.get(i).getIndex());
				eventsList.remove(i);
				break;
			}
		}
		exportToFile();

		String cmd = String.format(COMMAND_REMOVE_EVENT, eventName);
		return new Result(cmd, true, putInHashMap(KEY_EVENTS, eventsList));
	}

	public Result removeTask(int idx) {
		String taskName = new String();
		for (int i = 0; i < tasksList.size(); i++) {
			if (tasksList.get(i).getIndex() == idx) {
				savePrevCmd(idx, null, tasksList.get(i), null, COMMAND_REMOVE);
				taskName = tasksList.get(i).getName();
				indexStore.removeTask(tasksList.get(i).getIndex());
				tasksList.remove(i);
				break;
			}
		}
		exportToFile();

		String cmd = String.format(COMMAND_REMOVE_TASK, taskName);
		return new Result(cmd, true, putInHashMap(KEY_TASKS, tasksList));
	}

	public Result removeFloatingTask(int idx) {
		String taskName = new String();
		for (int i = 0; i < floatingTasksList.size(); i++) {
			if (floatingTasksList.get(i).getIndex() == idx) {
				savePrevCmd(idx, null, null, floatingTasksList.get(i), COMMAND_REMOVE);
				taskName = floatingTasksList.get(i).getName();
				indexStore.removeTask(floatingTasksList.get(i).getIndex());
				floatingTasksList.remove(i);
				break;
			}
		}
		exportToFile();

		String cmd = String.format(COMMAND_REMOVE_FLOATING, taskName);
		return new Result(cmd, true, putInHashMap(KEY_FLOATING, tasksList));
	}

	/***** UPDATE COMMAND EXECUTION ******/

	public Result updateEvent(int idx, ArrayList<String> fields, ArrayList<String> newValues) {
		int arrayListIndex = getArrayListIndexOfEvent(idx);
		Event eventToUpdate = (Event) eventsList.get(arrayListIndex);
		Event originalEvent = copyEvent(eventToUpdate);

		savePrevCmd(idx, originalEvent, null, null, COMMAND_UPDATE);

		for (int i = 0; i < fields.size(); i++) {
			eventToUpdate.update(fields.get(i), newValues.get(i));
		}

		exportToFile();

		String name = eventToUpdate.getName();
		String cmd = String.format(COMMAND_UPDATE_EVENT, name);

		return new Result(cmd, true, putInHashMap(KEY_EVENTS, eventsList));
	}

	private Event copyEvent(Event event) {
		int idx = event.getIndex();
		String eventName = event.getName();
		String startDateTime = event.getStartDateTimeSimplified();
		String endDateTime = event.getEndDateTimeSimplified();
		return new Event(idx, eventName, startDateTime, endDateTime);
	}

	public Result updateTask(int idx, ArrayList<String> fields, ArrayList<String> newValues) {
		int arrayListIndex = getArrayListIndexOfTask(idx);
		Task taskToUpdate = (Task) tasksList.get(arrayListIndex);
		Task originalTask = copyTask(taskToUpdate);

		savePrevCmd(idx, null, originalTask, null, COMMAND_UPDATE);

		for (int i = 0; i < fields.size(); i++) {
			taskToUpdate.update(fields.get(i), newValues.get(i));
		}
		exportToFile();

		String name = taskToUpdate.getName();
		String cmd = String.format(COMMAND_UPDATE_TASK, name);

		return new Result(cmd, true, putInHashMap(KEY_TASKS, eventsList));
	}

	private Task copyTask(Task task) {
		int idx = task.getIndex();
		String taskName = task.getName();
		String taskDoneStatus = String.valueOf(task.isDone());
		String dueDate = task.getDueDateSimplified();
		return new Task(idx, taskName, taskDoneStatus, dueDate);
	}

	public Result updateFloatingTask(int idx, ArrayList<String> fields, ArrayList<String> newValues) {

		int arrayListIndex = getArrayListIndexOfFloatingTask(idx);
		FloatingTask taskToUpdate = (FloatingTask) floatingTasksList.get(arrayListIndex);
		FloatingTask originalTask = copyFloatingTask(taskToUpdate);

		savePrevCmd(idx, null, null, originalTask, COMMAND_UPDATE);

		for (int i = 0; i < fields.size(); i++) {
			taskToUpdate.update(fields.get(i), newValues.get(i));
		}
		exportToFile();

		String name = taskToUpdate.getName();
		String cmd = String.format(COMMAND_UPDATE_FLOATING, name);

		return new Result(cmd, true, putInHashMap(KEY_FLOATING, eventsList));
	}

	private FloatingTask copyFloatingTask(FloatingTask task) {
		int idx = task.getIndex();
		String taskName = task.getName();
		String taskDoneStatus = String.valueOf(task.isDone());
		return new FloatingTask(idx, taskName, taskDoneStatus);
	}

	/***** DONE COMMAND EXECUTION ******/

	public Result markTaskAsDone(int idx) {
		if (isFloatingTask(idx)) {
			return markFloatingTaskAsDone(idx);
		}

		int arrayListIndex = getArrayListIndexOfTask(idx);
		Task taskToMark = (Task) tasksList.get(arrayListIndex);
		Task originalTask = taskToMark;

		String taskName = taskToMark.getName();

		if (taskToMark.isDone()) {
			disableUndo();
			return new Result(COMMAND_DONE_TASK, false, null);
		} else {
			savePrevCmd(taskToMark.getIndex(), null, originalTask, null, COMMAND_DONE);
			taskToMark.markAsDone();
			exportToFile();
			String cmd = String.format(COMMAND_DONE_TASK, taskName);
			return new Result(cmd, true, putInHashMap(KEY_TASKS, tasksList));
		}
	}

	public Result markFloatingTaskAsDone(int idx) {
		int arrayListIndex = getArrayListIndexOfFloatingTask(idx);
		FloatingTask taskToMark = (FloatingTask) floatingTasksList.get(arrayListIndex);
		FloatingTask originalTask = taskToMark;

		String taskName = taskToMark.getName();

		if (taskToMark.isDone()) {
			disableUndo();
			return new Result(COMMAND_DONE_FLOATING, false, null);
		} else {
			savePrevCmd(taskToMark.getIndex(), null, null, originalTask, COMMAND_DONE);
			taskToMark.markAsDone();
			exportToFile();
			String cmd = String.format(COMMAND_DONE_FLOATING, taskName);
			return new Result(cmd, true, putInHashMap(KEY_FLOATING, floatingTasksList));
		}
	}

	/***** UNDO COMMAND EXECUTION ******/

	public ArrayList<String> undo() {
		ArrayList<String> feedback = executeUndo();
		disableUndo();
		exportToFile();

		return feedback;
	}

	private ArrayList<String> executeUndo() {
		switch (prevCommand) {
			case COMMAND_ADD :
				return undoAdd();
			case COMMAND_REMOVE :
				return undoRemove();
			case COMMAND_UPDATE :
				return undoUpdate();
			case COMMAND_DONE :
				return undoMarkTaskAsDone();
			default :
				return handleInvalidUndo();
		}
	}

	private void disableUndo() {
		prevModIndex = INDEX_INVALID;
		prevCommand = COMMAND_INVALID_UNDO;
		prevModEvent = null;
		prevModTask = null;
		prevModFloatingTask = null;
	}

	private void savePrevCmd(int index, Event event, Task task, FloatingTask floatingTask, String command) {
		prevModIndex = index;
		prevModEvent = event;
		prevModTask = task;
		prevModFloatingTask = floatingTask;
		prevCommand = command;
	}

	private Result undoAdd() {
		if (isEvent(prevModIndex)) {
			return removeEvent(prevModIndex);
		} else if (isFloatingTask(prevModIndex)) {
			return removeFloatingTask(prevModIndex);
		} else {
			return removeTask(prevModIndex);
		}
	}

	private Result undoRemove() {
		indexStore.removeRecycledId(prevModIndex);
		if (prevModEvent != null) {
			return addEvent(prevModEvent);
		} else if (prevModFloatingTask != null) {
			return addFloatingTask(prevModFloatingTask);
		} else {
			return addTask(prevModTask);
		}
	}

	private Result undoUpdate() {
		if (isEvent(prevModIndex)) {
			return undoUpdateEvent();
		} else if (isFloatingTask(prevModIndex)) {
			return undoUpdateFloatingTask();
		} else {
			return undoUpdateTask();
		}
	}

	private Result undoUpdateEvent() {
		for (int i = 0; i < eventsList.size(); i++) {
			if (eventsList.get(i).getIndex() == prevModIndex) {
				eventsList.remove(i);
				eventsList.add(i, prevModEvent);
				Collections.sort(eventsList);
				indexStore.replaceEvent(prevModIndex, prevModEvent);
				break;
			}
		}
		
		
	}

	private void undoUpdateFloatingTask() {
		for (int i = 0; i < floatingTasksList.size(); i++) {
			if (floatingTasksList.get(i).getIndex() == prevModIndex) {
				floatingTasksList.remove(i);
				floatingTasksList.add(i, prevModFloatingTask);
				indexStore.replaceTask(prevModIndex, prevModFloatingTask);
				break;
			}
		}
	}

	private void undoUpdateTask() {
		for (int i = 0; i < tasksList.size(); i++) {
			if (tasksList.get(i).getIndex() == prevModIndex) {
				tasksList.remove(i);
				tasksList.add(i, prevModTask);
				Collections.sort(tasksList);
				indexStore.replaceTask(prevModIndex, prevModTask);
				break;
			}
		}
	}

	private ArrayList<String> undoMarkTaskAsDone() {
		if (prevModTask != null) {
			for (int i = 0; i < tasksList.size(); i++) {
				if (tasksList.get(i).getIndex() == prevModIndex) {
					tasksList.get(i).markAsUndone();
					indexStore.replaceTask(prevModIndex, tasksList.get(i));
					break;
				}
			}
		} else {
			for (int i = 0; i < floatingTasksList.size(); i++) {
				if (floatingTasksList.get(i).getIndex() == prevModIndex) {
					floatingTasksList.get(i).markAsUndone();
					indexStore.replaceTask(prevModIndex, floatingTasksList.get(i));
					break;
				}
			}
		}
		ArrayList<String> feedback = new ArrayList<String>();
		feedback.add(MSG_UNDO_UPDATE);
		return feedback;
	}

	private ArrayList<String> handleInvalidUndo() {
		ArrayList<String> feedback = new ArrayList<String>();

		feedback.add(MSG_UNDO_INVALID);
		return feedback;
	}

	/***** SEARCH COMMAND EXECUTION ******/

	/*
	 * public ArrayList<String> searchId(int id) { ArrayList<String> idFoundLines = new ArrayList<String>(); if (!isEvent(id) && !isFloatingTask(id)) { idFoundLines.clear(); }
	 * 
	 * else if (isEvent(id)) { Event event = indexStore.getEventById(id); idFoundLines.add(event.toString()); }
	 * 
	 * else if (isFloatingTask(id)) { FloatingTask task = indexStore.getTaskById(id); idFoundLines.add(task.toString()); } disableUndo(); return idFoundLines;
	 * 
	 * }
	 */

	public ArrayList<String> search(String arguments) {

		String[] splitedArg = arguments.split("\\s+");
		ArrayList<String> wordFoundLines = new ArrayList<String>();
		wordFoundLines.add(MSG_SEARCH_RESULTS);
		int state = 0;
		int num = 1;
		for (int i = 0; i < eventsList.size(); i++) {
			innerloop: while (state < splitedArg.length) {
				if (containsWord(eventsList.get(i).toString(), splitedArg[state])) {
					state++;
					if (state == splitedArg.length) {
						wordFoundLines = toDisplayEvent(wordFoundLines, eventsList.get(i), num);
						num++;
					}
				} else {
					break innerloop;
				}
			}
		}

		state = 0;
		for (int i = 0; i < tasksList.size(); i++) {
			innerloop: while (state < splitedArg.length) {
				if (containsWord(tasksList.get(i).toString(), splitedArg[state])) {
					state++;
					if (state == splitedArg.length) {
						wordFoundLines = toDisplayTasks(wordFoundLines, tasksList.get(i), num);
						num++;
					}
				} else {
					break innerloop;
				}
			}
		}

		state = 0;
		for (int i = 0; i < floatingTasksList.size(); i++) {
			innerloop: while (state < splitedArg.length) {
				if (containsWord(floatingTasksList.get(i).toString(), splitedArg[state])) {
					state++;
					if (state == splitedArg.length) {
						wordFoundLines = toDisplayFTasks(wordFoundLines, floatingTasksList.get(i), num);
						num++;
					}
				} else {
					break innerloop;
				}
			}
		}

		// Display error if no search results
		if (num == 1) {
			wordFoundLines.add(MSG_NO_SEARCH_RESULTS);
		}

		return wordFoundLines;
	}

	private ArrayList<String> toDisplayFTasks(ArrayList<String> wordFoundLines, FloatingTask floatingTask, int num) {

		wordFoundLines.add(num + ") " + floatingTask.getName() + "\t[ID:" + floatingTask.getIndex() + "] ");
		return wordFoundLines;
	}

	private ArrayList<String> toDisplayTasks(ArrayList<String> wordFoundLines, Task task, int num) {
		wordFoundLines.add("Tasks");
		wordFoundLines
				.add(num + ") " + task.getName() + " Due: " + task.getDueDate() + "\t[ID:" + task.getIndex() + "] ");

		return wordFoundLines;
	}

	private ArrayList<String> toDisplayEvent(ArrayList<String> wordFoundLines, Event event, int num) {
		wordFoundLines.add("Events");
		wordFoundLines.add(num + ") " + event.getName() + " From: " + event.getStartDateTime() + " To: "
				+ event.getEndDateTime() + "\t[ID:" + event.getIndex() + "] ");
		return wordFoundLines;
	}

	private boolean containsWord(String content, String keyword) {
		String[] splited = content.split("\\W");
		for (int i = 0; i < splited.length; i++) {
			if (splited[i].equalsIgnoreCase(keyword)) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<Event> getEventsList() {
		return eventsList;
	}

	public ArrayList<Task> getTasksList() {
		return tasksList;
	}

	public ArrayList<FloatingTask> getFloatingTasksList() {
		return floatingTasksList;
	}

	public void exportToFile() {
		// System.out.println("Exporting: " + _fileName);
		CalendarExporter exporter = new CalendarExporter(_fileName, eventsList, tasksList, floatingTasksList);
		exporter.export();
		// System.out.println("Export Successful!");
	}

	public void importFromFile() {
		System.out.println("Importing: " + _fileName);
		if (importer.importFromFile(_fileName)) {
			eventsList = importer.getEventsList();
			tasksList = importer.getTasksList();
			floatingTasksList = importer.getFloatingTasksList();
			System.out.println("Import Sucessful!");
		} else {
			System.out.println("Import failed!");
		}
	}

	private int getArrayListIndexOfEvent(int id) {
		int index = 0;

		for (int i = 0; i < eventsList.size(); i++) {
			if (eventsList.get(i).getIndex() == id) {
				i = index;
			}
		}

		return index;
	}

	private int getArrayListIndexOfTask(int id) {
		int index = 0;

		for (int i = 0; i < tasksList.size(); i++) {
			if (tasksList.get(i).getIndex() == id) {
				// i = index;
				index = i;
			}
		}

		return index;
	}

	private int getArrayListIndexOfFloatingTask(int id) {
		int index = 0;

		for (int i = 0; i < floatingTasksList.size(); i++) {
			if (floatingTasksList.get(i).getIndex() == id) {
				// i = index;
				index = i;
			}
		}

		return index;
	}

	private boolean isEvent(int id) {
		return indexStore.isEvent(id);
	}

	private boolean isFloatingTask(int id) {
		return indexStore.isFloatingTask(id);
	}

	private HashMap<String, ArrayList<CalendarObject>> putInHashMap(String key, ArrayList<CalendarObject> value) {
		HashMap<String, ArrayList<CalendarObject>> map;
		map = new HashMap<String, ArrayList<CalendarObject>>();
		map.put(key, value);
		return map;
	}

	/******
	 * HELPER METHODS FOR RECURRING EVENTS NOTE: THE ARRAYLIST THAT IS RETURNED DOES NOT RETURN THE ORIGINAL START DATE FOR EXAMPLE: IF THE START DATE IS ON 12/03/15, THE FIRST DATE TO BE ADDED WILL
	 * BE ON 13/03/15
	 ******/

	private ArrayList<String> processRecurringDates(String start, String recurringEnd, String recurringType) {
		switch (recurringType.toLowerCase()) {
			case KEY_DAILY :
				return getDailyRecurringDates(start, recurringEnd);

			case KEY_WEEKLY :
				return getWeeklyRecurringDates(start, recurringEnd);

			case KEY_MONTHLY :
				return getMonthlyRecurringDates(start, recurringEnd);

			case KEY_ANNUALLY :
				return getAnnualRecurringDates(start, recurringEnd);

			default :
				return null;
		}
	}

	private ArrayList<String> getDailyRecurringDates(String start, String end) {
		Date startDate = null;
		Date endDate = null;

		ArrayList<String> returnArray = new ArrayList<String>();

		try {
			startDate = parseDate(start);
			endDate = parseDate(end);
		} catch (Exception e) {
			return null;
		}

		long startMilli = dateToMilli(startDate);
		long endMilli = dateToMilli(endDate);

		while ((endMilli - startMilli) > 0) {
			startMilli += MILLISECONDS_A_DAY;
			String currDate = formatDateMilli(startMilli);
			returnArray.add(currDate);
		}

		return returnArray;
	}

	private ArrayList<String> getWeeklyRecurringDates(String start, String end) {
		int count = 0;

		Date startDate = null;
		Date endDate = null;

		ArrayList<String> returnArray = new ArrayList<String>();

		try {
			startDate = parseDate(start);
			endDate = parseDate(end);
		} catch (Exception e) {
			return null;
		}

		long startMilli = dateToMilli(startDate);
		long endMilli = dateToMilli(endDate);

		while ((endMilli - startMilli) > MILLISECONDS_A_WEEK) {
			startMilli += MILLISECONDS_A_WEEK;
			String currDate = formatDateMilli(startMilli);
			returnArray.add(currDate);
		}
		return returnArray;
	}

	private ArrayList<String> getMonthlyRecurringDates(String start, String end) {
		ArrayList<String> returnArray = new ArrayList<String>();

		String[] splitStart = start.split(DATE_DELIMETER);
		String startDay = splitStart[0];
		int startMonth = Integer.parseInt(splitStart[1]);
		int startYear = Integer.parseInt(splitStart[2]);
		String startTime = splitStart[3];

		Date startDate = null;
		Date endDate = null;

		try {
			startDate = parseDate(start);
			endDate = parseDate(end);
		} catch (Exception e) {
			return null;
		}

		long startMilli = dateToMilli(startDate);
		long endMilli = dateToMilli(endDate);

		while ((endMilli - startMilli) > 0) {
			startMonth++;

			if (startMonth == 13) {
				startMonth = 1;
				startYear++;
			}

			String currDate = formatCurrDateString(startDay, String.valueOf(startMonth), String.valueOf(startYear),
					startTime);

			try {
				startMilli = dateToMilli(parseDate(currDate));
			} catch (ParseException e) {
				continue;
			}

			if (isValidDate(startMilli, endMilli)) {
				returnArray.add(formatDateMilli(startMilli));
			}
		}
		return returnArray;
	}

	private ArrayList<String> getAnnualRecurringDates(String start, String end) {
		ArrayList<String> returnArray = new ArrayList<String>();

		String[] splitStart = start.split(DATE_DELIMETER);
		String startDay = splitStart[0];
		String startMonth = splitStart[1];
		int startYear = Integer.parseInt(splitStart[2]);
		String startTime = splitStart[3];

		Date startDate = null;
		Date endDate = null;

		try {
			startDate = parseDate(start);
			endDate = parseDate(end);
		} catch (Exception e) {
			return null;
		}

		long startMilli = dateToMilli(startDate);
		long endMilli = endDate.getTime();

		while ((endMilli - startMilli) > 0) {
			startYear++;

			String currDate = formatCurrDateString(startDay, startMonth, String.valueOf(startYear), startTime);

			try {
				startMilli = dateToMilli(parseDate(currDate));
			} catch (ParseException e) {
				continue;
			}

			if (isValidDate(startMilli, endMilli)) {
				returnArray.add(formatDateMilli(startMilli));
			}
		}
		return returnArray;
	}

	/****** HELPER METHODS FOR RECURRING EVENTS ******/
	private Date parseDate(String dateString) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		df.setLenient(false);
		return df.parse(dateString);
	}

	private String formatDate(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		df.setLenient(false);
		return df.format(date);
	}

	private String formatDateMilli(long date) {
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		df.setLenient(false);
		return df.format(date);
	}

	private long dateToMilli(Date date) {
		return date.getTime();
	}

	private String formatCurrDateString(String startDay, String startMonth, String startYear, String startTime) {
		return startDay + DATE_DELIMETER + startMonth + DATE_DELIMETER + startYear + DATE_DELIMETER + startTime;
	}

	private boolean isValidDate(long currDate, long endDate) {
		if (currDate > endDate) {
			return false;
		} else {
			return true;
		}
	}
}
