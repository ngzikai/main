package Tempo.Logic;

import java.util.*;

import Tempo.CalendarObjects.Event;
import Tempo.CalendarObjects.FloatingTask;
import Tempo.CalendarObjects.Task;

public class IndexStore {
	private static IndexStore instance = new IndexStore();
	
	private static int nextUnusedId;
	private static LinkedList<Integer> recycledId;
	public static HashMap<Integer, Event> events;
	public static HashMap<Integer, FloatingTask> tasks;
	
	private IndexStore() {
		nextUnusedId = 0;
		recycledId = new LinkedList<Integer>();
		events = new HashMap<Integer, Event>();
		tasks = new HashMap<Integer, FloatingTask>();
	}
	
	public static IndexStore getInstance() {
		return instance;
	}
		
	public void initialiseStore(ArrayList<Event> eventsList, ArrayList<Task> tasksList, 
			  					ArrayList<FloatingTask> floatingTasksList) {
		initialiseEventsMap(eventsList);
		initialiseTasksMap(tasksList, floatingTasksList);
		updateRecycledId();
	}
	
	private void initialiseEventsMap(ArrayList<Event> eventsList) {
		for (int i = 0; i < eventsList.size(); i++) {
			Event currEvent = eventsList.get(i);
			addEvent(currEvent.getIndex(), currEvent);
			updateNextUnusedId(currEvent.getIndex());
		}
	}
	
	private void initialiseTasksMap(ArrayList<Task> tasksList, 
									ArrayList<FloatingTask> floatingTasksList) {
		for (int i = 0; i < tasksList.size(); i++) {
			Task currTask = tasksList.get(i);
			addTask(currTask.getIndex(), currTask);
			updateNextUnusedId(currTask.getIndex());
		}
		
		for (int i = 0; i < floatingTasksList.size(); i++) {
			FloatingTask currTask = floatingTasksList.get(i);
			addTask(currTask.getIndex(), currTask);
			updateNextUnusedId(currTask.getIndex());
		}
	}
	
	private void updateNextUnusedId(int idx) {
		if (idx >= nextUnusedId) {
			nextUnusedId = idx + 1;
		}
	}
	
	private void updateRecycledId() {
		for (int i = 0; i < nextUnusedId; i++) {
			if (!isUsedId(i)) {
				recycledId.add(i);
			}
		}
	}
	
	private boolean isUsedId(int idx) {
		return (events.containsKey(idx) || tasks.containsKey(idx));
	}
	
	public void addEvent(int index, Event newEvent) {
		events.put(index, newEvent);
	}
	
	public void addTask(int index, FloatingTask newTask) {
		tasks.put(index, newTask);
	}
	
	public void removeEvent(int index) {
		events.remove(index);
		recycledId.add(index);
	}
	
	public void removeTask(int index) {
		tasks.remove(index);
		recycledId.add(index);
	}
	
	public void replaceEvent(int index, Event event) {
		removeEvent(index);
		addEvent(index, event);
		removeRecycledId(index);
	}
	
	public void replaceTask(int index, FloatingTask task) {
		removeTask(index);
		addTask(index, task);
		removeRecycledId(index);
	}
	
	public int getNewId() {
		int id;
		if (recycledId.isEmpty()) {
			id = nextUnusedId;
			updateNextUnusedId(id);
		} else {
			id = recycledId.remove();
		}
		
		return id;
	}
	
	public void removeRecycledId(int index) {
		for (int i = 0; i < recycledId.size(); i++) {
			if (recycledId.get(i) == index) {
				recycledId.remove(i);
				break;
			}
		}
	}
	
	public boolean isEvent(int id) {
		return events.containsKey(id);
	}
	
	public boolean isFloatingTask(int id) {
		FloatingTask task = getTaskById(id);
		return task.isFloatingTask();
	}
	
	public boolean isTask(int id) {
		return (!isEvent(id) && !isFloatingTask(id));
	}
	
	public Event getEventById(int id) {
		return events.get(id);
	}
	
	public FloatingTask getTaskById(int id) {
		return tasks.get(id);
	}
}