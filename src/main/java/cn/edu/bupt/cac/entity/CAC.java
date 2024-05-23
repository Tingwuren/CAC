package cn.edu.bupt.cac.entity;

import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Component;
import java.util.*;

@Data
@Component
// 中央空调类（Central Air Conditioning）
public class CAC {
    private static boolean isOn; // 中央空调是否开启
    private static boolean status; // 中央空调状态（工作/待机）
    @Getter
    private static String mode; // 工作模式（制冷/供暖）
    private static float frequency = 1;  //刷新频率 例如 frequency = 2 代表每秒钟刷新2次 用于监测各房间的状态
    @Getter
    private static int[] temperatureRange; // 温度范围
    @Getter
    private static double defaultTemperature; // 对应工作模式的缺省温度值
    @Getter
    private static List<Room> rooms; // 所有房间的列表
    @Getter
    private static List<Request> currentRequests; // 当前正在处理服务列表，最多3个
    @Getter
    private static List<Request> waitingRequests; // 等待处理的请求列表，无上限

    public CAC() {
        currentRequests = new ArrayList<>();
        waitingRequests = new ArrayList<>();
        rooms = new ArrayList<>();
    }

    public static void setStatus(boolean status) {
        CAC.status = status;
    }

    public static void setMode(String mode) {
        CAC.mode = mode;
        if ("cooling".equals(mode)) {
            temperatureRange = new int[]{18, 25};
            defaultTemperature = 22;
        } else if ("heating".equals(mode)) {
            temperatureRange = new int[]{25, 30};
            defaultTemperature = 28;
        }
    }

    public static boolean getIsOn() {
        return isOn;
    }

    public static void setIsOn(boolean b) {
        isOn = b;
    }

}
