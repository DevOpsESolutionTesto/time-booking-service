package com.fantasy.tbs.web.rest;

import static com.fantasy.tbs.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fantasy.tbs.IntegrationTest;
import com.fantasy.tbs.domain.TimeBooking;
import com.fantasy.tbs.repository.TimeBookingRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TimeBookingResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TimeBookingResourceIT {

    private static final ZonedDateTime DEFAULT_BOOKING = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_BOOKING = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_PERSONAL_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_PERSONAL_NUMBER = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/time-bookings";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TimeBookingRepository timeBookingRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTimeBookingMockMvc;

    private TimeBooking timeBooking;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TimeBooking createEntity(EntityManager em) {
        TimeBooking timeBooking = new TimeBooking().booking(DEFAULT_BOOKING).personalNumber(DEFAULT_PERSONAL_NUMBER);
        return timeBooking;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TimeBooking createUpdatedEntity(EntityManager em) {
        TimeBooking timeBooking = new TimeBooking().booking(UPDATED_BOOKING).personalNumber(UPDATED_PERSONAL_NUMBER);
        return timeBooking;
    }

    @BeforeEach
    public void initTest() {
        timeBooking = createEntity(em);
    }

    @Test
    @Transactional
    void createTimeBooking() throws Exception {
        int databaseSizeBeforeCreate = timeBookingRepository.findAll().size();
        // Create the TimeBooking
        restTimeBookingMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(timeBooking)))
            .andExpect(status().isCreated());

        // Validate the TimeBooking in the database
        List<TimeBooking> timeBookingList = timeBookingRepository.findAll();
        assertThat(timeBookingList).hasSize(databaseSizeBeforeCreate + 1);
        TimeBooking testTimeBooking = timeBookingList.get(timeBookingList.size() - 1);
        assertThat(testTimeBooking.getBooking()).isEqualTo(DEFAULT_BOOKING);
        assertThat(testTimeBooking.getPersonalNumber()).isEqualTo(DEFAULT_PERSONAL_NUMBER);
    }

    @Test
    @Transactional
    void createTimeBookingWithExistingId() throws Exception {
        // Create the TimeBooking with an existing ID
        timeBooking.setId(1L);

        int databaseSizeBeforeCreate = timeBookingRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTimeBookingMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(timeBooking)))
            .andExpect(status().isBadRequest());

        // Validate the TimeBooking in the database
        List<TimeBooking> timeBookingList = timeBookingRepository.findAll();
        assertThat(timeBookingList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllTimeBookings() throws Exception {
        // Initialize the database
        timeBookingRepository.saveAndFlush(timeBooking);

        // Get all the timeBookingList
        restTimeBookingMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(timeBooking.getId().intValue())))
            .andExpect(jsonPath("$.[*].booking").value(hasItem(sameInstant(DEFAULT_BOOKING))))
            .andExpect(jsonPath("$.[*].personalNumber").value(hasItem(DEFAULT_PERSONAL_NUMBER)));
    }

    @Test
    @Transactional
    void getTimeBooking() throws Exception {
        // Initialize the database
        timeBookingRepository.saveAndFlush(timeBooking);

        // Get the timeBooking
        restTimeBookingMockMvc
            .perform(get(ENTITY_API_URL_ID, timeBooking.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(timeBooking.getId().intValue()))
            .andExpect(jsonPath("$.booking").value(sameInstant(DEFAULT_BOOKING)))
            .andExpect(jsonPath("$.personalNumber").value(DEFAULT_PERSONAL_NUMBER));
    }

    @Test
    @Transactional
    void getNonExistingTimeBooking() throws Exception {
        // Get the timeBooking
        restTimeBookingMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewTimeBooking() throws Exception {
        // Initialize the database
        timeBookingRepository.saveAndFlush(timeBooking);

        int databaseSizeBeforeUpdate = timeBookingRepository.findAll().size();

        // Update the timeBooking
        TimeBooking updatedTimeBooking = timeBookingRepository.findById(timeBooking.getId()).get();
        // Disconnect from session so that the updates on updatedTimeBooking are not directly saved in db
        em.detach(updatedTimeBooking);
        updatedTimeBooking.booking(UPDATED_BOOKING).personalNumber(UPDATED_PERSONAL_NUMBER);

        restTimeBookingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedTimeBooking.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedTimeBooking))
            )
            .andExpect(status().isOk());

        // Validate the TimeBooking in the database
        List<TimeBooking> timeBookingList = timeBookingRepository.findAll();
        assertThat(timeBookingList).hasSize(databaseSizeBeforeUpdate);
        TimeBooking testTimeBooking = timeBookingList.get(timeBookingList.size() - 1);
        assertThat(testTimeBooking.getBooking()).isEqualTo(UPDATED_BOOKING);
        assertThat(testTimeBooking.getPersonalNumber()).isEqualTo(UPDATED_PERSONAL_NUMBER);
    }

    @Test
    @Transactional
    void putNonExistingTimeBooking() throws Exception {
        int databaseSizeBeforeUpdate = timeBookingRepository.findAll().size();
        timeBooking.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTimeBookingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, timeBooking.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(timeBooking))
            )
            .andExpect(status().isBadRequest());

        // Validate the TimeBooking in the database
        List<TimeBooking> timeBookingList = timeBookingRepository.findAll();
        assertThat(timeBookingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTimeBooking() throws Exception {
        int databaseSizeBeforeUpdate = timeBookingRepository.findAll().size();
        timeBooking.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTimeBookingMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(timeBooking))
            )
            .andExpect(status().isBadRequest());

        // Validate the TimeBooking in the database
        List<TimeBooking> timeBookingList = timeBookingRepository.findAll();
        assertThat(timeBookingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTimeBooking() throws Exception {
        int databaseSizeBeforeUpdate = timeBookingRepository.findAll().size();
        timeBooking.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTimeBookingMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(timeBooking)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TimeBooking in the database
        List<TimeBooking> timeBookingList = timeBookingRepository.findAll();
        assertThat(timeBookingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTimeBookingWithPatch() throws Exception {
        // Initialize the database
        timeBookingRepository.saveAndFlush(timeBooking);

        int databaseSizeBeforeUpdate = timeBookingRepository.findAll().size();

        // Update the timeBooking using partial update
        TimeBooking partialUpdatedTimeBooking = new TimeBooking();
        partialUpdatedTimeBooking.setId(timeBooking.getId());

        partialUpdatedTimeBooking.personalNumber(UPDATED_PERSONAL_NUMBER);

        restTimeBookingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTimeBooking.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTimeBooking))
            )
            .andExpect(status().isOk());

        // Validate the TimeBooking in the database
        List<TimeBooking> timeBookingList = timeBookingRepository.findAll();
        assertThat(timeBookingList).hasSize(databaseSizeBeforeUpdate);
        TimeBooking testTimeBooking = timeBookingList.get(timeBookingList.size() - 1);
        assertThat(testTimeBooking.getBooking()).isEqualTo(DEFAULT_BOOKING);
        assertThat(testTimeBooking.getPersonalNumber()).isEqualTo(UPDATED_PERSONAL_NUMBER);
    }

    @Test
    @Transactional
    void fullUpdateTimeBookingWithPatch() throws Exception {
        // Initialize the database
        timeBookingRepository.saveAndFlush(timeBooking);

        int databaseSizeBeforeUpdate = timeBookingRepository.findAll().size();

        // Update the timeBooking using partial update
        TimeBooking partialUpdatedTimeBooking = new TimeBooking();
        partialUpdatedTimeBooking.setId(timeBooking.getId());

        partialUpdatedTimeBooking.booking(UPDATED_BOOKING).personalNumber(UPDATED_PERSONAL_NUMBER);

        restTimeBookingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTimeBooking.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTimeBooking))
            )
            .andExpect(status().isOk());

        // Validate the TimeBooking in the database
        List<TimeBooking> timeBookingList = timeBookingRepository.findAll();
        assertThat(timeBookingList).hasSize(databaseSizeBeforeUpdate);
        TimeBooking testTimeBooking = timeBookingList.get(timeBookingList.size() - 1);
        assertThat(testTimeBooking.getBooking()).isEqualTo(UPDATED_BOOKING);
        assertThat(testTimeBooking.getPersonalNumber()).isEqualTo(UPDATED_PERSONAL_NUMBER);
    }

    @Test
    @Transactional
    void patchNonExistingTimeBooking() throws Exception {
        int databaseSizeBeforeUpdate = timeBookingRepository.findAll().size();
        timeBooking.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTimeBookingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, timeBooking.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(timeBooking))
            )
            .andExpect(status().isBadRequest());

        // Validate the TimeBooking in the database
        List<TimeBooking> timeBookingList = timeBookingRepository.findAll();
        assertThat(timeBookingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTimeBooking() throws Exception {
        int databaseSizeBeforeUpdate = timeBookingRepository.findAll().size();
        timeBooking.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTimeBookingMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(timeBooking))
            )
            .andExpect(status().isBadRequest());

        // Validate the TimeBooking in the database
        List<TimeBooking> timeBookingList = timeBookingRepository.findAll();
        assertThat(timeBookingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTimeBooking() throws Exception {
        int databaseSizeBeforeUpdate = timeBookingRepository.findAll().size();
        timeBooking.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTimeBookingMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(timeBooking))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the TimeBooking in the database
        List<TimeBooking> timeBookingList = timeBookingRepository.findAll();
        assertThat(timeBookingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTimeBooking() throws Exception {
        // Initialize the database
        timeBookingRepository.saveAndFlush(timeBooking);

        int databaseSizeBeforeDelete = timeBookingRepository.findAll().size();

        // Delete the timeBooking
        restTimeBookingMockMvc
            .perform(delete(ENTITY_API_URL_ID, timeBooking.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<TimeBooking> timeBookingList = timeBookingRepository.findAll();
        assertThat(timeBookingList).hasSize(databaseSizeBeforeDelete - 1);
    }


    @Test
    @Transactional
    void given_entrance_and_exit_time_When_calculating_WH_Then_response_should_contain_expected_duration() throws Exception {
        // given entrance time
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        TimeBooking givenEntranceTime = new TimeBooking();
        givenEntranceTime.setBooking(zonedDateTime.minusMinutes(80)); //expected working time duration
        givenEntranceTime.setPersonalNumber("test");

        restTimeBookingMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(givenEntranceTime)))
            .andExpect(status().isCreated());

        // given exit time
        TimeBooking givenExitTime = new TimeBooking();
        givenExitTime.setBooking(zonedDateTime);
        givenExitTime.setPersonalNumber("test");

        restTimeBookingMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(givenExitTime)))
            .andExpect(status().isCreated());

        // calculating working time
        restTimeBookingMockMvc
            .perform(get("/api/book/{personalNumber}/workingTime", givenEntranceTime.getPersonalNumber()))
            .andExpect(status().isOk())
            //expected working time duration in response
            .andExpect(content().string("01:20"));

    }


    @Test
    @Transactional
    void given_booking_without_exit_booking_When_check_it_out_Then_response_should_contain_booking_id() throws Exception {
        // given entrance time
        TimeBooking givenEntranceTime = new TimeBooking();
        givenEntranceTime.setBooking( ZonedDateTime.now());
        givenEntranceTime.setPersonalNumber("test");

        restTimeBookingMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(givenEntranceTime)))
            .andExpect(status().isCreated());

        restTimeBookingMockMvc
            .perform(get("/api/book/{personalNumber}/missingBookings", givenEntranceTime.getPersonalNumber()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.length()").value(1));

    }

}
