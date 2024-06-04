package cn.edu.bupt.cac.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomState {
    private String roomID; // 房间ID
    private BigDecimal temperature; // 房间温度
    private boolean isOn; // 从控机开关机状态
    private String state; // 从控机服务状态（waiting/processing/finished）
}
