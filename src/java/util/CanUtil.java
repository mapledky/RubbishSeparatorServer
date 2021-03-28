/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 * 垃圾桶的实例
 *
 * @author win
 */
public class CanUtil {

    public double temp = 0.0;//温度
    public double water = 0.0;//湿度
    public double fire = 0.0;//可燃
    public double weight = 0.0;//重量

    public int state = 0;//垃圾桶状态 0:未满 1：已满
    public int openstate = 0;//垃圾桶开合状态 0:关闭 1:打开
}
