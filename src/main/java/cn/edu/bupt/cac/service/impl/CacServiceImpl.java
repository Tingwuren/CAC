package cn.edu.bupt.cac.service.impl;

import cn.edu.bupt.cac.entity.Request;
import cn.edu.bupt.cac.entity.Response;
import cn.edu.bupt.cac.mapper.RequestMapper;
import cn.edu.bupt.cac.service.CacService;
import org.springframework.stereotype.Service;
import cn.edu.bupt.cac.entity.CAC;
import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Objects;

@Service
public class CacServiceImpl implements CacService {
    @Resource
    private RequestMapper requestMapper;

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
            response.setMessage("温度必须在对应模式范围内！");
            return response;
        }
        // 从请求中获取请求参数
        Request newRequest = getRequest(request);
        System.out.println("newRequest: " + newRequest);
        String state = null;

        if (Objects.equals(newRequest.getType(), "start")) {
            // 有来自从控机的温控启动请求，中央空调开始工作
            CAC.setStatus(true);
            System.out.println("中央空调开始工作");
            // 如果当前请求队列的请求个数小于3，则将新的 Request 实例添加到当前服务列表中
            if (CAC.getCurrentRequests().size() < 3) {
                CAC.getCurrentRequests().add(newRequest);
                newRequest.setState("processing");
                System.out.println("开始处理请求，房间号："+newRequest.getRoomId()+"，请求编号："+newRequest.getCount());
                state = "processing";
            }
            // 对请求队列采用调度算法
            CAC.getWaitingRequests().add(newRequest);
            newRequest.setState("waiting");

        }
        else if (Objects.equals(newRequest.getType(), "stop")) {
            // 有来自从控机的温控关闭请求，将新的 Request 实例状态变为 finished
            newRequest.setState("finished");

            // 将完成的请求添加到数据库中
            requestMapper.insert(newRequest);
            state = "finished";
        }

        // 检查是否所有房间都没有温控请求
        boolean allRoomsNoRequest = CAC.getRooms().stream()
                .allMatch(room -> room.getSac().getCurrentRequest() == null ||
                        room.getSac().getCurrentRequest().getType() == "stop");

        if (allRoomsNoRequest) {
            // 所有房间都没有温控要求，中央空调的状态回到待机状态
            CAC.setStatus(false);
        }

        // 返回处理结果
        Response response = new Response();
        response.setId(newRequest.getId());
        response.setState(state);
        return response;
    }
    private boolean isRequestContradictWithState(Request request) {
        // 检查请求与中央空调的状态是否矛盾
        // 如果矛盾，返回 true
        int[] temperatureRange = CAC.getTemperatureRange();
        return request.getEndTemp() < temperatureRange[0] || request.getEndTemp() > temperatureRange[1];
        // 否则，返回 false
    }

    @Override
    public Request getRequest(Request request) {
        int count = request.getCount();
        String type = request.getType();
        // System.out.println("type: " + type);
        String roomId = request.getRoomId();
        double startTemp = request.getStartTemp();
        String fanSpeed = request.getFanSpeed();
        double endTemp = request.getEndTemp();
        String duration = request.getDuration();
        double energy = request.getEnergy();
        double cost = request.getCost();
        String startTime = request.getStartTime();
        String endTime = request.getEndTime();

        // 创建一个新的 Request 实例
        Request newRequest = new Request();
        newRequest.setCount(count);
        newRequest.setId(count);
        newRequest.setType(type);
        newRequest.setRoomId(roomId);
        newRequest.setStartTemp(startTemp);
        newRequest.setFanSpeed(fanSpeed);
        newRequest.setEndTemp(endTemp);
        newRequest.setDuration(duration);
        newRequest.setEnergy(energy);
        newRequest.setCost(cost);
        newRequest.setStartTime(startTime);
        newRequest.setEndTime(endTime);
        return newRequest;
    }
}
