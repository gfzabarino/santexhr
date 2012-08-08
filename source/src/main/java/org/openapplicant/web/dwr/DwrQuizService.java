package org.openapplicant.web.dwr;

import org.openapplicant.domain.Response;
import org.openapplicant.service.QuizService;


public class DwrQuizService {
	
	private QuizService quizService;
	
	public void setQuizService(QuizService value) {
		quizService = value;
	}

	public void submitResponse(Long sittingId, Long questionId, Response response) {
		quizService.submitResponse(sittingId, questionId, response);
	}
}
