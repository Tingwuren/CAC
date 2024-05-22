package cn.edu.bupt.cac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
// 费用类
public class RoomCost {
    @TableId(type= IdType.AUTO)
    private Long id; // 费用ID，自增主键
    private Long roomId; // 房间ID
    private double currentEnergy; // 当前消耗的能量
    private double currentCost; // 当前消耗的费用
}
