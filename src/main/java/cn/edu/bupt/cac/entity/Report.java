package cn.edu.bupt.cac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Report {
    private String type; // 报告类型（日报表/周报表/月报表）
    private String roomId; // 房间ID
    private int count; // 对应从控机开关机次数
    private BigDecimal totalCost; // 总费用
    private List<ReportItem> reportItems; // 请求列表
}
