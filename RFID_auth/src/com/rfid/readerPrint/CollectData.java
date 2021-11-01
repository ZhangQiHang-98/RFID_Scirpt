package com.rfid.readerPrint;

import com.rfid.Config.ReaderPrintConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description 数据采集入口
 * @Author Zhang Qihang
 * @Date 2021/10/27 13:49
 */
public class CollectData {
    public static void main(String[] args) {
        //1.填入标签
        Map<String, Double> rssHashMap = new HashMap<String, Double>();
        rssHashMap.put("A991",0.0);
        rssHashMap.put("A992",0.0);
        rssHashMap.put("A993",0.0);
        rssHashMap.put("A994",0.0);
        rssHashMap.put("A995",0.0);

        Map<String, Integer> cntsHashMap = new HashMap<String, Integer>();
        cntsHashMap.put("A991",0);
        cntsHashMap.put("A992",0);
        cntsHashMap.put("A993",0);
        cntsHashMap.put("A994",0);
        cntsHashMap.put("A995",0);

        //2.根据RSS来选择中心标签
        String centralTagEpc = ReadPrintUtils.getCentralTag(rssHashMap,cntsHashMap);
        System.out.println("current central Tag EPC is " + centralTagEpc);

        //3.根据中心标签跳N个功率和M个频率来获得N*M的矩阵
        //ReadPrintUtils.getFeatureMatrix(centralTagEpc, ReaderPrintConfig.freqList, ReaderPrintConfig.powerList);
    }



}
