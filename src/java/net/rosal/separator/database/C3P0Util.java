/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rosal.separator.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author carry
 */

public class C3P0Util extends HttpServlet {

    ServletConfig config;//定义一个ServletConfig对象
    private static ComboPooledDataSource cpds = new ComboPooledDataSource();

    private static String user = "maple_express";
    private static String password = "200105DuKeyu";

    private static String expressDatabase = "jdbc:mysql://rm-bp1yi5q5e7340j776125010.mysql.rds.aliyuncs.com:3306/maple_separator?useUnicode=true&characterEncoding=utf8&useSSL=false";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.config = config;
    }

    /**
     *
     *
     * @return Connection
     * @throws java.beans.PropertyVetoException
     */
    public static Connection getConnection() throws PropertyVetoException {
        try {
            cpds.setDriverClass("com.mysql.jdbc.Driver");
            cpds.setJdbcUrl(expressDatabase);
            cpds.setUser(user);
            cpds.setPassword(password);
            setProperty(cpds);
            return cpds.getConnection();
        } catch (SQLException e) {
            return null;
        }
    }

    public static void setProperty(ComboPooledDataSource cpds) {
        cpds.setAcquireIncrement(3);// 可以设置连接池的各种属性
        cpds.setMaxPoolSize(10);
        cpds.setMinPoolSize(3);
        cpds.setMaxStatements(100);
        cpds.setCheckoutTimeout(3000);
        cpds.setIdleConnectionTestPeriod(60);
        cpds.setMaxIdleTime(60);
    }

    public static void close(Connection conn, PreparedStatement pst, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
            }
        }
        if (pst != null) {
            try {
                pst.close();
            } catch (SQLException e) {
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

}
