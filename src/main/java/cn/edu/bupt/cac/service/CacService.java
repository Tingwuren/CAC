package cn.edu.bupt.cac.service;

import cn.edu.bupt.cac.entity.Request;
import cn.edu.bupt.cac.entity.Response;

public interface CacService {
    void turnOn();
    void turnOff();
    Response handleRequest(Request request);
    Request getRequest(Request request);

}
