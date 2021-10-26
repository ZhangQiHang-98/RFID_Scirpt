package com.example.sdksamples;

import com.impinj.octane.*;

import java.util.ArrayList;
import java.util.Scanner;


public class ReadUserMemory {

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

            // Apply the new settings
            reader.applySettings(settings);

            // create the reader op sequence
            TagOpSequence seq = new TagOpSequence();
            seq.setOps(new ArrayList<TagOp>());
            seq.setExecutionCount((short) 0); // forever
            seq.setState(SequenceState.Active);
            seq.setId(1);

            TagReadOp readOp = new TagReadOp();
            readOp.setMemoryBank(MemoryBank.User);
            readOp.setWordCount((short) 2);
            readOp.setWordPointer((short) 0);

            // add to the list
            seq.getOps().add(readOp);

            String targetEpc = System.getProperty(SampleProperties.targetTag);

            if (targetEpc != null) {
                seq.setTargetTag(new TargetTag());
                seq.getTargetTag().setBitPointer(BitPointers.Epc);
                seq.getTargetTag().setMemoryBank(MemoryBank.Epc);
                seq.getTargetTag().setData(targetEpc);
            } else {
                // or just send NULL to apply to all tags
                seq.setTargetTag(null);
            }

            // add to the reader. The reader supports multiple sequences
            reader.addOpSequence(seq);

            // set up listeners to hear stuff back from SDK. Normally
            // you would turn on a tag report listener, but we will keep
            // it disabled here so we can see what is going on 

            // reader.setTagReportListener(new
            // TagReportListenerImplementation());
            reader.setTagOpCompleteListener(
                    new TagOpCompleteListenerImplementation());

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
