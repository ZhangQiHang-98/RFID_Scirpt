package com.example.sdksamples;

import com.impinj.octane.*;

import java.util.Scanner;

public class WaitForQuery {

    public static void main(String[] args) {


        try {
            String hostname = System.getProperty(SampleProperties.hostname);

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            ImpinjReader reader = new ImpinjReader();

            System.out.println("Connecting");
            reader.connect(hostname);

            Settings settings = reader.queryDefaultSettings();

            ReportConfig report = settings.getReport();
            report.setIncludeAntennaPortNumber(true);

            // don't get reports until we ask for them
            report.setMode(ReportMode.WaitForQuery);

            settings.setReaderMode(ReaderMode.AutoSetDenseReader);

            // set some special settings for antenna 1
            AntennaConfigGroup antennas = settings.getAntennas();
            antennas.disableAll();
            antennas.enableById(new short[]{1});

            settings.getReport().setIncludeAntennaPortNumber(true);
            settings.getReport().setIncludeSeenCount(true);

            reader.setTagReportListener(new TagReportListenerImplementation());

            // since we are not getting reports until we ask, we may get buffer
            // filling
            reader.setBufferOverflowListener(
                    new BufferOverflowListenerImplementation());
            reader.setBufferWarningListener(
                    new BufferWarningListenerImplementation());

            System.out.println("Applying Settings");
            reader.applySettings(settings);

            System.out.println("Starting");
            reader.start();

            System.out.println("Press Enter to continue and read all tags.");
            Scanner s = new Scanner(System.in);
            s.nextLine();
            reader.queryTags();

            reader.stop();
            reader.disconnect();
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }
}
