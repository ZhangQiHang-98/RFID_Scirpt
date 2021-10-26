package com.example.sdksamples;

import com.impinj.octane.*;

import java.util.Random;
import java.util.Scanner;


public class BulkReadWrite implements TagOpCompleteListener {

    static short numWordsInUserMemory = 32;
    static short maxReadWriteBlockSize = 8; // max supported by device is 32
    static ImpinjReader reader;
    static String tagData;
    static short numOpsAdded;
    static short numOpsExecuted;
    static short numWordsWritten;
    static Random rand = new Random();

    static void bulkRead(TagData accessPassword, MemoryBank bank,
                         short wordPointer, short wordCount) throws OctaneSdkException {

        tagData = "";
        numOpsExecuted = 0;
        numOpsAdded = 0;

        // Each TagReadOp can only access up to maxReadWriteBlockSize words.
        // So, we need to break this read up into multiple operations.
        while (wordCount > 0) {
            // Define a new tag operation sequence.
            TagOpSequence seq = new TagOpSequence();
            seq.setState(SequenceState.Active);
            seq.setExecutionCount((short) 1);
            seq.setId(wordCount); // just some unique ID
            seq.setSequenceStopTrigger(SequenceTriggerType.ExecutionCount);

            // Define a tag read operation
            TagReadOp op = new TagReadOp();
            op.setAccessPassword(accessPassword);
            op.setMemoryBank(bank);
            op.setWordPointer(wordPointer);
            op.setWordCount((wordCount < maxReadWriteBlockSize) ? wordCount
                    : maxReadWriteBlockSize);

            // Add the read op to the operation sequence
            seq.setTargetTag(null);
            seq.getOps().add(op);

            // Adjust the word count and pointer for the next reader operation
            wordCount -= op.getWordCount();
            wordPointer += op.getWordCount();

            // Add the operation sequence to the reader
            reader.addOpSequence(seq);
            numOpsAdded++;
        }
    }

    static void bulkWrite(TagData accessPassword, MemoryBank bank,
                          short wordPointer, TagData data) throws OctaneSdkException {

        short wordCount = (short) (data.getCountBytes() / 2);

        numOpsExecuted = 0;
        numOpsAdded = 0;
        numWordsWritten = 0;

        while (wordCount > 0) {
            // Define a new tag operation sequence.
            TagOpSequence seq = new TagOpSequence();
            seq.setState(SequenceState.Active);
            seq.setExecutionCount((short) 1);
            seq.setSequenceStopTrigger(SequenceTriggerType.ExecutionCount);
            seq.setId(wordCount); // just some unique ID

            // Define a tag read operation
            TagWriteOp op = new TagWriteOp();
            op.setAccessPassword(accessPassword);
            op.setMemoryBank(bank);
            op.setWordPointer(wordPointer);
            short opSizeWords = (wordCount < maxReadWriteBlockSize) ? wordCount
                    : maxReadWriteBlockSize;
            op.setData(TagData.fromWordList(data.toWordList().subList(
                    wordPointer, wordPointer + opSizeWords)));
            // Add the read op to the operation sequence
            seq.setTargetTag(null);
            seq.getOps().add(op);

            // Adjust the word count and pointer for the next reader operation
            wordCount -= opSizeWords;
            wordPointer += opSizeWords;

            // Add the operation sequence to the reader
            reader.addOpSequence(seq);
            numOpsAdded++;
        }
    }

    static TagData getRandomData(short numWords) throws OctaneSdkException {
        byte[] bytes = new byte[numWords * 2];
        rand.nextBytes(bytes);
        return TagData.fromByteArray(bytes);
    }

    public static void main(String[] args) {


        try {
            String hostname = System.getProperty(SampleProperties.hostname);

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            reader = new ImpinjReader();

            System.out.println("Connecting");
            reader.connect(hostname);

            Settings settings = reader.queryDefaultSettings();
            System.out.println("Applying Settings");

            settings.getReport().setMode(ReportMode.Individual);
            reader.applySettings(settings);

            reader.setTagOpCompleteListener(
                    new TagOpCompleteListenerImplementation());

            System.out.println("Starting");
            reader.start();

            System.out.println("Writing to tag, Press enter when operation "
                    + "completes ");
            TagData data = getRandomData(numWordsInUserMemory);
            bulkWrite(null, MemoryBank.User, (short) 0, data);
            Scanner s = new Scanner(System.in);
            s.nextLine();

            reader.deleteAllOpSequences();

            System.out.println("Reading from tag, Press enter when operation "
                    + "completes ");
            bulkRead(null, MemoryBank.User, (short) 0, numWordsInUserMemory);
            s.nextLine();

            reader.stop();
            reader.disconnect();
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    void handleReadOpComplete(TagReadOpResult tr) {

        numOpsExecuted++;

        if (tr.getResult() == ReadResultStatus.Success) {
            tagData += tr.getData().toHexWordString() + " ";

            if (numOpsExecuted == numOpsAdded) {
                System.out.println("Bulk read complete: " + tagData);
            }
        } else {
            System.out.print("Read operation failed: "
                    + tr.getResult().toString());

        }
    }

    void handleWriteOpComplete(TagWriteOpResult tw) {

        numOpsExecuted++;

        if (tw.getResult() == WriteResultStatus.Success) {
            numWordsWritten += tw.getNumWordsWritten();

            if (numOpsExecuted == numOpsAdded) {
                System.out.println("Bulk write complete" + numWordsWritten
                        + " written");
            }
        } else {
            System.out.print("Write operation failed: "
                    + tw.getResult().toString());

        }
    }

    public void onTagOpComplete(ImpinjReader reader, TagOpReport results) {
        System.out.println("TagOpComplete: ");
        for (TagOpResult t : results.getResults()) {

            if (t instanceof TagReadOpResult) {
                handleReadOpComplete((TagReadOpResult) t);

            }

            if (t instanceof TagWriteOpResult) {
                handleWriteOpComplete((TagWriteOpResult) t);
            }
        }
    }
}
