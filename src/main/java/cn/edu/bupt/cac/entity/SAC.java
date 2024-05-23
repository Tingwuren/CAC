package cn.edu.bupt.cac.entity;

import lombok.Data;

@Data
// 从控机类（Slave Air Conditioner）
public class SAC {
    private boolean isOn; // 空调是否开启
    private String mode; // 工作模式（制冷/供暖）
    private double defaultTemperature; // 默认温度
    private int targetTemperature; // 目标温度
    private String fanSpeed; // 风速（高/中/低）

    public Request getCurrentRequest() {
        return null;
    }
}
