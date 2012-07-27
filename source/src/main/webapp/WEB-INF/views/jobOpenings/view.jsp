<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="tt" uri="/WEB-INF/tlds/ttTagLibrary.tld"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<style type="text/css">
    .noneDisplay {
        display: none;
    }
    table.sortable {
        border-left: 1px solid #B1B1B1 !important;
    }
    #content {
        overflow-y: auto !important;
        height: auto !important;
    }
    .note {
        width: 285px;
        margin: 5px;
        height: 140px;
        resize: none;
    }
    .candidateSelectionCell {
        height: auto;
        padding: inherit;
    }
    #candidatesSelectionTableDiv {
        height: 145px;
        overflow: auto;
    }
    #addCandidates {
        position: absolute;
        right: 0;
        bottom: 0;
    }
</style>
<style type="text/css">
    @import "<c:url value='/css/layout/candidate_list.css'/>";
</style>
<div id="content">
    <form:form modelAttribute="jobOpening" action="update" id="formId">
        <c:if test="${!(jobOpening.id eq null)}">
        <form:hidden path="id"/>
        </c:if>
        <ul class="half left">
            <li>
                <label for="jobPosition">Job Position:</label>
                <div>
                    <form:select id="jobPosition" path="jobPosition" items="${jobPositions}" itemLabel="title"/>
                </div>
            </li>
            <li>
                <label for="finishDate">Finish Date:</label>
                <div>
                    <form:input id="finishDate" path="finishDate"/>
                </div>
            </li>
            <li>
                <label for="client">Client:</label>
                <div>
                    <form:input id="client" path="client"/>
                </div>
            </li>
            <li>
                <label for="description">Description:</label>
                <div>
                    <form:textarea id="description" path="description"/>
                </div>
            </li>
        </ul>
        <ul>
            <li>
                <label for="applicants">Applicants:</label>
                <div>
                    <div id="applicantsSection" style="float: left !important; width: 82% !important;;">
                    <tiles:insertAttribute name="applicants"/>
                    </div>
                    <div id="pager" class="pagination"></div>
                    <a id="selectCandidates">
                        <img src="<c:url value='/img/add.png' />" style="height: 16px; width: 16px" alt="Add"/>
                    </a>
                    <a id="deleteApplicants" title="Delete selected applicants">
                        <img src="<c:url value="/img/delete.png" />" style="width: 16px; height: 16px" alt="Delete" />
                    </a>
                </div>
            </li>
            <li class="actions">
                <input id="submit" type="submit" class="submit" name="save" value="Save"/>
                <input type="submit" class="submit" name="cancel" value="Cancel"/>
            </li>
        </ul>
        <c:if test="${!(success eq null)}">
         <div class="row">
            <span class="success">The Job Opening was saved successfully!</span>
         </div>
        </c:if>
    </form:form>
</div>
<div class="noneDisplay">
    <div id="modalContainer">
        <div id="candidatesList"></div>
        <input type="button" value="Add selected" id="addCandidates"/>
    </div>
    <c:forEach items="${candidateNotes}" var="candidateNote">
    <div id="noteDiv_${candidateNote.candidate.id}">
        <label for="noteText_${candidateNote.candidate.id}">Note:</label><br/>
        <textarea id="noteText_${candidateNote.candidate.id}" class="note">${candidateNote.note.body}</textarea>
    </div>
    </c:forEach>
</div>

