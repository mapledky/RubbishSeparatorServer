package net.rosal.separator.main.mobile;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.alibaba.fastjson.JSONObject;
import com.cloopen.rest.sdk.BodyType;
import com.cloopen.rest.sdk.CCPRestSmsSDK;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.rosal.separator.database.MobileDAO;
import net.rosal.separator.database.RedisUtil;
import redis.clients.jedis.Jedis;

/**
 *
 * @author carrytargaryen
 */
public class SMS extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
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
            case "001"://???????????????????????????
                requestSMS(request, response);
                break;

            case "002"://?????????????????????
                checkSMS(request, response);
                break;

        }

    }

    private void requestSMS(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jSONObject = new JSONObject();
        String phoneNumber = request.getParameter("phoneNumber");

        int code = (int) (Math.random() * 8998.0) + 1000 + 1;//?????????????????????
        boolean requestTime = true;
        //????????????????????????????????????,??????????????????????????????
        if (RedisUtil.exist("sms_request" + phoneNumber)) {
            //???????????????????????????60???
            Map data = RedisUtil.getMap("sms_request" + phoneNumber);
            /*
                data??????
                phoneNumber:xxx
                smsCode:xxx
                time:xxx(?????????)
             */
            long timestate = System.currentTimeMillis();
            if ((timestate - Long.parseLong((String) data.get("time"))) < 60000) {
                //????????????60???
                requestTime = false;
            }
        }
        if (requestTime) {
            //???????????????????????????app.cloopen.com
            String serverIp = "app.cloopen.com";
            //????????????
            String serverPort = "8883";
            //?????????,????????????????????????,?????????????????????????????????????????????ACCOUNT SID??????????????????AUTH TOKEN
            String accountSId = "8aaf070870e20ea101712bd4c20d26c1";
            String accountToken = "073cddfa15ef40b3becff595671d30a2";
            //?????????????????????????????????????????????APPID
            String appId = "8aaf070870e20ea101712bd4c27426c8";
            CCPRestSmsSDK sdk = new CCPRestSmsSDK();
            sdk.init(serverIp, serverPort);
            sdk.setAccount(accountSId, accountToken);
            sdk.setAppId(appId);
            sdk.setBodyType(BodyType.Type_JSON);
            String to = phoneNumber;
            String templateId = "579197";
            String[] datas = {String.valueOf(code), "60???", ""};
            HashMap<String, Object> result = sdk.sendTemplateSMS(to, templateId, datas);
            if ("000000".equals(result.get("statusCode"))) {
                Map<String, String> data = new HashMap<String, String>();
                long timestamp = System.currentTimeMillis();
                data.put("phoneNumber", phoneNumber);
                data.put("smsCode", String.valueOf(code));
                data.put("time", String.valueOf(timestamp));
                RedisUtil.delete("sms_request" + phoneNumber);
                RedisUtil.setMap("sms_request" + phoneNumber, data, 300);//??????????????????5??????
                jSONObject.put("result", "1");
                jSONObject.put("sms_id", timestamp);
            } else {
                //??????????????????????????????????????????
                jSONObject.put("result", "0");
            }
        } else {
            //?????????????????????????????????1?????????????????????
            jSONObject.put("result", "2");
        }
        try ( PrintWriter out = response.getWriter()) {
            out.write(jSONObject.toString());
        } catch (IOException ex) {
            Logger.getLogger(SMS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
    ?????????????????????
     */
    private void checkSMS(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String phoneNumber;
        String code;
        String smsCode;

        phoneNumber = request.getParameter("phoneNumber");
        code = request.getParameter("code");
        smsCode = request.getParameter("sms_id");
        JSONObject jsonObject = new JSONObject();
        //?????????????????????
        if (RedisUtil.exist("sms_request" + phoneNumber)) {
            Map data = RedisUtil.getMap("sms_request" + phoneNumber);
            //???????????????
            long timestate = System.currentTimeMillis();
            if (data.get("time").equals(smsCode) && (timestate - Long.parseLong((String) data.get("time"))) < 300000) {
                //??????????????????????????????5??????
                if (code.equals(data.get("smsCode"))) {
                    //?????????????????????
                    //???????????????????????????Id
                    String Id = MobileDAO.getId(phoneNumber);
                    if (Id != null) {
                        //?????????id
                        jsonObject.put("result", "1");
                        jsonObject.put("user_id", Id);
                    } else {
                        jsonObject.put("result", "0");
                    }
                } else {
                    //????????????????????????
                    jsonObject.put("result", "2");
                }
            } else {
                //???????????????????????????
                jsonObject.put("result", "2");
            }
        } else {
            //???????????????????????????
            jsonObject.put("result", "2");
        }
        try ( PrintWriter out = response.getWriter()) {
            out.write(jsonObject.toString());
        } catch (IOException ex) {
            Logger.getLogger(SMS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //?????????????????????
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
