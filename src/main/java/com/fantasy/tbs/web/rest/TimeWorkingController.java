package com.fantasy.tbs.web.rest;

import com.fantasy.tbs.domain.EmployeeWorkingTime;
import com.fantasy.tbs.service.CaculatingWorkingTimeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Tao Summer
 * @description
 * @date 2021/8/27 1:43
 */

@RestController
@RequestMapping("/api/timeWorking")
public class TimeWorkingController {

    private static final Logger log = LogManager.getLogger(TimeWorkingController.class);

    @Autowired
    CaculatingWorkingTimeService caculatingWorkingTimeService;

    /**
     * {@code GET  /publishCigStoreTaskExecute/:personalNumber} : get how many hours an employee worked
     *
     * @param personalNumber the name of an employee
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of EmployeeWorkingTime+ in body.
     */
    @GetMapping(value = "publishCigStoreTaskExecute")
    public ResponseEntity<List<EmployeeWorkingTime>> publishCigStoreTaskExecute(@PathVariable String personalNumber) {
        log.debug("Received  calculating working time with input {}", personalNumber);
        final List<EmployeeWorkingTime> employeeWorkingTimeList = caculatingWorkingTimeService.findEmployeeWorkingTimeByPersonalNum(personalNumber);
        final ResponseEntity<List<EmployeeWorkingTime>> response;
        if (!employeeWorkingTimeList.isEmpty()) {
            response = new ResponseEntity<>(employeeWorkingTimeList, HttpStatus.OK);
        } else {
            response = new ResponseEntity<>(HttpStatus.OK);
        }
        return response;
    }
}
