package com.fantasy.tbs.service.impl;

import com.fantasy.tbs.domain.TimeBookDTO;
import com.fantasy.tbs.domain.TimeBooking;
import com.fantasy.tbs.repository.TimeBookingRepository;
import com.fantasy.tbs.service.TimeBookingService;
import com.fantasy.tbs.service.mapper.TimeBookMapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link TimeBooking}.
 */
@Service
@Transactional
public class TimeBookingServiceImpl implements TimeBookingService {

    private final Logger log = LoggerFactory.getLogger(TimeBookingServiceImpl.class);

    private final TimeBookingRepository timeBookingRepository;
    private final TimeBookMapper timeBookMapper;

    public TimeBookingServiceImpl(TimeBookingRepository timeBookingRepository, TimeBookMapper timeBookMapper) {
        this.timeBookingRepository = timeBookingRepository;
        this.timeBookMapper = timeBookMapper;
    }

    @Override
    public TimeBooking save(TimeBooking timeBooking) {
        log.debug("Request to save TimeBooking : {}", timeBooking);
        return timeBookingRepository.save(timeBooking);
    }

    @Override
    public Optional<TimeBooking> partialUpdate(TimeBooking timeBooking) {
        log.debug("Request to partially update TimeBooking : {}", timeBooking);

        return timeBookingRepository
            .findById(timeBooking.getId())
            .map(
                existingTimeBooking -> {
                    if (timeBooking.getBooking() != null) {
                        existingTimeBooking.setBooking(timeBooking.getBooking());
                    }
                    if (timeBooking.getPersonalNumber() != null) {
                        existingTimeBooking.setPersonalNumber(timeBooking.getPersonalNumber());
                    }

                    return existingTimeBooking;
                }
            )
            .map(timeBookingRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeBooking> findAll() {
        log.debug("Request to get all TimeBookings");
        return timeBookingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TimeBooking> findOne(Long id) {
        log.debug("Request to get TimeBooking : {}", id);
        return timeBookingRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete TimeBooking : {}", id);
        timeBookingRepository.deleteById(id);
    }

    @Override
    public void bookTime(TimeBookDTO timeBookDTO) {
        timeBookingRepository.save(timeBookMapper.toTimeBooking(timeBookDTO));
    }

    /**
     * Returns the time worked by an employee
     * @param personalId the employee whose time worked is to be checked
     * @return
     */
    @Override
    public long hoursWorkedByEmployee(final String personalId) {
        log.debug("Request to get hours worked by employee : {}", personalId);
        List<TimeBooking> bookings=timeBookingRepository.findAll();
        //TODO we could apply filtering in the persistence layer
        List<TimeBooking> bookingsForEmployee=bookings.stream().filter(booking->booking.getPersonalNumber().equals(personalId)).collect(
                Collectors.toList());
        //The bookings are grouped depending on the day they were made. The key is the day(LocalDate) and the value is a set with the bookings for that day
        Map<LocalDate, List<TimeBooking>> groupedBookings=getBookingsGroupedByDate(bookingsForEmployee);

        long result=0;
        for(LocalDate current:groupedBookings.keySet()){
            result+=calculateTimeWorkedEachDay(groupedBookings.get(current).stream().collect(Collectors.toList()), current);
        }

        return result;
    }

    /**
     * Here are two error conditions considered: One booking per day and more that two bookings per day
     * @param bookingsForGivenDate
     * @param localDate
     * @return
     */
    private long calculateTimeWorkedEachDay(  List<TimeBooking> bookingsForGivenDate, LocalDate localDate ){
        log.info("found "+bookingsForGivenDate.size()+ " for date "+localDate);
        if(bookingsForGivenDate.size()==1){
            throw new IllegalArgumentException("There is only one booking done for the date:" +localDate);
        }
        if(bookingsForGivenDate.size()>2){
            throw new IllegalArgumentException("There are more than two bookings made for the date" +localDate);
        }
        long result= ChronoUnit.MINUTES.between(bookingsForGivenDate.get(0).getBooking(),bookingsForGivenDate.get(1).getBooking());
        result=result>=0? result: result*-1;

        return result;
    }

    private Map<LocalDate, List<TimeBooking>> getBookingsGroupedByDate(final List<TimeBooking> bookingsForEmployee) {
        Map<LocalDate, List<TimeBooking>> groupedBookings=new HashMap();
        for(TimeBooking currentTimeBooking: bookingsForEmployee){
            LocalDate currentDate=currentTimeBooking.getBooking().toLocalDate();
            if(groupedBookings.get(currentDate)==null){
                groupedBookings.put(currentDate,new ArrayList<>());
            }
            groupedBookings.get(currentDate).add(currentTimeBooking);
        }
        return groupedBookings;
    }


}