<script type="text/javascript">
    oltk.include('jquery/ui/ui.core.js');
    oltk.include('jquery/ui/ui.dialog.js');
    oltk.include('jquery/cluetip/jquery.cluetip.js');
    oltk.include('jquery/ui/ui.datepicker.js');
    <c:choose><c:when test="${!(fn:substring(pageContext.response.locale,0,2) eq 'en')}">oltk.include('jquery/ui/i18n/ui.datepicker-${fn:substring(pageContext.response.locale,0,2)}.js');
    $('#finishDate').datepicker($.datepicker.regional['${fn:substring(pageContext.response.locale,0,2)}']);</c:when><c:otherwise>$('#finishDate').datepicker();</c:otherwise></c:choose>

    var onLoad = function() {
        $.clueTips = function() {
            $('.candidate_name').cluetip({
                width:425,
                cluetipClass:'history',
                ajaxSettings: {
                    type: 'POST'
                }
            });

            $('.tooltip').each( function() {
                var activation = $(this).hasClass('hover') ? 'hover' : 'click';
                $(this).cluetip({
                    activation: activation,
                    width:175,
                    cluetipClass:'jtip',
                    local:true,
                    arrows:true,
                    closeText: '<img src="<c:url value="/img/jquery_cluetip/close-gray.png"/>"/>'
                });
            });
        };

        $.recordsPerPage = 5;
        $.pageNumber = 0;

        $.updatePager = function () {
            $.numberOfRecords =  $('#appTable').find('tr').length - 1; // - 1 because there is one blank extra row
            $.numberOfPages = Math.ceil($.numberOfRecords / $.recordsPerPage);
            if (($.pageNumber + 1) > $.numberOfPages && $.numberOfPages > 0) {
                $.pageNumber = $.numberOfPages - 1;
            }
            var str = '<ul>';
            str += '<li class=\"prev\" id=\"first\"><a>First</a></li>';
            str += '<li class=\"prev\" id=\"prev\"><a>Prev</a></li>';

            for (i = 0; i < $.numberOfPages; i++) {
                str += '<li id=\"pageNumber' + i + '\"><a title=\"Go to page ' + (i + 1) + '\" >' + (i + 1) + '</a></li>';
            }

            str += '<li class=\"next\" id=\"next\"><a>Next</a></li>';
            str += '<li class=\"next\" id=\"last\"><a>Last</a></li>';
            str += '</ul>';
            $('#pager').html(str);

            $('li[id^=pageNumber]').click(function() {
                $.pageNumber = $(this).attr('id').substr(10);
                $.updateTable();
            });

            $('#first').click(function() {
                if (!$(this).hasClass('disabled')) {
                    $.pageNumber = 0;
                    $.updateTable();
                }
            });

            $('#next').click(function() {
                if (!$(this).hasClass('disabled')) {
                    $.pageNumber++;
                    $.updateTable();
                }
            });

            $('#prev').click(function() {
                if (!$(this).hasClass('disabled')) {
                    $.pageNumber--;
                    $.updateTable();
                }
            });

            $('#last').click(function() {
                if (!$(this).hasClass('disabled')) {
                    $.pageNumber = $.numberOfPages - 1;
                    $.updateTable();
                }
            });
        };

        $.notesDivs = [];

        $('div[id^=noteDiv_]').each(function() {
            var id = $(this).attr('id').substr(8);
            $.notesDivs[id] = $(this);
            $(this).dialog({autoOpen:false, modal: true});
        });

        $.updateTable = function() {
            $('a[id^=note_]').click(function() {
                var id = $(this).attr('id').substr(5);
                var noteDiv = $.notesDivs[id]
                if (noteDiv == null) {
                    $.notesDivs[id] = $('<div id=\'noteDiv_\'' + id + '></div>')
                            .html('<label for="noteText_"' + id + '>Note:</label><br/><textarea id="noteText_' + id + '" class="note"/>');
                    $.notesDivs[id].dialog({autoOpen:false, modal: true});
                }
                $.notesDivs[id].dialog('open');
            });
            $('#appTable').find('tr').each(function (i) {
                if (i > 0) {
                    if ((i - 1) >= ($.pageNumber * $.recordsPerPage) && (i - 1) < (($.pageNumber + 1) * $.recordsPerPage) )
                        $(this).show();
                    else
                        $(this).hide();
                }
            });
            $('li[id^=pageNumber]').each(function() {
                var currentPage = $(this).attr('id').substr(10);
                if (currentPage == $.pageNumber) {
                    $(this).addClass('active');
                } else {
                    $(this).removeClass('active');
                }
            });
            if ($.numberOfPages > 1) {
                if ($.pageNumber == 0) {
                    $('#first').addClass('disabled');
                    $('#prev').addClass('disabled');
                    $('#last').removeClass('disabled');
                    $('#next').removeClass('disabled');
                } else if ($.pageNumber == ($.numberOfPages - 1)) {
                    $('#first').removeClass('disabled');
                    $('#prev').removeClass('disabled');
                    $('#last').addClass('disabled');
                    $('#next').addClass('disabled');
                } else {
                    $('#first').removeClass('disabled');
                    $('#prev').removeClass('disabled');
                    $('#last').removeClass('disabled');
                    $('#next').removeClass('disabled');
                }
            } else {
                $('#first').addClass('disabled');
                $('#prev').addClass('disabled');
                $('#last').addClass('disabled');
                $('#next').addClass('disabled');
            }
        };

        $.updatePager();
        $.updateTable();
        $.clueTips();

        $('#modalContainer').dialog({autoOpen:false, modal: true, width: 500});

        $('#submit').click(function() {
            var data = $('#formId').serializeArray();
            var ats = new Array();
            $(':hidden[id^="_applicants"]').each(function() {
                data.push({
                    name:'a',
                    value:$(this)[0].value
                });
                var note = '';
                var noteTextareaSelector = $('#noteText_' + $(this)[0].value);
                if (noteTextareaSelector.length > 0) {
                    note = noteTextareaSelector[0].value;
                }
                data.push({
                    name:'n',
                    value:note
                });
            });
            data.push({
                name:'submit',
                value:'submit'
            })
            $.post('<c:url value="update"/>',$.param(data), function (data, textStatus, jqXHR) {
                document.documentElement.innerHTML = data;
                onLoad();
            });
            return false;
        });

        $('#selectCandidates').click(function() {
            var aap = new Array();
            $(':hidden[id^="_applicants"]').each(function() {
                aap.push($(this)[0].value);
            });
            $.ajax({
                type:'POST',
                url: '<c:url value="listCandidatesForJobOpening"/>',
                data:{
                    'aap':aap
                },
                success:function(html) {
                    $('#candidatesList').html(html);
                    $('#modalContainer').dialog('open');
                }
            });
            return false;
        });

        $('#addCandidates').click(function() {
            var aap = new Array();
            $(':hidden[id^="_applicants"]').each(function() {
                aap.push($(this)[0].value);
            });
            var ata = new Array();
            $(':checked[id^="candidates"]').each(function() {
                ata.push($(this)[0].value);
            });
            $.ajax({
                type:'POST',
                url: '<c:url value="addCandidatesToJobOpening"/>',
                data:{
                    'aap':aap,
                    'ata':ata},
                success:function(html) {
                    $('#applicantsSection').html(html);
                    $.updatePager();
                    $.updateTable();
                    $.clueTips();
                    $('#modalContainer').dialog('close');
                }
            });
            return false;
        });

        $('#deleteApplicants').click(function() {
            var aap = new Array();
            $(':hidden[id^="_applicants"]').each(function() {
                aap.push($(this)[0].value);
            });
            var atd = new Array();
            $(':checked[id^="applicants"]').each(function() {
                atd.push($(this)[0].value);
            });
            $.ajax({
                type:'POST',
                url: '<c:url value="deleteApplicantsFromJobOpening"/>',
                data:{
                    'aap':aap,
                    'atd':atd},
                success:function(html) {
                    $('#applicantsSection').html(html);
                    $.updatePager();
                    $.updateTable();
                    $.clueTips();
                    $('#modalContainer').dialog('close');
                }
            });
            return false;
        });
    }

    $(document).ready(function() {
        onLoad();
    });
</script>