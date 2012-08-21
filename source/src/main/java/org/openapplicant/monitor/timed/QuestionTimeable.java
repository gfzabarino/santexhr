package org.openapplicant.monitor.timed;

import java.util.List;

import org.openapplicant.domain.Exam;
import org.openapplicant.domain.Sitting;
import org.openapplicant.domain.question.Question;

public class QuestionTimeable implements Timeable<Question> {

	Question question;
	
	public Question getEntity() {
		return question;
	}
	
	public QuestionTimeable(Question question) {
		this.question = question;
	}

	public Long calculateTime() {
		return question.getTimeAllowed().longValue();
	}

}
