package com.campuslink.search;

import com.campuslink.dto.CampusPlaceDTO;
import com.campuslink.entity.CampusPlace;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CampusPlaceSearchConverterTest {

    @Test
    void toDocumentShouldCopySearchFields() {
        CampusPlace place = new CampusPlace()
                .setId(1L)
                .setName("老上海烧烤")
                .setCategoryId(2L)
                .setArea("浦东")
                .setAddress("世纪大道 100 号")
                .setAvgPrice(88L)
                .setScore(45)
                .setSold(120)
                .setCreateTime(LocalDateTime.of(2026, 8, 30, 10, 0));

        CampusPlaceDocument document = new CampusPlaceSearchConverter().toDocument(place);

        assertThat(document.getId()).isEqualTo(1L);
        assertThat(document.getName()).isEqualTo("老上海烧烤");
        assertThat(document.getCategoryId()).isEqualTo(2L);
        assertThat(document.getDescription()).contains("老上海烧烤", "浦东", "世纪大道");
        assertThat(document.getCreateTime()).isEqualTo("2026-08-30T10:00");
    }

    @Test
    void toDtoShouldHideElasticsearchDocumentShape() {
        CampusPlaceDocument document = new CampusPlaceDocument();
        document.setId(2L);
        document.setName("人民广场咖啡");
        document.setCategoryId(3L);
        document.setArea("黄浦");
        document.setUpdateTime("2026-08-30T11:00");

        CampusPlaceDTO dto = new CampusPlaceSearchConverter().toDto(document);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getName()).isEqualTo("人民广场咖啡");
        assertThat(dto.getUpdateTime()).isEqualTo(LocalDateTime.of(2026, 8, 30, 11, 0));
    }
}
