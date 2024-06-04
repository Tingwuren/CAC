package cn.edu.bupt.cac.service;

import cn.edu.bupt.cac.entity.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CacService {
    BigDecimal getEnergyByRoomId(String roomId);

    void turnOn();
    void turnOff();
    Response handleRequest(Request request);

    BigDecimal getCostByRoomId(String roomId);

    void setTemperatureByRoomId(String roomId, BigDecimal temperature);

    void setOnByRoomId(String roomId, boolean on);

    BigDecimal getTemperatureByRoomId(String roomId);

    Report getReportByRoomId(String roomId, String type);
    
    void setStateByRoomId(String roomId, String state);

    List<RoomState> getRoomState();
}
