package org.openapplicant.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * User: Gian Franco Zabarino
 * Date: 7/5/12
 * Time: 10:06 AM
 */
@Entity
public class JobOpening extends DomainObject {
    private final static long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;
    private Company company;
    private JobPosition jobPosition;
    private String client;
    private Date startDate = new Date();
    private Date finishDate = new Date(System.currentTimeMillis() + MILLIS_IN_A_DAY * 28);
    private Status status = Status.NEW;
    private String description;
    private List<ApplicantNote> applicantNotes;
    private Candidate selectedApplicant;

    @Transient
    public Boolean isOpen() {
        Date now = new Date();
        return now.before(finishDate) && now.after(startDate);
    }

    @Column(nullable = false)
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Column(nullable = false)
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Column(nullable = false)
    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    @ManyToOne
    public JobPosition getJobPosition() {
        return jobPosition;
    }

    public void setJobPosition(JobPosition jobPosition) {
        this.jobPosition = jobPosition;
    }

    @Column
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne(optional = false)
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Column
    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    @ManyToOne
    public Candidate getSelectedApplicant() {
        return selectedApplicant;
    }

    public void setSelectedApplicant(Candidate selectedApplicant) {
        this.selectedApplicant = selectedApplicant;
    }

    @OneToMany
    @Cascade({CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    @JoinColumn(name = "jobOpening")
    public List<ApplicantNote> getApplicantNotes() {
        return applicantNotes;
    }

    public void setApplicantNotes(List<ApplicantNote> applicantNotes) {
        this.applicantNotes = applicantNotes;
    }

    @Transient
    public String getArchivedStatusLabel() {
        if (getStatus().equals(Status.ARCHIVED)) {
            if (hasSelectedApplicant()) {
                return Status.CANDIDATE_SELECTED_TRANSIENT_STATE;
            } else {
                return Status.NO_CANDIDATE_SELECTED_TRANSIENT_STATE;
            }
        } else {
            return Status.NOT_ARCHIVED_TRANSIENT_STATE;
        }
    }

    @Transient
    public Boolean hasSelectedApplicant() {
        return getSelectedApplicant() != null;
    }

    //========================================================================
    // STATUS
    //========================================================================
    public enum Status {
        NEW,
        IN_PROGRESS,
        HIRING_PROCESS,
        ARCHIVED;

        public final static String CANDIDATE_SELECTED_TRANSIENT_STATE = "CANDIDATE_SELECTED";
        public final static String NO_CANDIDATE_SELECTED_TRANSIENT_STATE = "NO_CANDIDATE_SELECTED";
        public final static String NOT_ARCHIVED_TRANSIENT_STATE = "NOT_ARCHIVED";

        public static List<Status> getActiveStatus() {
            return Arrays.asList(NEW, IN_PROGRESS, HIRING_PROCESS);
        }

        public static List<String> getArchivedStatus() {
            return Arrays.asList(CANDIDATE_SELECTED_TRANSIENT_STATE, NO_CANDIDATE_SELECTED_TRANSIENT_STATE);
        }
    }
}
