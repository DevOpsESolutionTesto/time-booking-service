package com.fantasy.tbs.web.rest;

import com.fantasy.tbs.domain.dto.EmployeeWorkingTimeDTO;
import com.fantasy.tbs.service.CaculatingWorkingTimeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timeWorking")
public class TimeWorkingController {

    private static final Logger log = LogManager.getLogger(TimeWorkingController.class);

    private final CaculatingWorkingTimeService caculatingWorkingTimeService;

    public TimeWorkingController(CaculatingWorkingTimeService caculatingWorkingTimeService) {
        this.caculatingWorkingTimeService = caculatingWorkingTimeService;
    }

    /**
     * {@code GET  /getWorkHoursByPersonalNumber/:personalNumber} : get how many hours an employee worked
     *
     * @param personalNumber the name of an employee
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of EmployeeWorkingTime+ in body.
     */
    @GetMapping(value = "/book/{personalNumber}/getWorkHoursByPersonalNumber")
    public ResponseEntity<EmployeeWorkingTimeDTO> getWorkHoursByPersonalNumber(@PathVariable String personalNumber) {
        log.debug("Received  calculating working time with input {}", personalNumber);
        final EmployeeWorkingTimeDTO employeeWorkingTime = caculatingWorkingTimeService.getWorkHoursByPersonalNumber(personalNumber);
        return ResponseEntity.ok(employeeWorkingTime);
    }
}
