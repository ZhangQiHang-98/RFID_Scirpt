package com.rfid.config;

import java.util.Arrays;
import java.util.List;

public class HoppingFreqConfig {
    // 阅读器地址：192.168.0.221
    public static String hostname = "192.168.1.27";
    // 天线端口选择  R220:{1,2}  R420:{1,2,3,4}
    public static short[] port = new short[]{1};
    // 传输功率设定
    public static double TxPowerinDbm = 30;
    // 接收灵敏度设定
    public double RxSensitivityinDbm = -70.0;
    // 阅读器模式
    public static String mode = "MaxThroughput";
    // 标签过滤设定
    public static String targetMask = "A998";

    // 采集的数据存放的位置
    public static String filePath = "D:\\RFID_Scirpt\\data\\hop\\";

    // 频率列表
    public static List<Double> freqList = Arrays.asList(getFreqList(920.625,924.375));

    public static Double[] getFreqList(Double startFreq, Double endFreq) {

        // 根据最小间隔0.25Mhz从920.625 MHz to 924.375生成频率列表
        Double[] freqList = new Double[(int) ((endFreq - startFreq) / 0.25 + 1)];
        for (int i = 0; i < freqList.length; i++) {
            freqList[i] = startFreq + i * 0.25;
        }
        return freqList;
    }

}
