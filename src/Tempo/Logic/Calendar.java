package Tempo.Logic;

import java.io.*;
import java.util.*;

import Tempo.CalendarObjects.Event;
import Tempo.CalendarObjects.FloatingTask;
import Tempo.CalendarObjects.Task;
import Tempo.Storage.CalendarExporter;
import Tempo.Storage.CalendarImporter;

public class Calendar {

	private static Calendar instance = new Calendar();
	private static IndexStore indexStore;
	private static CalendarImporter importer;

	private static final String MSG_ADDED_EVENT = "Event %1$s has been added.";
	private static final String MSG_ADDED_TASK = "Task %1$s has been added.";
	private static final String MSG_REMOVED_EVENT = "Event %1$s has been removed.";
	private static final String MSG_REMOVED_TASK = "Task %1$s has been removed.";
	private static final String MSG_UPDATED_EVENT = "Your event has been updated.";
	private static final String MSG_UPDATED_TASK = "Your task has been updated.";
	private static final String MSG_DONE_TASK = "Task %1$s has been marked as done.";
	private static final String MSG_DONE_INVALID = "Err: Task %1$s is alr marked as done!";
	private static final String MSG_UNDO_UPDATE = "Your updates have been reverted.";
	private static final String MSG_UNDO_INVALID = "Error: Cannot undo previous operation.";
	private static final String MSG_SEARCH_RESULTS = "These are your search results";
	private static final String MSG_NO_SEARCH_RESULTS = "(We do not have any results for your search)";

	private static final String COMMAND_ADD = "add";
	private static final String COMMAND_REMOVE = "remove";
	private static final String COMMAND_UPDATE = "update";
	private static final String COMMAND_DONE = "done";
	private static final String COMMAND_INVALID_UNDO = "invalid undo";

	private static final int INDEX_INVALID = -1;

	private String _fileName;

	private int prevModIndex = INDEX_INVALID;
	private Event prevModEvent = null;
	private Task prevModTask = null;
	private FloatingTask prevModFloatingTask = null;
	private String prevCommand = COMMAND_INVALID_UNDO;

	private ArrayList<Event> eventsList;
	private ArrayList<Task> tasksList;
	private ArrayList<FloatingTask> floatingTasksList;

