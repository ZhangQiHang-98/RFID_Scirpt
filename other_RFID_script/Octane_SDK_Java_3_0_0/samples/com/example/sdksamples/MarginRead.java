/*******************************************************************************
* IMPINJ CONFIDENTIAL AND PROPRIETARY
*
* This source code is the sole property of Impinj, Inc. Reproduction or
* utilization of this source code in whole or in part is forbidden without
* the prior written consent of Impinj, Inc.
*
* (c) Copyright Impinj, Inc. 2016. All rights reserved.
******************************************************************************/
package com.example.sdksamples;

import com.impinj.octane.*;

import java.util.ArrayList;
import java.util.Scanner;

// demonstrates margin read of tag memory 
public class MarginRead implements TagOpCompleteListener {

    public static void main(String[] args) {
        try {
            String hostname = System.getProperty(SampleProperties.hostname);

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            ImpinjReader reader = new ImpinjReader();
            String TARGET_EPC = "E280";

            // Connect
            System.out.println("Connecting to " + hostname);
            reader.connect(hostname);

            // Get the default settings
            Settings settings = reader.queryDefaultSettings();

            // Apply the new settings
            reader.applySettings(settings);

            // Create a tag operation sequence.
            // You can add multiple read, write, lock, kill and QT
            // operations to this sequence.
            TagOpSequence seq = new TagOpSequence();
            
            seq.setOps(new ArrayList<TagOp>());
            seq.setExecutionCount((short) 1);
            seq.setState(SequenceState.Active);
            seq.setId(1);

            seq.setTargetTag(new TargetTag());
            seq.getTargetTag().setBitPointer(BitPointers.Epc);
            seq.getTargetTag().setMemoryBank(MemoryBank.Epc);
            seq.getTargetTag().setData(TARGET_EPC);

            // Define a Margin Read operation.
            TagMarginReadOp marginReadOp = new TagMarginReadOp();

            // Define the mask to margin read.
            // A MarginReadMask can be created from a hexadecimal string or a bit string.
            // This mask is margin reading 1160.
            marginReadOp.setMarginMask(new MarginReadMask());
            marginReadOp.getMarginMask().setMaskFromHexString("1160");
            //marginReadOp.getMarginMask().setMaskFromBitString("0001000101100000");

            // Define the bit pointer (or "place to start looking") and the memory bank.
            // We're adding 16 to get to the second word of the EPC. Our TargetTag filter
            // already ensures the EPC starts with "E280"
            marginReadOp.setBitPointer(BitPointers.Epc + 16);
            marginReadOp.setMemoryBank(MemoryBank.Epc);

            // Add the margin operation to the tag operation sequence.
            seq.getOps().add(marginReadOp);

            // add to the reader. The reader supports multiple sequences
            reader.addOpSequence(seq);

            // Assign the TagOpComplete listener.
            // This specifies which method to call
            // when tag operations are complete.
            reader.setTagOpCompleteListener(new MarginRead());

            // Start the reader
            reader.start();

            System.out.println("Press Enter to exit.");
            Scanner s = new Scanner(System.in);
            s.nextLine();
            s.close();

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


    /**
     * Listener method called when a Tag Op Completes.
     *
     * @param reader The Impinj Reader
     * @param results The Tag Op Report
     */
    public void onTagOpComplete(ImpinjReader reader, TagOpReport results) {
        System.out.println("TagOpComplete: ");
        for (TagOpResult t : results.getResults()) {

            if (t instanceof TagMarginReadOpResult) {
                TagMarginReadOpResult mrResult = (TagMarginReadOpResult)t;
                System.out.println("Margin Read Complete (" + mrResult.getTag().getEpc() + ") " + mrResult.getResult());
            }
        }
    }
}
