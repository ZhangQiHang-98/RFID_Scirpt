package top.kotara.rfid.signaldispaly;

import com.impinj.octane.*;

import java.util.List;

public class OnTagOpCompleteListener implements TagOpCompleteListener {
    @Override
    public void onTagOpComplete(ImpinjReader impinjReader, TagOpReport tagOpReport) {
        List<TagOpResult> results=tagOpReport.getResults();
        for(TagOpResult result : results)
        {
            if (result instanceof TagWriteOpResult)
            {
                // These are the results of settings the access password.
                // Cast it to the correct type.
                TagWriteOpResult writeResult = (TagWriteOpResult)result;
                // Print out the results.
                System.out.println("Set access password complete.");
                System.out.println("EPC : "+ writeResult.getTag().getEpc().toString());
                System.out.println("Tid : "+ writeResult.getTag().getTid().toString());
                System.out.println("Status : "+ writeResult.getResult().toString());
                System.out.println("Number of words written : "+ writeResult.getNumWordsWritten());
            }else if (result instanceof TagReadOpResult)
            {
                // These are the results of settings the access password.
                // Cast it to the correct type.
                TagReadOpResult tagReadOpResult = (TagReadOpResult)result;
                // Print out the results.
                System.out.println("get Tid complete.");
                System.out.println("EPC : "+ tagReadOpResult.getTag().getEpc().toString());
                System.out.println("Tid : "+ tagReadOpResult.getTag().getTid().toString());
                System.out.println("Status : "+ tagReadOpResult.getResult().toString());
                System.out.println("Number of words written : "+ tagReadOpResult.getData());
            }
                else if (result instanceof TagLockOpResult)
            {
                // Cast it to the correct type.
                // These are the results of locking the access password or user memory.
                com.impinj.octane.TagLockOpResult lockResult = (TagLockOpResult)result;
                // Print out the results.
                System.out.println("get Tid complete.");
                System.out.println("EPC : "+ lockResult.getTag().getEpc());
                System.out.println("Tid : "+ lockResult.getTag().getTid().toString());
                System.out.println("Status : "+ lockResult.getResult());
            }
        }
    }
}
