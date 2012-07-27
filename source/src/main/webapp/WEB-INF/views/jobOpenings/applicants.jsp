<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net/el" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="tt" uri="/WEB-INF/tlds/ttTagLibrary.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>
<%@ page import="static org.openapplicant.domain.User.Role.ROLE_HR" %>
<%@ page import="static org.openapplicant.domain.User.Role.ROLE_HR_MANAGER" %>
<%@ page import="static org.openapplicant.domain.User.Role.ROLE_ADMIN" %>

<display:table name="applicants" id="c" class="sortable" keepStatus="true" requestURI="${requestScope['javax.servlet.forward.request_uri']}" htmlId="appTable">
    <display:column headerClass="header" media="html">
        <input type="checkbox" id="applicants${row.count}" value="${c.id}">
    </display:column>
    <display:column headerClass="header" title="Name" media="html">
        <input type="hidden" id="_applicants${row.count}" value="${c.id}">
        <a class="candidate_name" href="<c:url value='/admin/candidates/detail?id=${c.id}'/>" rel="<c:url value='/admin/candidates/history?id=${c.id}'/>" title="History: ${tt:abbreviateTo(c.name.fullName,37)}">
            <c:choose>
                <c:when test="${!empty c.name.first}">
                    <span><c:out value="${tt:abbreviateTo(c.name.first,14)}"/> <strong><c:out value="${tt:abbreviateTo(c.name.last,14)}"/></strong></span>
                </c:when>
                <c:otherwise>
                    <span><i>Information Required</i> <img src="<c:url value='/img/candidate_icons/warning.png'/>"/></span>
                </c:otherwise>
            </c:choose>
        </a>
    </display:column>
    <display:column title="Name" media="csv excel xml" >
        <c:choose>
            <c:when test="${!empty c.name.first}">
                <c:out value="${c.name}"/>
            </c:when>
            <c:otherwise>
                Information Required
            </c:otherwise>
        </c:choose>
    </display:column>
    <display:column headerClass="header" title="Date" >
        <fmt:formatDate value="${c.entityInfo.createdDate.time}" type="date" dateStyle="short" timeStyle="short" />
    </display:column>
    <display:column headerClass="header" title="Screen" class="numerical" media="html" >
        <c:choose>
            <c:when test="${!empty c.resume.screeningScore}">
                <c:out value="${c.resume.screeningScore}"/>
            </c:when>
            <c:otherwise>
                &mdash;
            </c:otherwise>
        </c:choose>
    </display:column>
    <display:column title="Screen" media="csv excel xml" >
        <c:choose>
            <c:when test="${!empty c.resume.screeningScore}">
                <c:out value="${c.resume.screeningScore}"/>
            </c:when>
            <c:otherwise>
                -
            </c:otherwise>
        </c:choose>
    </display:column>
    <display:column headerClass="header" title="Exam" class="numerical" media="html">
        <c:choose>
            <c:when test="${!empty c.lastSitting}">
                <c:out value="${c.lastSitting.score}"/>
            </c:when>
            <c:otherwise>
                &mdash;
            </c:otherwise>
        </c:choose>
    </display:column>
    <display:column title="Exam" media="csv excel xml">
        <c:choose>
            <c:when test="${!empty c.lastSitting}">
                <c:out value="${c.lastSitting.score}"/>
            </c:when>
            <c:otherwise>
                -
            </c:otherwise>
        </c:choose>
    </display:column>
    <display:column headerClass="header" title="Match" class="numerical" media="html">
        <c:choose>
            <c:when test="${!empty c.matchScore}">
                <c:out value="${c.matchScore}"/>
            </c:when>
            <c:otherwise>
                &mdash;
            </c:otherwise>
        </c:choose>
    </display:column>
    <display:column title="Match" media="csv excel xml">
        <c:choose>
            <c:when test="${!empty c.matchScore}">
                <c:out value="${c.matchScore}"/>
            </c:when>
            <c:otherwise>
                -
            </c:otherwise>
        </c:choose>
    </display:column>
    <display:column headerClass="header" title="Status" class="${c.status}">
        <c:out value="${tt:humanize(c.status)}"/>
    </display:column>
    <display:column media="html" headerClass="icon header" class="icon" title="<img src=\"${pageContext.request.contextPath}/img/table/phone.gif\" title=\"Sort by Contact Info\"/>">
        <c:if test="${!empty c.cellPhoneNumber.number || !empty c.homePhoneNumber.number || !empty c.workPhoneNumber.number}">
            <a class="tooltip hover" rel="#phone_tooltip_${c.id}" title="<c:out value="${tt:abbreviateTo(c.name.first, 15)}"/>">
                <img src="<c:url value='/img/table/phone.gif'/>"/>
            </a>
        </c:if>
        <div style="display:none" id="phone_tooltip_${c.id}">
            <ul>
                <c:if test="${!empty c.cellPhoneNumber.number}">
                    <li>
                        (c) <c:out value="${c.cellPhoneNumber.number}"/>
                    </li>
                </c:if>
                <c:if test="${!empty c.homePhoneNumber.number}">
                    <li>
                        (h) <c:out value="${c.homePhoneNumber.number}"/>
                    </li>
                </c:if>
                <c:if test="${!empty c.workPhoneNumber.number}">
                    <li>
                        (w) <c:out value="${c.workPhoneNumber.number}"/>
                    </li>
                </c:if>
            </ul>
        </div>
    </display:column>
    <display:column media="html" headerClass="icon header" class="icon" title="<img src=\"${pageContext.request.contextPath}/img/table/email.gif\" title=\"Sort by Email\"/>">
        <c:if test="${!empty c.email}">
            <a href='mailto:<c:out value="${c.email}"/>'>
                <img src="<c:url value='/img/table/email.gif'/>" title="Send Email to ${tt:abbreviateTo(c.name.first,15)} at ${tt:abbreviateTo(c.email,30)}"/>
            </a>
        </c:if>
    </display:column>
    <display:column media="html" headerClass="icon header" class="icon" title="<img src=\"${pageContext.request.contextPath}/img/table/resume.gif\" title=\"Sort by Resume\"/>">
        <c:if test="${!empty c.resume}">
            <a href="<c:url value='/admin/file?guid=${c.resume.guid}' />" target="_blank">
                <img src="<c:url value='/img/table/resume.gif'/>" title="Download ${tt:abbreviateTo(c.name.first,15)}'s Resume"/>
            </a>
        </c:if>
    </display:column>
    <security:authorize ifNotGranted="<%=ROLE_HR.name()%>">
        <display:column media="html" headerClass="icon header" class="icon" title="<img src=\"${pageContext.request.contextPath}/img/table/analytics.gif\" title=\"Sort by Exam Results\"/>">
            <c:if test="${!empty c.lastSitting}">
                <a href="<c:url value='/admin/results/exam?s=${c.lastSitting.id}' />">
                    <img src="<c:url value='/img/table/analytics.gif'/>" title="${tt:abbreviateTo(c.name.first,15)}'s Exam Results"/>
                </a>
            </c:if>
        </display:column>
    </security:authorize>
    <security:authorize ifAnyGranted="<%=ROLE_HR_MANAGER.name() + \",\" + ROLE_HR.name() %>">
        <display:column media="html" headerClass="icon header" class="icon" title="<img src=\"${pageContext.request.contextPath}/img/table/packet.gif\" title=\"Sort by Printable Packet\"/>">
            <a href="<c:url value='/admin/report.pdf?candidate=${c.id}' />">
                <img src="<c:url value='/img/table/packet.gif'/>" title="${tt:abbreviateTo(c.name.first,15)}'s Packet"/>
            </a>
        </display:column>
    </security:authorize>
    <security:authorize ifAnyGranted="<%=(ROLE_ADMIN.name() + \",\" + ROLE_HR_MANAGER.name())%>">
        <display:column media="html" headerClass="icon header" class="icon" title="<img src=\"${pageContext.request.contextPath}/img/table/notes.png\" title=\"Notes\"/>">
            <a href="<c:url value='/admin/candidates/notes?candidate=${c.id}' />">
                <img src="<c:url value='/img/table/notes.png'/>" title="${tt:abbreviateTo(c.name.first,15)}'s Notes"/>
            </a>
        </display:column>
    </security:authorize>
</display:table>