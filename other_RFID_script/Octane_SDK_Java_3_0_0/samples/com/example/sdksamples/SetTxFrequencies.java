package com.example.sdksamples;

import com.impinj.octane.*;

import java.util.ArrayList;
import java.util.Scanner;

public class SetTxFrequencies {

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

            reader.setTagReportListener(new TagReportListenerImplementation());

            FeatureSet features = reader.queryFeatureSet();
            Settings settings = reader.queryDefaultSettings();

            settings.getReport().setIncludeAntennaPortNumber(true);
            settings.getReport().setMode(ReportMode.Individual);

            if (!features.isHoppingRegion()) {
                // settings fixed frequencies is allowed if its non hopping
                ArrayList<Double> freqList = new ArrayList<Double>();
                freqList.add(865.7);
                freqList.add(866.3);
                freqList.add(866.9);
                freqList.add(867.5);

                settings.setTxFrequenciesInMhz(freqList);
            }

            System.out.println("Applying Settings");
            reader.applySettings(settings);

            System.out.println("Starting");
            reader.start();

            System.out.println("Press Enter to exit.");
            Scanner s = new Scanner(System.in);
            s.nextLine();

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
