/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rosal.separator.database;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.beans.PropertyVetoException;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.EmojiAdapter;
import util.GeoHash;
import util.LocationBean;
import util.SensitiveWordsUtils;
import util.UserState;

/**
 *
 * @author win
 */
public class MobileDAO {

    //根据location geohash数据匹配搜索
    public static JSONArray searchLocation(List<String> location) {
        JSONArray jSONArray = new JSONArray();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "SELECT * FROM caninfo WHERE dismiss !=0";
        try {
            connection = C3P0Util.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String Id = resultSet.getString("Id");
                String password_correct = resultSet.getString("password");
                String latitude = resultSet.getString("latitude");
                String longitude = resultSet.getString("longitude");
                String dismiss = resultSet.getString("dismiss");
                String name = resultSet.getString("name");
                GeoHash geoHash = new GeoHash(Double.parseDouble(latitude), Double.parseDouble(longitude), 6);
                String geohash_location = geoHash.getGeoHashBase32();
                for (int i = 0; i < location.size(); i++) {
                    if ((geohash_location.substring(0, LocationBean.ACURACY - 1)).equals(location.get(i).substring(0, LocationBean.ACURACY - 1))) {
                        //匹配搜索
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("Id", Id);
                        jSONObject.put("latitude", latitude);
                        jSONObject.put("longitude", longitude);
                        jSONObject.put("dismiss", dismiss);
                        jSONObject.put("name", name);
                        jSONArray.add(jSONObject);
                        break;
                    }
                }
            }
        } catch (PropertyVetoException | SQLException ex) {
            Logger.getLogger(ControllerDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            C3P0Util.close(connection, preparedStatement, resultSet);
        }
        return jSONArray;
    }

    //根据地理位置匹配搜索订单
    public static JSONArray getOrderByArea(List<String> location, int acuracy) {
        JSONArray jSONArray = new JSONArray();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "SELECT * FROM userorder WHERE dismiss !=0";
        try {
            connection = C3P0Util.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String Id = resultSet.getString("Id");
                String user_id = resultSet.getString("user_id");
                String title = EmojiAdapter.emojiRecovery(resultSet.getString("title"));
                String description = EmojiAdapter.emojiRecovery(resultSet.getString("description"));
                String latitude = resultSet.getString("latitude");
                String longitude = resultSet.getString("longitude");
                String price = resultSet.getString("price");
                String time = resultSet.getString("time");
                String phoneNumber = resultSet.getString("phoneNumber");
                String dismiss = resultSet.getString("dismiss");

                GeoHash geoHash = new GeoHash(Double.parseDouble(latitude), Double.parseDouble(longitude), acuracy);
                String geohash_location = geoHash.getGeoHashBase32();
                for (int i = 0; i < location.size(); i++) {
                    if ((geohash_location.substring(0, acuracy - 1)).equals(location.get(i).substring(0, acuracy - 1))) {
                        //匹配搜索
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("Id", Id);
                        jSONObject.put("user_id", user_id);
                        jSONObject.put("title", title);
                        jSONObject.put("description", description);
                        jSONObject.put("latitude", latitude);
                        jSONObject.put("longitude", longitude);
                        jSONObject.put("price", price);
                        jSONObject.put("time", time);
                        jSONObject.put("phoneNumber", phoneNumber);
                        jSONObject.put("dismiss", dismiss);
                        jSONArray.add(jSONObject);
                        break;
                    }
                }
            }
        } catch (PropertyVetoException | SQLException ex) {
            Logger.getLogger(ControllerDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            C3P0Util.close(connection, preparedStatement, resultSet);
        }
        return jSONArray;
    }

    //获取用户的所有订单
    public static JSONArray getAllOrder(String user_id) {
        JSONArray jSONArray = new JSONArray();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "SELECT * FROM userorder WHERE user_id = ?";
        try {
            connection = C3P0Util.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String Id = resultSet.getString("Id");
                String title = EmojiAdapter.emojiRecovery(resultSet.getString("title"));
                String description = EmojiAdapter.emojiRecovery(resultSet.getString("description"));
                String latitude = resultSet.getString("latitude");
                String longitude = resultSet.getString("longitude");
                String price = resultSet.getString("price");
                String time = resultSet.getString("time");
                String phoneNumber = resultSet.getString("phoneNumber");
                String dismiss = resultSet.getString("dismiss");

                JSONObject jSONObject = new JSONObject();
                jSONObject.put("Id", Id);
                jSONObject.put("user_id", user_id);
                jSONObject.put("title", title);
                jSONObject.put("description", description);
                jSONObject.put("latitude", latitude);
                jSONObject.put("longitude", longitude);
                jSONObject.put("price", price);
                jSONObject.put("time", time);
                jSONObject.put("phoneNumber", phoneNumber);
                jSONObject.put("dismiss", dismiss);
                jSONArray.add(jSONObject);
            }
        } catch (PropertyVetoException | SQLException ex) {
            Logger.getLogger(ControllerDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            C3P0Util.close(connection, preparedStatement, resultSet);
        }
        return jSONArray;
    }

