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
	
    private QuizService quizService;
    
	public void clearExamTimeMonitorBySitting(String sittingGuid) {
		Sitting sitting = quizService.findSittingByGuid(sittingGuid);
		SittingTimeable sittingTimeable = new SittingTimeable(sitting);
		ExamTimeMonitor examTimeMonitor = get(sittingTimeable);
		if (examTimeMonitor != null) {
			logger.debug("********** stopCountDownTask");
			examTimeMonitor.stopCountDownTask();
		}
		removeInstance(sittingTimeable);
		quizService.doSittingFinished(sitting);
	}    
	
	public boolean isExamMonitoring(String sittingGuid){
		return this.get(sittingGuid) != null;
	}
	
    public void setQuizService(QuizService quizService) {
        this.quizService = quizService;
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
		removeInstance(entity);
	}

	
}
