<%@ taglib prefix="c"    uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="/WEB-INF/tlds/ttTagLibrary.tld" prefix="tt" %>

<style type="text/css">
		.openapplicant_quiz_question {
			width: 60%;
		}
</style>

<h3 class="question-title">${question.prompt}</h3>
<c:if test="${not empty question.promptContent}">
    <!-- code -->
    <code id="code">
        <textarea disabled="disabled">${question.promptContent}</textarea>
    </code>
    <!-- code -->
</c:if>
<ul id="answerChoices" class="input-radio">
<c:forEach items="${sitting.questionsAndResponses}" var="questionAndResponse"><c:if test="${question eq questionAndResponse.question}"><c:set var="response" value="${questionAndResponse.response.content}"/></c:if></c:forEach>
<c:forEach var="choice" items="${questionViewHelper.choices}" varStatus="row">
    <li>
        <label>
            <input type="radio"<c:if test="${choice eq response}"> checked=""</c:if> class="radio" name="answerIndex" value="${row.index}"<c:if test="${disableInputOnTimedQuestions}"> disabled="disabled"</c:if>>
            <strong><c:out value="${choice}"/></strong>
        </label>
    </li>
</c:forEach>
    <li>
        <label>
            <input id="dontKnowTheAnswer" type="radio" class="radio" name="answerIndex" value="true"<c:forEach items="${sitting.questionsAndResponses}" var="questionAndResponse"><c:if test="${(question eq questionAndResponse.question) && !(questionAndResponse.response eq null) && (questionAndResponse.response.dontKnowTheAnswer == true)}"> checked=""</c:if></c:forEach><c:if test="${disableInputOnTimedQuestions}"> disabled="disabled"</c:if>>
            <strong>No se la respuesta</strong>
        </label>
    </li>
</ul>

<div style="display:none">
	<textarea id="response"></textarea>
</div>
	
<script type="text/javascript">
	oltk.include('openapplicant/quiz/helper/recorder.js');
	
	$('#answerChoices li').each(function() {
		var self = this;
		var choice = $.trim($('label', this).text());
		var radio = $('input:radio', this)[0];
        if (radio.id == 'dontKnowTheAnswer') {
            function dontKnowTheAnswerChange() {
                $('#response').val('');
                $('input:radio', self).attr("checked", true);
                openapplicant.quiz.helper.recorder.keypressNoPaste();
            }
            $(radio).change(dontKnowTheAnswerChange);
        } else {
            function multipleChoiceChange() {
                $('#response').val(choice);
                openapplicant.quiz.helper.recorder.keypressNoPaste();
            };
            $(radio).change(multipleChoiceChange);
        }
    });
	
	openapplicant.quiz.helper.recorder.init('#response', '#dontKnowTheAnswer', '#flagQuestionCheck');
</script>