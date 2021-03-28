/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rosal.separator.main.cancontrol;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.rosal.separator.database.ControllerDAO;
import net.rosal.separator.database.MobileDAO;
import net.rosal.separator.database.RedisUtil;

/**
 *
 * @author win
 */
public class MainControl extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try ( PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet MainControl</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet MainControl at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");

        String requestCode = request.getParameter("requestCode");
        switch (requestCode) {
            case "001":
                //登录垃圾桶Id
                loginCanId(request, response);
                break;
            case "002":
                //获取所有垃圾桶信息的接口
                getAllCanData(request, response);
                break;
            case "003":
                //增加用户积分
                addUserScore(request, response);
                break;
            default:
                break;
        }
    }

    private void loginCanId(HttpServletRequest request, HttpServletResponse response) {
        String Id = request.getParameter("Id");
        String password = request.getParameter("password");

        JSONObject jSONObject = new JSONObject();
        if (Id != null && password != null) {
            //对照id
            jSONObject = ControllerDAO.loginCan(Id, password);
        }
        try ( PrintWriter out = response.getWriter()) {
            out.write(jSONObject.toString());
        } catch (IOException ex) {
            Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getAllCanData(HttpServletRequest request, HttpServletResponse response) {
        JSONArray jSONArray = ControllerDAO.getAllCanData();
        try ( PrintWriter out = response.getWriter()) {
            out.write(jSONArray.toString());
        } catch (IOException ex) {
            Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addUserScore(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jSONObject = new JSONObject();
        String user_id = request.getParameter("user_id");
        if (user_id != null) {
            //在缓存中查看用户是否重复提交
            if (!RedisUtil.exist("user_scan" + user_id)) {
                //数据库添加
                if (MobileDAO.addUserScore(user_id)) {
                    jSONObject.put("result", "1");
                    //存入缓存
                    Map<String, String> cache = new HashMap<>();
                    cache.put("time", String.valueOf(System.currentTimeMillis()));
                    RedisUtil.setMap("user_scan" + user_id, cache, 3600);//1个小时后过期
                } else {
                    jSONObject.put("result", "0");
                }
            } else {
                jSONObject.put("result", "0");
            }
        }
        try ( PrintWriter out = response.getWriter()) {
            out.write(jSONObject.toString());
        } catch (IOException ex) {
            Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
