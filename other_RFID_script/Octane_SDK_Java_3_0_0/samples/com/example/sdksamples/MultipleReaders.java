package com.example.sdksamples;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Settings;

import java.util.ArrayList;
import java.util.Scanner;

public class MultipleReaders {

    static ArrayList<ImpinjReader> readers = new ArrayList<ImpinjReader>();

    public static void main(String[] args) {

        // Connect to the reader.
        if (args.length < 1) {
            System.out.print(
                    "Must pass at least one reader hostname or IP as argument 1");
            return;
        }

        for (int i = 0; i < args.length; i++) {
            String name = "Reader_" + args[i];
            ImpinjReader reader = new ImpinjReader();
            reader.setName(name);

            try {
                System.out.println("Attempting connection to " + name);
                reader.connect(args[i]);

            } catch (OctaneSdkException ex) {
                // keep trying other readers if this doesn't work
                System.out.println("Error Connecting  to " + name + ": "
                        + ex.toString() + "...continuing with other readers");
                continue;
            }

            try {
                Settings settings = reader.queryDefaultSettings();
                System.out.println("Applying Settings to " + name);
                reader.applySettings(settings);

                reader.setTagReportListener(
                        new TagReportListenerImplementation());

                System.out.println("Starting " + name);
                reader.start();
                readers.add(reader);
            } catch (OctaneSdkException ex) {
                System.out.println("Could not start reader " + name + ": "
                        + ex.toString());
            }
        }

        System.out.println("Press Enter to continue and read all tags.");
        Scanner s = new Scanner(System.in);
        s.nextLine();

        for (int i = 0; i < readers.size(); i++) {

            try {
                ImpinjReader reader = readers.get(i);
                reader.stop();
                reader.disconnect();
            } catch (OctaneSdkException ex) {
                System.out.println("Failed to stop reader: " + ex.getMessage());
            }

        }
    }
}
