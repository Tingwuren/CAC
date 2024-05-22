package cn.edu.bupt.cac.DTO;

import lombok.Data;

@Data
public class AuthResponse {
    private String mode;
    private double defaultTemperature;

    public AuthResponse(String mode, double defaultTemperature) {
        this.mode = mode;
        this.defaultTemperature = defaultTemperature;
    }
}
