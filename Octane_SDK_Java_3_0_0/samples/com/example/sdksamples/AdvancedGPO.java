package com.example.sdksamples;

import com.impinj.octane.*;

import java.util.Scanner;

// this example shows the different settings for advanced GPO.  
public class AdvancedGPO {

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

            FeatureSet features = reader.queryFeatureSet();

            if (features.getGpoCount() < 4) {
                System.out.print("Must use a reader with at least 4 GPOs to "
                        + "run this example");
                System.exit(-2);
            }

            // Get the default settings
            Settings settings = reader.queryDefaultSettings();

            GpoConfigGroup gpos = settings.getGpos();

            // this gpo will be high when tags inventory is running
            gpos.getGpo((short) 1).setMode(GpoMode.ReaderInventoryStatus);

            // this will go high when a client app connects
            gpos.getGpo((short) 2).setMode(GpoMode.LLRPConnectionStatus);

            // this will pulse for a period of time
            gpos.getGpo((short) 3).setMode(GpoMode.Pulsed);
            gpos.getGpo((short) 3).setGpoPulseDurationMsec(1000);

            // just a normal GPO
            gpos.getGpo((short) 4).setMode(GpoMode.Normal);

            // Apply the new settings
            reader.applySettings(settings);

            // disconnect and reconnect to show GPO
            System.out.println("Disconnecting from " + hostname);
            reader.disconnect();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                // ignore this since its just an example and just keep going
            }
            System.out.println("Connecting to " + hostname);
            reader.connect(hostname);

            // delay so we can see the GPO state
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                // ignore this since its just an example and just keep going
            }

            // don't connect a listener so we dont get the reports
            // typical applications will still connect a tag listener 
            // reader.setTagReportListener(new TagReportListenerImplementation());

            // Start the reader
            System.out.println("Starting inventory on " + hostname);
            reader.start();

            // Set the GPO high, every three seconds.
            // The GPO will remain high for the period
            // specified by GpoPulseDurationMsec.
            for (int i = 0; i < 5; i++) {
                reader.setGpo(3, true);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    // ignore this since its just an example and just keep going
                }
            }

            System.out.println("Press Enter to exit.");
            Scanner s = new Scanner(System.in);
            s.nextLine();

            System.out.println("Stopping  " + hostname);
            reader.stop();

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
