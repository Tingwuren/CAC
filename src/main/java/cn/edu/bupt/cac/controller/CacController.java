package cn.edu.bupt.cac.controller;

import cn.edu.bupt.cac.DTO.AuthRequest;
import cn.edu.bupt.cac.DTO.AuthResponse;
import cn.edu.bupt.cac.entity.CAC;
import cn.edu.bupt.cac.entity.User;
import cn.edu.bupt.cac.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/cac")
public class CacController {
    private final CAC cac;
    @Autowired
    private UserMapper userMapper;
    public CacController(CAC cac) {
        this.cac = cac;
    }
    // 开启中央空调
    @PostMapping(path = "/on", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> on() {
        cac.turnOn();
        return ResponseEntity.ok("{\"message\": \"中央空调已开启\"}");
    }

    // 关闭中央空调
    @PostMapping(path = "/off", consumes = "application/json", produces = "application/json")
    String off() {
        cac.turnOff();
        return "中央空调已关闭";
    }

    // 设置中央空调工作模式
    @PostMapping(path = "/setMode", consumes = "application/json", produces = "application/json")
    public String setMode(@RequestBody Map<String, String> body) {
        if (!cac.isOn()) {
            return "中央空调未开启";
        }
        String mode = body.get("mode");
        cac.setMode(mode);
        return "中央空调的工作模式已设置为 " + mode;
    }

    @PostMapping("/auth")
    public AuthResponse auth(@RequestBody AuthRequest request) {
        // 验证房间号和身份证号
        if (request.getRoomNumber().isEmpty() || request.getIdNumber().isEmpty()) {
            throw new IllegalArgumentException("房间号和身份证号不能为空");
        }

        User user = userMapper.findByRoomNumberAndIdNumber(request.getRoomNumber(), request.getIdNumber());
        if (user == null) {
            throw new IllegalArgumentException("房间号或身份证号无效");
        }
        // 获取工作模式和缺省工作温度
        String mode = cac.getMode();
        double defaultTemperature = cac.getDefaultTemperature();

        return new AuthResponse(mode, defaultTemperature);
    }
}
