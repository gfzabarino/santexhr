<%@ taglib prefix="c"    uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div id="examProgress">	
	<div>
		<c:choose>
			<c:when test="${isExamInTime eq true}">				
				 <li>
		         	 <label id="examTime"></label>          	 
		         </li>
			</c:when>
		    <c:otherwise>
		       	<li>
		        	 <label>The exam time has expired.</label> 
		        </li>
		    </c:otherwise>
		</c:choose>
	</div>
</div>

 

