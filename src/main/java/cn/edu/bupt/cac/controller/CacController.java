package cn.edu.bupt.cac.controller;

import cn.edu.bupt.cac.DTO.AuthRequest;
import cn.edu.bupt.cac.DTO.AuthResponse;
import cn.edu.bupt.cac.entity.*;
import cn.edu.bupt.cac.mapper.UserMapper;
import cn.edu.bupt.cac.service.CacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/cac")
public class CacController {
    private final CacService cacService;
    @Autowired
    private UserMapper userMapper;
    public CacController(CacService cacService) {
        this.cacService = cacService;
    }
    // 开启中央空调
    @PostMapping(path = "/on", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> on() {
        cacService.turnOn();
        System.out.println("中央空调已开启");
        System.out.println("中央空调模式设置为：" + CAC.getMode());
        System.out.println("中央空调温度范围设置为：" + Arrays.toString(CAC.getTemperatureRange()));
        System.out.println("中央空调默认温度设置为：" + CAC.getDefaultTemperature());
        System.out.println("中央空调当前状态为：" + CAC.getStatus());
        return ResponseEntity.ok("{\"message\": \"中央空调已开启\"}");
    }

    // 关闭中央空调
    @PostMapping(path = "/off", consumes = "application/json", produces = "application/json")
    String off() {
        cacService.turnOff();
        return "中央空调已关闭";
    }

    // 设置中央空调工作模式
    @PostMapping(path = "/setMode", consumes = "application/json", produces = "application/json")
    public String setMode(@RequestBody Map<String, String> body) {
        if (!CAC.getIsOn()) {
            return "中央空调未开启";
        }
        String mode = body.get("mode");
        CAC.setMode(mode);
        return "中央空调的工作模式已设置为 " + mode;
    }

    @PostMapping("/auth")
    public AuthResponse handleAuth(@RequestBody AuthRequest request) {
        // 验证房间号和身份证号
        if (request.getRoomID().isEmpty() || request.getIdNumber().isEmpty()) {
            throw new IllegalArgumentException("房间号和身份证号不能为空");
        }

        User user = userMapper.findByRoomNumberAndIdNumber(request.getRoomID(), request.getIdNumber());
        if (user == null) {
            throw new IllegalArgumentException("房间号或身份证号无效");
        }
        // 获取工作模式、缺省工作温度、默认风速、默认频率
        String mode = CAC.getMode();
        int defaultTemperature = CAC.getDefaultTemperature();
        int frequency = CAC.getFrequency();
        String defaultFanSpeed = CAC.getDefaultFanSpeed();


        return new AuthResponse(mode, defaultTemperature, defaultFanSpeed, frequency);
    }

    @PostMapping("/request")
    public Response handleRequest(@RequestBody Request request) {
        System.out.println("收到请求：" + request);
        Response response = new Response();
        if (!CAC.getIsOn()) {
            System.out.println("中央空调未开启");
            response.setMessage("中央空调未开启");
            return response;
        }
        return cacService.handleRequest(request);
    }

    // Todo: 实时计算每个房间所消耗的能量和支付金额（需求11）

    // Todo: 根据房间ID和报表类型（日、周、月）给出报表（需求12）

}
