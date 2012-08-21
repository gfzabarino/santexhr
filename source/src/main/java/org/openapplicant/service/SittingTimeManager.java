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
	
	public void finishSitting(Sitting sitting) {
		finishSitting(new SittingTimeable(sitting));
	}

    public void finishSitting(SittingTimeable sittingTimeable) {
        ExamTimeMonitor examTimeMonitor = get(sittingTimeable);
        if (examTimeMonitor != null) {
            logger.debug("********** stopCountDownTask");
            examTimeMonitor.stopCountDownTask();
        }
        removeInstance(sittingTimeable);
        sittingService.doSittingFinished(sittingTimeable.getEntity());
    }

	public boolean isExamMonitoring(String sittingGuid){
		return this.get(sittingGuid) != null;
	}
	

	@Override
	public String createKey(SittingTimeable entity) {
		return entity.getEntity().getGuid();
	}

	@Override
	public ExamTimeMonitor createTimeMonitor(SittingTimeable entity, long time) {
		return new ExamTimeMonitor(time, entity, entity.getEntity().getGuid(), this);
	}

	@Override
	public void notifyFinishEvent(SittingTimeable entity)  {
		finishSitting(entity);
	}
}
