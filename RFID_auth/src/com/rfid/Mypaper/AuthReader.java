package com.rfid.Mypaper;


import com.impinj.octane.*;
import com.rfid.config.HoppingFreqConfig;
import com.rfid.config.Myconfig;
import com.rfid.config.ReaderPrintConfig;
import com.rfid.readerPrint.ReadPrintUtils;
import com.rfid.rfTool.TagReportListenerImplementation;

import java.util.*;

/**
 * @className: authReader
 * @Description: 与认证相关的信号采集
 * @author: Zhang Qihang
 * @date: 2021/11/23 15:49
 */
public class AuthReader {
    /**
     * 采集各个频率下的所有初始数据，搭建相位指纹库
     *
     * @param
     * @return
     * @author Zhang QiHang.
     * @date 2021/11/23 15:55
     */
    public static void buildPhaseData() {

        //1.填入标签
        Map<String, Double> rssHashMap = new HashMap<String, Double>();
        rssHashMap.put("A991", 0.0);
        rssHashMap.put("A992", 0.0);
        rssHashMap.put("A993", 0.0);
        rssHashMap.put("A994", 0.0);
        rssHashMap.put("A995", 0.0);

        Map<String, Integer> cntsHashMap = new HashMap<String, Integer>();
        cntsHashMap.put("A991", 0);
        cntsHashMap.put("A992", 0);
        cntsHashMap.put("A993", 0);
        cntsHashMap.put("A994", 0);
        cntsHashMap.put("A995", 0);

        //2.根据RSS来选择中心标签
        String centralTagEpc = ReadPrintUtils.getCentralTag(rssHashMap, cntsHashMap);

        //3. 获取标签的初始数据
        collectData(centralTagEpc);
    }

    public static void collectData(String centralTagEpc) {
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

            // 遍历频率，构建数据库
            for (double freq : Myconfig.freqList) {
                System.out.println("current freq is " + freq);
                // Setting类为阅读器的配置类，获取阅读器的默认设置，如readerMode，searchMode，filters
                Settings settings = reader.queryDefaultSettings();
                // 设置查找模式
                settings.setSearchMode(SearchMode.ReaderSelected);
                // 设置过滤标签设置
                TagFilter filter1 = new TagFilter();
                filter1.setMemoryBank(MemoryBank.Epc);
                filter1.setBitPointer(BitPointers.Epc);
                filter1.setBitCount(4 * centralTagEpc.length());
                filter1.setTagMask(centralTagEpc);
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
                String mode = ReadPrintUtils.chooseMode(readerModel, ReaderPrintConfig.mode);
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
                antennas.getAntenna((short) 1).setTxPowerinDbm(Myconfig.TxPowerinDbm);
                antennas.getAntenna((short) 1).setRxSensitivityinDbm(Myconfig.RxSensitivityinDbm);

                // 规定标签信息
                reader.setTagReportListener(new TagReportListenerImplementation() {
                    @Override
                    public void onTagReported(ImpinjReader reader0, TagReport report0) {
                        List<Tag> tags = report0.getTags();
                        for (Tag tag : tags) {
                            // 指定标签读取
                            if (centralTagEpc.equals(tag.getEpc().toString())) {
                                String temp = tag.getEpc().toString() + "," + freq + "," + tag.getChannelInMhz()
                                        + "," + tag.getLastSeenTime().ToString() + "," + tag.getPeakRssiInDbm()
                                        + "," + tag.getPhaseAngleInRadians();
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
                Thread.sleep(Myconfig.collectTime);
                reader.stop();
                // 跳频休眠时间
                Thread.sleep(500);
            }
            // 停止收集，并且记录数据
            reader.disconnect();
            // 写入数据
            Utils.myWriteFile("auth_database", TagInfoArray);
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    public static void main(String[] args) {
        //1. 建立数据库
        buildPhaseData();
        //2. 进行认证
    }
}
