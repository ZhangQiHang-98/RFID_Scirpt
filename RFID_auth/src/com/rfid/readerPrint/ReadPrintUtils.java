package com.rfid.readerPrint;

import com.impinj.octane.*;
import com.rfid.config.ReaderPrintConfig;
import com.rfid.rfTool.TagReportListenerImplementation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description ReadPrint的工具函数类
 * @Author Zhang Qihang
 * @Date 2021/10/27 13:54
 */
public class ReadPrintUtils {

    /**
     * 选择阅读器模式
     *
     * @param readerModel 阅读器型号
     * @param mode        阅读器可选阅读模式
     * @return {@link java.lang.String}
     * @author Zhang QiHang.
     * @date 2021/10/27 15:53
     */
    public static String chooseMode(String readerModel, String mode) {
        String chooseMode = "";
        //SpeedwayR220
        if (readerModel.equals("SpeedwayR220")) {
            if (mode.equals("AutoSetDenseReader")) {
                chooseMode = "AutoSetDenseReader";
            } else if (mode.equals("DenseReaderM4")) {
                chooseMode = "DenseReaderM4";
            } else if (mode.equals("DenseReaderM4Two")) {
                chooseMode = "DenseReaderM4Two";
            } else if (mode.equals("DenseReaderM8")) {
                chooseMode = "DenseReaderM8";
            } else {
                System.out.println("阅读器型号：" + readerModel + "，不支持当前所设阅读模式"
                        + mode + "（或阅读模式输入有误），已自动设置为AutoSetDenseReader模式。");
                chooseMode = "AutoSetDenseReader";
            }
        }
        //SpeedwayR420
        //AutoSetDenseReader AutoSetDenseReaderDeepScan AutoSetStaticDRM AutoSetStaticFast
        //MaxThroughput Hybrid
        else if (readerModel.equals("SpeedwayR420")) {
            if (mode.equals("AutoSetDenseReader")) {
                chooseMode = "AutoSetDenseReader";
            } else if (mode.equals("AutoSetDenseReaderDeepScan")) {
                chooseMode = "AutoSetDenseReaderDeepScan";
            } else if (mode.equals("AutoSetStaticDRM")) {
                chooseMode = "AutoSetStaticDRM";
            } else if (mode.equals("AutoSetStaticFast")) {
                chooseMode = "AutoSetStaticFast";
            } else if (mode.equals("DenseReaderM4")) {
                chooseMode = "DenseReaderM4";
            } else if (mode.equals("DenseReaderM4Two")) {
                chooseMode = "DenseReaderM4Two";
            } else if (mode.equals("DenseReaderM8")) {
                chooseMode = "DenseReaderM8";
            } else if (mode.equals("MaxThroughput")) {
                chooseMode = "MaxThroughput";
            } else if (mode.equals("Hybrid")) {
                chooseMode = "Hybrid";
            } else {
                System.out.println("阅读器型号：" + readerModel + "，不支持当前所设阅读模式"
                        + mode + "（或阅读模式输入有误），已自动设置为AutoSetDenseReader模式。");
                chooseMode = "AutoSetDenseReader";
            }
        }
        return chooseMode;
    }


    /**
     * 根据一段时间的阅读得到平均RSS最大的中心标签的EPC
     *
     * @param rssHashMap  标签阵列的总RSS值
     * @param cntsHashMap 标签阵列得总读取次数
     * @return {@link String 中心标签的EPC}
     * @author Zhang QiHang.
     * @date 2021/10/27 15:59
     */
    public static String getCentralTag(Map<String, Double> rssHashMap, Map<String, Integer> cntsHashMap) {
        StringBuffer centralTag = new StringBuffer();

        String hostname = ReaderPrintConfig.hostname;
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
            report.setIncludeFirstSeenTime(true);
            report.setIncludeChannel(true);
            report.setMode(ReportMode.Individual);// 每个标签单独作为一个report返回
            report.setMode(ReportMode.Individual);

            String mode = chooseMode(readerModel, ReaderPrintConfig.mode);
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
                        String curTag = t.getEpc().toString();
                        // 如果标签阵列中有当前监听到的标签
                        if (rssHashMap.containsKey(curTag)) {
                            Double curRSS = t.getPeakRssiInDbm();
                            // 添加总RSS值
                            rssHashMap.put(curTag, rssHashMap.get(curTag) + curRSS);
                            // 添加对应的读取次数
                            cntsHashMap.put(curTag, cntsHashMap.get(curTag) + 1);
                        }
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

            //打印输出中间结果
//            System.out.println(rssHashMap);
//            System.out.println(cntsHashMap);

            //遍历更新rssHashMap,同时取得最小的RSS对应的标签EPC
            double rssMaxValue = -100;
            for (String key : rssHashMap.keySet()) {
                int temp = cntsHashMap.get(key);
                double value = rssHashMap.get(key);
                rssHashMap.put(key, value / temp);
                if (value / temp > rssMaxValue) {
                    centralTag.delete(0, centralTag.length());
                    centralTag.append(key);
                    rssMaxValue = value / temp;
                }
            }
            // 输出最终rss信号排序
            System.out.println(rssHashMap);
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
        return centralTag.toString();
    }


