package top.kotara.rfid.signaldispaly;

import com.impinj.octane.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TagReportListenerImplementation implements TagReportListener{
    private FileWriter fileWriter;
    String filename=SampleProperties.outfilename;

    public TagReportListenerImplementation(){
        try {
            fileWriter = new FileWriter(filename,true);
        }catch (IOException e) {
            System.out.println("Cannot write to "+filename);
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        fileWriter.close();
        super.finalize();
    }
    public FileWriter getFilewriter(){
        return fileWriter;
    }



    @Override
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();
        for (Tag t : tags) {
           {

                String outStr=t.getEpc()+","+t.getLastSeenTime().ToString()+","+(3.14*2-t.getPhaseAngleInRadians())+","+t.getPeakRssiInDbm()+","+t.getRfDopplerFrequency()+","+t.getAntennaPortNumber();
               // System.out.println(SampleProperties.outfilename);
                short portnum = t.getAntennaPortNumber();
               System.out.println(outStr);
                try {
                    fileWriter.write(outStr+"\n");
                } catch (IOException e) {
                    System.out.println("Cannot write to "+filename);
                    e.printStackTrace();
                }
            }
            //System.out.println(" --------------------------------------------------------\n EPC: " + t.getEpc().toString());
//
//            if (reader.getName() != null) {
//                System.out.println(" Reader_name: " + reader.getName());
//            } else {
//                System.out.println(" Reader_ip: " + reader.getAddress());
//            }
//
//            if (t.isAntennaPortNumberPresent()) {
//                System.out.println(" antenna: " + t.getAntennaPortNumber());
//            }
//
//            if (t.isFirstSeenTimePresent()) {
//                System.out.println(" first: " + t.getFirstSeenTime().ToString());
//            }
//
//            if (t.isLastSeenTimePresent()) {
//                System.out.println(" last: " + t.getLastSeenTime().ToString());
//            }
//
//            if (t.isSeenCountPresent()) {
//                System.out.println(" count: " + t.getTagSeenCount());
//            }
//
//            if (t.isRfDopplerFrequencyPresent()) {
//                System.out.println(" doppler: " + t.getRfDopplerFrequency());
//            }
//
//            if (t.isPeakRssiInDbmPresent()) {
//                System.out.println(" peak_rssi: " + t.getPeakRssiInDbm());
//            }
//
//            if (t.isChannelInMhzPresent()) {
//                System.out.println(" chan_MHz: " + t.getChannelInMhz());
//            }
//
//            if (t.isPcBitsPresent()) {
//                System.out.println(" PcBits: " + t.getPcBits()+"|end of pcBits");
//            }
//
//            if (t.isFastIdPresent()) {
//                System.out.println(" fast_id: " + t.getTid().toHexString());
//
//                System.out.println(" model: " +
//                        t.getModelDetails().getModelName());
//
//                System.out.println(" epcsize: " +
//                        t.getModelDetails().getEpcSizeBits());
//
//                System.out.println(" usermemsize: " +
//                        t.getModelDetails().getUserMemorySizeBits());
//                System.out.println(" usermemsize: " +
//                        t.);
//            }
//
//            System.out.println("");
        }

    }


}

