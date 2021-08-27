package com.fantasy.tbs.service;

import com.fantasy.tbs.domain.EmployeeWorkingTime;

import java.util.List;

public interface CaculatingWorkingTimeService {
    /**
     * @description: Get employee working time by personalNumber
     * @param: [personalNumber] personal number
     * @return: List<EmployeeWorkingTime></EmployeeWorkingTime>
     * @author: Tao Summer
     * @date: 2021/8/26 17:51
     */
    List<EmployeeWorkingTime> findEmployeeWorkingTimeByPersonalNum(String personalNumber);
}
