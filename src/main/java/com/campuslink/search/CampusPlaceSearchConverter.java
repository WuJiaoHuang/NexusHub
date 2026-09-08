package com.campuslink.search;

import com.campuslink.dto.CampusPlaceDTO;
import com.campuslink.entity.CampusPlace;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CampusPlaceSearchConverter {

    public CampusPlaceDocument toDocument(CampusPlace place) {
        if (place == null) {
            return null;
        }
        CampusPlaceDocument document = new CampusPlaceDocument();
        document.setId(place.getId());
        document.setName(place.getName());
        document.setCategoryId(place.getCategoryId());
        document.setImages(place.getImages());
        document.setArea(place.getArea());
        document.setAddress(place.getAddress());
        document.setDescription(buildDescription(place));
        document.setAvgPrice(place.getAvgPrice());
        document.setScore(place.getScore());
        document.setSold(place.getSold());
        document.setComments(place.getComments());
        document.setX(place.getX());
        document.setY(place.getY());
        document.setOpenHours(place.getOpenHours());
        document.setCreateTime(toText(place.getCreateTime()));
        document.setUpdateTime(toText(place.getUpdateTime()));
        return document;
    }

    public List<CampusPlaceDocument> toDocuments(List<CampusPlace> places) {
        if (places == null) {
            return Collections.emptyList();
        }
        return places.stream().map(this::toDocument).collect(Collectors.toList());
    }

    public CampusPlaceDTO toDto(CampusPlaceDocument document) {
        if (document == null) {
            return null;
        }
        CampusPlaceDTO dto = new CampusPlaceDTO();
        dto.setId(document.getId());
        dto.setName(document.getName());
        dto.setCategoryId(document.getCategoryId());
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

    private String buildDescription(CampusPlace place) {
        StringBuilder builder = new StringBuilder();
        append(builder, place.getName());
        append(builder, place.getArea());
        append(builder, place.getAddress());
        append(builder, place.getOpenHours());
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
