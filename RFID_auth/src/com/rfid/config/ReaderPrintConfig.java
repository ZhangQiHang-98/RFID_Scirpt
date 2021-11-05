package com.rfid.config;

/**
 * @Description 用于验证ReaderPrint的阅读器配置
 * @Author Zhang Qihang
 * @Date 2021/10/27 13:47
 */
public class ReaderPrintConfig {
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
    public static String targetMask = "";
    // 频率列表
    public static double[] freqList = new double[]{920.625, 920.875, 921.125, 921.375, 921.625, 921.875, 922.125, 922.375};
    // 功率列表
    //public static double[] powerList = new double[]{20,22,24,26,28,30};
    public static double[] powerList = new double[]{20, 22, 24, 26, 28, 30};
    // 读取时间
    public static long duration = 1000;

}

