/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rosal.separator.database;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * @author carry
 */
public class RedisUtil extends HttpServlet {

    /**
     * 非切片链接池
     */
    private ServletConfig config;
    private static JedisPool jedisPool;
    private static final String ip = "127.0.0.1";

    /**
     * @param config
     * @throws javax.servlet.ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.config = config;

        //循环输入虚拟垃圾桶数据
        inputVirtualData();
    }

    private void inputVirtualData() {
        //获取所有垃圾桶的信息
        JSONArray all_can = ControllerDAO.getAllCanData();

        
        String recycle = "25&50&1.23&5.6&0&0";//可回收垃圾数据包
        String kitchen = "25&50&1.23&5.6&0&0";//厨余垃圾数据包
        String other = "25&50&1.23&5.6&0&0";//其他垃圾数据包
        String harm = "25&50&1.23&5.6&0&0";//有害垃圾数据包
        List<String> candata = new ArrayList<String>();
        candata.add(recycle);
        candata.add(kitchen);
        candata.add(other);
        candata.add(harm);
        //循环将数据存入缓存
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (int i =0 ; i < all_can.size(); i++) {

                        JSONObject jSONObject = all_can.getJSONObject(i);
                        if (RedisUtil.exist("can_data" + jSONObject.getString("Id"))) {
                            RedisUtil.delete("can_data" + jSONObject.getString("Id"));
                        }
                        RedisUtil.setList("can_data" + jSONObject.getString("Id"), candata, 600);

                    }
                    try {
                        Thread.sleep(5 * 60 * 1000);
                    } catch (InterruptedException ex) {
                        System.out.println(ex.toString());
                        Logger.getLogger(RedisUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }

    /**
     * 非切片连接池初始化
     */
    private static void initialPool() {
        // 池基本配置
        JedisPoolConfig jedisconfig = new JedisPoolConfig();
        jedisconfig.setMaxTotal(10);
        jedisconfig.setMaxWaitMillis(10000);
        jedisconfig.setTestOnBorrow(true);
        jedisPool = new JedisPool(jedisconfig, ip, 6379);
        System.out.println("redis连接池初始化");
    }

    /**
     * 在多线程环境同步初始化
     */
    private static synchronized void poolInit() {
        if (jedisPool == null) {
            initialPool();
        }
    }

    /**
     * 非切片客户端链接 同步获取非切片Jedis实例
     *
     * @return Jedis
     */
    public static synchronized Jedis getJedis() {
        if (jedisPool == null) {
            poolInit();
        }
        Jedis jedis = null;
        try {
            if (jedisPool != null) {
                jedis = jedisPool.getResource();
            }
        } catch (Exception e) {
        }
        return jedis;
    }

    /**
     * 释放jedis资源
     *
     * @param jedis
     */
    @SuppressWarnings("deprecation")
    public static void returnResource(final Jedis jedis) {
        if (jedis != null && jedisPool != null) {
            jedisPool.returnResource(jedis);
        }
    }

    /**
     *
     * @param key
     * @return
     */
    public static String buildKey(String key) {
        return key;
    }

    @SuppressWarnings("empty-statement")
    public static boolean exist(String key) {
        String bKey = buildKey(key);
        Jedis jedis = getJedis();
        if (jedis == null) {
            return false;
        }
        boolean result = jedis.exists(bKey);;
        returnResource(jedis);
        return result;
    }

    public static boolean delete(String key) {
        String bKey = buildKey(key);
        Jedis jedis = getJedis();
        if (jedis == null) {
            return false;
        }
        boolean result = jedis.del(bKey) == 0;
        returnResource(jedis);
        return result;
    }

    /**
     * 设置 list
     *
     * @param <T>
     * @param key
     * @param list
     */
    public static <T> void setList(String key, List<T> list, int expire) {
        String bKey = buildKey(key);
        Jedis jedis = getJedis();
        try {
            jedis.setex(bKey.getBytes(), expire, SerializeUtil.serialize(list));
        } catch (Exception e) {
            returnResource(jedis);
            System.out.println(e);
        }
        returnResource(jedis);
    }

    /**
     * 获取list
     *
     * @param <T>
     * @param key
     * @return list
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(String key) {
        String bKey = buildKey(key);
        Jedis jedis = getJedis();
        if (jedis == null) {
            return null;
        }
        if (!jedis.exists(bKey.getBytes())) {
            returnResource(jedis);
            return null;
        }

        byte[] in = jedis.get(bKey.getBytes());
        List<T> list = (List<T>) SerializeUtil.unserialize(in);
        returnResource(jedis);
        return list;
    }

    /**
     * 设置 map
     *
     * @param <T>
     * @param key
     * @param map
     */
    public static <T> void setMap(String key, Map<String, T> map, int expire) {
        String bKey = buildKey(key);
        Jedis jedis = getJedis();
        try {
            jedis.setex(bKey.getBytes(), expire, SerializeUtil.serialize(map));
        } catch (Exception e) {
            returnResource(jedis);
            System.out.println(e);
        }
        returnResource(jedis);
    }

    /**
     * 获取list
     *
     * @param <T>
     * @param key
     * @return list
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> getMap(String key) {
        String bKey = buildKey(key);
        Jedis jedis = getJedis();
        if (jedis == null) {
            return null;
        }
        if (!jedis.exists(bKey.getBytes())) {
            returnResource(jedis);
            return null;
        }
        byte[] in = jedis.get(bKey.getBytes());
        Map<String, T> map = (Map<String, T>) SerializeUtil.unserialize(in);
        returnResource(jedis);
        return map;
    }

}
