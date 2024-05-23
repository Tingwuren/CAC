package cn.edu.bupt.cac.entity;

import lombok.Data;

@Data
public class Response {
    private int id; // 响应ID，自增主键
    private Long requestID; // 请求ID
    private String message; // 返回信息
    private String state; // 请求状态（waiting/processing/finished）
}
