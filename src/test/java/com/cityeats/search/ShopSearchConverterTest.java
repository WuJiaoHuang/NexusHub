package com.cityeats.search;

import com.cityeats.dto.ShopDTO;
import com.cityeats.entity.Shop;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ShopSearchConverterTest {

    @Test
    void toDocumentShouldCopySearchFields() {
        Shop shop = new Shop()
                .setId(1L)
                .setName("老上海烧烤")
                .setTypeId(2L)
                .setArea("浦东")
                .setAddress("世纪大道 100 号")
                .setAvgPrice(88L)
                .setScore(45)
                .setSold(120)
                .setCreateTime(LocalDateTime.of(2026, 8, 30, 10, 0));

        ShopDocument document = new ShopSearchConverter().toDocument(shop);

        assertThat(document.getId()).isEqualTo(1L);
        assertThat(document.getName()).isEqualTo("老上海烧烤");
        assertThat(document.getTypeId()).isEqualTo(2L);
        assertThat(document.getDescription()).contains("老上海烧烤", "浦东", "世纪大道");
        assertThat(document.getCreateTime()).isEqualTo("2026-08-30T10:00");
    }

    @Test
    void toDtoShouldHideElasticsearchDocumentShape() {
        ShopDocument document = new ShopDocument();
        document.setId(2L);
        document.setName("人民广场咖啡");
        document.setTypeId(3L);
        document.setArea("黄浦");
        document.setUpdateTime("2026-08-30T11:00");

        ShopDTO dto = new ShopSearchConverter().toDto(document);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getName()).isEqualTo("人民广场咖啡");
        assertThat(dto.getUpdateTime()).isEqualTo(LocalDateTime.of(2026, 8, 30, 11, 0));
    }
}
