package top.kotara.rfid.signaldispaly;

import com.impinj.octane.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class OnReaderModifiedListener implements ReaderStopListener, ReaderStartListener {
    private FileWriter fileWriter;
    String filename=SampleProperties.outfilename;
    @Override
    public void onReaderStart(ImpinjReader impinjReader, ReaderStartEvent readerStartEvent) {
        try {
            try {
                fileWriter = new FileWriter(filename);
            }catch (IOException e) {
                System.out.println("Cannot write to "+filename);
                e.printStackTrace();
            }
            fileWriter.write("%%starttime="+new Date().getTime() +"\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReaderStop(ImpinjReader impinjReader, ReaderStopEvent readerStopEvent) {
        try {
            try {
                fileWriter = new FileWriter(filename,true);
            }catch (IOException e) {
                System.out.println("Cannot write to "+filename);
                e.printStackTrace();
            }
            fileWriter.write("\n%%endtime="+new Date().getTime() +"\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
