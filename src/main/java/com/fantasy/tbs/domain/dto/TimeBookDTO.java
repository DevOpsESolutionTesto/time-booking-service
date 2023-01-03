package com.fantasy.tbs.domain.dto;

import java.time.ZonedDateTime;

public class TimeBookDTO {

    private ZonedDateTime timeStamp;
    private String personalNumber;

    public ZonedDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(ZonedDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getPersonalNumber() {
        return personalNumber;
    }

    public void setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
    }
}
