package com.fantasy.tbs.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A TimeBooking.
 */
@Entity
@Table(name = "time_booking")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TimeBooking implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking")
    private ZonedDateTime booking;

    @Column(name = "personal_number")
    private String personalNumber;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TimeBooking id(Long id) {
        this.id = id;
        return this;
    }

    public ZonedDateTime getBooking() {
        return this.booking;
    }

    public TimeBooking booking(ZonedDateTime booking) {
        this.booking = booking;
        return this;
    }

    public void setBooking(ZonedDateTime booking) {
        this.booking = booking;
    }

    public String getPersonalNumber() {
        return this.personalNumber;
    }

    public TimeBooking personalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
        return this;
    }

    public void setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimeBooking)) {
            return false;
        }
        return id != null && id.equals(((TimeBooking) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TimeBooking{" +
            "id=" + getId() +
            ", booking='" + getBooking() + "'" +
            ", personalNumber='" + getPersonalNumber() + "'" +
            "}";
    }
}
