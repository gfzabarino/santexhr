package org.openapplicant.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapplicant.domain.*;
import org.openapplicant.domain.event.CandidateCreatedEvent;
import org.openapplicant.domain.event.SittingCompletedEvent;
import org.openapplicant.domain.event.SittingCreatedEvent;
import org.openapplicant.domain.link.ExamLink;
import org.openapplicant.domain.question.Question;
import org.openapplicant.monitor.timed.QuestionTimeable;
import org.openapplicant.monitor.timed.SittingTimeable;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class QuizService extends ApplicationService {
	
	private static final Log log = LogFactory.getLog(QuizService.class);
	
	private SittingTimeManager sittingTimeManager;
	
	private QuestionTimeManager questionTimeManager;
	

    public void setSittingTimeManager(SittingTimeManager sittingTimeManager) {
        this.sittingTimeManager = sittingTimeManager;
    }
    
    

    public void setQuestionTimeManager(QuestionTimeManager questionTimeManager) {
		this.questionTimeManager = questionTimeManager;
	}
	
	/**
	 * Retrieves an exam link by its guid, sets the exam link to be used.
	 * 
	 * @param guid the exam links guid
	 * @return examLink the exam link with the given guid.  Returns null if 
	 * no exam link exists with the given guid.
	 */
	public ExamLink getExamLinkByGuid(String guid) {
		try {
		    return getExamLinkDao().findByGuid(guid);
		} catch(DataRetrievalFailureException e) {
			log.error("getExamLinkByGuid", e);
			return null;
		}
	}
	
	/**
	 * Sets the examLink to used.
	 * @param guid
	 */
	public void useExamLink(String guid) {
	    	ExamLink examLink = getExamLinkDao().findByGuidOrNull(guid);
	    	
	    	//FIXME: this should return a company exam link
	    	if(null == examLink)
	    	    return;

	    	examLink.setUsed(true);
	    	getExamLinkDao().save(examLink);
	}
	
	/**
	 * Retrieves a company by its request url
	 * 
	 * @param url the company's request url name
	 * @return company
	 * @throws DataRetrievalFailureException if no company exists.
	 */
	public Company findByUrl(String url) {
		return getCompanyDao().findByProxyname(getHostname(url));
	}
	
	private String getHostname(String url) {
		//host "http://localhost:8080/recruit-quiz-webclient/actions/submitCandidateLogin.jsp" 
		String[] splitURL = url.split("/");
		return splitURL[2];
	}
	
	/**
	 * This retruns 
	 * @param candidate a partially filled in candidate
	 * @param company the company to which the candidate will be associated
	 * @return
	 */
	public Candidate resolveCandidate(Candidate candidate, Company company) {	    
	    if(candidate.getId() != null) {
	    	return getCandidateDao().findOrNull(candidate.getId());
	    }
	    
	    Candidate existingCandidate = getCandidateDao().findByEmailAndCompanyIdOrNull(candidate.getEmail(), company.getId());
	    if(null == existingCandidate) {
	    	candidate.setCompany(company);
	    	candidate = getCandidateDao().save(candidate);
	    	getCandidateWorkFlowEventDao().save(new CandidateCreatedEvent(candidate));
	    } else {
	    	candidate = existingCandidate;
	    }
	    
	    return candidate;
	}
	
	/**
	 * Creates a for the given candidate, creating the candidate if it is 
	 * new.
	 * 
	 * @param candidate the candidate to a create a sitting for.
	 * @param examArtifactId the artifactId of the exam to take
	 * @return the created sitting
	 */
	public Sitting createSitting(Candidate candidate, String examArtifactId) {
		// FIXME: what if artifactId refers to a different exam version after the break?
		Exam exam = findExamByArtifactId(examArtifactId);

		Sitting sitting = null;
		for(Sitting s : candidate.getSittings()) {
		    if(s.getExam().getArtifactId().equals(examArtifactId)) {
		    	sitting = s;
		    	break;
		    }
		}
		
		if(null == sitting) {
		    sitting = new Sitting(exam);
		    candidate.addSitting(sitting);
		    candidate.setStatus(Candidate.Status.EXAM_STARTED);
		    getCandidateWorkFlowEventDao().save(new SittingCreatedEvent(sitting));
		    getCandidateDao().save(candidate);
		}
		 SittingTimeable sittingTimeable = new SittingTimeable(sitting);
		 sittingTimeManager.startTimer(sittingTimeable);
		return sitting;
	}
	
	/**
	 * Go to question.
	 *
	 * @param sitting the sitting
	 * @param questionGuid the question guid
	 * @return the question
	 */
	public Question goToQuestion(Sitting sitting, String questionGuid) {
		Question question = sitting.goToNextQuestion(questionGuid);
		
        QuestionTimeable questionTimeable = new QuestionTimeable(question);
        
        if (!isAnswered(sitting, questionGuid)) {
        	questionTimeManager.startTimer(questionTimeable);
        }
        
    	return question;		
	}
	
	/**
	 * @param sitting
	 * @param questionGuid
	 * @return
	 */
	private Boolean isAnswered(Sitting sitting, String questionGuid) {
		for (QuestionAndResponse qar: sitting.getQuestionsAndResponses()) {
			if (qar.getGuid().equals(questionGuid)) {
				return qar.getResponse().getLoadTimestamp() != 0;
			}
		}
		return false;
	}
	
	/**
	 * @param sitting
	 * @return
	 */
	public Long getExamRemainingTime(Sitting sitting) {
		Long rv = null;
		if (sitting != null) {
			SittingTimeable sittingTimeable = new SittingTimeable(sitting);
			rv = sittingTimeManager.getRemainingTime(sittingTimeable);
		}
		return rv;
	}
	
	/**
	 * @param question
	 * @return
	 */
	public Long getQuestionRemainingTime(Question question) {
		Long rv = null;
		if (question != null) {
			QuestionTimeable questionTimeable = new QuestionTimeable(question);
			rv = questionTimeManager.getRemainingTime(questionTimeable);
		}
		return rv;
	}
	
	public void clearExamTimeMonitor(String guid) {
		if(sittingTimeManager.isExamMonitoring(guid)){
            log.debug("********** Removing sitting from timeManager");
            sittingTimeManager.clearExamTimeMonitorBySitting(guid);
		}
	}
	
	/**
	 * Evaluate candidate status.
	 * If the last question is submitted, the status of the candidates changes 
	 * to READY_FOR_GRADING
	 *
	 * @param sitting the sitting
	 */
	public void doSittingFinished(Sitting sitting) {
        if (sitting.getStatus().equals(Sitting.Status.STARTED)) {
            log.debug("********** Changing Sitting and Candidate status");
            sitting.setStatus(Sitting.Status.FINISHED);
            sitting.getCandidate().setStatus(Candidate.Status.READY_FOR_GRADING);
            getCandidateDao().save(sitting.getCandidate());
            getCandidateWorkFlowEventDao().save(new SittingCompletedEvent(sitting));
            getSittingDao().save(sitting);
        }
	}
	

	/**
	 * Retrieves a sitting with the given id
	 * 
	 * @param id
	 * @return Sitting
	 * @throws DataRetrievalFailureException
	 *             if no sitting has sittingId
	 */
	public Sitting findSittingById(Long id) {
		return getSittingDao().find(id);
	}
	
	/**
	 * Retrieves a sitting with the given guid
	 * 
	 * @param guid
	 * @return Sitting
	 * @throws DataRetrievalFailureException
	 *             if no sitting has guid
	 */	
	public Sitting findSittingByGuid(String guid) {
		return getSittingDao().findByGuid(guid);
	}
	
	/**
	 * Persists a candidate's response to a question.
	 * 
	 * @param sittingGuid the guid of the current sitting
	 * @param questionGuid the guid of the question responded to
	 * @param response the candidate's response.
	 * @return the saved response.
	 */
	public Response submitResponse(String sittingGuid, String questionGuid, Response response) {
        log.debug("Client submit response: " + (StringUtils.isBlank(response.getContent()) ? (response.isDontKnowTheAnswer() ? "dontKnow" : "nothing") : response.getContent()));
        Sitting sitting = getSittingDao().findByGuid(sittingGuid);
        if (!sitting.isFinished()) {
            log.debug("Client submit response... assigned");
            sitting.assignResponse(questionGuid, response);
            getSittingDao().save(sitting);
            return getResponseDao().save(response);
        }
        return null;
    }
}
