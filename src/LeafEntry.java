import java.util.ArrayList;

/**
 * This class implements the entries on the leaf level of the RStarTree.
 * @author Christos Kontos
 */
public class LeafEntry extends Entry {
    private final long recordID;
    private final long dataFileBlockID;

    /**
     * The constructor for the leaf entry of the RStarTree.
     * @param recordID the recordID of the record being represented by the entry.
     * @param dataFileBlockID the dataFileBlockID of the block the record is in.
     * @param recordBounds the bounds of the record.
     */
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

