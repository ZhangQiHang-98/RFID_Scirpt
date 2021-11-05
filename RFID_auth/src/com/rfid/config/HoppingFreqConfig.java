package com.rfid.config;

import java.util.Arrays;
import java.util.List;

public class HoppingFreqConfig {
    // 阅读器地址：192.168.0.221
    public static String hostname = "192.168.0.221";
    // 天线端口选择  R220:{1,2}  R420:{1,2,3,4}
    public static short[] port = new short[]{1};
    // 传输功率设定
    public static double TxPowerinDbm = 25;
    // 接收灵敏度设定
    public double RxSensitivityinDbm = -70.0;
    // 阅读器模式
    public static String mode = "MaxThroughput";
    // 标签过滤设定
    public static String targetMask = "A996";

    // 频率列表
    public static List<Double> freqList = Arrays.asList(920.625, 920.875, 921.125, 921.375, 921.625, 921.875, 922.125, 922.375);

}
