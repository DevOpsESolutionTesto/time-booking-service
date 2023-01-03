package com.fantasy.tbs.service;

import com.fantasy.tbs.domain.dto.EmployeeWorkingTimeDTO;

public interface CaculatingWorkingTimeService {
    /**
     * Get employee working hours by personalNumber
     */
    EmployeeWorkingTimeDTO getWorkHoursByPersonalNumber(String personalNumber);
}
