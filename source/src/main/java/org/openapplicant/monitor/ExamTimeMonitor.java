package org.openapplicant.monitor;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapplicant.web.controller.QuizController;
import org.springframework.stereotype.Component;

@Component
public class ExamTimeMonitor{
	private static final Log logger = LogFactory.getLog(QuizController.class);
	
	private static Timer timer;
	private long seconds;
	
	public ExamTimeMonitor(final long totalExamTime) {
		timer = new Timer();
		timer.schedule(new CountDownTask(totalExamTime), 0, 1000);		
	}
	
	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public long getSeconds() {
		return seconds;
	}

	public void setSeconds(long seconds) {
		this.seconds = seconds;
	}

	class CountDownTask extends TimerTask{		
		
		public CountDownTask(final long totalExamTimeSec){
			seconds = totalExamTimeSec; 
		}		

		public void run() {
			if (seconds > 0) {
				logger.debug(seconds + " seconds remaining");
				seconds--;
			} else {
				logger.debug("Countdown finished");
				this.cancel();
			}
		}
	}
}