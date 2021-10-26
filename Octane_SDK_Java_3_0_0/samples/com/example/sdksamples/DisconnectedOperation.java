package com.example.sdksamples;

import com.impinj.octane.AutoStartMode;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Settings;

import java.util.Scanner;


public class DisconnectedOperation {

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

            // dont' connect a listener here because it may be hard to see the
            // printouts
            // reader.setTagReportListener(new TagReportListenerImplementation());

            // Get the default settings
            Settings settings = reader.queryDefaultSettings();

            // set the start trigger to immediate
            settings.getAutoStart().setMode(AutoStartMode.Immediate);

            // dont' send events and reports when disconnected
            settings.setHoldReportsOnDisconnect(true);

            settings.getReport().setIncludeAntennaPortNumber(true);
            settings.getReport().setIncludeFirstSeenTime(true);

            // Apply the new settings
            reader.applySettings(settings);
            // save the settings into the reader so they live through reboot in
            // case the reader reboots while disconnected
            reader.saveSettings();

            // disconnect but keep going
            reader.disconnect();

            System.out.println("Reader running in disconnect operation .");
            Thread.sleep(3000);

            System.out.println("Reboot the reader and press enter.");
            Scanner s = new Scanner(System.in);
            s.nextLine();

            System.out.println("After reader comes up and starts inventorying "
                    + "press enter.");
            s = new Scanner(System.in);
            s.nextLine();

            reader.setTagReportListener(new TagReportListenerImplementation());

            System.out.println("Reconnecting to get data ");
            reader.connect(hostname);

            System.out.println("Resuming events and reports ");
            Thread.sleep(2000); // so you can read this
            reader.resumeEventsAndReports();

            System.out.println("Stopping  " + hostname);
            reader.stop();

            // save the defaults back to the device
            reader.applyDefaultSettings();
            reader.saveSettings();

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
