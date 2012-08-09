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
	 @import "<c:url value='/css/layout/pagination.css'/>";
</style>

<form id="openapplicant_question_form" class="openapplicant_quiz_question">
	
	<input type="hidden" id="sittingId" value="${sitting.id}"/>
	<input type="hidden" id="questionId" value="${question.id}"/>
	<input type="hidden" id="remainingTime" value="${remainingTime}"/>
	<div class="row">
	   <span id="name"><c:out value="${sitting.exam.name}"/>
	   <c:out value="${sitting.nextQuestionIndex}"/> of <c:out value="${fn:length(sitting.exam.questions)}"/></span>   
	   <span id="time_allowed"><c:out value="${question.timeAllowed}"/> s</span>
	</div>
	<tiles:insertAttribute name="questionKind"/>
   	<div class="pagination">
   		<ul>
   			<c:set var="hasPreviousQuestion" value="${sitting.nextQuestionIndex -2 >= 0?'':'disabled'}"></c:set>
	   		<li class="prev ${hasPreviousQuestion}">
				<a id="${hasPreviousQuestion}previousQuestion" >Prev</a>	   		
	   		</li>
			<c:forEach items="${sitting.exam.questions}" var="questionIndex" varStatus="index">
				<li class="${sitting.nextQuestionIndex == index.index + 1?'active':''}">
					<a class="goToQuestion" id="goToQuestion_${questionIndex.id}"> <c:out value="${index.index + 1}"/></a> 
				</li> 
			</c:forEach>	
			<li class="next">
				<a id="nextQuestion" >
					&nbsp;${sitting.nextQuestionIndex == fn:length(sitting.exam.questions) ? 'finish':'Next'}
				</a>	   		
	   		</li>   			
   		</ul>
   	</div>
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
				 $(document).everyTime('5s',function(i) {
					$.ajax({
						type: "POST",
						url: '<c:url value="progress"/>',
						data: {remainingTime:totalTime,id:$('#sittingId').val()},
						success: function (data){
							if(totalTime == 0){
								$(document).stopTime('keepalive');
								submitResponse();								
								nextQuestion();													
							}
						},
						error: function (request, status, error){
							$("#examTime").html("The exam time has expired.");
							$(document).stopTime('keepalive');
							submitResponse();								
							nextQuestion();													
						}  
					});
				}, 0);
			}			 
	});
	//End - check progress functionality
	
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
		else { window.location = "<c:url value='/quiz/question?s=${sitting.guid}&id=${sitting.id}'/>"; }
	}
	
	function previousQuestion() {
		if(!submittedResponse) { setTimeout("previousQuestion()", 10); }
		else { window.location = "<c:url value='/quiz/prevQuestion?s=${sitting.guid}&id=${sitting.id}'/>"; }
	}
	
	$('#nextQuestion').click( function() {
		openapplicant.quiz.helper.timer.destroy();		
		submitResponse();
		nextQuestion();
	});
	
	$('#previousQuestion').click( function() {
		openapplicant.quiz.helper.timer.destroy();		
		submitResponse();
		previousQuestion();
	});
	
	$('.goToQuestion').click( function() {
		openapplicant.quiz.helper.timer.destroy();		
		submitResponse();
		var qId = $(this).attr("id").split("_")[1];
		$(location).attr('href',"<c:url value='/quiz/goToQuestion?s=${sitting.guid}&qId=" + qId + "&id=${sitting.id}'/>");
	});
</script>