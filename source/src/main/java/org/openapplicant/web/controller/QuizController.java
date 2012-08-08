package org.openapplicant.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapplicant.domain.Candidate;
import org.openapplicant.domain.Exam;
import org.openapplicant.domain.Sitting;
import org.openapplicant.domain.link.CandidateExamLink;
import org.openapplicant.domain.link.ExamLink;
import org.openapplicant.domain.question.CodeQuestion;
import org.openapplicant.domain.question.EssayQuestion;
import org.openapplicant.domain.question.IQuestionVisitor;
import org.openapplicant.domain.question.MultipleChoiceQuestion;
import org.openapplicant.domain.question.Question;
import org.openapplicant.monitor.ExamTimeMonitor;
import org.openapplicant.service.QuizService;
import org.openapplicant.web.view.MultipleChoiceHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;


@Controller
public class QuizController {
	private static final Log logger = LogFactory.getLog(QuizController.class);
	
	private QuizService quizService;
	
	private long totalExamTime = 0;	
	private ExamTimeMonitor examMonitor;
	private boolean isExamTimed = true;
	
	private final static String QUIZ_THANKS_VIEW = "quiz/thanks";
	
	public void setQuizService(QuizService value) {
		quizService = value;
	}
	
	@RequestMapping(method=GET)
	public String index(	@RequestParam(value="exam", required=false) String guid,
							Map<String,Object> model) {

		if(null == guid || StringUtils.isBlank(guid)) {
			model.put("error", "You need an exam link to take an exam.");
			return "quiz/sorry";
		}
	
		ExamLink examLink = quizService.getExamLinkByGuid(guid);
	
		if(null == examLink) {
			model.put("error", "Your exam link is old or invalid.");
			return "quiz/sorry";
		}
		
		if(examLink.isUsed()) {
			model.put("error", "This exam link has already been used.");
			return "quiz/sorry";
		}
		
		putCandidate(model, examLink);
		model.put("examLink", examLink);
		model.put("welcomeText", examLink.getCompany().getWelcomeText());
		
	    return "quiz/index";
	}
	
	@RequestMapping(method=GET)
	public String info(	@RequestParam(value="exam") String guid,
						Map<String,Object> model) {
		
		ExamLink examLink = quizService.getExamLinkByGuid(guid);
		
		putCandidate(model, examLink);
		model.put("examLink", examLink);

	    return "quiz/info";
	}
	
	@RequestMapping(method=POST)
	public String info(	@RequestParam(value="examLink") String guid,
						@ModelAttribute(value="candidate") Candidate candidate,
						@RequestParam(value="examArtifactId") String examArtifactId,
						Map<String,Object> model) {
		
		ExamLink examLink = quizService.getExamLinkByGuid(guid);
	
	    candidate = quizService.resolveCandidate(candidate, examLink.getCompany());
	    Sitting sitting = quizService.createSitting(candidate, examArtifactId);
	    
		if(sitting.isFinished()) {
			model.put("error", "This exam has already been completed.");
			return "quiz/sorry";
		}
		
	    return "redirect:question?s="+sitting.getGuid();
	}
	
	@RequestMapping(method=GET)
	public String question(	@RequestParam(value="s") String guid,
							Map<String,Object> model, HttpServletRequest req) {
		logger.info("Question: building question");
		Sitting sitting = quizService.findSittingByGuid(guid);
		model.put("sitting", sitting);	
		String redirect = "";
		
		if(sitting.hasNextQuestion()) {
			Question question = quizService.nextQuestion(sitting);
			timeProcess(sitting);
		
			//Verify the remaining time
			if(examMonitor != null && examMonitor.getSeconds() == 0){
				redirect = QUIZ_THANKS_VIEW;
			}else{			
				model.put("question", question);
				model.put("questionViewHelper", new MultipleChoiceHelper(question));
				if(isExamTimed){
					model.put("isExamInTime", "true");
					model.put("remainingTime", examMonitor.getSeconds());
				}
				redirect =  new QuizQuestionViewVisitor(question).getView();
			}
		} else {			
			redirect = QUIZ_THANKS_VIEW;
		}
		
		if(QUIZ_THANKS_VIEW.equals(redirect)){
			model.put("completionText", sitting.getCandidate().getCompany().getCompletionText());
			totalExamTime = 0;
			isExamTimed = true;
		}
		
		return redirect;
	}
	
