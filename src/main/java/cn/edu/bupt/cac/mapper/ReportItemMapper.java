package cn.edu.bupt.cac.mapper;

import cn.edu.bupt.cac.entity.ReportItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ReportItemMapper extends BaseMapper<ReportItem> {
    @Select("SELECT SUM(energy) FROM report_item WHERE room_id = #{roomId}")
    Double sumEnergyByRoomId(String roomId);

    @Select("SELECT SUM(cost) FROM report_item WHERE room_id = #{roomId}")
    Double sumCostByRoomId(String roomId);

    @Select("<script>"
            + "SELECT * FROM report_item WHERE room_id = #{roomId}"
            + "<if test='type == \"day\"'> AND PARSEDATETIME(CONCAT(YEAR(CURRENT_DATE()), '-', start_time), 'yyyy-MM-dd HH:mm:ss') >= TIMESTAMPADD(DAY, -1, CURRENT_TIMESTAMP())</if>"
            + "<if test='type == \"week\"'> AND PARSEDATETIME(CONCAT(YEAR(CURRENT_DATE()), '-', start_time), 'yyyy-MM-dd HH:mm:ss') >= TIMESTAMPADD(WEEK, -1, CURRENT_TIMESTAMP())</if>"
            + "<if test='type == \"month\"'> AND PARSEDATETIME(CONCAT(YEAR(CURRENT_DATE()), '-', start_time), 'yyyy-MM-dd HH:mm:ss') >= TIMESTAMPADD(MONTH, -1, CURRENT_TIMESTAMP())</if>"
            + "</script>")
    List<ReportItem> selectListByRoomIdAndType(@Param("roomId") String roomId, @Param("type") String type);
}
