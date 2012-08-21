package org.openapplicant.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapplicant.domain.Candidate;
import org.openapplicant.domain.Sitting;
import org.openapplicant.domain.event.SittingCompletedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User: Gian Franco Zabarino
 * Date: 21/08/12
 */
@Service
@Transactional
public class SittingService extends ApplicationService {
    private static final Log log = LogFactory.getLog(SittingService.class);

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
}
