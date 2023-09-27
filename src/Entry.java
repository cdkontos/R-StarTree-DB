import java.io.Serializable;
import java.util.ArrayList;

public class Entry implements Serializable {
    private BoundingBox boundingBox;
    private Long childNodeBlockID;

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

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

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
}
