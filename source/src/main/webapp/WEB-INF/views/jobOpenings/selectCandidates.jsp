<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<div id="candidatesSelectionTableDiv">
<table>
    <tbody>
        <tr>
        <c:forEach items="${candidates}" var="candidate" varStatus="row">
            <td class="candidateSelectionCell"><input type="checkbox" id="candidates${row.count}" value="${candidate.id}"/><label for="candidates${row.count}">${candidate.name}</label></td>
        <c:if test="${row.count mod 4 == 0 && row.count > 1}"></tr><tr></c:if>
        </c:forEach>
        </tr>
    </tbody>
</table>
</div>