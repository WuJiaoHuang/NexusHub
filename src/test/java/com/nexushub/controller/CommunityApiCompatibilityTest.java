package com.nexushub.controller;

import com.nexushub.search.PlaceSearchService;
import com.nexushub.dto.Result;
import com.nexushub.service.IEventRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.HashOperations;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(properties = "nexushub.security.admin-user-ids=1")
@AutoConfigureMockMvc
@SqlConfig(encoding = "UTF-8")
@Sql(scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"})
class CommunityApiCompatibilityTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @MockBean
    private PlaceSearchService placeSearchService;
    @MockBean
    private StringRedisTemplate redis;
    @MockBean
    private IEventRegistrationService registrationService;

    @BeforeEach
    void authenticateFromServerSideTokenData() {
        HashOperations<String, Object, Object> hashes = mock(HashOperations.class);
        when(redis.opsForHash()).thenReturn(hashes);
        when(hashes.entries("login:token:admin-token")).thenReturn(Collections.singletonMap("id", "1"));
        when(hashes.entries("login:token:member-token")).thenReturn(Collections.singletonMap("id", "2"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/events", "/places", "/admin/search/places/rebuild", "/places/", "/events;ignored=1"})
    void anonymousWritesAreRejected(String path) throws Exception {
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/events", "/places", "/admin/search/places/rebuild", "/places/", "/events;ignored=1"})
    void loggedInMemberCannotPerformAdministrativeWrites(String path) throws Exception {
        mvc.perform(post(path).header("authorization", "member-token")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updatesAndLegacyDeletionRequireAdministrativePermission() throws Exception {
        mvc.perform(put("/places").contentType(MediaType.APPLICATION_JSON).content("{\"id\":1}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/places").header("authorization", "member-token")
                .contentType(MediaType.APPLICATION_JSON).content("{\"id\":1}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/upload/posts/delete").param("name", "anything"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/upload/posts/delete").header("authorization", "member-token").param("name", "anything"))
                .andExpect(status().isForbidden());
    }

    @Test
    void memberCanRegisterButAnonymousRegistrationAndPrivateStatusAreRejected() throws Exception {
        when(registrationService.registerForEvent(1L)).thenReturn(Result.ok(99L));
        mvc.perform(post("/events/1/registrations")).andExpect(status().isUnauthorized());
        mvc.perform(get("/events/1/registrations/status")).andExpect(status().isUnauthorized());
        mvc.perform(post("/events/1/registrations").header("authorization", "member-token"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(99));
        verify(registrationService).registerForEvent(1L);
    }

    @Test
    void configuredAdminCanCreateEventsUpdatePlacesAndRebuildIndex() throws Exception {
        mvc.perform(post("/events").header("authorization", "admin-token")
                .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Admin event\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        mvc.perform(put("/places").header("authorization", "admin-token")
                .contentType(MediaType.APPLICATION_JSON).content("{\"id\":1,\"name\":\"Updated cafe\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        mvc.perform(post("/admin/search/places/rebuild").header("authorization", "admin-token"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        verify(placeSearchService).rebuild();
    }

    @Test
    void creatingPlaceSynchronizesSearchAfterDatabaseCommit() throws Exception {
        mvc.perform(post("/places").header("authorization", "admin-token").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Community cafe\",\"categoryId\":1}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));

        assertThat(jdbc.queryForObject("SELECT name FROM place WHERE id=5", String.class))
                .isEqualTo("Community cafe");
        verify(placeSearchService).saveOrUpdate(5L);
    }

    @Test
    void renamedEventKeepsRouteAndJsonFieldNames() throws Exception {
        mvc.perform(get("/events/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.placeId").value(4))
                .andExpect(jsonPath("$.data.title").value("Java 后端技术交流会"))
                .andExpect(jsonPath("$.data.capacity").value(100))
                .andExpect(jsonPath("$.data.registrationStartTime").exists());
    }

    @Test
    void renamedPlaceKeepsCategoryQueryAndJsonFieldNames() throws Exception {
        mvc.perform(get("/places/nearby").param("categoryId", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].categoryId").value(1))
                .andExpect(jsonPath("$.data[0].name").value("街角风味餐厅"))
                .andExpect(jsonPath("$.data[0].avgPrice").value(18));
    }

    @Test
    void demoRelationshipsAndCapacityWindowsAreConsistent() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM event", Integer.class)).isEqualTo(4);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM event e LEFT JOIN place p ON p.id=e.place_id "
                + "WHERE e.place_id IS NOT NULL AND p.id IS NULL", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM post p LEFT JOIN place l ON l.id=p.place_id "
                + "WHERE p.place_id IS NOT NULL AND l.id IS NULL", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM event e LEFT JOIN event_capacity c ON c.event_id=e.id "
                + "WHERE c.event_id IS NULL OR c.remaining_slots <> e.capacity "
                + "OR c.registration_start_time <> e.registration_start_time "
                + "OR c.registration_end_time <> e.registration_end_time "
                + "OR e.registration_start_time >= e.registration_end_time "
                + "OR e.registration_end_time >= e.start_time OR e.start_time >= e.end_time", Integer.class)).isZero();
    }
}
