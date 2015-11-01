package Tempo.Storage;

import java.io.*;
import java.util.*;

import Tempo.CalendarObjects.Event;
import Tempo.CalendarObjects.FloatingTask;
import Tempo.CalendarObjects.Task;

public class CalendarExporter {
	private String _fileName; 
	
	private ArrayList<Event> events;
	private ArrayList<Task> tasks;
	private ArrayList<FloatingTask> floatingTasks;
	
	private static final String HEADER_EVENTS = "--EVENTS--";
	private static final String HEADER_TASKS = "--TASKS--";
	private static final String HEADER_FLOATING_TASKS = "--FLOATING TASKS--";
	private BufferedWriter out;
	
	public CalendarExporter(String fileName, ArrayList<Event> events, ArrayList<Task> tasks, ArrayList<FloatingTask> floatingTasks){
		this._fileName = fileName;
		this.events = events;
		this.tasks = tasks;
		this.floatingTasks = floatingTasks;
		
		try {
			out = new BufferedWriter(new FileWriter(_fileName));
		} catch (IOException e) {
			System.out.println("Error while exporting calendar");
		}					
						
	}
	
	public void export(){
		try {
			out.write(HEADER_EVENTS + "\n");
			for(int i = 0; i < events.size(); i++){
				out.write(events.get(i).toString() + "\n");
			}
			out.write(HEADER_TASKS + "\n");
			for(int i = 0; i < tasks.size(); i++){
				out.write(tasks.get(i).toString() + "\n");
			}
			out.write(HEADER_FLOATING_TASKS + "\n");
			for(int i = 0; i < floatingTasks.size(); i++){
				out.write(floatingTasks.get(i).toString() + "\n");
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	public void writeToFile(String s){
//		try {
//			out.write(s + System.getProperty("line.separator"));
//			out.flush();
//		} catch (Exception e) {
//			System.out.println("Error while exporting calendar");
//		}
//	}
	
	
}