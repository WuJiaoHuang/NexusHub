package com.nexushub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nexushub.common.BusinessException;
import com.nexushub.entity.EventCapacity;
import com.nexushub.entity.EventRegistration;
import com.nexushub.mapper.EventCapacityMapper;
import com.nexushub.mapper.EventRegistrationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@SqlConfig(encoding = "UTF-8")
@Sql(scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"})
class RegistrationConcurrencyTest {
    @Autowired
    private IEventRegistrationTransactionalService service;
    @Autowired
    private EventCapacityMapper capacities;
    @Autowired
    private EventRegistrationMapper registrations;

    @Test
    void concurrentUsersCannotOversellAndFailedInsertsRollBack() throws Exception {
        setSlots(5);
        compete(false);

        assertThat(capacities.selectById(1L).getRemainingSlots()).isZero();
        assertThat(registrations.selectCount(new QueryWrapper<EventRegistration>().eq("event_id", 1L)))
                .isEqualTo(5);
    }

    @Test
    void concurrentRetriesOfSameUserConsumeOnlyOneSlot() throws Exception {
        setSlots(5);
        compete(true);

        assertThat(capacities.selectById(1L).getRemainingSlots()).isEqualTo(4);
        assertThat(registrations.selectCount(new QueryWrapper<EventRegistration>()
                .eq("event_id", 1L).eq("user_id", 500L))).isEqualTo(1);
    }

    private void setSlots(int slots) {
        EventCapacity capacity = capacities.selectById(1L);
        capacity.setRemainingSlots(slots);
        capacities.updateById(capacity);
    }

    private void compete(boolean sameUser) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < 24; i++) {
                final long index = i;
                futures.add(pool.submit(() -> {
                    start.await();
                    EventRegistration registration = new EventRegistration();
                    registration.setId(20000L + index);
                    registration.setEventId(1L);
                    registration.setUserId(sameUser ? 500L : 500L + index);
                    try {
                        service.createEventRegistration(registration);
                    } catch (BusinessException | DuplicateKeyException expectedRejection) {
                        // Capacity exhaustion or the unique user/event constraint rejects a contender.
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
