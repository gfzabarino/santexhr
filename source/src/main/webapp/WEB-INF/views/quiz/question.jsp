<%@ taglib prefix="c"    uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib uri="/WEB-INF/tlds/ttTagLibrary.tld" prefix="tt" %>

<script type="text/javascript" src="<c:url value='/dwr/engine.js'/>"></script>
<script type="text/javascript" src="<c:url value='/dwr/interface/QuizService.js'/>"></script>
<script type="text/javascript">
	oltk.include('openapplicant/quiz/controller/LoadingController.js');
	dwr.engine.setPreHook( function() { openapplicant.quiz.controller.LoadingController.show(); } );
	dwr.engine.setPostHook( function() { openapplicant.quiz.controller.LoadingController.hide(); } );
</script>

<style type="text/css">
	 @import "<c:url value='/css/layout/quiz.css'/>";
</style>

<form id="openapplicant_question_form" class="openapplicant_quiz_question">
	
	<input type="hidden" id="sittingId" value="${sitting.id}"/>
	<input type="hidden" id="questionId" value="${question.id}"/>
	<input type="hidden" id="remainingTime" value="${remainingTime}"/>
	<div class="row">	   
	   <span id="name"><c:out value="${sitting.exam.name}"/>, <c:out value="${sitting.nextQuestionIndex}"/> of <c:out value="${fn:length(sitting.exam.questions)}"/></span>
	   <span id="time_allowed"><c:out value="${question.timeAllowed}"/> s</span>
	</div>	
	<tiles:insertAttribute name="questionKind"/>
		
	<a id="next" style="display:none;">continue</a>
	<tiles:insertAttribute name="progressTime"/>
	<div id="errorMessage"></div>
</form>
<script type="text/javascript">	
	//Begin - check progress functionality
	oltk.include('jquery/time/jquery.timers-1.2.js');
	oltk.include('jquery/jquery.js');
	$(document).ready(function(){
		    var totalTime = $("#remainingTime").val();		  
			if(totalTime != ""){
				 //Display Total Exam time - CountDown.	
				 $(document).everyTime('1s',function(i) {
					 if(totalTime > 0){
					 	totalTime = totalTime - 1;			  
					 	$("#examTime").html("Exam Time: " + totalTime + " s");
					 }
					 else
					 {
						 $("#examTime").html("Exam Time: " + totalTime + " s");
						 $(document).stopTime('displayRemainingTime');
					 }
				 });	 
				 //Server ping and Check Server Remaining Time.
				 $(document).everyTime('10s',function(i) {
					$.ajax({
						type: "POST",
						url: '<c:url value="progress"/>',
						data: {remainingTime:totalTime},
						success: function (data){
							if(totalTime == 0){
								submitResponse();
								$(document).stopTime('keepalive');					
							}
						},
						error: function (request, status, error){					
							submitResponse();
							$(document).stopTime('keepalive');
						}  
					});
				}, 0);
			}			 
	});
	//End - check progress functionality
	
	$('a#next').fadeIn('slow');	
	oltk.include('openapplicant/quiz/helper/timer.js');
	openapplicant.quiz.helper.timer.init('#time_allowed', ${null==question.timeAllowed ? 0 : question.timeAllowed},
		submitResponse,
		nextQuestion
	);

	var submittedResponse = false;
	
	function canContinue() {
		submittedResponse = true;
	}

	function submitResponse() {
		var sittingId = $('#sittingId').val();
		var questionId = $('#questionId').val();
		var response = openapplicant.quiz.helper.recorder.getResponse();
		QuizService.submitResponse(sittingId, questionId, response, canContinue);
	}
	
	function nextQuestion() {
		if(!submittedResponse) { setTimeout("nextQuestion()", 10); }
		else { window.location = "<c:url value='/quiz/question?s=${sitting.guid}'/>"; }
	}
	
	$('#next').click( function() {
		openapplicant.quiz.helper.timer.destroy();		
		submitResponse();
		nextQuestion();
	});
</script>