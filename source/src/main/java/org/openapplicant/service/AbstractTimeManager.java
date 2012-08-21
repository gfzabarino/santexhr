package org.openapplicant.service;

import java.util.HashMap;
import java.util.Map;

import org.openapplicant.monitor.AbstractTimeMonitor;
import org.openapplicant.monitor.timed.Timeable;

public abstract class AbstractTimeManager<T extends Timeable, M extends AbstractTimeMonitor<T>> {

	private Map<String,M> instances = new HashMap<String, M>();
	
	public void createInstance(T entity, long time) {
		instances.put(createKey(entity), createTimeMonitor(entity, time));
	}
	
	public void removeInstance(T entity) {
		instances.remove(createKey(entity));
	}
	
	public M get(T entity) {
		return instances.get(createKey(entity));
	}
	
	public M get(String guid) {
		return instances.get(guid);
	}
	
	public abstract String createKey(T entity);
	
	public abstract M createTimeMonitor(T entity, long time);
	
	public abstract void notifyFinishEvent(T entity);
	
	public void startTimer(T entity) {
		
		long totalExamTime = 0;
		//Counter time on the server side.
		if(instances.get(createKey(entity)) == null){			
			totalExamTime = entity.calculateTime();			
			//Verify if the exam is timed or untimed.
			if(totalExamTime > 0){				
				//examMonitor = new ExamTimeMonitor(totalExamTime);
				createInstance(entity,totalExamTime);
			}
		}
	}
	
	public Long getRemainingTime(T entity) {
		M monitor = get(entity);
		if (monitor != null) {
			return monitor.getSeconds();
		}
		return null;
	}
}
