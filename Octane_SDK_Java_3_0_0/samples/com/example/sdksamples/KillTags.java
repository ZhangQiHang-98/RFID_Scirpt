package com.example.sdksamples;

import com.impinj.octane.*;

import java.util.Scanner;

public class KillTags implements TagOpCompleteListener {

    static String killPassword = "1234abcd";
    // you had better set this to kill the tag you want
    String targetEpc;
    ImpinjReader reader;

    public static void main(String[] args) {

        KillTags kt = new KillTags();

        kt.run();

    }

    void run() {

        try {
            String hostname = System.getProperty(SampleProperties.hostname);

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            targetEpc = System.getProperty(SampleProperties.targetTag);

            // Always use a target tag for kill
            // because the action is not reversible
            // since its not reversible, we make the example set this property
            if (targetEpc == null) {
                throw new Exception("You must specify the '"
                        + SampleProperties.targetTag + "' property "
                        + "for this example as the block permalock command "
                        + "is not reversible");
            }

            reader = new ImpinjReader();

            // Connect
            System.out.println("Connecting to " + hostname);
            reader.connect(hostname);

            // Get the default settings
            Settings settings = reader.queryDefaultSettings();

            settings.getReport().setIncludeAntennaPortNumber(true);

            // Apply the new settings
            reader.applySettings(settings);

            TagOpSequence seq = new TagOpSequence();
            // just try once in this example
            seq.setExecutionCount((short) 1);

            // always use a target tag for kill
            // because the action is not reversible
            seq.setTargetTag(new TargetTag());
            seq.getTargetTag().setBitPointer(BitPointers.Epc);
            seq.getTargetTag().setMemoryBank(MemoryBank.Epc);
            seq.getTargetTag().setData(targetEpc);

            // create a operation to write the kill password.
            // tags cannot be killed with a zero kill password.
            TagWriteOp setPassword = new TagWriteOp();
            setPassword.setMemoryBank(MemoryBank.Reserved);
            setPassword.setWordPointer(WordPointers.KillPassword);
            setPassword.setData(TagData.fromHexString(killPassword));

            seq.getOps().add(setPassword);
            reader.addOpSequence(seq);

            // set up listeners to hear stuff back from SDK

            // this one is optional but you can have it on to prove the tag was
            // killed
            //reader.setTagReportListener(new TagReportListenerImplementation());

            // this one gets the operation complete on the kill password and
            // performs the kill
            reader.setTagOpCompleteListener(this);

            System.out.println("Trying to kill tag matching EPC pattern " +
                    targetEpc);

            System.out.println("Writing access password ");
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

    public void onTagOpComplete(ImpinjReader reader, TagOpReport results) {
        System.out.println("TagOpComplete: ");
        for (TagOpResult t : results.getResults()) {
            System.out.print("  EPC: " + t.getTag().getEpc().toHexString());

            if (t instanceof TagWriteOpResult) {
                TagWriteOpResult tw = (TagWriteOpResult) t;
                System.out.print(" write id: " + tw.getOpId());
                System.out.print(" sequence: " + tw.getSequenceId());
                System.out.print(" result: " + tw.getResult().toString());
                System.out.println(" words_written: " + tw.getNumWordsWritten());

                // if this succeeded, go ahead and add the op Spec to kill the
                // tag

                if (tw.getResult() == WriteResultStatus.Success) {
                    System.out.println("Adding Kill Command ");
                    addKillSequence();
                } else {
                    System.out.println("Write command failed ");
                }
            } else if (t instanceof TagKillOpResult) {
                TagKillOpResult tk = (TagKillOpResult) t;
                System.out.print(" kill id: " + tk.getOpId());
                System.out.print(" sequence: " + tk.getSequenceId());
                System.out.println(" result: " + tk.getResult().toString());
            } else {
                System.out.println("Unhandled operation");
            }
        }
    }

    private void addKillSequence() {
        TagOpSequence seq = new TagOpSequence();
        // just try once in this example
        seq.setExecutionCount((short) 1);

        try {
            // try to always use a target tag for kill
            // because the action is not reversible
            // since its not reversible, we make the example set this property
            if (targetEpc != null) {
                seq.setTargetTag(new TargetTag());
                seq.getTargetTag().setBitPointer(BitPointers.Epc);
                seq.getTargetTag().setMemoryBank(MemoryBank.Epc);
                seq.getTargetTag().setData(targetEpc);
            } else {
                seq.setTargetTag(null);
            }

            TagKillOp tagKill = new TagKillOp();
            tagKill.setKillPassword(TagData.fromHexString(killPassword));

            seq.getOps().add(tagKill);
            reader.addOpSequence(seq);
        } catch (OctaneSdkException e) {
            System.out.println("Could not add kill operation");
        }

    }
}
