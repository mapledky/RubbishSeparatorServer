/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rosal.separator.main.cancontrol;

import java.io.IOException;
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
    private String Id;//智能垃圾桶的id
    private String latitude;//纬度信息
    private String longitude;//经度信息
    private String name;//垃圾桶的名称

    @OnOpen//打开连接执行用户的id
    public void onOpen(Session session, @PathParam("Id") String id, @PathParam("latitude") String latitude, @PathParam("longitude") String longitude) {
        this.session = session;
        Map<String, String> params = session.getPathParameters();//获取用户传递的参数
        Id = params.get("Id");
        client.put(Id, this);
        addOnlineCount();
        //上传经纬度到数据库
        latitude = params.get("latitude");
        longitude = params.get("longitude");
        name = ControllerDAO.uploadLocation(id, latitude, longitude);
    }

    @OnMessage//收到消息执行
    public void onMessage(String message, Session session) {
        String[] message_array = message.split("/");
        switch (message_array[0]) {
            case "1001":
                //心跳线路检测
                sendMessage("1001");
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
        client.remove(Id);
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

}
