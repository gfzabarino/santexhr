package org.openapplicant.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapplicant.domain.Sitting;
import org.openapplicant.monitor.QuestionTimeMonitor;
import org.openapplicant.monitor.timed.QuestionTimeable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class QuestionTimeManager extends AbstractTimeManager<QuestionTimeable, QuestionTimeMonitor>{
	
	private static final Log logger = LogFactory.getLog(QuestionTimeManager.class);

    private SittingService sittingService;

    public void setSittingService(SittingService sittingService) {
        this.sittingService = sittingService;
    }

	@Override
	public QuestionTimeMonitor createTimeMonitor(QuestionTimeable entity, long time) {
		return new QuestionTimeMonitor(time, entity, entity.getEntity().getGuid(), this);
	}

    public boolean finishAnyTimedQuestionForSitting(Sitting sitting) {
        Iterator<Map.Entry<String,QuestionTimeMonitor>> instancesIterator = instancesIterator();
        List<Map.Entry<String,QuestionTimeMonitor>> instancesArray = new ArrayList<Map.Entry<String, QuestionTimeMonitor>>();
        while(instancesIterator.hasNext()) {
            instancesArray.add(instancesIterator.next());
        }
        boolean didFinishAtLeastOneQuestion = false;
        for (Map.Entry<String,QuestionTimeMonitor> instance : instancesArray) {
            QuestionTimeMonitor questionTimeMonitor = instance.getValue();
            QuestionTimeable questionTimeable = questionTimeMonitor.getEntity();
            if (questionTimeable.belongsToSitting(sitting)) {
                questionTimeMonitor.stopMonitor();
                questionTimeMonitor.getTimeManager().notifyFinishEvent(questionTimeable);
                sittingService.submitEmptyResponseToQuestion(sitting, questionTimeable.getEntity());
                didFinishAtLeastOneQuestion = true;
            }
        }
        return didFinishAtLeastOneQuestion;
    }
}
