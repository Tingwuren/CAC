package cn.edu.bupt.cac.DTO;

import lombok.Data;

@Data
public class AuthResponse {
    private String mode; // 工作模式
    private int defaultTemperature; // 默认温度
    private String defaultFanSpeed; // 默认风速
    private int defaultFrequency; // 默认频率

    public AuthResponse(String mode, int defaultTemperature, String defaultFanSpeed, int defaultFrequency) {
        this.mode = mode;
        this.defaultTemperature = defaultTemperature;
        this.defaultFanSpeed = defaultFanSpeed;
        this.defaultFrequency = defaultFrequency;
    }
}
