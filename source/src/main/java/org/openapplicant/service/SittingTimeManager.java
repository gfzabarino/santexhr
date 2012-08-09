package org.openapplicant.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapplicant.domain.Exam;
import org.openapplicant.domain.question.Question;
import org.openapplicant.monitor.ExamTimeMonitor;
import org.springframework.stereotype.Component;

@Component
public class SittingTimeManager {
	
	private static final Log logger = LogFactory.getLog(SittingTimeManager.class);
	
	private final Map<String,ExamTimeMonitor> sittingExamTimeMonitorMap = new ConcurrentHashMap<String,ExamTimeMonitor>();
	
	public ExamTimeMonitor getExamTimeBySittingId(String sittingId){
		return sittingExamTimeMonitorMap.get(sittingId);
	}
	
	public void createExamTimeMonitorForSitting(String sittingId, long totalExamTime){
		sittingExamTimeMonitorMap.put(sittingId, new ExamTimeMonitor(totalExamTime));
	}
	
	public void clearExamTimeMonitorBySitting(String sittingId){
		sittingExamTimeMonitorMap.remove(sittingId);
	}
	
	public boolean isExamMonitoring(String sittingId){
		return sittingExamTimeMonitorMap.get(sittingId) != null;
	}

	public void timerProcess(String sittingId, Exam exam){
		logger.debug("timerProcess()");
		
		long totalExamTime = 0;
		//Counter time on the server side.
		if(sittingExamTimeMonitorMap.get(sittingId) == null){			
			totalExamTime = calculateTotalExamTime(exam);			
			//Verify if the exam is timed or untimed.
			if(totalExamTime > 0){				
				//examMonitor = new ExamTimeMonitor(totalExamTime);
				createExamTimeMonitorForSitting(sittingId,totalExamTime);
			}
		}
	}
	
	private long calculateTotalExamTime(Exam exam){	
		logger.debug("calculateTotalExamTime()");
		List<Question> questionList= exam.getQuestions();
		long totalExamTime = 0; 
		
		for (Question question : questionList) {
			if(question.getTimeAllowed() != null){
				totalExamTime = totalExamTime + question.getTimeAllowed();
			}
		}
		
		// if the globalExamTime is more than the sum of time question, 
		// take it, in other case the sum is taken.
		if(exam.getTotalTime() > totalExamTime){
			totalExamTime = exam.getTotalTime();
		}	
		
		return totalExamTime;
	}
	
}
