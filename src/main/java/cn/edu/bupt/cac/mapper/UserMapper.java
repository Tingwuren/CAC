package cn.edu.bupt.cac.mapper;

import cn.edu.bupt.cac.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM users WHERE room_number = #{roomNumber} AND id_number = #{idNumber}")
    User findByRoomNumberAndIdNumber(@Param("roomNumber") String roomNumber, @Param("idNumber") String idNumber);
}
