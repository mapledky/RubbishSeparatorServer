/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rosal.separator.database;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.beans.PropertyVetoException;
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
                GeoHash geoHash = new GeoHash(Double.parseDouble(latitude), Double.parseDouble(longitude));
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

    //获取id
    public static String getId(String phoneNumber) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String Id = null;
        String sql_searchPhoneNumber = "SELECT * FROM mobileinfo WHERE phoneNumber = ? AND dismiss != 0";//查找用户是否登录
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
                Id = resultSet.getString("Id");
                preparedStatement.close();
                preparedStatement = connection.prepareStatement(sql_updateLastTime);
                preparedStatement.setString(1, String.valueOf(time));
                preparedStatement.setString(2, phoneNumber);
                preparedStatement.executeUpdate();
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
}
