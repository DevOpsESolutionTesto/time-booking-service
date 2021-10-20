package com.fantasy.tbs.web.rest;

import com.fantasy.tbs.domain.TimeBookDTO;
import com.fantasy.tbs.service.impl.TimeBookingServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TimeBookingController {

    private final TimeBookingServiceImpl timeBookingService;

    public TimeBookingController(TimeBookingServiceImpl timeBookingService) {
        this.timeBookingService = timeBookingService;
    }

    @PostMapping("/book")
    public ResponseEntity<Void> addTimeBooking(@RequestBody TimeBookDTO timeBookDTO) {
        timeBookingService.bookTime(timeBookDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/book/{personalNumber}/workingTime")
    public ResponseEntity<String> getWorkingTime(@PathVariable String personalNumber) {
        int workingMinutes = timeBookingService.getTotalWorkingHoursForPersonalNumber(personalNumber);
        //07:40
        String outputFormat = LocalTime.MIN
            .plus(Duration.ofMinutes(workingMinutes))
            .toString();

        return ResponseEntity.ok(outputFormat);
    }

    @GetMapping("/book/{personalNumber}/missingBookings")
    public List<Long> getPotentialMissingBookingIds(@PathVariable String personalNumber) {
        return timeBookingService.findPotentialMissingBookingIds(personalNumber);
    }
}
