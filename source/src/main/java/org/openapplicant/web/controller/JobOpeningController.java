package org.openapplicant.web.controller;

import org.openapplicant.domain.JobOpening;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * User: Gian Franco Zabarino
 * Date: 16/07/12
 * Time: 13:34
 */
@Controller
public class JobOpeningController extends AdminController
{

    @ModelAttribute(value = "activeStatuses")
    public List<JobOpening.Status> populateActiveStatuses() {
        return JobOpening.Status.getActiveStatus();
    }

    @ModelAttribute(value = "archivedStatuses")
    public List<JobOpening.Status> populateArchivedStatuses() {
        return JobOpening.Status.getArchivedStatus();
    }

    @RequestMapping(method = GET)
    public String all(Map<String, Object> model) {
        model.put("jobOpeningsSidebar", true);
        model.put("jobOpenings", getAdminService().findAllJobOpeningsByCompany(currentUser().getCompany()));
        return "jobOpenings/index";
    }

    @RequestMapping(method = GET)
    public String active(Map<String, Object> model) {
        model.put("jobOpenings", getAdminService().findActiveJobOpeningsByCompany(currentUser().getCompany()));
        model.put("viewActive", true);
        return "jobOpenings/index";
    }

    @RequestMapping(method = GET)
    public String archived(Map<String, Object> model) {
        model.put("jobOpenings", getAdminService().findArchivedJobOpeningsByCompany(currentUser().getCompany()));
        model.put("viewArchived", true);
        return "jobOpenings/index";
    }

    @RequestMapping(method = GET, value = "viewstatus")
    public String viewStatus(@RequestParam JobOpening.Status status,
                             Map<String, Object> model) {
        model.put("jobOpenings", getAdminService().findJobOpeningsByCompanyAndStatus(currentUser().getCompany(), status));
        model.put("view_status", status);
        return "jobOpenings/index";
    }

    @RequestMapping(method = GET)
    public String view(@RequestParam(value = "jo") Long jobOpeningId,
                       Map<String, Object> model) {
        model.put("jobOpening", getAdminService().findJobOpeningById(jobOpeningId));
        return "jobOpenings/view";
    }
}
