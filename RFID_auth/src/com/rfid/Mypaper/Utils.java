package com.rfid.Mypaper;

import com.rfid.config.Myconfig;
import com.rfid.config.ReaderPrintConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @className: utils
 * @Description: 论文中所调用的工具类
 * @author: Zhang Qihang
 * @date: 2021/11/23 15:43
 */
public class Utils {
    public static <T> void myWriteFile(String filename, ArrayList<T> content) {
        String timeFlag = new SimpleDateFormat("yyyyMMddHHMMSS").format(new Date());

        File file = new File(Myconfig.filePath + timeFlag + filename + ".csv");
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
