package org.openapplicant.monitor.timed;

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
        if (question.getTimeAllowed() != null)
		    return question.getTimeAllowed().longValue();
        return 0L;
	}

}
