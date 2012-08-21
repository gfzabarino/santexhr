package org.openapplicant.monitor.timed;

import java.util.List;

import org.openapplicant.domain.Exam;
import org.openapplicant.domain.Sitting;
import org.openapplicant.domain.question.Question;

public class SittingTimeable implements Timeable<Sitting> {

	Sitting sitting;
	
	public Sitting getEntity() {
		return sitting;
	}
	
	public SittingTimeable(Sitting sitting) {
		this.sitting = sitting;
	}

	public Long calculateTime() {
		Exam exam = sitting.getExam();
		if ( exam != null) {
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
		return 0l;
	}

}
