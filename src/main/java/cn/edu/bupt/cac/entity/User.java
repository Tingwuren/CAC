package cn.edu.bupt.cac.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("`user`")
// 用户类
public class User {
    private String roomId; // 房间号
    private String idNumber; // 身份证号

    public String getRoomID() {
        return roomId;
    }
}
