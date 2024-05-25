package cn.edu.bupt.cac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;

@Data
public class Report {
    @TableId(type= IdType.AUTO)
    private Long id; // 报告ID，自增主键
    private String type; // 报告类型（日报表/周报表/月报表）
    private Long roomId; // 房间ID
    private int SCcnt; // 对应从控机开关机次数
    private List<ReportItem> reportItems; // 请求列表
}
