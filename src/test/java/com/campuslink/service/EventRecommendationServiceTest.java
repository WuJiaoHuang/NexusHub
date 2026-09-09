package com.campuslink.service;

import com.campuslink.dto.Result;
import com.campuslink.dto.UserDTO;
import com.campuslink.entity.CampusEvent;
import com.campuslink.entity.EventCapacity;
import com.campuslink.service.impl.EventRecommendationServiceImpl;
import com.campuslink.utils.UserHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventRecommendationServiceTest {

    @AfterEach
    void tearDown() {
        UserHolder.removeUser();
    }

    @Test
    void recommendShouldSortByInterestPopularityAndFreshnessScore() {
        ICampusEventService campusEventService = mock(ICampusEventService.class);
        IEventCapacityService eventCapacityService = mock(IEventCapacityService.class);
        IUserInterestService userInterestService = mock(IUserInterestService.class);
        EventRecommendationServiceImpl service = new EventRecommendationServiceImpl();
        ReflectionTestUtils.setField(service, "campusEventService", campusEventService);
        ReflectionTestUtils.setField(service, "eventCapacityService", eventCapacityService);
        ReflectionTestUtils.setField(service, "userInterestService", userInterestService);

        UserDTO user = new UserDTO();
        user.setId(1L);
        UserHolder.saveUser(user);
        CampusEvent javaEvent = event(1L, "Java 后端技术分享会", "Java,后端", 100, LocalDateTime.now().minusHours(2));
        CampusEvent photoEvent = event(2L, "摄影社秋季采风", "摄影", 100, LocalDateTime.now().minusHours(1));
        CampusEvent basketballEvent = event(3L, "校园篮球联赛", "篮球,体育", 100, LocalDateTime.now().minusDays(1));
        when(campusEventService.list()).thenReturn(Arrays.asList(photoEvent, basketballEvent, javaEvent));
        when(eventCapacityService.list()).thenReturn(Arrays.asList(
                capacity(1L, 90),
                capacity(2L, 10),
                capacity(3L, 95)
        ));
        when(userInterestService.listTagsByUserId(1L)).thenReturn(Arrays.asList("Java"));

        Result result = service.recommend(3);

        assertThat(result.getSuccess()).isTrue();
        List<CampusEvent> events = (List<CampusEvent>) result.getData();
        assertThat(events).extracting(CampusEvent::getTitle)
                .containsExactly("Java 后端技术分享会", "摄影社秋季采风", "校园篮球联赛");
    }

    private CampusEvent event(Long id, String title, String tags, Integer capacity, LocalDateTime registrationStartTime) {
        return new CampusEvent()
                .setId(id)
                .setTitle(title)
                .setTags(tags)
                .setCapacity(capacity)
                .setCreateTime(registrationStartTime)
                .setRegistrationStartTime(registrationStartTime);
    }

    private EventCapacity capacity(Long eventId, Integer remainingSlots) {
        return new EventCapacity()
                .setEventId(eventId)
                .setRemainingSlots(remainingSlots);
    }
}
