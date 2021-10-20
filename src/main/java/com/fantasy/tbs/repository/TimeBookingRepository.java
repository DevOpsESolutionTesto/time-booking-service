package com.fantasy.tbs.repository;

import com.fantasy.tbs.domain.TimeBooking;
import com.fantasy.tbs.domain.Type;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data SQL repository for the TimeBooking entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TimeBookingRepository extends JpaRepository<TimeBooking, Long> {

    @Query("select t from TimeBooking t where t.booking < :booking and t.personalNumber = :personalNumber order by t.booking desc")
    Optional<TimeBooking> findLastWithBookingBeforeAndPersonalNumber(@Param("booking") ZonedDateTime booking,
                                                                     @Param("personalNumber") String personalNumber);

    @Query("select SUM(t.duration) from TimeBooking t where t.type = 'entrance' and t.personalNumber = :personalNumber")
    Optional<Integer> getTotalWorkingHoursBy(@Param("personalNumber") String personalNumber);

    @Query("select t.id from TimeBooking t where t.type = 'entrance' and t.personalNumber = :personalNumber and t.duration = 0 order by t.booking desc")
    List<Long> findAllEntrancesWithZeroDuration(@Param("personalNumber") String personalNumber);

}
