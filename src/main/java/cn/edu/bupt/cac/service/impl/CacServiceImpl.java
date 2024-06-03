package cn.edu.bupt.cac.service.impl;

import cn.edu.bupt.cac.entity.ReportItem;
import cn.edu.bupt.cac.entity.Request;
import cn.edu.bupt.cac.entity.Response;
import cn.edu.bupt.cac.mapper.ReportItemMapper;
import cn.edu.bupt.cac.service.CacService;
import org.springframework.stereotype.Service;
import cn.edu.bupt.cac.entity.CAC;
import javax.annotation.Resource;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;

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

        CAC.setDefaultFanSpeed("low");
        // 设置默认刷新频率
        CAC.setFrequency(12);
    }

    @Override
    public void turnOff() {
        CAC.setIsOn(false);
    }

    @Override
    public Response handleRequest(Request request) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");

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

        ReportItem newReportItem = new ReportItem();
        newReportItem.setRoomId(roomId);
        newReportItem.setFanSpeed(fanSpeed);

        if (Objects.equals(type, "start")) {
            // 有来自从控机的温控启动请求，且服务队列为空，中央空调开始工作
            if (CAC.getCurrentReportItems().isEmpty()) {
                CAC.setStatus(true);
                System.out.println("中央空调开始工作");
            }
            // 如果服务队列的请求个数小于3，则将新的 ReportItem 实例添加到当前服务列表中
            if (CAC.getCurrentReportItems().size() < 3) {
                newReportItem.setState("processing");
                String currentTime = LocalDateTime.now().format(formatter);
                newReportItem.setStartTime(currentTime);
                CAC.getCurrentReportItems().add(newReportItem);
                System.out.println("当前服务队列：" + CAC.getCurrentReportItems());
                // Todo: 设置reportItem的房间开始温度
                // reportItem.setStartTemp();

                // Todo: 设置reportItem的风速属性

                System.out.println("开始送风，房间号："+ newReportItem.getRoomId());
                state = "processing";
            }
            else {
                // Todo: 进行调度，选择一个reportItem出服务队列，进入等待队列（需求13）

                // Todo: 给进入等待的房间发送waiting响应

                // Todo: 将进入等待的reportItem结束，添加到数据库

                // Todo: 进行调度，选择一个reportItem进服务队列（需求13）

                // 否则将实例添加到等待队列
                CAC.getWaitingReportItems().add(newReportItem);
                newReportItem.setState("waiting");
                System.out.println("当前请求正在等待");
                state = "waiting";
            }

        }
        else if (Objects.equals(type, "stop")) {
            // 从服务队列currentReportItems中选择房间号为roomId的reportItem
            ReportItem reportItem = findReportItemByRoomId(roomId);
            // 有来自从控机的温控关闭请求，将reportItem实例状态变为 finished
            reportItem.setState("finished");
            String currentTime = LocalDateTime.now().format(formatter);
            reportItem.setEndTime(currentTime);

            // 计算服务时长
            long durationInSeconds = calculateDurationInSeconds(reportItem.getStartTime(), currentTime);

            // 将服务时长（以秒为单位）设置为 reportItem 的 duration 属性
            reportItem.setDuration(String.valueOf(durationInSeconds));

            // Todo: 设置reportItem的房间结束温度

            // Todo: 计算reportItem消耗的能量和费用（需求10）

            // 将完成的请求添加到数据库中
            System.out.println("插入报表项：" + reportItem);
            reportItemMapper.insert(reportItem);
            state = "finished";

            // Todo: 从服务队列中删除reportItem实例
            removeReportItemByRoomId(roomId);
            System.out.println("当前服务队列：" + CAC.getCurrentReportItems());
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
    private ReportItem findReportItemByRoomId(String roomID) {
        Optional<ReportItem> optionalReportItem = CAC.getCurrentReportItems().stream()
                .filter(item -> roomID.equals(item.getRoomId()))
                .findFirst();

        return optionalReportItem.orElse(null);
    }

    private void removeReportItemByRoomId(String roomID) {
        // 从服务队列 currentReportItems 中选择房间号为 roomID 的 reportItem
        ReportItem reportItem = findReportItemByRoomId(roomID);

        if (reportItem != null) {
            // 从服务队列中删除 reportItem 实例
            CAC.getCurrentReportItems().remove(reportItem);
        } else {
            // 没有找到房间号为 roomID 的 reportItem
        }
    }

    private long calculateDurationInSeconds(String startTimeStr, String endTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");

        // 解析开始时间
        LocalDate startDate = LocalDate.of(Year.now().getValue(), Month.of(Integer.parseInt(startTimeStr.substring(0, 2))), Integer.parseInt(startTimeStr.substring(3, 5)));
        LocalTime startTime = LocalTime.parse(startTimeStr.substring(6), DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalDateTime start = LocalDateTime.of(startDate, startTime);

        // 解析结束时间
        LocalDate endDate = LocalDate.of(Year.now().getValue(), Month.of(Integer.parseInt(endTimeStr.substring(0, 2))), Integer.parseInt(endTimeStr.substring(3, 5)));
        LocalTime endTime = LocalTime.parse(endTimeStr.substring(6), DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalDateTime end = LocalDateTime.of(endDate, endTime);

        // 计算服务时长

        return Duration.between(start, end).getSeconds();
    }
}
