package com.fantasy.tbs.service.impl;

import com.fantasy.tbs.domain.TimeBookDTO;
import com.fantasy.tbs.domain.TimeBooking;
import com.fantasy.tbs.domain.Type;
import com.fantasy.tbs.repository.TimeBookingRepository;
import com.fantasy.tbs.service.TimeBookingService;
import com.fantasy.tbs.service.mapper.TimeBookMapper;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.google.common.primitives.Ints;
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

    private final CancelTimeBookingAction cancelTimeBookingAction;
    private final AssignTimeBookingAction assignTimeBookingAction;
    private final TimeBookingRepository timeBookingRepository;
    private final TimeBookMapper timeBookMapper;

    public TimeBookingServiceImpl(TimeBookingRepository timeBookingRepository, TimeBookMapper timeBookMapper) {
        this.timeBookingRepository = timeBookingRepository;
        this.timeBookMapper = timeBookMapper;
        this.cancelTimeBookingAction = new CancelTimeBookingAction(timeBookingRepository);
        this.assignTimeBookingAction = new AssignTimeBookingAction(timeBookingRepository);
    }

    @Override
    public TimeBooking save(TimeBooking timeBooking) {
        log.debug("Request to save TimeBooking : {}", timeBooking);

        return assignTimeBookingAction
            .andThen(timeBookingRepository::save)
            .apply(timeBooking);
    }

    @Override
    public Optional<TimeBooking> partialUpdate(TimeBooking timeBooking) {
        log.debug("Request to partially update TimeBooking : {}", timeBooking);

        return timeBookingRepository
            .findById(timeBooking.getId())
            .map(cancelTimeBookingAction)
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
            .map(assignTimeBookingAction)
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
        timeBookingRepository.findById(id)
            .map(cancelTimeBookingAction)
            .ifPresent(timeBookingRepository::delete);
    }

    @Override
    public void bookTime(TimeBookDTO timeBookDTO) {
        save(timeBookMapper.toTimeBooking(timeBookDTO));
    }

    @Override
    public int getTotalWorkingHoursForPersonalNumber(String personalNumber) {
        return timeBookingRepository.getTotalWorkingHoursBy(personalNumber).orElse(0);
    }

    @Override
    public List<Long> findPotentialMissingBookingIds(String personalNumber) {
        return timeBookingRepository.findAllEntrancesWithZeroDuration(personalNumber);
    }


    public static Duration calculateDuration(TimeBooking left, TimeBooking right){
        return Duration.between(left.getBooking(), right.getBooking());
    }

    public static int toMinutes(Duration duration){
        return Ints.checkedCast(duration.getSeconds() / 60);
    }


    interface Action extends UnaryOperator<TimeBooking>{

    }

    static class CancelTimeBookingAction implements Type.Visitor<TimeBooking, TimeBooking>, Action{

        private final TimeBookingRepository timeBookingRepository;

        CancelTimeBookingAction(TimeBookingRepository timeBookingRepository) {
            this.timeBookingRepository = timeBookingRepository;
        }

        @Override
        public TimeBooking visitEntrance(TimeBooking arg) {
            return arg;
        }

        @Override
        public TimeBooking visitExit(TimeBooking arg) {
            timeBookingRepository.findLastWithBookingBeforeAndPersonalNumber(arg.getBooking(), arg.getPersonalNumber())
                .ifPresent(previousEntrance -> previousEntrance.setDuration(0));

            return arg;
        }

        @Override
        public TimeBooking apply(TimeBooking timeBooking) {
            return detectTypeOf(timeBooking).accept(timeBooking, this);
        }

        private Type detectTypeOf(TimeBooking timeBooking){
            return timeBooking.getType() == Type.exit ? Type.exit : Type.entrance;
        }
    }

    static class AssignTimeBookingAction implements Type.Visitor<TimeBooking, TimeBooking>, Action{

        private final TimeBookingRepository timeBookingRepository;

        AssignTimeBookingAction(TimeBookingRepository timeBookingRepository) {
            this.timeBookingRepository = timeBookingRepository;
        }

        @Override
        public TimeBooking visitEntrance(TimeBooking arg) {
            arg.setType(Type.entrance);
            arg.setDuration(0);
            return arg;
        }

        @Override
        public TimeBooking visitExit(TimeBooking arg) {
            arg.setType(Type.exit);
            arg.setDuration(0);

            timeBookingRepository
                .findLastWithBookingBeforeAndPersonalNumber(arg.getBooking(), arg.getPersonalNumber())
                .ifPresent(previousEntrance -> {
                        Duration duration = calculateDuration(previousEntrance, arg);
                        previousEntrance.setDuration(toMinutes(duration));
                    });

            return arg;
        }

        @Override
        public TimeBooking apply(TimeBooking timeBooking) {
            Type detectedType = detectTypeOf(timeBooking);

            return detectedType.accept(timeBooking, this);
        }

        private Type detectTypeOf(TimeBooking timeBooking){
            return timeBookingRepository
                .findLastWithBookingBeforeAndPersonalNumber(timeBooking.getBooking(), timeBooking.getPersonalNumber())
                .map(previousTimeBooking -> previousTimeBooking.getType() == Type.exit ? Type.entrance : Type.exit)
                .orElse(Type.entrance);
        }

    }
}
