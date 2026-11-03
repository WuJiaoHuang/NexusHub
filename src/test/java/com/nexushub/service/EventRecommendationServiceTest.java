package com.nexushub.service;

import com.nexushub.dto.Result;
import com.nexushub.dto.UserDTO;
import com.nexushub.entity.Event;
import com.nexushub.entity.EventCapacity;
import com.nexushub.service.impl.EventRecommendationServiceImpl;
import com.nexushub.utils.UserHolder;
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
        IEventService eventService = mock(IEventService.class);
        IEventCapacityService eventCapacityService = mock(IEventCapacityService.class);
        IUserInterestService userInterestService = mock(IUserInterestService.class);
        EventRecommendationServiceImpl service = new EventRecommendationServiceImpl();
        ReflectionTestUtils.setField(service, "eventService", eventService);
        ReflectionTestUtils.setField(service, "eventCapacityService", eventCapacityService);
        ReflectionTestUtils.setField(service, "userInterestService", userInterestService);

        UserDTO user = new UserDTO();
        user.setId(1L);
        UserHolder.saveUser(user);
        Event javaEvent = event(1L, "Java 后端技术交流会", "Java,后端", 100, LocalDateTime.now().minusHours(2));
        Event photoEvent = event(2L, "城市摄影交流活动", "摄影", 100, LocalDateTime.now().minusHours(1));
        Event basketballEvent = event(3L, "城市周末篮球活动", "篮球,体育", 100, LocalDateTime.now().minusDays(1));
        when(eventService.list()).thenReturn(Arrays.asList(photoEvent, basketballEvent, javaEvent));
        when(eventCapacityService.list()).thenReturn(Arrays.asList(
                capacity(1L, 90),
                capacity(2L, 10),
                capacity(3L, 95)
        ));
        when(userInterestService.listTagsByUserId(1L)).thenReturn(Arrays.asList("Java"));

        Result result = service.recommend(3);

        assertThat(result.getSuccess()).isTrue();
        List<Event> events = (List<Event>) result.getData();
        assertThat(events).extracting(Event::getTitle)
                .containsExactly("Java 后端技术交流会", "城市摄影交流活动", "城市周末篮球活动");
    }

    private Event event(Long id, String title, String tags, Integer capacity, LocalDateTime registrationStartTime) {
        return new Event()
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
