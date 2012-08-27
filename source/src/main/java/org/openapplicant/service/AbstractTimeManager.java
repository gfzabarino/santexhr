package org.openapplicant.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapplicant.monitor.AbstractTimeMonitor;
import org.openapplicant.monitor.timed.Timeable;

import java.util.*;

public abstract class AbstractTimeManager<T extends Timeable, M extends AbstractTimeMonitor<T>> {

	private Map<String,M> instances = new HashMap<String, M>();

    private final static Log logger = LogFactory.getLog(AbstractTimeManager.class);
	
	public void createInstance(T entity, long time) {
		instances.put(entity.getUniqueIdentifier(), createTimeMonitor(entity, time));
	}
	
	public void removeInstance(T entity) {
        logger.debug("removing monitor " + entity + " instance from timeManager. Id: " + entity.getUniqueIdentifier());
		instances.remove(entity.getUniqueIdentifier());
	}
	
	public M get(T entity) {
        logger.debug("fetching monitor " + entity + " instance from timeManager. Id: " + entity.getUniqueIdentifier());
        logger.debug("result: " + instances.get(entity.getUniqueIdentifier()));
		return instances.get(entity.getUniqueIdentifier());
	}
	
	protected M get(String id) {
        logger.debug("fetching monitor for id " + id + " instance from timeManager");
        logger.debug("result: " + instances.get(id));
		return instances.get(id);
	}
	
	public abstract M createTimeMonitor(T entity, long time);
	
	public void notifyFinishEvent(T entity) {
        removeInstance(entity);
    }
	
	public void startTimer(T entity) {
		
		long totalTime = 0;
		//Counter time on the server side.
		if(instances.get(entity.getUniqueIdentifier()) == null){
			totalTime = entity.calculateTime();
			//Verify if the exam is timed or untimed.
			if(totalTime > 0){
				//examMonitor = new ExamTimeMonitor(totalTime);
                logger.debug("Starting new timer for id: " + entity.getUniqueIdentifier());
				createInstance(entity,totalTime);
			}
		}
	}
	
	public Long getRemainingTime(T entity) {
		M monitor = get(entity);
		if (monitor != null) {
			return monitor.getMillis();
		}
		return null;
	}

    protected Iterator<Map.Entry<String, M>> instancesIterator() {
        return instances.entrySet().iterator();
    }
}
