package com.rfid.Mypaper;

import com.impinj.octane.*;
import com.rfid.config.HoppingFreqConfig;
import com.rfid.config.Myconfig;
import com.rfid.config.ReaderPrintConfig;
import com.rfid.readerPrint.ReadPrintUtils;
import com.rfid.rfTool.TagReportListenerImplementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * @className: CollectPenPhase
 * @Description: 第二阶段：收集笔迹，先考虑单独一个字
 * @author: Zhang Qihang
 * @date: 2021/11/23 15:49
 */
public class CollectPenPhase extends Thread {
    private String name;
    private volatile boolean flag = false;

    @Override
    public void run() {
        String hostname = Myconfig.hostname;
        // 记录总信息
        ArrayList<String> TagInfoArray = new ArrayList<String>();
        try {
            if (hostname == null) {
                throw new Exception("Must specify the hostname property'");
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
            // 包含阅读器的功能和特性
            FeatureSet f = reader.queryFeatureSet();
            String readerModel = f.getReaderModel().toString();//SpeedwayR420 SpeedwayR220

            // 在指定的功率与频率列表中随机选择一个功率和频率
            double[] powerList = Myconfig.getPowerList(20.0, 30.0);
            double[] freqList = Myconfig.getFreqList(920.625, 924.125);

            while (flag == false) {
                // 随机选择一个功率和频率
                int powerIndex = (int) (Math.random() * powerList.length);
                double power = powerList[powerIndex];
                //double power = 25.0;
                int freqIndex = (int) (Math.random() * freqList.length);
                double freq = freqList[freqIndex];
                // 随机选择持续时间
                long stayTime = Myconfig.getStayTime();
                // Setting类为阅读器的配置类，获取阅读器的默认设置，如readerMode，searchMode，filters
                Settings settings = reader.queryDefaultSettings();
                // 设置查找模式
                settings.setSearchMode(SearchMode.ReaderSelected);

                // 设置过滤标签设置
                TagFilter filter1 = new TagFilter();
                filter1.setMemoryBank(MemoryBank.Epc);
                filter1.setBitPointer(BitPointers.Epc);
                filter1.setBitCount(4 * Myconfig.TagEPC.length());
                filter1.setTagMask(Myconfig.TagEPC);
                filter1.setFilterOp(TagFilterOp.Match);
                FilterSettings filterSettings = new FilterSettings();
                filterSettings.setTagFilter1(filter1);
                filterSettings.setMode(TagFilterMode.OnlyFilter1);
                settings.setFilters(filterSettings);

                // 可以对标签进行一系列的调整
                ReportConfig report = settings.getReport();
                report.setIncludeAntennaPortNumber(true);
                report.setIncludePeakRssi(true);
                report.setIncludePhaseAngle(true);
                report.setIncludeLastSeenTime(true);
                report.setIncludeChannel(true);
                report.setMode(ReportMode.Individual);// 每个标签单独作为一个report返回
                report.setMode(ReportMode.Individual);
                String mode = ReadPrintUtils.chooseMode(readerModel, Myconfig.mode);
                settings.setReaderMode(ReaderMode.valueOf(mode));

                // 填入当前频率
                // 填入频率
                ArrayList<Double> freqs = new ArrayList<Double>();
                freqs.add(freq);
                settings.setTxFrequenciesInMhz(freqs);
                // 可以对天线进行调整
                AntennaConfigGroup antennas = settings.getAntennas();
                antennas.disableAll();
                antennas.enableById(new short[]{1});
                antennas.getAntenna((short) 1).setIsMaxRxSensitivity(false);
                antennas.getAntenna((short) 1).setIsMaxTxPower(false);
                antennas.getAntenna((short) 1).setTxPowerinDbm(power);
                antennas.getAntenna((short) 1).setRxSensitivityinDbm(Myconfig.RxSensitivityinDbm);

                // 规定标签信息
                reader.setTagReportListener(new TagReportListenerImplementation() {
                    @Override
                    public void onTagReported(ImpinjReader reader0, TagReport report0) {
                        List<Tag> tags = report0.getTags();
                        for (Tag tag : tags) {
                            // 指定标签读取
                            if (Myconfig.TagEPC.equals(tag.getEpc().toString())) {
                                String temp = null;
                                try {
                                    temp = tag.getEpc().toString() + "," + freq + "," + tag.getChannelInMhz()
                                            + "," + tag.getLastSeenTime().ToString() + "," + tag.getPeakRssiInDbm()
                                            + "," + tag.getPhaseAngleInRadians() + "," + power + "," +
                                            +antennas.getAntenna((short) 1).getTxPowerinDbm();
                                    System.out.println(temp);
                                } catch (OctaneSdkException e) {
                                    e.printStackTrace();
                                }
                                TagInfoArray.add(temp);
                            }
                        }
                    }
                });
                // 应用配置
                // 直接更改不会生效，必须进行apply
                reader.applySettings(settings);
                reader.start();
                // 收集时间
                Thread.sleep(100);
                reader.stop();
                // 跳频休眠时间
            }
            // 停止收集，并且记录数据
            reader.disconnect();
            // 写入数据
            Utils.myWriteFile("pen_data", TagInfoArray);
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("按任意键开始收集，按任意键结束收集");
        sc.nextLine();
        CollectPenPhase cp = new CollectPenPhase();
        cp.start();
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        cp.flag = true;
    }
}
