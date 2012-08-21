package org.openapplicant.monitor;

import org.openapplicant.domain.question.Question;
import org.openapplicant.monitor.timed.QuestionTimeable;
import org.openapplicant.service.AbstractTimeManager;
import org.openapplicant.service.QuestionTimeManager;
import org.springframework.stereotype.Component;

@Component
public class QuestionTimeMonitor extends AbstractTimeMonitor<QuestionTimeable>{

	public QuestionTimeMonitor(long totalExamTime, QuestionTimeable entity,
			String objectGuid, QuestionTimeManager timeManager) {
		super(totalExamTime, entity, objectGuid, (AbstractTimeManager<QuestionTimeable, QuestionTimeMonitor>) timeManager);
	}
}