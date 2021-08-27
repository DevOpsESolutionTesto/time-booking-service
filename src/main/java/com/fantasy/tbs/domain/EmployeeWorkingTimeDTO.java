package com.fantasy.tbs.domain;

/**
 * @author Tao Summer
 * @description Employee working time dto
 * @date 2021/8/26 18:15
 */

public class EmployeeWorkingTimeDTO {
    private String personalNumber;
    private Double workingTime;

    public String getPersonalNumber() {
        return personalNumber;
    }

    public void setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
    }

    public Double getWorkingTime() {
        return workingTime;
    }

    public void setWorkingTime(Double workingTime) {
        this.workingTime = workingTime;
    }
}
