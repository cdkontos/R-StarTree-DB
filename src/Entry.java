import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class Entry implements Serializable {
    private BoundingBox boundingBox;
    private Long childNodeBlockID;
    private long recordID;

    Entry(Node child)
    {
        this.childNodeBlockID = child.getBlockID();
        adjustBoxEntries(child.getEntries());
    }
    Entry(BoundingBox boundingBox)
    {
        this.boundingBox = boundingBox;
    }
    void setChildNodeBlockID(Long childNodeBlockID)
    {
        this.childNodeBlockID = childNodeBlockID;
    }

    void setRecordId(Long recordId) {this.recordID = recordId;}


    public BoundingBox getBoundingBox() {return boundingBox;}

    public Long getRecordId() {return recordID;}
    public Long getChildNodeBlockID() {
        return childNodeBlockID;
    }

    void adjustBoxEntries(ArrayList<Entry> entries)
    {
        boundingBox = new BoundingBox(Bounds.findMinBounds(entries));
    }

    void adjustBoxEntry(Entry entry)
    {
        boundingBox = new BoundingBox(Bounds.findMinBounds(boundingBox,entry.getBoundingBox()));
    }

    public Record getRecord(Map<Long, Record> recordMap) {
        if (recordMap != null && recordMap.containsKey(recordID)) {
            // Retrieve and return the Record object from the map based on recordId
            return recordMap.get(recordID);
        } else {
            // Return null or handle the case where the Record is not found
            return null;
        }
    }

    public Node getChildNode(Map<Long, Node> nodeMap) {
        if (nodeMap != null && nodeMap.containsKey(childNodeBlockID)) {
            // Retrieve and return the Node object from the map based on childNodeBlockID
            return nodeMap.get(childNodeBlockID);
        } else {
            // Return null or handle the case where the Node is not found
            return null;
        }
    }
}
