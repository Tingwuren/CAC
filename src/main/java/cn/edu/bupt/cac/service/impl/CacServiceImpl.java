package cn.edu.bupt.cac.service.impl;

import cn.edu.bupt.cac.entity.ReportItem;
import cn.edu.bupt.cac.entity.Request;
import cn.edu.bupt.cac.entity.Response;
import cn.edu.bupt.cac.mapper.ReportItemMapper;
import cn.edu.bupt.cac.service.CacService;
import org.springframework.stereotype.Service;
import cn.edu.bupt.cac.entity.CAC;
import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Objects;

@Service
public class CacServiceImpl implements CacService {
    @Resource
    private ReportItemMapper reportItemMapper;

    @Override
    public void turnOn() {
        CAC.setIsOn(true);
        CAC.setStatus(false); // 中央空调开机，默认状态为待机

        // 获取当前的月份
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;

        // 根据月份设置工作模式
        if (month >= 6 && month <= 8) {
            CAC.setMode("cooling"); // 如果是6月到8月，设置为制冷模式
        } else if (month >= 12 || month <= 2) {
            CAC.setMode("heating"); // 如果是12月到2月，设置为制热模式
        } else {
            CAC.setMode("cooling"); // 如果是其他月份，设置为制冷模式
        }
    }

    @Override
    public void turnOff() {
        CAC.setIsOn(false);
    }

    @Override
    public Response handleRequest(Request request) {
        if (isRequestContradictWithState(request)) {
            // 请求与中央空调的状态矛盾，返回错误信息
            Response response = new Response();
            System.out.println("请求温度超出对应模式范围");
            response.setMessage("温度必须在对应模式范围内！");
            return response;
        }
        // 从请求中获取请求参数
        String type = request.getType();
        String roomId = request.getRoomId();
        String fanSpeed = request.getFanSpeed();
        String state = null;

        ReportItem reportItem = new ReportItem();
        reportItem.setRoomId(roomId);
        reportItem.setFanSpeed(fanSpeed);

        if (Objects.equals(type, "start")) {
            // 有来自从控机的温控启动请求，且服务队列为空，中央空调开始工作
            if (CAC.getCurrentReportItems().isEmpty()) {
                CAC.setStatus(true);
                System.out.println("中央空调开始工作");
            }
            // 如果服务队列的请求个数小于3，则将新的 ReportItem 实例添加到当前服务列表中
            if (CAC.getCurrentReportItems().size() < 3) {
                CAC.getCurrentReportItems().add(reportItem);
                reportItem.setState("processing");
                System.out.println("开始送风，房间号："+ reportItem.getRoomId());
                state = "processing";
            }
            else {
                // 否则将实例添加到等待队列
                CAC.getWaitingReportItems().add(reportItem);
                reportItem.setState("waiting");
                System.out.println("当前请求正在等待");
                state = "waiting";
            }

        }
        else if (Objects.equals(type, "stop")) {
            // 有来自从控机的温控关闭请求，将新的 Request 实例状态变为 finished
            reportItem.setState("finished");

            // Todo: 当有来自从控机的温控关闭请求时，需要从服务队列中删除ReportItem实例

            // Todo: 计算ReportItem的各个属性，如消耗的能量和费用等。

            // 将完成的请求添加到数据库中
            System.out.println("插入报表项：" + reportItem);
            reportItemMapper.insert(reportItem);
            state = "finished";

            // Todo: 启动调度，当有请求完成时，从等待队列中选择一个请求进行处理

        }

        // Todo: 检查所有房间都没有温控请求，中央空调的状态回到待机状态

        // 返回处理结果
        Response response = new Response();
        response.setRoomID(roomId);
        response.setState(state);
        response.setMessage("请求处理成功");
        return response;
    }
    private boolean isRequestContradictWithState(Request request) {
        // 检查请求与中央空调的状态是否矛盾
        // 如果矛盾，返回 true
        int[] temperatureRange = CAC.getTemperatureRange();
        return request.getTargetTemp() < temperatureRange[0] || request.getTargetTemp() > temperatureRange[1];
        // 否则，返回 false
    }
}
