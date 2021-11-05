package com.rfid.hoppingFreq;

import com.impinj.octane.*;
import com.rfid.config.HoppingFreqConfig;
import com.rfid.config.ReaderPrintConfig;
import com.rfid.readerPrint.ReadPrintUtils;
import com.rfid.rfTool.TagReportListenerImplementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class CollectPhase {
    /**
     * 普通采集相位的程序
     *
     * @param
     * @return
     * @author Zhang QiHang.
     * @date 2021/11/5 16:28
     */
    public static void collectNormalPhase() {
        String hostname = HoppingFreqConfig.hostname;
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


            // Setting类为阅读器的配置类，获取阅读器的默认设置，如readerMode，searchMode，filters
            Settings settings = reader.queryDefaultSettings();

            System.out.println(readerModel);

            // 可以对标签进行一系列的调整
            ReportConfig report = settings.getReport();
            report.setIncludeAntennaPortNumber(true);
            report.setIncludePeakRssi(true);
            report.setIncludePhaseAngle(true);
            report.setIncludeLastSeenTime(true);
            report.setIncludeChannel(true);
            report.setMode(ReportMode.Individual);// 每个标签单独作为一个report返回
            report.setMode(ReportMode.Individual);

            // 设置过滤标签设置
            TagFilter filter1 = new TagFilter();
            filter1.setMemoryBank(MemoryBank.Epc);
            filter1.setBitPointer(BitPointers.Epc);
            filter1.setBitCount(4L * HoppingFreqConfig.targetMask.length());
            filter1.setTagMask(HoppingFreqConfig.targetMask);
            filter1.setFilterOp(TagFilterOp.Match);
            FilterSettings filterSettings = new FilterSettings();
            filterSettings.setTagFilter1(filter1);
            filterSettings.setMode(TagFilterMode.OnlyFilter1);
            settings.setFilters(filterSettings);

            String mode = ReadPrintUtils.chooseMode(readerModel, ReaderPrintConfig.mode);
            settings.setReaderMode(ReaderMode.valueOf(mode));

            // 可以对天线进行一系列的调整
            AntennaConfigGroup antennas = settings.getAntennas();
            antennas.disableAll();
            antennas.enableById(new short[]{1});
            antennas.getAntenna((short) 1).setIsMaxRxSensitivity(false);
            antennas.getAntenna((short) 1).setIsMaxTxPower(false);
            antennas.getAntenna((short) 1).setTxPowerinDbm(ReaderPrintConfig.TxPowerinDbm);
            antennas.getAntenna((short) 1).setRxSensitivityinDbm(-70);

            // 对标签返回信息做了规范
            reader.setTagReportListener(new TagReportListenerImplementation() {
                @Override
                public void onTagReported(ImpinjReader reader0, TagReport report0) {
                    // tags为得到的所有标签
                    List<Tag> tags = report0.getTags();
                    for (Tag t : tags) {
                        if (HoppingFreqConfig.targetMask.equals(t.getEpc().toString())) {
                            String temp = t.getEpc().toString() + "," + t.getLastSeenTime().ToString() + ","
                                    + t.getPhaseAngleInRadians() + "," + t.getPeakRssiInDbm();
                            System.out.println(temp);
                            TagInfoArray.add(temp);
                        }
                        // 如果标签阵列中有当前监听到的标签
                    }
                }
            });

            // 直接更改不会生效，必须进行apply
            reader.applySettings(settings);

            //开始扫描
            System.out.println("在控制台敲击回车开始扫描.");
            System.out.println("再次敲击回车结束扫描.");
            Scanner s = new Scanner(System.in);
            s.nextLine();
            //System.out.println("Starting");
            reader.start();
            s = new Scanner(System.in);
            s.nextLine();
            reader.stop();
            reader.disconnect();

            ReadPrintUtils.myWriteFile("hopping", TagInfoArray);
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }


    public static void collectHoppingPhase() {
        String hostname = HoppingFreqConfig.hostname;
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

            // Setting类为阅读器的配置类，获取阅读器的默认设置，如readerMode，searchMode，filters
            Settings settings = reader.queryDefaultSettings();

            System.out.println(readerModel);

            // 可以对标签进行一系列的调整
            ReportConfig report = settings.getReport();
            report.setIncludeAntennaPortNumber(true);
            report.setIncludePeakRssi(true);
            report.setIncludePhaseAngle(true);
            report.setIncludeLastSeenTime(true);
            report.setIncludeChannel(true);
            report.setMode(ReportMode.Individual);// 每个标签单独作为一个report返回
            report.setMode(ReportMode.Individual);

            // 设置过滤标签设置
            TagFilter filter1 = new TagFilter();
            filter1.setMemoryBank(MemoryBank.Epc);
            filter1.setBitPointer(BitPointers.Epc);
            filter1.setBitCount(4L * HoppingFreqConfig.targetMask.length());
            filter1.setTagMask(HoppingFreqConfig.targetMask);
            filter1.setFilterOp(TagFilterOp.Match);
            FilterSettings filterSettings = new FilterSettings();
            filterSettings.setTagFilter1(filter1);
            filterSettings.setMode(TagFilterMode.OnlyFilter1);
            settings.setFilters(filterSettings);

            String mode = ReadPrintUtils.chooseMode(readerModel, ReaderPrintConfig.mode);
            settings.setReaderMode(ReaderMode.valueOf(mode));

            // 可以对天线进行一系列的调整
            AntennaConfigGroup antennas = settings.getAntennas();
            antennas.disableAll();
            antennas.enableById(new short[]{1});
            antennas.getAntenna((short) 1).setIsMaxRxSensitivity(false);
            antennas.getAntenna((short) 1).setIsMaxTxPower(false);
            antennas.getAntenna((short) 1).setTxPowerinDbm(ReaderPrintConfig.TxPowerinDbm);
            antennas.getAntenna((short) 1).setRxSensitivityinDbm(-70);

            //调频处理
            if (!f.isHoppingRegion()) {
                ArrayList<Double> freqList = new ArrayList<>();
                Collections.shuffle(HoppingFreqConfig.freqList);
                freqList.addAll(HoppingFreqConfig.freqList);
                settings.setTxFrequenciesInMhz(freqList);
            }

            // 对标签返回信息做了规范
            reader.setTagReportListener(new TagReportListenerImplementation() {
                @Override
                public void onTagReported(ImpinjReader reader0, TagReport report0) {
                    // tags为得到的所有标签
                    List<Tag> tags = report0.getTags();
                    for (Tag t : tags) {
                        if (HoppingFreqConfig.targetMask.equals(t.getEpc().toString())) {
                            String temp = t.getEpc().toString() + "," + t.getChannelInMhz() + ","
                                    + t.getLastSeenTime().ToString() + "," + t.getPhaseAngleInRadians()
                                    + "," + t.getPeakRssiInDbm();
                            System.out.println(temp);
                            TagInfoArray.add(temp);
                        }
                        // 如果标签阵列中有当前监听到的标签
                    }
                }
            });

            // 直接更改不会生效，必须进行apply
            reader.applySettings(settings);

            //开始扫描
            System.out.println("在控制台敲击回车开始扫描.");
            System.out.println("再次敲击回车结束扫描.");
            Scanner s = new Scanner(System.in);
            s.nextLine();
            //System.out.println("Starting");
            reader.start();
            s = new Scanner(System.in);
            s.nextLine();
            reader.stop();
            reader.disconnect();

            ReadPrintUtils.myWriteFile("hopping", TagInfoArray);
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    public static void main(String[] args) {
//        collectNormalPhase();
        collectHoppingPhase();
    }
}
