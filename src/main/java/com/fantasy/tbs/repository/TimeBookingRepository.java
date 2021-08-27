package com.fantasy.tbs.repository;

import com.fantasy.tbs.domain.TimeBooking;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data SQL repository for the TimeBooking entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TimeBookingRepository extends JpaRepository<TimeBooking, Long> {

    @Query(value = "SELECT * FROM time_booking WHERE personal_number = ?1",nativeQuery = true)
    List<TimeBooking> findEmployeeWorkingTimeByPersonalNum(String personalNumber);
}
