package cn.edu.bupt.cac.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
// 中央空调类（Central Air Conditioning）
public class CAC {
    private boolean isOn; // 中央空调是否开启
    private boolean status; // 中央空调状态（工作/待机）
    private String mode; // 工作模式（制冷/供暖）
    private float frequency = 1;  //刷新频率 例如 frequency = 2 代表每秒钟刷新2次 用于监测各房间的状态
    private int[] temperatureRange; // 温度范围
    private double defaultTemperature; // 对应工作模式的缺省温度值
    private List<Room> rooms; // 所有房间的列表
    private List<Request> currentRequests; // 当前正在处理请求列表

    public void turnOn() {
        this.isOn = true;
        this.setStatus(false); // 中央空调开机，默认状态为待机
        this.setMode("cooling"); // 中央空调开机，默认模式为制冷模式
    }

    public void turnOff() {
        this.isOn = false;
    }

    public void setMode(String mode) {
        this.mode = mode;
        if ("cooling".equals(mode)) {
            this.temperatureRange = new int[]{18, 25};
            this.defaultTemperature = 22;
        } else if ("heating".equals(mode)) {
            this.temperatureRange = new int[]{25, 30};
            this.defaultTemperature = 28;
        }
    }

    public void handleRequest(Request request) {
        if (!this.isOn) {
            // 中央空调关闭，不响应来自房间的任何温控请求
            return;
        }

        if (request.getType()) {
            // 有来自从控机的温控要求，中央空调开始工作
            this.status = true;
            // ... 处理温控请求 ...
        }

        // 检查是否所有房间都没有温控要求
        boolean allRoomsNoRequest = this.rooms.stream()
                .allMatch(room -> room.getSac().getCurrentRequest() == null || !room.getSac().getCurrentRequest().getType());

        if (allRoomsNoRequest) {
            // 所有房间都没有温控要求，中央空调的状态回到待机状态
            this.status = false;
        }
    }
}
