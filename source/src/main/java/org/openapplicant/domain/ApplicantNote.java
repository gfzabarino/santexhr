package org.openapplicant.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
* User: Gian Franco Zabarino
* Date: 25/07/12
* Time: 16:48
*/
@Entity
public class ApplicantNote extends DomainObject {
    private Candidate candidate;
    private Note note;

    public ApplicantNote() {}

    @ManyToOne(optional = false)
    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    @OneToOne
    @Cascade(CascadeType.ALL)
    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }
}
