package cn.edu.bupt.cac.mapper;

import cn.edu.bupt.cac.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM `user` WHERE room_id = #{roomNumber} AND id_number = #{idNumber}")
    User findByRoomNumberAndIdNumber(@Param("roomNumber") String roomNumber, @Param("idNumber") String idNumber);

    @Update("UPDATE `user` SET energy = #{energy}, cost = #{cost} WHERE room_id = #{roomId}")
    void updateEnergyAndCostByRoomId(@Param("roomId") String roomId, @Param("energy") BigDecimal energy, @Param("cost") BigDecimal cost);

    @Select("SELECT `count` FROM `user` WHERE room_id = #{roomId}")
    int getCountByRoomId(@Param("roomId") String roomId);

    @Update("UPDATE `user` SET `count` = `count` + 1 WHERE room_id = #{roomId}")
    void increaseCountByRoomId(@Param("roomId") String roomId);
}
