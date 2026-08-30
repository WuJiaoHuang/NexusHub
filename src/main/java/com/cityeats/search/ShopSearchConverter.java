package com.cityeats.search;

import com.cityeats.dto.ShopDTO;
import com.cityeats.entity.Shop;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ShopSearchConverter {

    public ShopDocument toDocument(Shop shop) {
        if (shop == null) {
            return null;
        }
        ShopDocument document = new ShopDocument();
        document.setId(shop.getId());
        document.setName(shop.getName());
        document.setTypeId(shop.getTypeId());
        document.setImages(shop.getImages());
        document.setArea(shop.getArea());
        document.setAddress(shop.getAddress());
        document.setDescription(buildDescription(shop));
        document.setAvgPrice(shop.getAvgPrice());
        document.setScore(shop.getScore());
        document.setSold(shop.getSold());
        document.setComments(shop.getComments());
        document.setX(shop.getX());
        document.setY(shop.getY());
        document.setOpenHours(shop.getOpenHours());
        document.setCreateTime(toText(shop.getCreateTime()));
        document.setUpdateTime(toText(shop.getUpdateTime()));
        return document;
    }

    public List<ShopDocument> toDocuments(List<Shop> shops) {
        if (shops == null) {
            return Collections.emptyList();
        }
        return shops.stream().map(this::toDocument).collect(Collectors.toList());
    }

    public ShopDTO toDto(ShopDocument document) {
        if (document == null) {
            return null;
        }
        ShopDTO dto = new ShopDTO();
        dto.setId(document.getId());
        dto.setName(document.getName());
        dto.setTypeId(document.getTypeId());
        dto.setImages(document.getImages());
        dto.setArea(document.getArea());
        dto.setAddress(document.getAddress());
        dto.setX(document.getX());
        dto.setY(document.getY());
        dto.setAvgPrice(document.getAvgPrice());
        dto.setSold(document.getSold());
        dto.setComments(document.getComments());
        dto.setScore(document.getScore());
        dto.setOpenHours(document.getOpenHours());
        dto.setCreateTime(toTime(document.getCreateTime()));
        dto.setUpdateTime(toTime(document.getUpdateTime()));
        return dto;
    }

    private String buildDescription(Shop shop) {
        StringBuilder builder = new StringBuilder();
        append(builder, shop.getName());
        append(builder, shop.getArea());
        append(builder, shop.getAddress());
        append(builder, shop.getOpenHours());
        return builder.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(value);
    }

    private String toText(LocalDateTime time) {
        return time == null ? null : time.toString();
    }

    private LocalDateTime toTime(String text) {
        return text == null || text.trim().isEmpty() ? null : LocalDateTime.parse(text);
    }
}
