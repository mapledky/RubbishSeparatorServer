/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rosal.separator.main.mobile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.rosal.separator.database.MobileDAO;
import net.rosal.separator.database.RedisUtil;
import net.rosal.separator.main.cancontrol.MainControl;
import util.GeoHash;
import util.UserState;

/**
 *
 * @author win
 */
public class MainMobile extends HttpServlet {

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
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet MainMobile</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet MainMobile at " + request.getContextPath() + "</h1>");
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
                //根据经纬度获取信息
                getCanInfoByLocation(request, response);
                break;
            case "002":
                //根据Id登录应用，判断是否已经失效
                loginWithId(request, response);
                break;
            case "003":
                //修改用户名
                changeUserName(request, response);
                break;
            case "004":
                //获取垃圾桶的具体信息
                getCanData(request, response);
                break;
            case "005":
                //发布用户订单
                publishUserOrder(request, response);
                break;
            case "006":
                //更改用户头像
                changeUserHead(request, response);
                break;
            case "007":
                //根据范围匹配搜索
                searchOrderByArea(request, response);
                break;
            case "008":
                //根据用户id获取用户信息
                getUserInfoById(request, response);
                break;
            case "009":
                //根据用户phoneNumber获取用户信息
                getUserInfoByPhone(request, response);
                break;
            case "010":
                //获取腾讯云api id
                getTencentApi(request, response);
                break;
            case "011":
                //获取个人订单
                getPersonOrder(request, response);
                break;
            case "012":
                //更改订单状态
                changeOrderState(request, response);
                break;
            case "013":
                //根据id获取订单信息
                getOrderById(request, response);
                break;
            default:
                break;
        }
    }

    private void getCanInfoByLocation(HttpServletRequest request, HttpServletResponse response) {
        String longitude = request.getParameter("longitude");
        String latitude = request.getParameter("latitude");

        JSONArray jSONArray = new JSONArray();
        if (longitude != null && latitude != null) {
            GeoHash geoHash = new GeoHash(Double.parseDouble(latitude), Double.parseDouble(longitude), 6);
            List<String> location = geoHash.getGeoHashBase32For9();
            //在数据库中查找和location相匹配的数据
            jSONArray = MobileDAO.searchLocation(location);
        }
        try (PrintWriter out = response.getWriter()) {
            out.write(jSONArray.toString());
        } catch (IOException ex) {
            Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loginWithId(HttpServletRequest request, HttpServletResponse response) {
        String Id = request.getParameter("Id");
        String phoneNumber = request.getParameter("phoneNumber");
        JSONObject jSONObject = new JSONObject();

        if (Id != null && phoneNumber != null) {
            jSONObject = MobileDAO.getUserById(Id, phoneNumber);
        }
        try (PrintWriter out = response.getWriter()) {
            out.write(jSONObject.toString());
        } catch (IOException ex) {
            Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getOrderById(HttpServletRequest request, HttpServletResponse response) {
        String order_id = request.getParameter("Id");
        JSONObject jSONObject = new JSONObject();
        if (order_id != null) {
            jSONObject = MobileDAO.getOrderById(order_id);
        }
        try (PrintWriter out = response.getWriter()) {
            out.write(jSONObject.toString());
        } catch (IOException ex) {
            Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getPersonOrder(HttpServletRequest request, HttpServletResponse response) {
        String user_id = request.getParameter("Id");
        JSONArray jSONArray = new JSONArray();

        if (user_id != null) {
            jSONArray = MobileDAO.getAllOrder(user_id);
        }
        try (PrintWriter out = response.getWriter()) {
            out.write(jSONArray.toString());
        } catch (IOException ex) {
            Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //更改订单状态
    private void changeOrderState(HttpServletRequest request, HttpServletResponse response) {
        String user_id = request.getParameter("Id");
        String order_id = request.getParameter("order_id");
        JSONObject jSONObject = new JSONObject();
        if (user_id != null && order_id != null) {
            jSONObject = MobileDAO.changeOrderState(user_id, order_id);
        }
        try (PrintWriter out = response.getWriter()) {
            out.write(jSONObject.toString());
        } catch (IOException ex) {
            Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void changeUserName(HttpServletRequest request, HttpServletResponse response) {
        String Id = request.getParameter("Id");
        String phoneNumber = request.getParameter("phoneNumber");
        String change_name = request.getParameter("name");
        JSONObject jSONObject = new JSONObject();

        if (Id != null && phoneNumber != null & change_name != null) {
            jSONObject = MobileDAO.changeUserName(Id, phoneNumber, change_name);
        }
        try (PrintWriter out = response.getWriter()) {
            out.write(jSONObject.toString());
        } catch (IOException ex) {
            Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getCanData(HttpServletRequest request, HttpServletResponse response) {
        String Id_string = request.getParameter("can_id");
        JSONArray jSONArray_alldata = new JSONArray();
        if (Id_string != null) {
            String[] can_id_array = Id_string.split("/");//本次要获取的垃圾桶的id数组
            //在redis缓存中获取垃圾桶的相关信息
            for (int i = 0; i < can_id_array.length; i++) {
                if (RedisUtil.exist("can_data" + can_id_array[i])) {
                    JSONArray jSONArray = new JSONArray();//四个垃圾桶数据json格式
                    List<String> can_data = RedisUtil.getList("can_data" + can_id_array[i]);
                    Iterator iterator = can_data.iterator();
                    while (iterator.hasNext()) {
                        String data = (String) iterator.next();
                        String[] data_array = data.split("&");
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("temp", data_array[0]);
                        jSONObject.put("water", data_array[1]);
                        jSONObject.put("fire", data_array[2]);
                        jSONObject.put("weight", data_array[3]);
                        jSONObject.put("state", data_array[4]);
                        jSONObject.put("openstate", data_array[5]);
                        jSONArray.add(jSONObject);
                    }
                    jSONArray_alldata.add(jSONArray);
                } else {
                    JSONArray jSONArray = new JSONArray();
                    jSONArray_alldata.add(jSONArray);
                }
            }
        }
        try (PrintWriter out = response.getWriter()) {
            out.write(jSONArray_alldata.toString());
        } catch (IOException ex) {
            Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //根据id获取用户信息

    private void getUserInfoById(HttpServletRequest request, HttpServletResponse response) {
        String user_id = request.getParameter("user_id");

        JSONObject jSONObject = new JSONObject();

        if (user_id != null) {
            jSONObject = MobileDAO.getUserinfoById(user_id);
        }

        try (PrintWriter out = response.getWriter()) {
            out.write(jSONObject.toString());
        } catch (IOException ex) {
            Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //根据phone获取用户信息
    private void getUserInfoByPhone(HttpServletRequest request, HttpServletResponse response) {
        String phoneNumber = request.getParameter("phoneNumber");

        JSONObject jSONObject = new JSONObject();

        if (phoneNumber != null) {
            jSONObject = MobileDAO.getUserInfoByPhone(phoneNumber);
        }

        try (PrintWriter out = response.getWriter()) {
            out.write(jSONObject.toString());
        } catch (IOException ex) {
            Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //获取腾讯云api id
    public void getTencentApi(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jSONObject = new JSONObject();

        jSONObject.put("app_id", UserState.tecent_appid);
        jSONObject.put("secret_key", UserState.tecent_secret_key);
        jSONObject.put("secret_pass", UserState.tecent_secret_pass);
        try (PrintWriter out = response.getWriter()) {
            out.write(jSONObject.toString());
        } catch (IOException ex) {
            Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //根据范围匹配
    public void searchOrderByArea(HttpServletRequest request, HttpServletResponse response) {
        String latitude = request.getParameter("latitude");
        String longitude = request.getParameter("longtitude");
        String acuracy = request.getParameter("acuracy");
        //4:30km 5 2.4km 6 610
        JSONArray jSONArray = new JSONArray();
        if (longitude != null && latitude != null && acuracy != null) {
            GeoHash geoHash = new GeoHash(Double.parseDouble(latitude), Double.parseDouble(longitude), Integer.parseInt(acuracy));
            List<String> location = geoHash.getGeoHashBase32For9();
            //在数据库中查找和location相匹配的数据
            jSONArray = MobileDAO.getOrderByArea(location, Integer.parseInt(acuracy));
        }
        try (PrintWriter out = response.getWriter()) {
            out.write(jSONArray.toString());
        } catch (IOException ex) {
            Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //发布用户订单
    public void publishUserOrder(HttpServletRequest request, HttpServletResponse response) {
        String user_id = request.getParameter("user_id");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String images = request.getParameter("images");
        String latitude = request.getParameter("latitude");
        String longitude = request.getParameter("longitude");
        String price = request.getParameter("price");
        String phoneNumber = request.getParameter("phoneNumber");

        JSONObject jSONObject = new JSONObject();
        if (user_id != null && title != null && description != null && images != null && latitude != null && longitude != null && price != null) {
            jSONObject = MobileDAO.userGiveOrder(user_id, title, description, images, latitude, longitude, price, phoneNumber);
        }
        try (PrintWriter out = response.getWriter()) {
            out.write(jSONObject.toString());
        } catch (IOException ex) {
            Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //更改用户头像
    public void changeUserHead(HttpServletRequest request, HttpServletResponse response) {
        String user_id = request.getParameter("Id");
        String phoneNumber = request.getParameter("phoneNumber");
        String headstate = request.getParameter("headstate");

        JSONObject jSONObject = new JSONObject();

        if (user_id != null && headstate != null && phoneNumber != null) {
            jSONObject = MobileDAO.changeHead(user_id, headstate, phoneNumber);
        }

        try (PrintWriter out = response.getWriter()) {
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