	private Calendar() {
		eventsList = new ArrayList<Event>();
		tasksList = new ArrayList<Task>();
		floatingTasksList = new ArrayList<FloatingTask>();
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

	public ArrayList<String> addEvent(Event newEvent) {
		eventsList.add(newEvent);
		indexStore.addEvent(newEvent.getIndex(), newEvent);
		Collections.sort(eventsList);

		ArrayList<String> feedback = new ArrayList<String>();
		feedback.add(String.format(MSG_ADDED_EVENT, newEvent.getName()));
		return feedback;
	}

	public ArrayList<String> addEvent(String name, String start, String end) {
		int newEventIndex = indexStore.getNewId();
		Event newEvent = new Event(newEventIndex, name, start, end);
		eventsList.add(newEvent);
		indexStore.addEvent(newEventIndex, newEvent);
		Collections.sort(eventsList);
		exportToFile();

		savePrevCmd(newEventIndex, newEvent, null, null, COMMAND_ADD);

		ArrayList<String> feedback = new ArrayList<String>();
		feedback.add(String.format(MSG_ADDED_EVENT, name));
		return feedback;
	}

	public ArrayList<String> addTask(Task newTask) {
		tasksList.add(newTask);
		indexStore.addTask(newTask.getIndex(), newTask);
		Collections.sort(tasksList);

		ArrayList<String> feedback = new ArrayList<String>();
		feedback.add(String.format(MSG_ADDED_TASK, newTask.getName()));
		return feedback;
	}

	public ArrayList<String> addTask(String name, String dueDate) {
		int newTaskIndex = indexStore.getNewId();
		Task newTask = new Task(newTaskIndex, name, dueDate);
		tasksList.add(newTask);
		indexStore.addTask(newTaskIndex, newTask);
		Collections.sort(tasksList);
		exportToFile();

		savePrevCmd(newTaskIndex, null, newTask, null, COMMAND_ADD);

		ArrayList<String> feedback = new ArrayList<String>();
		feedback.add(String.format(MSG_ADDED_TASK, name));
		return feedback;
	}

	public ArrayList<String> addFloatingTask(FloatingTask newTask) {
		floatingTasksList.add(newTask);
		indexStore.addTask(newTask.getIndex(), newTask);

		ArrayList<String> feedback = new ArrayList<String>();
		feedback.add(String.format(MSG_ADDED_TASK, newTask.getName()));
		return feedback;
	}

	public ArrayList<String> addFloatingTask(String name) {
		int newTaskIndex = indexStore.getNewId();
		FloatingTask newFloatingTask = new FloatingTask(newTaskIndex, name);
		indexStore.addTask(newTaskIndex, newFloatingTask);
		floatingTasksList.add(newFloatingTask);
		exportToFile();

		savePrevCmd(newTaskIndex, null, null, newFloatingTask, COMMAND_ADD);

		ArrayList<String> feedback = new ArrayList<String>();
		feedback.add(String.format(MSG_ADDED_TASK, name));
		return feedback;
	}

	/***** REMOVE COMMAND EXECUTION ******/

	public ArrayList<String> removeEvent(int idx) {
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

		ArrayList<String> feedback = new ArrayList<String>();
		feedback.add(String.format(MSG_REMOVED_EVENT, eventName));

		return feedback;
	}

	public ArrayList<String> removeTask(int idx) {
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

		ArrayList<String> feedback = new ArrayList<String>();
		feedback.add(String.format(MSG_REMOVED_TASK, taskName));

		return feedback;
	}

	public ArrayList<String> removeFloatingTask(int idx) {
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

		ArrayList<String> feedback = new ArrayList<String>();
		feedback.add(String.format(MSG_REMOVED_TASK, taskName));

		return feedback;
	}

	/***** UPDATE COMMAND EXECUTION ******/

	public ArrayList<String> updateEvent(int idx, ArrayList<String> fields, ArrayList<String> newValues) {
		int arrayListIndex = getArrayListIndexOfEvent(idx);
		Event eventToUpdate = eventsList.get(arrayListIndex);
		Event originalEvent = copyEvent(eventToUpdate);

		savePrevCmd(idx, originalEvent, null, null, COMMAND_UPDATE);

		for (int i = 0; i < fields.size(); i++) {
			eventToUpdate.update(fields.get(i), newValues.get(i));
		}

		exportToFile();

		ArrayList<String> feedback = new ArrayList<String>();
		feedback.add(MSG_UPDATED_EVENT);

		return feedback;
	}

	private Event copyEvent(Event event) {
		int idx = event.getIndex();
		String eventName = event.getName();
		String startDateTime = event.getStartDateTimeSimplified();
		String endDateTime = event.getEndDateTimeSimplified();
		return new Event(idx, eventName, startDateTime, endDateTime);
	}

	public ArrayList<String> updateTask(int idx, ArrayList<String> fields, ArrayList<String> newValues) {
		int arrayListIndex = getArrayListIndexOfTask(idx);
		Task taskToUpdate = tasksList.get(arrayListIndex);
		Task originalTask = copyTask(taskToUpdate);

		savePrevCmd(idx, null, originalTask, null, COMMAND_UPDATE);

		for (int i = 0; i < fields.size(); i++) {
			taskToUpdate.update(fields.get(i), newValues.get(i));
		}
		exportToFile();

		ArrayList<String> feedback = new ArrayList<String>();
		feedback.add(MSG_UPDATED_TASK);

		return feedback;

	}

	private Task copyTask(Task task) {
		int idx = task.getIndex();
		String taskName = task.getName();
		String taskDoneStatus = String.valueOf(task.isDone());
		String dueDate = task.getDueDateSimplified();
		return new Task(idx, taskName, taskDoneStatus, dueDate);
	}

	/***** DONE COMMAND EXECUTION ******/

	public ArrayList<String> markTaskAsDone(int idx) {
		if (isFloatingTask(idx)) {
			return markFloatingTaskAsDone(idx);
		}

		int arrayListIndex = getArrayListIndexOfTask(idx);
		Task taskToMark = tasksList.get(arrayListIndex);
		Task originalTask = taskToMark;
		ArrayList<String> feedback = new ArrayList<String>();

		String taskName = taskToMark.getName();

		if (taskToMark.isDone()) {
			feedback.add(String.format(MSG_DONE_INVALID, taskName));
			disableUndo();
		} else {
			savePrevCmd(taskToMark.getIndex(), null, originalTask, null, COMMAND_DONE);
			taskToMark.markAsDone();
			exportToFile();
			feedback.add(String.format(MSG_DONE_TASK, taskName));
		}

		return feedback;

	}

	public ArrayList<String> markFloatingTaskAsDone(int idx) {
		int arrayListIndex = getArrayListIndexOfFloatingTask(idx);
		FloatingTask taskToMark = floatingTasksList.get(arrayListIndex);
		FloatingTask originalTask = taskToMark;
		ArrayList<String> feedback = new ArrayList<String>();

		String taskName = taskToMark.getName();

		if (taskToMark.isDone()) {
			feedback.add(String.format(MSG_DONE_INVALID, taskName));
			disableUndo();
		} else {
			savePrevCmd(taskToMark.getIndex(), null, null, originalTask, COMMAND_DONE);
			taskToMark.markAsDone();
			exportToFile();
			feedback.add(String.format(MSG_DONE_TASK, taskName));
		}

		return feedback;
	}

	/***** UPDATE COMMAND EXECUTION ******/

	public ArrayList<String> updateFloatingTask(int idx, ArrayList<String> fields, ArrayList<String> newValues) {

		int arrayListIndex = getArrayListIndexOfFloatingTask(idx);
		FloatingTask taskToUpdate = floatingTasksList.get(arrayListIndex);
		FloatingTask originalTask = copyFloatingTask(taskToUpdate);

		savePrevCmd(idx, null, null, originalTask, COMMAND_UPDATE);

		for (int i = 0; i < fields.size(); i++) {
			taskToUpdate.update(fields.get(i), newValues.get(i));
		}
		exportToFile();

		ArrayList<String> feedback = new ArrayList<String>();
		feedback.add(MSG_UPDATED_TASK);

		return feedback;
	}

	private FloatingTask copyFloatingTask(FloatingTask task) {
		int idx = task.getIndex();
		String taskName = task.getName();
		String taskDoneStatus = String.valueOf(task.isDone());
		return new FloatingTask(idx, taskName, taskDoneStatus);
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
		case COMMAND_ADD:
			return undoAdd();
		case COMMAND_REMOVE:
			return undoRemove();
		case COMMAND_UPDATE:
			return undoUpdate();
		case COMMAND_DONE:
			return undoMarkTaskAsDone();
		default:
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

	private ArrayList<String> undoAdd() {
		if (isEvent(prevModIndex)) {
			return removeEvent(prevModIndex);
		} else if (isFloatingTask(prevModIndex)) {
			return removeFloatingTask(prevModIndex);
		} else {
			return removeTask(prevModIndex);
		}
	}

	private ArrayList<String> undoRemove() {
		indexStore.removeRecycledId(prevModIndex);
		if (prevModEvent != null) {
			return addEvent(prevModEvent);
		} else if (prevModFloatingTask != null) {
			return addFloatingTask(prevModFloatingTask);
		} else {
			return addTask(prevModTask);
		}
	}

	private ArrayList<String> undoUpdate() {
		if (isEvent(prevModIndex)) {
			undoUpdateEvent();
		} else if (isFloatingTask(prevModIndex)) {
			undoUpdateFloatingTask();
		} else {
			undoUpdateTask();
		}

		ArrayList<String> feedback = new ArrayList<String>();
		feedback.add(MSG_UNDO_UPDATE);
		return feedback;
	}

	private void undoUpdateEvent() {
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
	 * public ArrayList<String> searchId(int id) { ArrayList<String>
	 * idFoundLines = new ArrayList<String>(); if (!isEvent(id) &&
	 * !isFloatingTask(id)) { idFoundLines.clear(); }
	 * 
	 * else if (isEvent(id)) { Event event = indexStore.getEventById(id);
	 * idFoundLines.add(event.toString()); }
	 * 
	 * else if (isFloatingTask(id)) { FloatingTask task =
	 * indexStore.getTaskById(id); idFoundLines.add(task.toString()); }
	 * disableUndo(); return idFoundLines;
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
}