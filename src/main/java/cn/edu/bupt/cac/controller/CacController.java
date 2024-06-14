package cn.edu.bupt.cac.controller;

import cn.edu.bupt.cac.DTO.AuthRequest;
import cn.edu.bupt.cac.DTO.AuthResponse;
import cn.edu.bupt.cac.DTO.StateRequest;
import cn.edu.bupt.cac.DTO.StateResponse;
import cn.edu.bupt.cac.entity.*;
import cn.edu.bupt.cac.mapper.UserMapper;
import cn.edu.bupt.cac.service.CacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
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
        if (!CAC.getIsOn()) {
            throw new IllegalArgumentException("中央空调未开启");
        }
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

    // 实时计算每个房间所消耗的能量和支付金额（需求11）
    @PostMapping("/state")
    public StateResponse getState(@RequestBody StateRequest request) {

        String roomId = request.getRoomID();
        BigDecimal temperature = request.getTemperature();
        boolean on = request.isOn();
        cacService.setTemperatureByRoomId(roomId, temperature);
        cacService.setOnByRoomId(roomId, on);

        StateResponse response = new StateResponse();
        response.setFrequency(CAC.getFrequency());
        BigDecimal energy = cacService.getEnergyByRoomId(roomId);
        response.setEnergy(energy);
        BigDecimal cost = cacService.getCostByRoomId(roomId);
        response.setCost(cost);

        userMapper.updateEnergyAndCostByRoomId(roomId, energy, cost);

        System.out.println("收到状态请求：" + request + "，返回状态响应：" + response);
        return response;
    }

    // 配置刷新频率（需求8）
    @PostMapping("/setFrequency")
    public String setFrequency(@RequestBody Map<String, Integer> body) {
        if (!CAC.getIsOn()) {
            return "中央空调未开启";
        }
        int frequency = body.get("frequency");
        CAC.setFrequency(frequency);
        return "中央空调的刷新频率已设置为 " + frequency;
    }

    @GetMapping("/getFrequency")
    public Integer getFrequency() {
        if (!CAC.getIsOn()) {
            return 0;
        }
        int frequency = CAC.getFrequency();
        // System.out.println("中央空调的刷新频率为：" + frequency);
        return frequency;
    }

    // 获取各个房间的温度、开关机状态、服务状态（需求8）
    @GetMapping("/getRoomState")
    public List<RoomState> getRoomState() {
        if (!CAC.getIsOn()) {
            throw new IllegalArgumentException("中央空调未开启");
        }
        return cacService.getRoomState();
    }


    // 根据房间ID和报表类型（日、周、月）给出报表（需求12）
    @GetMapping("/getReport")
    public Report getReport(@RequestParam String roomId, @RequestParam String type) {
        if (!CAC.getIsOn()) {
            throw new IllegalArgumentException("中央空调未开启");
        }
        return cacService.getReportByRoomId(roomId, type);
    }
}
