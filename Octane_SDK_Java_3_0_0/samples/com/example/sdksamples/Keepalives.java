package com.example.sdksamples;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Settings;

import java.util.Scanner;


public class Keepalives {

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

            // turn on the keepalives
            settings.getKeepalives().setEnabled(true);
            settings.getKeepalives().setPeriodInMs(3000);

            // turn on automatic link monitoring
            settings.getKeepalives().setEnableLinkMonitorMode(true);
            settings.getKeepalives().setLinkDownThreshold(5);

            // set up a listener for keepalives
            reader.setKeepaliveListener(new KeepAliveListenerImplementation());

            // set up a listener for connection Lost
            reader.setConnectionLostListener(
                    new ConnectionLostListenerImplementation());

            // apply the settings to enable keepalives
            reader.applySettings(settings);

            System.out.println("Press Enter to exit.");
            Scanner s = new Scanner(System.in);
            s.nextLine();

            reader.disconnect();
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }
}
