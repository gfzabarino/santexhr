package org.openapplicant.monitor.timed;

import org.openapplicant.domain.Sitting;
import org.openapplicant.domain.question.Question;

public class QuestionTimeable implements Timeable<Question> {

    Sitting sitting;
	Question question;
	
	public Question getEntity() {
		return question;
	}

    public String getUniqueIdentifier() {
        return sitting.getGuid() + question.getGuid();
    }

    public QuestionTimeable(Sitting sitting, Question question) {
        this.sitting = sitting;
		this.question = question;
	}

	public Long calculateTime() {
        if (question.getTimeAllowed() != null)
		    return question.getTimeAllowed().longValue();
        return 0L;
	}

    public boolean belongsToSitting(Sitting sitting) {
        return this.sitting.equals(sitting);
    }

}
