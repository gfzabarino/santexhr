package org.openapplicant.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapplicant.domain.question.Question;
import org.openapplicant.monitor.QuestionTimeMonitor;
import org.openapplicant.monitor.timed.QuestionTimeable;
import org.springframework.stereotype.Component;

@Component
public class QuestionTimeManager extends AbstractTimeManager<QuestionTimeable, QuestionTimeMonitor>{
	
	private static final Log logger = LogFactory.getLog(QuestionTimeManager.class);
	
    private QuizService quizService;
    
    
	public void clearExamTimeMonitorBySitting(String questionGuid) {
//		Sitting sitting = quizService.findSittingByGuid(questionGuid);
//		
//		ExamTimeMonitor examTimeMonitor = get(sitting);
//		if (examTimeMonitor != null) {
//			logger.debug("********** stopCountDownTask");
//			examTimeMonitor.stopCountDownTask();
//		}
//		//removeInstance(sitting);
//		quizService.doSittingFinished(sitting);
	}    
	
	public boolean isExamMonitoring(String sittingGuid){
		return this.get(sittingGuid) != null;
	}
	
    public void setQuizService(QuizService quizService) {
        this.quizService = quizService;
    }

	@Override
	public String createKey(QuestionTimeable entity) {
		return entity.getEntity().getGuid() + entity.getEntity().getId();
	}

	@Override
	public QuestionTimeMonitor createTimeMonitor(QuestionTimeable entity, long time) {
		return new QuestionTimeMonitor(time, entity, entity.getEntity().getGuid(), this);
	}

	@Override
	public void notifyFinishEvent(QuestionTimeable entity) {
		// DO nothing
	}

	
}
