package com.cityeats.search;

import lombok.Data;

@Data
public class ShopDocument {
    private Long id;
    private String name;
    private Long typeId;
    private String images;
    private String area;
    private String address;
    private String description;
    private Long avgPrice;
    private Integer score;
    private Integer sold;
    private Integer comments;
    private Double x;
    private Double y;
    private String openHours;
    private String createTime;
    private String updateTime;
}