    /**
     * 功能描述
     *
     * @param centralTagEpc 选定标签的EPC值
     * @param freqList      频率列表
     * @param powerList     功率列表
     * @return
     * @author Zhang QiHang.
     * @date 2021/10/28 16:02
     */
    public static void getFeatureMatrix(String centralTagEpc, double[] freqList, double[] powerList, long duration) {

        String hostname = ReaderPrintConfig.hostname;
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

            // 两重循环进行跳功率和频率的读取
            for (double freq : freqList) {
                for (double power : powerList) {
                    // 输出当前功率和频率
                    System.out.println("Current freq:" + freq + " Current power:" + power + " Start collecting data~");
                    // 记录总相位读数
                    ArrayList<Double> phases = new ArrayList<Double>();

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
                    String mode = chooseMode(readerModel, ReaderPrintConfig.mode);
                    settings.setReaderMode(ReaderMode.valueOf(mode));

                    // 填入频率
                    ArrayList<Double> freqs = new ArrayList<Double>();
                    freqs.add(freq);
                    settings.setTxFrequenciesInMhz(freqs);
                    // 可以对天线进行一系列的调整(填入功率)
                    AntennaConfigGroup antennas = settings.getAntennas();
                    antennas.disableAll();
                    antennas.enableById(new short[]{1});
                    antennas.getAntenna((short) 1).setIsMaxRxSensitivity(false);
                    antennas.getAntenna((short) 1).setIsMaxTxPower(false);
                    antennas.getAntenna((short) 1).setTxPowerinDbm(power);

                    antennas.getAntenna((short) 1).setRxSensitivityinDbm(-70);

                    // 对标签返回信息做了规范
                    reader.setTagReportListener(new TagReportListenerImplementation() {
                        @Override
                        public void onTagReported(ImpinjReader reader0, TagReport report0) {
                            // 当前读到的所有标签
                            List<Tag> tags = report0.getTags();

                            for (Tag t : tags) {
                                // 如果当前读到标签中有中央标签，则记录当前相位
                                if (centralTagEpc.equals(t.getEpc().toString())) {
                                    phases.add(t.getPhaseAngleInRadians());
                                    String temp = t.getEpc().toString() + "," +
                                            freq + "," + power + ","
                                            + t.getChannelInMhz() + "," + t.getLastSeenTime().ToString() + ","
                                            + t.getPhaseAngleInRadians();
                                    // 直接存储最原始数据，然后处理
                                    TagInfoArray.add(temp);
                                }
                            }

                        }
                    });
                    // 直接更改不会生效，必须进行apply
                    reader.applySettings(settings);
                    reader.start();
                    Thread.sleep(duration);
                    reader.stop();
                    Thread.sleep(500);
                }
            }
            reader.disconnect();

            myWriteFile("readPrint", TagInfoArray);
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }


    /**
     * 测试函数，测试阅读器的一些功能
     *
     * @param
     * @return
     * @author Zhang QiHang.
     * @date 2021/10/29 14:47
     */
    public static void testFunc() {
        String hostname = ReaderPrintConfig.hostname;
        try {
            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + ReaderPrintConfig.hostname + "' property");
            }
            ImpinjReader reader = new ImpinjReader();

            System.out.println("Connecting");
            reader.connect(hostname);

            //reader.setTagReportListener(new TagReportListenerImplementation());

            FeatureSet features = reader.queryFeatureSet();
            List<TxPowerTableEntry> Txpowers = features.getTxPowers();
            for (TxPowerTableEntry power : Txpowers) {
                System.out.println(power.Index);
                System.out.println(power.Dbm);
            }
            System.out.println(features.getTxFrequencies().size());
            Settings settings = reader.queryDefaultSettings();

            settings.getReport().setIncludeAntennaPortNumber(true);
            settings.getReport().setIncludeChannel(true);
            settings.getReport().setMode(ReportMode.Individual);

            if (!features.isHoppingRegion()) {
                // settings fixed frequencies is allowed if its non hopping
                ArrayList<Double> freqList = new ArrayList<Double>();
                freqList.add(920.625);
                freqList.add(921.625);
                freqList.add(922.625);
                freqList.add(923.625);
                settings.setTxFrequenciesInMhz(freqList);
            }

            System.out.println("Applying Settings");
            reader.applySettings(settings);

//            System.out.println("Starting");
//            reader.start();
//
//            System.out.println("Press Enter to exit.");
//            Scanner s = new Scanner(System.in);
//            s.nextLine();
//
//            reader.stop();
            reader.disconnect();
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    /**
     * 写单个的文件
     *
     * @param filename 传入的文件名
     * @param content  每一行的内容
     * @param <T>      字符串或数字
     */
    public static <T> void myWriteFile(String filename, ArrayList<T> content) {
        String timeFlag = new SimpleDateFormat("yyyyMMddHHMMSS").format(new Date());

        File file = new File(ReaderPrintConfig.filePath + timeFlag + filename + ".csv");
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < content.size(); i++) {
                String temp = (String) content.get(i);
                bw.write(temp); // 写入所有的EPC,RSSI,Phase,Hz,time,天线号
                //	bw.write("," + (i + 1));// ,id
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
