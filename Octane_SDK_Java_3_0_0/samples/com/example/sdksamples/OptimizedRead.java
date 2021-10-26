package com.example.sdksamples;

import com.impinj.octane.*;

import java.util.Scanner;


public class OptimizedRead {

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

            // Get the default settings
            Settings settings = reader.queryDefaultSettings();

            settings.getReport().setIncludeAntennaPortNumber(true);

            // read two words from the start of user memory on all tags 
            TagReadOp readUser = new TagReadOp();
            readUser.setMemoryBank(MemoryBank.User);
            readUser.setWordCount((short) 2);
            readUser.setWordPointer((short) 0);
            readUser.Id = 222;

            // reader the non-serialzed part of the TID (first 2 words)
            TagReadOp readTid = new TagReadOp();
            readTid.setMemoryBank(MemoryBank.Tid);
            readTid.setWordPointer((short) 0);
            readTid.setWordCount((short) 2);
            readTid.Id = 333;

            // add to the optimized read operations
            settings.getReport().getOptimizedReadOps().add(readUser);
            settings.getReport().getOptimizedReadOps().add(readTid);

            // set up listeners to hear stuff back from SDK
            reader.setTagReportListener(
                    new TagReportListenerImplementation());
            reader.setTagOpCompleteListener(
                    new TagOpCompleteListenerImplementation());

            // Apply the new settings
            reader.applySettings(settings);

            // Start the reader
            reader.start();

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
