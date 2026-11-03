package com.nexushub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("event_capacity")
public class EventCapacity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "event_id", type = IdType.INPUT)
    private Long eventId;
    private Integer remainingSlots;
    private LocalDateTime registrationStartTime;
    private LocalDateTime registrationEndTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
