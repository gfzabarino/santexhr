package org.openapplicant.monitor;

import org.openapplicant.monitor.timed.SittingTimeable;
import org.openapplicant.service.AbstractTimeManager;
import org.openapplicant.service.SittingTimeManager;
import org.springframework.stereotype.Component;

@Component
public class ExamTimeMonitor extends AbstractTimeMonitor<SittingTimeable>{

	public ExamTimeMonitor(long totalExamTime, SittingTimeable entity,
			String objectGuid, SittingTimeManager timeManager) {
		super(totalExamTime, entity, objectGuid, (AbstractTimeManager<SittingTimeable, ExamTimeMonitor>)timeManager);
	}
}