package cn.edu.bupt.cac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;

@Data
// 请求类
public class ReportItem {
    @TableId(type= IdType.AUTO)
    private Long id; // 请求ID，自增主键
    private String roomId; // 请求房间ID
    private String state; // 请求状态（等待/处理中/已完成）
    private String fanSpeed; // 风速（高/中/低）
    private String duration; // 请求时长，等于结束时间减去开始时间
    private String startTime; // 开始时间
    private String endTime; // 结束时间
    private BigDecimal startTemp;
    private BigDecimal endTemp;
    private BigDecimal energy; // 消耗能量，与请求时长和风速有关
    private BigDecimal cost; // 支付费用，与消耗能量有关
}
