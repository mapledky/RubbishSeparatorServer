/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rosal.separator.main.cancontrol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import net.rosal.separator.database.ControllerDAO;
import net.rosal.separator.database.MobileDAO;
import net.rosal.separator.database.RedisUtil;
import util.CanUtil;

/**
 *
 * @author carrytargaryen
 */
/**
 * @ServerEndpoint
 * 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 */
@ServerEndpoint("/InteractSocket_separator/{Id}/{latitude}/{longitude}")
public class InteractSocket_separator {

    private static int onLineCount = 0;//在线智能垃圾桶连接数
    public static Map<String, InteractSocket_separator> client = new ConcurrentHashMap<>();

    //垃圾桶的相关信息
    private Session session;
    private String can_id;//智能垃圾桶的id
    private String can_latitude;//纬度信息
    private String can_longitude;//经度信息
    private String can_name;//垃圾桶的名称

    //垃圾桶实例列表
    private ArrayList<CanUtil> canlist;

    @OnOpen//打开连接执行用户的id
    public void onOpen(Session session, @PathParam("Id") String id, @PathParam("latitude") String latitude, @PathParam("longitude") String longitude) {
        this.session = session;
        Map<String, String> params = session.getPathParameters();//获取用户传递的参数
        can_id = params.get("Id");
        client.put(can_id, this);
        addOnlineCount();
        //上传经纬度到数据库
        can_latitude = params.get("latitude");
        can_longitude = params.get("longitude");
        can_name = ControllerDAO.uploadLocation(id, latitude, longitude);
        //初始化垃圾桶实例列表
        canlist = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            CanUtil canUtil = new CanUtil();
            canlist.add(canUtil);
        }
    }

    @OnMessage//收到消息执行
    public void onMessage(String message, Session session) {
        String[] message_array = message.split("/");
        switch (message_array[0]) {
            case "1001":
                //心跳线路检测
                sendMessage("1001");
                break;
            case "1002":
                //垃圾桶实时数据上传
                uploadCanData(message);
                break;
            case "1003":
                //上传用户Id信息以获取积分
                uploadUserId(message);
                break;
            default:
                break;
        }
    }

    /*
        websocket  session发送文本消息有两个方法：getAsyncRemote()和
       getBasicRemote()  getAsyncRemote()和getBasicRemote()是异步与同步的区别，
       大部分情况下，推荐使用getAsyncRemote()。
     */
    public synchronized void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException ex) {
            Logger.getLogger(InteractSocket_separator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @OnClose//关闭连接执行
    public void onClose(Session session) {
        client.remove(can_id);
        subOnlineCount();
    }

    @OnError//连接错误的时候执行
    public void onError(Throwable error, Session session) {
    }

    //获取当前在线人数的方法
    public static synchronized int getOnlineCount() {
        return onLineCount;
    }

    //增加在线人数
    public static synchronized void addOnlineCount() {
        InteractSocket_separator.onLineCount++;
    }

    //在线人数减
    public static synchronized void subOnlineCount() {
        InteractSocket_separator.onLineCount--;
    }

    //上传垃圾桶的实时信息
    public void uploadCanData(String message) {
        String[] message_array = message.split("/");
        for (int i = 1; i < message_array.length; i++) {
            String[] data = message_array[i].split("&");
            canlist.get(i - 1).temp = Double.parseDouble(data[0]);
            canlist.get(i - 1).water = Double.parseDouble(data[1]);
            canlist.get(i - 1).fire = Double.parseDouble(data[2]);
            canlist.get(i - 1).weight = Double.parseDouble(data[3]);
            canlist.get(i - 1).state = Integer.parseInt(data[4]);
            canlist.get(i - 1).openstate = Integer.parseInt(data[5]);
        }

        String recycle = message_array[1];//可回收垃圾数据包
        String kitchen = message_array[2];//厨余垃圾数据包
        String other = message_array[3];//其他垃圾数据包
        String harm = message_array[4];//有害垃圾数据包

        List<String> candata = new ArrayList<String>();
        candata.add(recycle);
        candata.add(kitchen);
        candata.add(other);
        candata.add(harm);
        //将数据存入缓存
        if (RedisUtil.exist("can_data" + can_id)) {
            RedisUtil.delete("can_data" + can_id);
        }
        RedisUtil.setList("can_data" + can_id, candata, 600);
        //10分钟未上传视为掉线
    }

    //上传用户信息以获取id
    public void uploadUserId(String message) {
        String[] message_array = message.split("/");
        String user_id = message_array[1];//用户id
        String can_category = message_array[2];//垃圾桶种类，1：可回收垃圾 2：厨余垃圾 3：其他垃圾 4：有害垃圾

        //在缓存中查看用户是否重复提交
        if (!RedisUtil.exist("user_scan" + user_id)) {
            //数据库添加
            if (MobileDAO.addUserScore(user_id)) {
                sendMessage("2003/1");
                //存入缓存
                Map<String, String> cache = new HashMap<>();
                cache.put("time", String.valueOf(System.currentTimeMillis()));
                RedisUtil.setMap("user_scan" + user_id, cache, 3600);//1个小时后过期
            } else {
                sendMessage("2003/3");
            }
        } else {
            sendMessage("2003/2");
        }
    }
}
