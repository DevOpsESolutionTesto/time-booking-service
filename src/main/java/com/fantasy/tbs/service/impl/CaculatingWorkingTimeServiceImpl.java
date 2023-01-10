package com.fantasy.tbs.service.impl;

import com.fantasy.tbs.domain.TimeBooking;
import com.fantasy.tbs.domain.dto.EmployeeWorkingTimeDTO;
import com.fantasy.tbs.service.CaculatingWorkingTimeService;
import com.fantasy.tbs.service.TimeBookingService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for CaculatingWorkingTimeService {@link EmployeeWorkingTimeDTO}.
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
    @Transactional(readOnly = true)
    public EmployeeWorkingTimeDTO getWorkHoursByPersonalNumber(String personalNumber) {
        log.debug("Request to get EmployeeWorkingTime  {0}", personalNumber);
        List<TimeBooking> timeBookingListForEmployee = timeBookingService.getTimeBookingListByPersonalNumber(personalNumber);

        if (timeBookingListForEmployee.isEmpty()) {
            throw new IllegalArgumentException("The bookings made for the employee do not exist" + personalNumber);
        }
        // Get grouped list of employee working books by date
        Map<LocalDate, List<TimeBooking>> groupedBookingList = getGroupedBookingListByDate(timeBookingListForEmployee);
        // get total work hours for employee
        AtomicReference<Double> result = new AtomicReference<>(0.00);
        groupedBookingList
            .entrySet()
            .stream()
            .forEach(
                g -> {
                    LocalDate localDate = g.getKey();
                    List<TimeBooking> bookingList = g.getValue();
                    result.updateAndGet(v -> (v + caculateWorkingHoursPerDay(bookingList, localDate)));
                }
            );
        EmployeeWorkingTimeDTO employeeWorkingTime = new EmployeeWorkingTimeDTO();
        employeeWorkingTime.setPersonalNumber(personalNumber);
        employeeWorkingTime.setWorkingHours(result.get());

        return employeeWorkingTime;
    }

    private Map<LocalDate, List<TimeBooking>> getGroupedBookingListByDate(List<TimeBooking> timeBookingListForEmployee) {
        Map<LocalDate, List<TimeBooking>> groupedBookingList = new HashMap<>();
        timeBookingListForEmployee
            .stream()
            .forEach(
                t -> {
                    LocalDate currentDate = t.getBooking().toLocalDate();
                    if (!groupedBookingList.containsKey(currentDate)) {
                        groupedBookingList.put(currentDate, new ArrayList<>());
                    } else {
                        groupedBookingList.get(currentDate).add(t);
                    }
                }
            );
        return groupedBookingList;
    }

    private double caculateWorkingHoursPerDay(List<TimeBooking> timeBookingList, LocalDate localDate) {
        if (timeBookingList.size() != 2) {
            throw new IllegalArgumentException("The bookings made for the date " + localDate + "do not match");
        }
        return calculateDuration(timeBookingList.get(0), timeBookingList.get(1));
    }

    public static double calculateDuration(TimeBooking start, TimeBooking end) {
        Duration duration = Duration.between(start.getBooking(), end.getBooking());
        return toHours(duration);
    }

    public static double toHours(Duration duration) {
        return (new BigDecimal(duration.getSeconds() / 3600)).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
