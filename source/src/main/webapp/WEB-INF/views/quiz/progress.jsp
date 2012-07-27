<%@ taglib prefix="c"    uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div id="examProgress">	
	<div>
			<c:if test="${isExamInTime eq true}">				
				 <li>
		         	 <label id="examTime"></label>          	 
		         </li>
			</c:if>
	</div>
</div>

 

