package top.kotara.rfid.signaldispaly;

import com.alibaba.fastjson.JSONObject;
import com.impinj.octane.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        try {
            String hostname="192.168.0.221";
            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            ImpinjReader reader = new ImpinjReader();
            SampleProperties.currMHZ = SampleProperties.mHz[0];

            String DataFor="5tag";
            String TargetID="E183";
            String RefID="E184";
            String PosX="2.4";
            String PosY="1.0";
            String DistanceBetweenAntes="0.25";
            String Speed="0.1";
            String Mode="NonStop";
            String WaveLength="0.32564";

            JSONObject jo=new JSONObject();
            jo.put("DataFor",DataFor);
            jo.put("TargetID",TargetID);
            jo.put("RefID",RefID);
            jo.put("PosX",PosX);
            jo.put("PosY",PosY);
            jo.put("DistanceBetweenAntes",DistanceBetweenAntes);
            jo.put("Speed",Speed);
            jo.put("Mode",Mode);
			jo.put("WaveLength",WaveLength);
            String strNow = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString();
            jo.put("Date",strNow);
			String ModifyTime="" + new Date().getTime();
			jo.put("ModifyTime",ModifyTime);
            //SampleProperties.outfilename =strNow+"-"+dataUseFor+"-Material "+material+"-TagID"+tagID+"-Speed"+speed+"-DisBtwAntAndTag"+dis+"-DisBtwAnts"+antDis+"-position"+position+"-"+ new Date().getTime()+".csv";
            //SampleProperties.outfilename =strNow+"-"+dataUseFor+"-Mode-"+Mode+"-TagetTag-"+TagetID+"-RefTag-"+RefID+"-PosX-"+PosX+"-PosY-"+PosY+"-DistanceBetweenAntes-"+DistanceBetweenAntes+"-Speed-"+Speed+"-"+ new Date().getTime()+".csv";
            SampleProperties.outfilename=String.format("%s-%s-%d.csv",DataFor,strNow,new Date().getTime());
            System.out.println(SampleProperties.outfilename);

            // Connect
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




            // Get the default settings
            Settings settings = reader.queryDefaultSettings();
            settings.getTxFrequenciesInMhz().add(SampleProperties.currMHZ);
            settings.setReaderMode(ReaderMode.MaxThroughput);
            System.out.println("----------------------------" + settings.getTxFrequenciesInMhz());
            AntennaConfigGroup antennaConfigs = settings.getAntennas();
            antennaConfigs.disableAll();
            antennaConfigs.setIsMaxTxPower(false);
            antennaConfigs.setTxPowerinDbm(SampleProperties.txPower);
            antennaConfigs.enableAll();

            settings.getReport().setMode
(ReportMode.Individual);
            settings.getReport().setIncludeChannel(true);
            settings.getReport().setIncludePhaseAngle(true);
            settings.getReport().setIncludePeakRssi(true);
            settings.getReport().setIncludeLastSeenTime(true);
            settings.getReport().setIncludeAntennaPortNumber(true);
            settings.getReport().setIncludeSeenCount(true);
            settings.getReport().setIncludeFastId(true);
            settings.getReport().setIncludePcBits(true);


            // set up low duty cycle mode
            LowDutyCycleSettings ldc = settings.getLowDutyCycle();
            ldc.setEmptyFieldTimeoutInMs(2000);
            ldc.setFieldPingIntervalInMs(1000);
            ldc.setIsEnabled(false);

            // Apply the new settings
            reader.applySettings(settings);
            TagReportListenerImplementation tagReportListenerImplementation=new TagReportListenerImplementation();
            try {
                tagReportListenerImplementation.getFilewriter().write("%"+jo.toJSONString()+"\n");
            } catch (IOException e) {
                System.out.println("Cannot write to "+SampleProperties.outfilename);
                e.printStackTrace();
            }
            reader.setTagReportListener(tagReportListenerImplementation);


            reader.start();
            System.in.read();
            while (System.in.read() != '\n') { }


            while (reader.isConnected()) {
                try {
                    System.out.println("-------------------------------------------Stopping  " + hostname);
                    reader.stop();
                    System.out.println("-------------------------------------------Disconnecting from " + hostname);
                    reader.disconnect();
                } catch (OctaneSdkException e1) {
                    System.out.println("-------------------------------------------" + e1.getMessage());
                } catch (Exception e2) {
                    System.out.println("-------------------------------------------" + e2.getMessage());
                    e2.printStackTrace(System.out);
                }
            }
            System.out.println("-----------Done-------------------");
        } catch (OctaneSdkException ex) {
            System.out.println("-------------------------------------------"+ex.getMessage());
        } catch (Exception ex) {
            System.out.println("-------------------------------------------"+ex.getMessage());
            ex.printStackTrace(System.out);
        }
        System.exit(0);
    }

}
