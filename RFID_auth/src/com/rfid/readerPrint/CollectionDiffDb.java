package com.rfid.readerPrint;

import com.rfid.rfTool.RFRead;

import java.util.HashMap;

/**
 * @Description 测试同一标签、同一位置下不同功率的各信号区别
 * @Author Zhang Qihang
 * @Date 2021/10/20 15:25
 */
public class CollectionDiffDb {
    public static void main(String[] args) {


        // 配置阅读器地址
        String hostname = Config.hostname;
        System.out.println(hostname);

        // 天线端口选择  R220:{1,2}  R420:{1,2,3,4}
        short[] port = new short[]{1};//仅使用RF-Ware自带的1号天线

        // 传输功率设定
        double TxPowerinDbm = 25;// Dbm 10~32.5

        // 接收灵敏度设定
        double RxSensitivityinDbm = -70.0;

        // 用0或1表示是否需要采集RSSI/phase/时间/天线号/频率 （testflag=6~13中使用）
        int RSSI = 1, phase = 1, time = 1, portN = 1, freq = 1, readRate = 1;
        int howlong = 2000;

        // label记录每个EPC对应的tag编号 <String:EPC,Integer:tagID> （testflag=6~13中使用）
        HashMap<String, Integer> label = new HashMap<String, Integer>();

        // 模式选择
        // SpeedwayR220: AutoSetDenseReader/DenseReaderM4/DenseReaderM4Two/DenseReaderM8
        // SpeedwayR420: AutoSetDenseReader/AutoSetDenseReaderDeepScan/AutoSetStaticDRM/AutoSetStaticFast/MaxThroughput/Hybrid/DenseReaderM4/DenseReaderM4Two/DenseReaderM8
        String mode = "MaxThroughput";
        String targetMask = "E200";
        label.put("E200 001B 6404 0188 0590 0010",1);
        // 是否在控制台输出读到的信息
        boolean output6 = true;
        // 存储的文件名
        String filename6 = "readAll";
        // 用0或1表示是否需要采集RSSI/phase/时间/天线号/频率
        // 调用readAll采集数据
        RFRead.readAllT(hostname, port, TxPowerinDbm, RxSensitivityinDbm, mode, label, targetMask, filename6, output6,howlong, RSSI,
                phase, time, portN, freq,readRate);
//        RFRead.readAll(hostname, port, TxPowerinDbm, RxSensitivityinDbm, mode, label, targetMask, filename6, output6, RSSI,
//                phase, time, portN, freq,readRate);
    }
/*    public static void main(String[] args) {

        try {
            // 配置阅读器地址
            String hostname = Config.hostname;
            System.out.println(hostname);
            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + Config.hostname + "' property");
            }

            // 创建阅读器对象
            ImpinjReader reader = new ImpinjReader();

            // 进行连接
            while (!reader.isConnected()) {
                try {
                    System.out.println("Connecting to " + hostname);
                    reader.connect(hostname);
                } catch (OctaneSdkException e1) {
                    System.out.println("******************************************************************" + e1.getMessage());
                } catch (Exception e2) {
                    System.out.println("******************************************************************" + e2.getMessage());
                    e2.printStackTrace(System.out);
                }
            }

            // Setting类为阅读器的配置类，获取阅读器的默认设置，如readerMode，searchMode，filters
            Settings settings = reader.queryDefaultSettings();

            // 可以对天线进行一系列调整
            AntennaConfigGroup antennaConfigs = settings.getAntennas();

            // 可以对标签进行一系列的调整
            ReportConfig report = settings.getReport();
            report.setMode(ReportMode.Individual);

            // 设置低占空比模式（有什么用）
            LowDutyCycleSettings ldc = settings.getLowDutyCycle();

            // 测试过滤标签功能
            String targetMask = "A200";
            TagFilter t1 = settings.getFilters().getTagFilter1();
            t1.setBitCount(targetMask.length() * 4);
            t1.setBitPointer(BitPointers.Epc);
            t1.setMemoryBank(MemoryBank.Epc);
            t1.setFilterOp(TagFilterOp.Match);
            t1.setTagMask(targetMask);
            settings.getFilters().setMode(TagFilterMode.OnlyFilter1);
            // Apply the new settings
            // 直接更改不会生效，必须进行apply
            reader.applySettings(settings);

            // connect a listener
            reader.setTagReportListener(new TagReportListenerImplementation());

            // Start the reader
            reader.start();

            System.out.println("Press Enter to exit.");
            Scanner s = new Scanner(System.in);
            s.nextLine();

            System.out.println("Stopping  " + hostname);
            reader.stop();

            System.out.println("Disconnecting from " + hostname);
            reader.disconnect();

            System.out.println("Done");
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }*/
}