    //修改用户订单状态
    public static JSONObject changeOrderState(String user_id, String order_id) {
        JSONObject jSONObject = new JSONObject();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "UPDATE userorder SET dismiss = ? WHERE user_id = ? AND Id = ?";
        try {
            connection = C3P0Util.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "0");
            preparedStatement.setString(2, user_id);
            preparedStatement.setString(3, order_id);
            preparedStatement.executeUpdate();
            jSONObject.put("result", "1");
        } catch (PropertyVetoException | SQLException ex) {
            Logger.getLogger(ControllerDAO.class.getName()).log(Level.SEVERE, null, ex);
            jSONObject.put("result", "0");
        } finally {
            C3P0Util.close(connection, preparedStatement, resultSet);
        }
        return jSONObject;
    }

    //获取id
    public static String getId(String phoneNumber) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String Id = null;
        String sql_searchPhoneNumber = "SELECT * FROM mobileinfo WHERE phoneNumber = ?";//查找用户是否登录
        String sql_updateLastTime = "UPDATE mobileinfo set lastTime = ? WHERE phoneNumber = ?";//更新上次登录时间
        String sql_insert = "INSERT INTO mobileinfo(phoneNumber,createTime,lastTime) VALUES (?,?,?)";
        String sql_search = "SELECT LAST_INSERT_ID()";
        Long time = System.currentTimeMillis();
        try {
            connection = C3P0Util.getConnection();
            preparedStatement = connection.prepareStatement(sql_searchPhoneNumber);
            preparedStatement.setString(1, phoneNumber);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                //该手机号已经注册，更新登录时间
                String dismiss = resultSet.getString("dismiss");
                if (dismiss.equals("0")) {
                    return null;
                }
                Id = resultSet.getString("Id");
                preparedStatement.close();
                preparedStatement = connection.prepareStatement(sql_updateLastTime);
                preparedStatement.setString(1, String.valueOf(time));
                preparedStatement.setString(2, phoneNumber);
                preparedStatement.executeUpdate();
                //通过id创建用户文件
                File file = new File(UserState.USER_FILE + "/user" + Id + "/");
                if (!file.exists()) {
                    file.mkdir();
                }
                return Id;
            } else {
                //该手机号未注册过，注册并返回
                preparedStatement.close();
                preparedStatement = connection.prepareStatement(sql_insert);
                preparedStatement.setString(1, phoneNumber);
                preparedStatement.setString(2, String.valueOf(time));
                preparedStatement.setString(3, String.valueOf(time));
                preparedStatement.executeUpdate();
                preparedStatement.close();
                preparedStatement = connection.prepareStatement(sql_search);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    Id = resultSet.getString("LAST_INSERT_ID()");
                }

                //通过id创建用户文件
                File file = new File(UserState.USER_FILE + "/user" + Id + "/");
                if (!file.exists()) {
                    file.mkdir();
                }
                return Id;
            }

        } catch (PropertyVetoException | SQLException ex) {
            Logger.getLogger(MobileDAO.class.getName()).log(Level.SEVERE, null, ex);
            return Id;
        } finally {
            C3P0Util.close(connection, preparedStatement, resultSet);
            return Id;
        }
    }

    //根据Id登录
    public static JSONObject getUserById(String Id, String phoneNumber) {
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        JSONObject jSONObject = new JSONObject();
        String sql_search = "SELECT * FROM mobileinfo WHERE Id = ? AND phoneNumber = ?";
        String sql_update = "UPDATE mobileinfo SET lastTime = ? WHERE Id = ?";
        try {
            connection = C3P0Util.getConnection();
            preparedStatement = connection.prepareStatement(sql_search);
            preparedStatement.setString(1, Id);
            preparedStatement.setString(2, phoneNumber);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                //存在该Id
                Id = resultSet.getString("Id");
                phoneNumber = resultSet.getString("phoneNumber");
                String createTime = resultSet.getString("createTime");
                String lastTime = resultSet.getString("lastTime");
                String username = resultSet.getString("username");
                String headstate = resultSet.getString("headstate");
                String score = resultSet.getString("score");
                String dismiss = resultSet.getString("dismiss");
                if (dismiss.equals("0")) {
                    //该Id不存在
                    jSONObject.put("result", "0");
                    return jSONObject;
                }
                long current = System.currentTimeMillis();
                if ((current - Long.parseLong(lastTime)) < 604800000) {
                    //一周之内Id有效
                    jSONObject.put("result", "1");
                    jSONObject.put("Id", Id);
                    jSONObject.put("phoneNumber", phoneNumber);
                    jSONObject.put("createTime", createTime);
                    jSONObject.put("lastTime", String.valueOf(current));
                    jSONObject.put("username", EmojiAdapter.emojiRecovery(username));
                    jSONObject.put("headstate", headstate);
                    jSONObject.put("score", score);
                    jSONObject.put("dismiss", dismiss);
                    //更新有效时间
                    preparedStatement.close();
                    preparedStatement = connection.prepareStatement(sql_update);
                    preparedStatement.setString(1, String.valueOf(current));
                    preparedStatement.setString(2, Id);
                    preparedStatement.executeUpdate();
                    //更新完成
                    return jSONObject;
                } else {
                    //iD已经失效
                    jSONObject.put("result", "2");
                    jSONObject.put("Id", Id);
                    jSONObject.put("phoneNumber", phoneNumber);
                    return jSONObject;
                }
            } else {
                //该Id不存在
                jSONObject.put("result", "0");
                return jSONObject;
            }
        } catch (PropertyVetoException | SQLException ex) {
            Logger.getLogger(MobileDAO.class.getName()).log(Level.SEVERE, null, ex);
            jSONObject.put("result", "0");
            return jSONObject;
        } finally {
            C3P0Util.close(connection, preparedStatement, resultSet);
            return jSONObject;
        }
    }

    //根据电话获取用户信息
    public static JSONObject getUserInfoByPhone(String phoneNumber) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        JSONObject jSONObject = new JSONObject();
        String sql_search = "SELECT * FROM mobileinfo WHERE phoneNumber = ? AND dismiss != 0";
        try {
            connection = C3P0Util.getConnection();
            preparedStatement = connection.prepareStatement(sql_search);
            preparedStatement.setString(1, phoneNumber);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                //存在该Id
                String Id = resultSet.getString("Id");
                String createTime = resultSet.getString("createTime");
                String lastTime = resultSet.getString("lastTime");
                String username = EmojiAdapter.emojiRecovery(resultSet.getString("username"));
                String headstate = resultSet.getString("headstate");
                String score = resultSet.getString("score");
                String dismiss = resultSet.getString("dismiss");
                if (dismiss.equals("0")) {
                    //该Id不存在
                    jSONObject.put("result", "0");
                    return jSONObject;
                }
                jSONObject.put("result", "1");
                jSONObject.put("Id", Id);
                jSONObject.put("phoneNumber", phoneNumber);
                jSONObject.put("createTime", createTime);
                jSONObject.put("lastTime", String.valueOf(lastTime));
                jSONObject.put("username", username);
                jSONObject.put("headstate", headstate);
                jSONObject.put("score", score);
                jSONObject.put("dismiss", dismiss);
                return jSONObject;
            } else {
                //该Id不存在
                jSONObject.put("result", "0");
                return jSONObject;
            }
        } catch (PropertyVetoException | SQLException ex) {
            Logger.getLogger(MobileDAO.class.getName()).log(Level.SEVERE, null, ex);
            jSONObject.put("result", "0");
            return jSONObject;
        } finally {
            C3P0Util.close(connection, preparedStatement, resultSet);
            return jSONObject;
        }
    }

    //根据id获取用户信息
    public static JSONObject getUserinfoById(String user_id) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        JSONObject jSONObject = new JSONObject();
        String sql_search = "SELECT * FROM mobileinfo WHERE Id = ? AND dismiss != 0";
        try {
            connection = C3P0Util.getConnection();
            preparedStatement = connection.prepareStatement(sql_search);
            preparedStatement.setString(1, user_id);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                //存在该Id
                String Id = resultSet.getString("Id");
                String phoneNumber = resultSet.getString("phoneNumber");
                String createTime = resultSet.getString("createTime");
                String lastTime = resultSet.getString("lastTime");
                String username = EmojiAdapter.emojiRecovery(resultSet.getString("username"));
                String headstate = resultSet.getString("headstate");
                String score = resultSet.getString("score");
                String dismiss = resultSet.getString("dismiss");
                if (dismiss.equals("0")) {
                    //该Id不存在
                    jSONObject.put("result", "0");
                    return jSONObject;
                }
                jSONObject.put("result", "1");
                jSONObject.put("Id", Id);
                jSONObject.put("phoneNumber", phoneNumber);
                jSONObject.put("createTime", createTime);
                jSONObject.put("lastTime", String.valueOf(lastTime));
                jSONObject.put("username", username);
                jSONObject.put("headstate", headstate);
                jSONObject.put("score", score);
                jSONObject.put("dismiss", dismiss);
                return jSONObject;
            } else {
                //该Id不存在
                jSONObject.put("result", "0");
                return jSONObject;
            }
        } catch (PropertyVetoException | SQLException ex) {
            Logger.getLogger(MobileDAO.class.getName()).log(Level.SEVERE, null, ex);
            jSONObject.put("result", "0");
            return jSONObject;
        } finally {
            C3P0Util.close(connection, preparedStatement, resultSet);
            return jSONObject;
        }

    }

    //修改头像
    public static JSONObject changeHead(String Id, String headstate, String phoneNumber) {
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        JSONObject jSONObject = new JSONObject();
        String sql_search = "SELECT * FROM mobileinfo WHERE Id = ? AND phoneNumber = ?";
        String sql_update = "UPDATE mobileinfo SET headstate = ? WHERE Id = ?";
        try {
            connection = C3P0Util.getConnection();
            preparedStatement = connection.prepareStatement(sql_search);
            preparedStatement.setString(1, Id);
            preparedStatement.setString(2, phoneNumber);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                //存在该Id
                Id = resultSet.getString("Id");
                phoneNumber = resultSet.getString("phoneNumber");
                String createTime = resultSet.getString("createTime");
                String lastTime = resultSet.getString("lastTime");
                String username = EmojiAdapter.emojiRecovery(resultSet.getString("username"));
                String dismiss = resultSet.getString("dismiss");
                if (dismiss.equals("0")) {
                    //该Id不存在
                    jSONObject.put("result", "0");
                    return jSONObject;
                }
                long current = System.currentTimeMillis();
                if ((current - Long.parseLong(lastTime)) < 604800000) {
                    //一周之内Id有效
                    preparedStatement.close();
                    preparedStatement = connection.prepareStatement(sql_update);
                    preparedStatement.setString(1, headstate);
                    preparedStatement.setString(2, Id);
                    preparedStatement.executeUpdate();
                    //更新完成
                    jSONObject.put("result", "1");
                    jSONObject.put("Id", Id);
                    jSONObject.put("phoneNumber", phoneNumber);
                    jSONObject.put("createTime", createTime);
                    jSONObject.put("lastTime", String.valueOf(current));
                    jSONObject.put("username", username);
                    jSONObject.put("headstate", headstate);
                    jSONObject.put("dismiss", dismiss);
                    return jSONObject;
                } else {
                    //iD已经失效
                    jSONObject.put("result", "2");
                    jSONObject.put("Id", Id);
                    jSONObject.put("phoneNumber", phoneNumber);
                    return jSONObject;
                }
            } else {
                //该Id不存在
                jSONObject.put("result", "0");
                return jSONObject;
            }
        } catch (PropertyVetoException | SQLException ex) {
            Logger.getLogger(MobileDAO.class.getName()).log(Level.SEVERE, null, ex);
            jSONObject.put("result", "0");
            return jSONObject;
        } finally {
            C3P0Util.close(connection, preparedStatement, resultSet);
            return jSONObject;
        }
    }

    //修改用户名信息
    public static JSONObject changeUserName(String Id, String phoneNumber, String change_name) {
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        JSONObject jSONObject = new JSONObject();
        String sql_search = "SELECT * FROM mobileinfo WHERE Id = ? AND phoneNumber = ?";
        String sql_update = "UPDATE mobileinfo SET username = ? WHERE Id = ?";
        try {
            connection = C3P0Util.getConnection();
            preparedStatement = connection.prepareStatement(sql_search);
            preparedStatement.setString(1, Id);
            preparedStatement.setString(2, phoneNumber);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                //存在该Id
                Id = resultSet.getString("Id");
                phoneNumber = resultSet.getString("phoneNumber");
                String createTime = resultSet.getString("createTime");
                String lastTime = resultSet.getString("lastTime");
                String username = EmojiAdapter.emojiRecovery(resultSet.getString("username"));
                String headstate = resultSet.getString("headstate");
                String dismiss = resultSet.getString("dismiss");
                if (dismiss.equals("0")) {
                    //该Id不存在
                    jSONObject.put("result", "0");
                    return jSONObject;
                }
                long current = System.currentTimeMillis();
                if ((current - Long.parseLong(lastTime)) < 604800000) {
                    //一周之内Id有效
                    //查看是否含有敏感词汇
                    if (SensitiveWordsUtils.contains(username)) {
                        //包含敏感词汇
                        //iD已经失效
                        jSONObject.put("result", "3");
                        jSONObject.put("Id", Id);
                        jSONObject.put("phoneNumber", phoneNumber);
                        return jSONObject;
                    }
                    preparedStatement.close();
                    preparedStatement = connection.prepareStatement(sql_update);
                    preparedStatement.setString(1, EmojiAdapter.emojiConvert(change_name));
                    preparedStatement.setString(2, Id);
                    preparedStatement.executeUpdate();
                    //更新完成
                    jSONObject.put("result", "1");
                    jSONObject.put("Id", Id);
                    jSONObject.put("phoneNumber", phoneNumber);
                    jSONObject.put("createTime", createTime);
                    jSONObject.put("lastTime", String.valueOf(current));
                    jSONObject.put("username", change_name);
                    jSONObject.put("headstate", headstate);
                    jSONObject.put("dismiss", dismiss);
                    return jSONObject;
                } else {
                    //iD已经失效
                    jSONObject.put("result", "2");
                    jSONObject.put("Id", Id);
                    jSONObject.put("phoneNumber", phoneNumber);
                    return jSONObject;
                }
            } else {
                //该Id不存在
                jSONObject.put("result", "0");
                return jSONObject;
            }
        } catch (PropertyVetoException | SQLException ex) {
            Logger.getLogger(MobileDAO.class.getName()).log(Level.SEVERE, null, ex);
            jSONObject.put("result", "0");
            return jSONObject;
        } finally {
            C3P0Util.close(connection, preparedStatement, resultSet);
            return jSONObject;
        }
    }

    //用户发布订单
    public static JSONObject userGiveOrder(String user_id, String title, String description, String images, String latitude, String longitude, String price, String phoneNumber) {
        JSONObject jSONObject = new JSONObject();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String Id = null;
        String sql_insert = "INSERT INTO userorder(user_id,title,description,images,latitude,longitude,price,time,phoneNumber) VALUES (?,?,?,?,?,?,?,?,?)";
        String sql_search = "SELECT LAST_INSERT_ID()";
        Long time = System.currentTimeMillis();

        //检测内容是否有敏感词汇
        if (SensitiveWordsUtils.contains(title) || SensitiveWordsUtils.contains(description)) {
            jSONObject.put("result", "2");
            return jSONObject;
        }
        try {
            connection = C3P0Util.getConnection();
            preparedStatement = connection.prepareStatement(sql_insert);
            preparedStatement.setString(1, user_id);
            preparedStatement.setString(2, EmojiAdapter.emojiConvert(title));
            preparedStatement.setString(3, EmojiAdapter.emojiConvert(description));
            preparedStatement.setString(4, images);
            preparedStatement.setString(5, latitude);
            preparedStatement.setString(6, longitude);
            preparedStatement.setString(7, price);
            preparedStatement.setString(8, String.valueOf(time));
            preparedStatement.setString(9, user_id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            preparedStatement = connection.prepareStatement(sql_search);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Id = resultSet.getString("LAST_INSERT_ID()");
            }
            jSONObject.put("Id", Id);
            jSONObject.put("result", "1");
            return jSONObject;
        } catch (PropertyVetoException | SQLException ex) {
            Logger.getLogger(MobileDAO.class.getName()).log(Level.SEVERE, null, ex);
            jSONObject.put("result", "0");
            return jSONObject;
        } finally {
            C3P0Util.close(connection, preparedStatement, resultSet);
        }
    }

    //用户根据地理位置获取周围订单
    //增加用户积分
    public static boolean addUserScore(String user_id) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql_update = "UPDATE mobileinfo SET score = score+1 WHERE Id = ?";
        try {
            connection = C3P0Util.getConnection();
            preparedStatement = connection.prepareStatement(sql_update);
            preparedStatement.setString(1, user_id);
            int result = preparedStatement.executeUpdate();
            if (result > 0) {
                return true;
            }
        } catch (PropertyVetoException | SQLException ex) {
            Logger.getLogger(MobileDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            C3P0Util.close(connection, preparedStatement, resultSet);
        }
        return false;
    }

}
