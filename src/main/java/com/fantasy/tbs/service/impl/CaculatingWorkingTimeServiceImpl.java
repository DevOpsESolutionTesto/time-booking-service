package com.fantasy.tbs.service.impl;

import com.fantasy.tbs.domain.EmployeeWorkingTime;
import com.fantasy.tbs.domain.TimeBooking;
import com.fantasy.tbs.service.CaculatingWorkingTimeService;
import com.fantasy.tbs.service.TimeBookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tao Summer
 * @description Service Implementation for managing {@link EmployeeWorkingTime}.
 * @date 2021/8/26 18:07
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CaculatingWorkingTimeServiceImpl implements CaculatingWorkingTimeService {

    private final Logger log = LoggerFactory.getLogger(CaculatingWorkingTimeServiceImpl.class);

    private final TimeBookingService timeBookingService;

    public CaculatingWorkingTimeServiceImpl(TimeBookingService timeBookingService) {
        this.timeBookingService = timeBookingService;
    }

    @Override
    public List<EmployeeWorkingTime> findEmployeeWorkingTimeByPersonalNum(String personalNumber) {
        log.debug("Request to get EmployeeWorkingTime");
        List<TimeBooking> timeBookingList = timeBookingService.findEmployeeWorkingTimeByPersonalNum(personalNumber);
        List<EmployeeWorkingTime> employeeWorkingTimeList = new ArrayList<>();
        if(!timeBookingList.isEmpty()){
            timeBookingList.stream().forEach(timeBooking -> {
                EmployeeWorkingTime employeeWorkingTime = new EmployeeWorkingTime();
                // Get the number of days between start and end, if end is after start,
                // returns a positive number, otherwise returns a negative number
                Long workingMinutesLong = timeBooking.getBooking().until(LocalDateTime.now(), ChronoUnit.MINUTES);
                // Convert from minutes to hours with two decimal places
                Double workingMinutesDouble = new BigDecimal(((double)workingMinutesLong/60)).setScale(2,RoundingMode.HALF_UP).doubleValue();
                employeeWorkingTime.setWorkingTime(workingMinutesDouble);
                employeeWorkingTime.setPersonalNumber(timeBooking.getPersonalNumber());
                employeeWorkingTimeList.add(employeeWorkingTime);
            });
        }
        return employeeWorkingTimeList;
    }
}
