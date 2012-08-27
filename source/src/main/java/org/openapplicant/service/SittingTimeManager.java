package org.openapplicant.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapplicant.domain.Sitting;
import org.openapplicant.monitor.ExamTimeMonitor;
import org.openapplicant.monitor.timed.SittingTimeable;
import org.springframework.stereotype.Component;

@Component
public class SittingTimeManager extends AbstractTimeManager<SittingTimeable, ExamTimeMonitor>{
	
	private static final Log logger = LogFactory.getLog(SittingTimeManager.class);
    private SittingService sittingService;

    public void setSittingService(SittingService sittingService) {
        this.sittingService = sittingService;
    }

    /**
     * Marks a sitting as finished and stops the monitor if specified. This method can override the default
     * behaviour of stopping the monitor.
     * @param sitting
     */
    public void finishSitting(Sitting sitting, boolean stopMonitor) {
        finishSitting(new SittingTimeable(sitting), stopMonitor);
    }

    /**
     * Marks a sitting as finished and stops the monitor. This method should be called when the user
     * finishes the sitting voluntary.
     * @param sitting
     */
	public void finishSitting(Sitting sitting) {
		finishSitting(sitting, true);
	}

    /**
     * Marks a sitting as finished and stops the monitor if specified. This method can override the default
     * behaviour of stopping the monitor.
     * @param sittingTimeable
     */
    private void finishSitting(SittingTimeable sittingTimeable, boolean stopMonitor) {
        ExamTimeMonitor examTimeMonitor = get(sittingTimeable);
        if (examTimeMonitor != null && stopMonitor) {
            logger.debug("********** stopMonitor");
            examTimeMonitor.stopMonitor();
        }
        sittingService.doSittingFinished(sittingTimeable.getEntity());
    }

	public boolean isExamMonitoring(String sittingGuid){
		return this.get(sittingGuid) != null;
	}

	@Override
	public ExamTimeMonitor createTimeMonitor(SittingTimeable entity, long time) {
		return new ExamTimeMonitor(time, entity, entity.getEntity().getGuid(), this);
	}

    /**
     * This method shouldn't stop the monitor, it should only be used notify this manager to change
     * the SittingTimeable's Sitting's state. The monitor should stop it self.
     * @param entity
     */
	@Override
	public void notifyFinishEvent(SittingTimeable entity)  {
        super.notifyFinishEvent(entity);
		finishSitting(entity, false);
	}
}
