package cn.edu.bupt.cac.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StateRequest {
    private String roomID; // 房间ID
    private BigDecimal temperature; // 房间温度
    private boolean isOn; // 空调是否开启
}
