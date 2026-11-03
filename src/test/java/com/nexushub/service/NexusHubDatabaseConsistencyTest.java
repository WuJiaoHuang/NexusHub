package com.nexushub.service;

import com.nexushub.common.BusinessException;
import com.nexushub.dto.Result;
import com.nexushub.entity.Place;
import com.nexushub.entity.EventCapacity;
import com.nexushub.entity.EventRegistration;
import com.nexushub.mapper.EventCapacityMapper;
import com.nexushub.mapper.EventRegistrationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@SqlConfig(encoding = "UTF-8")
@Sql(scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class NexusHubDatabaseConsistencyTest {

    @Autowired
    private IPlaceService placeService;
    @Autowired
    private IEventRegistrationTransactionalService registrationTransactionalService;
    @Autowired
    private EventCapacityMapper eventCapacityMapper;
    @Autowired
    private EventRegistrationMapper eventRegistrationMapper;

    @Test
    void placeQueryByCategoryShouldUseCategoryIdColumn() {
        Result result = placeService.queryPlaceByType(1, 1, null, null);

        assertThat(result.getSuccess()).isTrue();
        List<Place> places = (List<Place>) result.getData();
        assertThat(places).extracting(Place::getId).contains(1L);
        assertThat(places).allMatch(place -> Long.valueOf(1L).equals(place.getCategoryId()));
    }

    @Test
    void sameUserShouldNotRegisterSameEventTwiceOrConsumeExtraSlots() {
        EventCapacity before = eventCapacityMapper.selectById(1L);
        eventRegistrationMapper.insert(registration(9001L, 1L, 1L));

        registrationTransactionalService.createEventRegistration(registration(9002L, 1L, 1L));

        EventCapacity after = eventCapacityMapper.selectById(1L);
        assertThat(after.getRemainingSlots()).isEqualTo(before.getRemainingSlots());
    }

    @Test
    void zeroRemainingSlotsShouldRejectRegistrationAndRollbackRecord() {
        EventCapacity capacity = eventCapacityMapper.selectById(1L);
        capacity.setRemainingSlots(0);
        eventCapacityMapper.updateById(capacity);

        assertThatThrownBy(() -> registrationTransactionalService.createEventRegistration(registration(9003L, 2L, 1L)))
                .isInstanceOf(BusinessException.class);
        assertThat(eventRegistrationMapper.selectById(9003L)).isNull();
        assertThat(eventCapacityMapper.selectById(1L).getRemainingSlots()).isZero();
    }

    @Test
    void successfulRegistrationShouldDecreaseDatabaseSlots() {
        EventCapacity before = eventCapacityMapper.selectById(1L);

        registrationTransactionalService.createEventRegistration(registration(9004L, 2L, 1L));

        EventCapacity after = eventCapacityMapper.selectById(1L);
        assertThat(eventRegistrationMapper.selectById(9004L)).isNotNull();
        assertThat(after.getRemainingSlots()).isEqualTo(before.getRemainingSlots() - 1);
    }

    private EventRegistration registration(Long id, Long userId, Long eventId) {
        EventRegistration registration = new EventRegistration();
        registration.setId(id);
        registration.setUserId(userId);
        registration.setEventId(eventId);
        return registration;
    }
}
