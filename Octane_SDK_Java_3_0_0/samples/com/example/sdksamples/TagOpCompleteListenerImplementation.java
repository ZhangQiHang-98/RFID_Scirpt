package com.example.sdksamples;

import com.impinj.octane.*;

public class TagOpCompleteListenerImplementation implements
        TagOpCompleteListener {

    public void onTagOpComplete(ImpinjReader reader, TagOpReport results) {
        System.out.println("TagOpComplete: ");
        for (TagOpResult t : results.getResults()) {
            System.out.print("  EPC: " + t.getTag().getEpc().toHexString());
            if (t instanceof TagReadOpResult) {
                TagReadOpResult tr = (TagReadOpResult) t;
                System.out.print(" READ: id: " + tr.getOpId());
                System.out.print(" sequence: " + tr.getSequenceId());
                System.out.print(" result: " + tr.getResult().toString());
                if (tr.getResult() == ReadResultStatus.Success) {
                    System.out.print(" data: " + tr.getData().toHexWordString());
                }
            }

            if (t instanceof TagWriteOpResult) {
                TagWriteOpResult tw = (TagWriteOpResult) t;
                System.out.print(" WRITE: id: " + tw.getOpId());
                System.out.print(" sequence: " + tw.getSequenceId());
                System.out.print(" result: " + tw.getResult().toString());
                System.out.print(" words_written: " + tw.getNumWordsWritten());
            }

            if (t instanceof TagKillOpResult) {
                TagKillOpResult tk = (TagKillOpResult) t;
                System.out.print(" KILL: id: " + tk.getOpId());
                System.out.print(" sequence: " + tk.getSequenceId());
                System.out.print(" result: " + tk.getResult().toString());
            }

            if (t instanceof TagLockOpResult) {
                TagLockOpResult tl = (TagLockOpResult) t;
                System.out.print(" LOCK: id: " + tl.getOpId());
                System.out.print(" sequence: " + tl.getSequenceId());
                System.out.print(" result: " + tl.getResult().toString());
            }

            if (t instanceof TagBlockPermalockOpResult) {
                TagBlockPermalockOpResult tbp = (TagBlockPermalockOpResult) t;
                System.out.print(" BLOCK_PERMALOCK id: " + tbp.getOpId());
                System.out.print(" sequence: " + tbp.getSequenceId());
                System.out.print(" result: " + tbp.getResult().toString());
            }

            if (t instanceof TagQtGetOpResult) {
                TagQtGetOpResult tqt = (TagQtGetOpResult) t;
                System.out.print(" QT_GET id: " + tqt.getOpId());
                System.out.print(" sequence: " + tqt.getSequenceId());
                System.out.print(" result: " + tqt.getResult().toString());
                if (tqt.getResult() == QtGetConfigResultStatus.Success) {
                    System.out.print(" mode: "
                            + tqt.getDataProfile().toString());
                    System.out.print(" range: "
                            + tqt.getAccessRange().toString());
                }
            }

            if (t instanceof TagQtSetOpResult) {
                TagQtSetOpResult tqt = (TagQtSetOpResult) t;
                System.out.print(" QT_SET id: " + tqt.getOpId());
                System.out.print(" sequence: " + tqt.getSequenceId());
                System.out.print(" result: " + tqt.getResult().toString());
            }

            System.out.println("");
        }
    }
}
