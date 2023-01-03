package com.fantasy.tbs.repository;

import com.fantasy.tbs.domain.TimeBooking;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the TimeBooking entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TimeBookingRepository extends JpaRepository<TimeBooking, Long> {
    @Query(value = "SELECT t FROM TimeBooking  t WHERE t.personalNumber  = :personalNumber order by t.booking asc", nativeQuery = true)
    List<TimeBooking> getTimeBookingListByPersonalNumber(@Param("personalNumber") String personalNumber);
}
