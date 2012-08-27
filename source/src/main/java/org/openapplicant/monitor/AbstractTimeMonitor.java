package org.openapplicant.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapplicant.monitor.timed.Timeable;
import org.openapplicant.service.AbstractTimeManager;

import java.util.Timer;
import java.util.TimerTask;


public abstract class AbstractTimeMonitor<T extends Timeable>{
	private static final Log logger = LogFactory.getLog(AbstractTimeMonitor.class);

	private static Timer timer = null;
    private boolean cancelled = false;
    private long finishTime = 0L;
    private AbstractTimeManager<T, AbstractTimeMonitor<T>> timeManager;
    private T entity;
    private long marginTime = 10000; // mS
	
	public AbstractTimeMonitor(final long totalExamTime, final T entity, final String objectGuid,  final AbstractTimeManager timeManager) {
		this(totalExamTime, entity, objectGuid, timeManager, 10000);
	}

    public AbstractTimeMonitor(final long totalExamTime, final T entity, final String objectGuid, final AbstractTimeManager timeManager, final long marginTime) {
        timer = new Timer();
        timer.schedule(new FinishTimerTask(), totalExamTime * 1000);
        finishTime = System.currentTimeMillis() + totalExamTime * 1000;
        this.timeManager = timeManager;
        this.marginTime = marginTime;
        this.entity = entity;
    }

	public long getMillis() {
        long millis = finishTime - System.currentTimeMillis();
		return millis < 0 ? 0L : millis;
	}

    public T getEntity() {
		return entity;
	}

    public Timer getTimer() {
        return timer;
    }

    public AbstractTimeManager<T, AbstractTimeMonitor<T>> getTimeManager() {
        return timeManager;
    }

    public void stopMonitor() {
        logger.debug("********** stopMonitor");
        synchronized (getTimer()) {
            getTimer().cancel();
            cancelled = true;
        }
    }

	class FinishTimerTask extends TimerTask {

		public void run() {
            logger.debug("********** Countdown finished. Scheduling new task and cancel this particular one. Entity: " + entity);
            synchronized (getTimer()) {
                if (!cancelled) {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            logger.debug("********** Notifying SittingTimeManager");
                            timeManager.notifyFinishEvent(entity);
                        }
                    }, marginTime);
                }
            }
		}
	}
}