package com.fantasy.tbs.web.rest;

import com.fantasy.tbs.domain.EmployeeWorkingTime;
import com.fantasy.tbs.service.CaculatingWorkingTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Tao Summer
 * @description
 * @date 2021/8/27 1:43
 */

@RestController
@RequestMapping("/api/timeWorking")
public class TimeWorkingController {

    @Autowired
    CaculatingWorkingTimeService caculatingWorkingTimeService;

    @GetMapping(value = "publishCigStoreTaskExecute")
    public ResponseEntity<List<EmployeeWorkingTime>> publishCigStoreTaskExecute(String personalNumber) {
        return new ResponseEntity<>(caculatingWorkingTimeService.findEmployeeWorkingTimeByPersonalNum(personalNumber),HttpStatus.OK);
    }
}
