package org.openapplicant.web.controller;

import org.openapplicant.domain.*;
import org.openapplicant.util.Pagination;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * User: Gian Franco Zabarino
 * Date: 16/07/12
 * Time: 13:34
 */
@Controller
public class JobOpeningController extends AdminController {

    private final static Pagination pagination = Pagination.zeroBased().perPage(10000); // FIXME We need real pagination!

    @ModelAttribute(value = "activeStatuses")
    public List<JobOpening.Status> populateActiveStatuses() {
        return JobOpening.Status.getActiveStatus();
    }

    @ModelAttribute(value = "archivedStatuses")
    public List<String> populateArchivedStatuses() {
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
    public String viewStatus(@RequestParam(required = false) JobOpening.Status status,
                             @RequestParam(required = false) String archivedStatus,
                             Map<String, Object> model) {
        List<JobOpening> jobOpenings = null;
        if (status != null) {
            jobOpenings = getAdminService().findJobOpeningsByCompanyAndStatus(currentUser().getCompany(), status);
            model.put("view_status", status);
        } else {
            if (archivedStatus.equals(JobOpening.Status.CANDIDATE_SELECTED_TRANSIENT_STATE)) {
                jobOpenings = getAdminService().findJobOpeningsByCompanyAndArchivedStatusWithSelectedCandidates(
                        currentUser().getCompany());
                model.put("view_archivedStatus", archivedStatus);
            } else if (archivedStatus.equals(JobOpening.Status.NO_CANDIDATE_SELECTED_TRANSIENT_STATE)) {
                jobOpenings = getAdminService().findJobOpeningsByCompanyAndArchivedStatusWithNoSelectedCandidates(
                        currentUser().getCompany());
                model.put("view_archivedStatus", archivedStatus);
            }
        }
        model.put("jobOpenings", jobOpenings);
        return "jobOpenings/index";
    }

    @RequestMapping(method = GET)
    public String view(@RequestParam(value = "jo") Long jobOpeningId,
                       Map<String, Object> model) {
        JobOpening jobOpening = getAdminService().findJobOpeningById(jobOpeningId);
        model.put("jobOpening", jobOpening);
        model.put("candidateNotes", jobOpening.getApplicantNotes());
        model.put("candidates", createCandidateListFrom(jobOpening.getApplicantNotes()));
        model.put("jobPositions", getAdminService().findJobPositionsByCompany(currentUser().getCompany()));
        return "jobOpenings/view";
    }

    @RequestMapping(method = GET)
    public String add(Map<String, Object> model) {
        model.put("jobOpening", new JobOpening());
        model.put("jobPositions", getAdminService().findJobPositionsByCompany(currentUser().getCompany()));
        return "jobOpenings/view";
    }

    @RequestMapping(method = POST)
    public String update(@ModelAttribute JobOpening jobOpening,
                         @RequestParam(value = "a", required = false) List<Candidate> candidates,
                         @RequestParam(value = "n", required = false) List<Note> notes,
                         @RequestParam(value = "submit", required = false) String submit,
                         Map<String, Object> model) {
        if ((candidates != null && notes == null) ||(candidates == null && notes != null) ||
                (candidates != null && notes != null && candidates.size() != notes.size()))
            throw new IllegalStateException();
        if (submit != null) {
            model.put("jobPositions", getAdminService().findJobPositionsByCompany(currentUser().getCompany()));
                Errors errors = jobOpening.validate();
            if (errors.hasErrors()) {
                model.put("jobOpening", jobOpening);
                model.put("applicants", jobOpening.getApplicantNotes());
                return "jobOpenings/view";
            }
            List<ApplicantNote> applicantNotes = new ArrayList<ApplicantNote>();
            if (candidates != null) {
                for (int i = 0; i < candidates.size(); i++) {
                    ApplicantNote applicantNote = new ApplicantNote();
                    applicantNote.setCandidate(candidates.get(i));
                    applicantNote.setNote(notes.get(i));
                    applicantNotes.add(applicantNote);
                }
            }
            jobOpening.setApplicantNotes(applicantNotes);
            if (jobOpening.getId() != null && jobOpening.getId() > 0) {
                getAdminService().updateJobOpeningInfo(
                        jobOpening.getId(),
                        jobOpening.getJobPosition(),
                        jobOpening.getFinishDate(),
                        jobOpening.getClient(),
                        jobOpening.getDescription(),
                        jobOpening.getApplicantNotes()
                );
            } else {
                jobOpening.setCompany(currentUser().getCompany());
                getAdminService().saveJobOpening(jobOpening);
            }
            model.put("candidateNotes", jobOpening.getApplicantNotes());
            model.put("candidates", createCandidateListFrom(jobOpening.getApplicantNotes()));
            model.put("jobOpening", jobOpening);
            model.put("success", Boolean.TRUE);
            return "jobOpenings/view";
        }
        return "redirect:all";
    }

    @RequestMapping(method = POST)
    public String listCandidatesForJobOpening(
            @RequestParam(value = "aap", required = false) List<Candidate> applicantsAlreadyPresent,
            Map<String, Object> model) {
        List<Candidate> candidates = getAdminService().findAllCandidatesByCompany(currentUser().getCompany(), pagination);
        if (applicantsAlreadyPresent != null) {
            for (Candidate applicant : applicantsAlreadyPresent) {
                if (candidates.contains(applicant)) {
                    candidates.remove(applicant);
                }
            }
        }
        model.put("candidates", candidates);
        return "jobOpenings/candidates";
    }

    @RequestMapping(method = POST)
    public String addCandidatesToJobOpening(
            @RequestParam(value = "aap", required = false) List<Candidate> applicantsAlreadyPresent,
            @RequestParam(value = "ata", required = false) List<Candidate> applicantsToAdd,
            Map<String, Object> model) {
        List<Candidate> candidates = new ArrayList<Candidate>();
        if (applicantsAlreadyPresent != null) {
            candidates.addAll(applicantsAlreadyPresent);
        }
        if (applicantsToAdd != null) {
            candidates.addAll(applicantsToAdd);
        }
        model.put("candidates", candidates);
        return "jobOpenings/applicants";
    }

    @RequestMapping(method = POST)
    public String deleteApplicantsFromJobOpening(
            @RequestParam(value = "aap", required = false) List<Candidate> applicantsAlreadyPresent,
            @RequestParam(value = "atd", required = false) List<Candidate> applicantsToDelete,
            Map<String, Object> model) {
        List<Candidate> candidates = new ArrayList<Candidate>();
        if (applicantsAlreadyPresent!= null) {
            candidates.addAll(applicantsAlreadyPresent);
            if (applicantsToDelete != null) {
                candidates.removeAll(applicantsToDelete);
            }
        }
        model.put("candidates", candidates);
        return "jobOpenings/applicants";
    }

    protected List<Candidate> createCandidateListFrom(List<ApplicantNote> applicantNotes) {
        ArrayList<Candidate> candidates = new ArrayList<Candidate>();
        for (ApplicantNote applicantNote : applicantNotes) {
            candidates.add(applicantNote.getCandidate());
        }
        return candidates;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder, Locale locale) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(DateFormat.getDateInstance(DateFormat.SHORT, locale), false));
        binder.registerCustomEditor(JobPosition.class, new PropertyEditorSupport() {
            @Override
            public String getAsText() {
                if (this.getValue() != null && this.getValue() instanceof JobPosition) {
                    return ((JobPosition)getValue()).getId().toString();
                }
                return super.getAsText();
            }

            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (StringUtils.hasText(text)) {
                    setValue(getAdminService().findJobPositionById(Long.parseLong(text)));
                }
            }
        });
        binder.registerCustomEditor(Candidate.class, new PropertyEditorSupport() {
            @Override
            public String getAsText() {
                if (this.getValue() != null && this.getValue() instanceof Candidate) {
                    return ((Candidate)getValue()).getId().toString();
                }
                return super.getAsText();
            }

            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (StringUtils.hasText(text)) {
                    setValue(getAdminService().findCandidateById(Long.parseLong(text)));
                }
            }
        });
        binder.registerCustomEditor(Note.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (StringUtils.hasText(text)) {
                    Note note = new Note(currentUser(), text);
                    setValue(note);
                }
            }
        });
    }
}
