package org.openapplicant.web.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapplicant.domain.Candidate;
import org.openapplicant.domain.Sitting;
import org.openapplicant.domain.link.CandidateExamLink;
import org.openapplicant.domain.link.ExamLink;
import org.openapplicant.domain.question.*;
import org.openapplicant.monitor.timed.QuestionTimeable;
import org.openapplicant.monitor.timed.SittingTimeable;
import org.openapplicant.service.QuestionTimeManager;
import org.openapplicant.service.QuizService;
import org.openapplicant.service.SittingTimeManager;
import org.openapplicant.web.view.MultipleChoiceHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;


@Controller
public class QuizController {
	private static final Log logger = LogFactory.getLog(QuizController.class);
	
	private QuizService quizService;
	

	
	private final static String QUIZ_THANKS_VIEW = "quiz/thanks";
	
	public void setQuizService(QuizService value) {
		quizService = value;
	}


	@RequestMapping(method=GET)
    public String index(	@RequestParam(value="exam", required=false) String guid,
                             Map<String,Object> model) {
        if (doIndex(guid, model)) {
            return "quiz/index";
        } else {
            return "quiz/sorry";
        }
    }

    @RequestMapping(method=GET)
    public String index2(	@RequestParam(value="exam", required=false) String guid,
                             Map<String,Object> model) {
        if (doIndex(guid, model)) {
            return "quiz/index2";
        } else {
            return "quiz/sorry";
        }
    }

    /**
     *
     * @return true if there were no errors, false if there were
     */
    private boolean doIndex(String guid, Map<String, Object> model) {
        if(null == guid || StringUtils.isBlank(guid)) {
            model.put("error", "You need an exam link to take an exam.");
            return false;
        }

        ExamLink examLink = quizService.getExamLinkByGuid(guid);

        if(null == examLink) {
            model.put("error", "Your exam link is old or invalid.");
            return false;
        }

        if(examLink.isUsed()) {
            model.put("error", "This exam link has already been used.");
            return false;
        }

        putCandidate(model, examLink);
        model.put("examLink", examLink);
        model.put("welcomeText", examLink.getCompany().getWelcomeText());
        return true;
    }

	@RequestMapping(method=GET)
	public String start(	@RequestParam(value="exam") String guid,
						Map<String,Object> model) {
		
		ExamLink examLink = quizService.getExamLinkByGuid(guid);
        Sitting sitting = quizService.createSitting(((CandidateExamLink) examLink).getCandidate(),
                ((CandidateExamLink) examLink).getExams().get(0).getArtifactId());
       
        if(sitting.isFinished()) {
            model.put("error", "This exam has already been completed.");
            return "quiz/sorry";
        }
        return "redirect:goToQuestion?s="+sitting.getGuid()+"&qg="+sitting.getNextQuestion().getGuid();
	}
	
	@RequestMapping(method=GET)
	public String goToQuestion(	@RequestParam(value="s") String guid,@RequestParam(value="qg") String questionGuid,
			Map<String,Object> model ) {
        String redirect;

        Sitting sitting = quizService.findSittingByGuid(guid);
        model.put("sitting", sitting);

        if (!sitting.isFinished()) {
        	
            Question question = quizService.goToQuestion(sitting, questionGuid);
            model.put("question", question);
            model.put("questionViewHelper", new MultipleChoiceHelper(question));
            
            model.put("remainingTime", quizService.getExamRemainingTime(sitting));
            model.put("remainingQuestionTime", quizService.getQuestionRemainingTime(question));
            
            redirect = new QuizQuestionViewVisitor(question).getView();
        } else {
            model.put("completionText", sitting.getCandidate().getCompany().getCompletionText());
            redirect = QUIZ_THANKS_VIEW;
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

	@RequestMapping(method = RequestMethod.POST)
	public String finish(@RequestParam("guid") String guid,
                         Map<String, Object> model) {
        logger.debug("********** Finish Exam Client Request");
        quizService.clearExamTimeMonitor(guid);
        Sitting sitting = quizService.findSittingByGuid(guid);
        model.put("completionText", sitting.getCandidate().getCompany().getCompletionText());
		return QUIZ_THANKS_VIEW;
	}
}
