import java.util.ArrayList;

public class LeafEntry extends Entry {
    private final long recordID;
    private final long dataFileBlockID;

    LeafEntry(long recordID, long dataFileBlockID, ArrayList<Bounds> recordBounds)
    {
        super(new BoundingBox(recordBounds));
        this.recordID = recordID;
        this.dataFileBlockID = dataFileBlockID;
    }

    public long getRecordID()
    {
        return recordID;
    }

    public long getDataFileBlockID() {
        return dataFileBlockID;
    }
}

