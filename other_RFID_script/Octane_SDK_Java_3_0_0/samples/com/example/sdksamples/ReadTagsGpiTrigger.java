package com.example.sdksamples;

import com.impinj.octane.*;

import java.util.Scanner;


public class ReadTagsGpiTrigger {

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
            // reader.setTagReportListener(new
            // TagReportListenerImplementation());

            // Get the default settings
            Settings settings = reader.queryDefaultSettings();

            // turn this on to get tag reports. But only at the end of the spec
            reader.setTagReportListener(new TagReportListenerImplementation());
            settings.getReport().setMode(ReportMode.BatchAfterStop);

            // turn on these listeners to see how the GPI triggers
            reader.setGpiChangeListener(
                    new GpiChangeListenerImplementation());
            reader.setReaderStopListener(
                    new ReaderStopListenerImplementation());
            reader.setReaderStartListener(
                    new ReaderStartListenerImplementation());

            // include this so we know where it was seen but only when we stop
            // it
            settings.getReport().setIncludeAntennaPortNumber(true);

            // enable this GPI and set some debounce
            settings.getGpis().get(1).setIsEnabled(true);
            settings.getGpis().get(1).setPortNumber(1);
            settings.getGpis().get(1).setDebounceInMs(50);

            // set autostart to go on GPI level
            settings.getAutoStart().setGpiPortNumber(1);
            settings.getAutoStart().setMode(AutoStartMode.GpiTrigger);
            settings.getAutoStart().setGpiLevel(true);

            // if you set start, you have to set stop
            settings.getAutoStop().setMode(AutoStopMode.GpiTrigger);
            settings.getAutoStop().setGpiPortNumber(1);
            settings.getAutoStop().setGpiLevel(false);
            settings.getAutoStop().setTimeout(60000);

            // Apply the new settings
            reader.applySettings(settings);

            System.out.println("Press Enter To Exit.");
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
