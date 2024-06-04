package cn.edu.bupt.cac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;

@Data
// 房间类
public class Room {
    @TableId(type= IdType.AUTO)
    private Long id; // 房间ID，自增主键
    private User user; // 房间用户
    private BigDecimal temperature; // 房间温度
    private double ambientTemperature; // 室外温度
    private SAC sac; // 房间内的从控机

    public SAC getSAC() {
        return sac;
    }

    public void setSAC(SAC sac) {
        this.sac = sac;
    }
}
