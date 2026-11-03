package com.nexushub.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PlaceDTO {
    private Long id;
    private String name;
    private Long categoryId;
    private String images;
    private String area;
    private String address;
    private Double x;
    private Double y;
    private Long avgPrice;
    private Integer sold;
    private Integer comments;
    private Integer score;
    private String openHours;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
