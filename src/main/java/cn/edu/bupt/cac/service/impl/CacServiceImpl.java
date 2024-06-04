package cn.edu.bupt.cac.service.impl;

import cn.edu.bupt.cac.entity.*;
import cn.edu.bupt.cac.mapper.ReportItemMapper;
import cn.edu.bupt.cac.mapper.UserMapper;
import cn.edu.bupt.cac.service.CacService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CacServiceImpl implements CacService {
    @Resource
    private ReportItemMapper reportItemMapper;
    @Resource
    private UserMapper userMapper;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");

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

        // 获取users表中的用户信息
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            System.out.println("用户信息：" + user);
            Room room = new Room();
            room.setUser(user);
            // 创建一个新的SAC对象，并设置到room中
            SAC sac = new SAC();
            sac.setState("finished");
            room.setSAC(sac);
            // 设置其他的房间属性
            CAC.getRooms().add(room);
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

        ReportItem newReportItem = new ReportItem();
        newReportItem.setRoomId(roomId);


        if (Objects.equals(type, "start")) {
            // 有来自从控机的温控启动请求，且服务队列为空，中央空调开始工作
            if (CAC.getCurrentReportItems().isEmpty()) {
                CAC.setStatus(true);
                System.out.println("中央空调开始工作");
            }

            // 如果服务队列的请求个数小于3，将新的 ReportItem 实例添加到当前服务列表中
            if (CAC.getCurrentReportItems().size() < 3) {

                ReportItem reportItem = getCurrent(roomId);
                if (reportItem != null) {
                    // 如果服务队列有房间Id为roomId的ReportItem，则将其移出服务队列
                    removeFromCurrent(roomId);
                    System.out.println("服务队列：" + CAC.getCurrentReportItems());
                    // 将新的 ReportItem 实例添加到当前服务列表中
                    addToCurrent(roomId,fanSpeed);
                    System.out.println("服务队列：" + CAC.getCurrentReportItems());
                } else {
                    // 如果服务队列没有房间Id为roomId的ReportItem，则将新的 ReportItem 实例添加到当前服务列表中
                    addToCurrent(roomId,fanSpeed);
                    System.out.println("服务队列：" + CAC.getCurrentReportItems());
                }
                state = "processing";
                setStateByRoomId(roomId, "processing");
            }
            // 否则将其加入等待队列
            else {
                // Todo: 进行调度，选择一个reportItem出服务队列，进入等待队列（需求13）

                // Todo: 给进入等待的房间发送waiting响应

                // Todo: 将进入等待的reportItem结束，添加到数据库

                // Todo: 进行调度，选择一个reportItem进服务队列（需求13）

                // 否则将实例添加到等待队列
                CAC.getWaitingReportItems().add(newReportItem);
                System.out.println("等待队列：" + CAC.getWaitingReportItems());
                newReportItem.setState("waiting");
                state = "waiting";
                setStateByRoomId(roomId, state);
            }
        }
        else if (Objects.equals(type, "stop")) {
            // 从服务队列中删除reportItem实例
            removeFromCurrent(roomId);
            System.out.println("服务队列：" + CAC.getCurrentReportItems());
            state = "finished";
            setStateByRoomId(roomId, state);

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

    private BigDecimal calculateEnergy(String fanSpeed, long durationInSeconds) {
        // 根据风速和服务时长计算消耗的能量
        BigDecimal durationInMinutes = BigDecimal.valueOf(durationInSeconds).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal energy = BigDecimal.ZERO;
        System.out.println("风速：" + fanSpeed + "，服务时长：" + durationInSeconds + "秒");
        switch (fanSpeed) {
            case "low":
                energy = BigDecimal.valueOf(0.8).multiply(durationInMinutes);
                break;
            case "medium":
                energy = BigDecimal.valueOf(1.0).multiply(durationInMinutes);
                break;
            case "high":
                energy = BigDecimal.valueOf(1.2).multiply(durationInMinutes);
                break;
            default:
                break;
        }
        return energy.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calculateCost(BigDecimal energy) {
        // 根据消耗的能量计算支付费用
        BigDecimal cost = energy.multiply(BigDecimal.valueOf(5));
        return cost.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    @Override
    public BigDecimal getEnergyByRoomId(String roomId) {
        Double energy = reportItemMapper.sumEnergyByRoomId(roomId);
        return new BigDecimal(energy != null ? energy : 0.0).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public BigDecimal getCostByRoomId(String roomId) {
        Double cost = reportItemMapper.sumCostByRoomId(roomId);
        return new BigDecimal(cost != null ? cost : 0.0).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    private boolean isRequestContradictWithState(Request request) {
        // 检查请求与中央空调的状态是否矛盾
        // 如果矛盾，返回 true
        int[] temperatureRange = CAC.getTemperatureRange();
        return request.getTargetTemp() < temperatureRange[0] || request.getTargetTemp() > temperatureRange[1];
        // 否则，返回 false
    }
    private ReportItem getCurrent(String roomID) {
        Optional<ReportItem> optionalReportItem = CAC.getCurrentReportItems().stream()
                .filter(item -> roomID.equals(item.getRoomId()))
                .findFirst();

        return optionalReportItem.orElse(null);
    }

    private void addToCurrent(String roomId, String fanSpeed) {
        ReportItem newReportItem = new ReportItem();
        newReportItem.setRoomId(roomId);
        newReportItem.setState("processing");
        String currentTime = LocalDateTime.now().format(formatter);
        newReportItem.setStartTime(currentTime);

        // 设置reportItem的房间开始温度
        BigDecimal startTemp = getTemperatureByRoomId(roomId);
        newReportItem.setStartTemp(startTemp);

        // 设置reportItem的风速属性
        newReportItem.setFanSpeed(fanSpeed);

        CAC.getCurrentReportItems().add(newReportItem);

        System.out.println("开始送风，房间号："+ newReportItem.getRoomId());
    }
    private void removeFromCurrent(String roomId) {
        // 从服务队列currentReportItems中选择房间号为roomId的reportItem
        ReportItem reportItem = getCurrent(roomId);
        // 有来自从控机的温控关闭请求，将reportItem实例状态变为 finished
        reportItem.setState("finished");

        // 设置reportItem的结束时间
        String currentTime = LocalDateTime.now().format(formatter);
        reportItem.setEndTime(currentTime);

        // 计算服务时长
        long durationInSeconds = calculateDurationInSeconds(reportItem.getStartTime(), currentTime);
        // 将服务时长（以秒为单位）设置为 reportItem 的 duration 属性
        reportItem.setDuration(String.valueOf(durationInSeconds));

        // 设置reportItem的房间结束温度
        BigDecimal endTemp = getTemperatureByRoomId(roomId);
        reportItem.setEndTemp(endTemp);

        // 计算reportItem消耗的能量和费用（需求10）
        BigDecimal energy = calculateEnergy(reportItem.getFanSpeed(), durationInSeconds);
        reportItem.setEnergy(energy);
        BigDecimal cost = calculateCost(reportItem.getEnergy());
        reportItem.setCost(cost);

        // 将完成的请求添加到数据库中
        reportItemMapper.insert(reportItem);
        System.out.println("插入报表项：" + reportItem);

        // 从服务队列中删除 reportItem 实例
        CAC.getCurrentReportItems().remove(reportItem);
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

    @Override
    public void setTemperatureByRoomId(String roomId, BigDecimal temperature) {
        // 设置CAC.rooms中的room
        // room.user.roomID为roomId的房间，room.temperature的温度为temperature
        for (Room room : CAC.getRooms()) {
            if (room.getUser().getRoomID().equals(roomId)) {
                room.setTemperature(temperature);
                break;
            }
        }
    }

    @Override
    public void setOnByRoomId(String roomId, boolean on) {
        // 设置CAC.rooms中的room
        // room.user.roomID为roomId的房间，中room.SAC.isOn的状态为on
        for (Room room : CAC.getRooms()) {
            if (room.getUser().getRoomID().equals(roomId)) {
                boolean oldOn = room.getSAC().isOn();
                if (oldOn && !on) {
                    // 若On属性由true变为false，使房间的开关机次数加一
                    userMapper.increaseCountByRoomId(roomId);
                }
                room.getSAC().setOn(on);
                break;
            }
        }
    }

    @Override
    public BigDecimal getTemperatureByRoomId(String roomId) {
        for (Room room : CAC.getRooms()) {
            if (room.getUser().getRoomID().equals(roomId)) {
                return room.getTemperature();
            }
        }
        return null; // 返回null表示没有找到匹配的房间
    }

    @Override
    public void setStateByRoomId(String roomId, String state) {
        // 设置CAC.rooms中的room
        // room.user.roomID为roomId的房间，room.SAC.state的状态为state
        for (Room room : CAC.getRooms()) {
            if (room.getUser().getRoomID().equals(roomId)) {
                room.getSAC().setState(state);
                break;
            }
        }
    }

    @Override
    public List<RoomState> getRoomState() {
        List<Room> rooms = CAC.getRooms();
        return rooms.stream()
                .map(room -> {
                    RoomState roomState = new RoomState();
                    roomState.setRoomID(room.getUser().getRoomID());
                    roomState.setTemperature(room.getTemperature());
                    roomState.setOn(room.getSAC().isOn());
                    roomState.setState(room.getSAC().getState());
                    return roomState;
                })
                .toList();
    }

    @Override
    public Report getReportByRoomId(String roomId, String type) {
        Report report = new Report();
        report.setRoomId(roomId);
        report.setType(type);

        // 获取开关机次数
        report.setCount(userMapper.getCountByRoomId(roomId));
        // 根据报表类型（日/月/年）和房间ID获取报表项
        List<ReportItem> reportItems = reportItemMapper.selectListByRoomIdAndType(roomId, type);
        report.setReportItems(reportItems);
        // 获取报表项的总费用
        BigDecimal totalCost = reportItems.stream()
                .map(ReportItem::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalCost(totalCost);
        return report;
    }
}
