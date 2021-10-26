package com.example.sdksamples;

import com.impinj.octane.*;

public class RxSensitivityRamp {

    public static void main(String[] args) {
        try {
            String hostname = System.getProperty(SampleProperties.hostname);

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            ImpinjReader reader = new ImpinjReader();

            // Connect
            System.out.println("Connecting to " + hostname);
            reader.connect(hostname);

            // get the features
            FeatureSet features = reader.queryFeatureSet();

            // Get the default settings
            Settings settings = reader.queryDefaultSettings();

            // send a tag report for every tag read
            settings.getReport().setMode(ReportMode.Individual);
            settings.getReport().setIncludePeakRssi(true);

            // set just one specific antenna for this example
            AntennaConfigGroup ag = settings.getAntennas();

            ag.disableAll();
            ag.getAntenna((short) 1).setEnabled(true);
            ag.setIsMaxRxSensitivity(false);

            // connect a listener
            reader.setTagReportListener(new TagReportListenerImplementation());

            for (RxSensitivityTableEntry t : features.getRxSensitivities()) {
                System.out.println("Setting receive sensitivity to " + t.Dbm);
                ag.getAntenna((short) 1).setIsMaxRxSensitivity(false);
                ag.getAntenna((short) 1).setRxSensitivityinDbm(t.Dbm);
                // Apply the new settings
                reader.applySettings(settings);

                // Start the reader
                reader.start();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // ignore this since its just an example and just keep going
                }

                reader.stop();
            }

            System.out.println("Disconnecting from " + hostname);
            reader.disconnect();

            System.out.println("Done");
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }
}