	@RequestMapping(method=GET)
	public String goToQuestion(	@RequestParam(value="s") String guid,@RequestParam(value="qId") Long qId,
			Map<String,Object> model ) {
			String redirect = "";
			
			Sitting sitting = quizService.findSittingByGuid(guid);
			model.put("sitting", sitting);
			Question question = quizService.goToQuestion(sitting, qId);
			//Verify the remaining time
			if(examMonitor != null && examMonitor.getSeconds() == 0){
				redirect = QUIZ_THANKS_VIEW;
			}else{	
				model.put("question", question);
				model.put("questionViewHelper", new MultipleChoiceHelper(question));
				if(isExamTimed){
					model.put("isExamInTime", "true");
					model.put("remainingTime", examMonitor.getSeconds());
				}
				redirect =  new QuizQuestionViewVisitor(question).getView();
			}
			
			
			return redirect;
	}
	
	@RequestMapping(method=GET)
	public String prevQuestion(	@RequestParam(value="s") String guid,
							Map<String,Object> model ) {
		String redirect = "";
		Sitting sitting = quizService.findSittingByGuid(guid);
		model.put("sitting", sitting);
		
		if(sitting.hasPreviousQuestion()) {
			Question question = quizService.previousQuestion(sitting);
			
			//Verify the remaining time
			if(examMonitor != null && examMonitor.getSeconds() == 0){
				redirect = QUIZ_THANKS_VIEW;
			}else{	
			
				model.put("question", question);
				model.put("questionViewHelper", new MultipleChoiceHelper(question));
				if(isExamTimed){
					model.put("isExamInTime", "true");
					model.put("remainingTime", examMonitor.getSeconds());
				}
				redirect =  new QuizQuestionViewVisitor(question).getView();
			}
		}
		return redirect;
	}
	
	private void putCandidate(Map<String,Object> model, ExamLink examLink) {
		if(examLink instanceof CandidateExamLink) 
			model.put("candidate", ((CandidateExamLink) examLink).getCandidate());
		else
			model.put("candidate", new Candidate());
	}
	
	private static class QuizQuestionViewVisitor implements IQuestionVisitor {
		private String view;

		public QuizQuestionViewVisitor(Question question) {
			question.accept(this);
		}
		
		public String getView() {
			return view;
		}
		
		public void visit(CodeQuestion question) {
			this.view = "quiz/codeQuestion";
		}

		public void visit(EssayQuestion question) {
			this.view = "quiz/essayQuestion";
		}

		public void visit(MultipleChoiceQuestion question) {
			this.view = "quiz/multipleChoiceQuestion";
		}
	} 
	
	
	
	private void timeProcess(Sitting sitting){
		//Counter time on the server side.
		if(totalExamTime == 0 && isExamTimed){
			calculateTotalExamTime(sitting.getExam());
			if(isExamTimed){				
				examMonitor = new ExamTimeMonitor(totalExamTime);					
			}
		}		
	}
	
	private void calculateTotalExamTime(Exam exam){		
		List<Question> questionList= exam.getQuestions();

		for (Question question : questionList) {
			if(question.getTimeAllowed() != null){
				this.totalExamTime = this.totalExamTime + question.getTimeAllowed();
			}
		}
		
		if(exam.getTotalTime() > this.totalExamTime){
			this.totalExamTime = exam.getTotalTime();
		}	
		
		isExamTimed = this.totalExamTime > 0;		
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public Map<String,Object>  progress(@RequestParam("remainingTime") long clientRemainingTime) throws TimeoutException{
		Map<String,Object> dataProgress = new HashMap<String,Object>();
		if(isExamTimed){
			long serverRemainingTime = examMonitor.getSeconds();
			long serverRemainingTimeMin = serverRemainingTime;
			long serverRemainingTimeMax = serverRemainingTime + 2;
			
			if(serverRemainingTime > 2)
				serverRemainingTimeMin = serverRemainingTime - 2;		
			
			if( clientRemainingTime > serverRemainingTimeMin && clientRemainingTime <= serverRemainingTimeMax){
				dataProgress.put("remainingTime", serverRemainingTime);
			}else{
				dataProgress.put("remainingTime", "");
				logger.debug("The exam time has expired.");
				throw new TimeoutException("The exam time has expired.");			
			}
		}
		return dataProgress;
	}
	
}
