package com.example.sdksamples;

import com.impinj.octane.*;

import java.util.ArrayList;
import java.util.Scanner;


// demonstrates block permalocking of user memory 
public class BlockPermalock {

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
            seq.setExecutionCount((short) 1);
            seq.setState(SequenceState.Active);
            seq.setId(1);

            // lock the first block of memory. This only works on user memory
            TagBlockPermalockOp blockOp = new TagBlockPermalockOp();
            blockOp.setBlockMask(BlockPermalockMask.fromBlockNumber((short) 0));

            // add to the list
            seq.getOps().add(blockOp);

            String targetTag = System.getProperty("targetTag");

            // since its not reversible, we make the example set this property
            if (targetTag != null) {
                seq.setTargetTag(new TargetTag());
                seq.getTargetTag().setBitPointer((short) 32);
                seq.getTargetTag().setMemoryBank(MemoryBank.Epc);
                seq.getTargetTag().setData(targetTag);
            } else {
                throw new Exception("You must specify the 'targetTag' property "
                        + "for this example as the block permalock command "
                        + "is not reversible");
            }
            // add to the reader. The reader supports multiple sequences
            reader.addOpSequence(seq);

            // set up the listener for the tag operation
            reader.setTagOpCompleteListener(
                    new TagOpCompleteListenerImplementation());

            // typically the application would also listen for tag reports
            // but we don't here since it would print out too much
            // reader.setTagReportListener(new TagReportListenerImplementation());

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
