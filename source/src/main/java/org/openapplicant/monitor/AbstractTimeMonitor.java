package org.openapplicant.monitor;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapplicant.monitor.timed.Timeable;
import org.openapplicant.service.AbstractTimeManager;


public abstract class AbstractTimeMonitor<T extends Timeable>{
	private static final Log logger = LogFactory.getLog(AbstractTimeMonitor.class);
	
	private static Timer timer = null;
	private long seconds = 0;
    private AbstractTimeManager<T, AbstractTimeMonitor<T>> timeManager;
    private T entity;
    private long marginTime = 10000; // mS
	
	public AbstractTimeMonitor(final long totalExamTime, final T entity, final String objectGuid,  final AbstractTimeManager timeManager) {
		this(totalExamTime, entity, objectGuid, timeManager, 10000);
	}

    public AbstractTimeMonitor(final long totalExamTime, final T entity, final String objectGuid, final AbstractTimeManager timeManager, final long marginTime) {
        timer = new Timer();
        timer.schedule(new CountDownTask(totalExamTime), 0, 1000);
        this.timeManager = timeManager;
        this.marginTime = marginTime;
        this.entity = entity;
    }
	
	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public long getSeconds() {
		return seconds;
	}

	public void setSeconds(long seconds) {
		this.seconds = seconds;
	}
	
	

    public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}

	public void stopCountDownTask() {
        getTimer().cancel();
    }

	class CountDownTask extends TimerTask{		
		
		public CountDownTask(final long totalTimeSec){
			seconds = totalTimeSec; 
		}		

		public void run() {
			if (seconds > 0) {
				logger.debug("********** " + seconds + " seconds remaining");
				seconds--;
			} else {
				logger.debug("********** Countdown finished. Scheduling new task and cancel this particular one.");
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        logger.debug("********** Notifying SittingTimeManager");
                        timeManager.notifyFinishEvent(entity);
                    }
                }, marginTime);
				this.cancel();
			}
		}
	}
	
	
}