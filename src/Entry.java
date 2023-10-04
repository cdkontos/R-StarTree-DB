import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
/**
 * A class representing an entry in a node of an RStarTree.
 * @author Christos Kontos
 */
public class Entry implements Serializable {
    private BoundingBox boundingBox;
    private Long childNodeBlockID;
    private long recordID;

    /**
     * Constructs an Entry object based on a child node.
     * @param child The child node from which to create the entry.
     */
    Entry(Node child)
    {
        this.childNodeBlockID = child.getBlockID();
        adjustBoxEntries(child.getEntries());
    }

    /**
     * Constructs an Entry object with a specified bounding box.
     * @param boundingBox The bounding box associated with the entry.
     */
    Entry(BoundingBox boundingBox)
    {
        this.boundingBox = boundingBox;
    }

    /**
     * Sets the block ID of the child node associated with this entry.
     * @param childNodeBlockID The block ID of the child node.
     */
    void setChildNodeBlockID(Long childNodeBlockID)
    {
        this.childNodeBlockID = childNodeBlockID;
    }
    /**
     * Sets the record ID associated with this entry.
     * @param recordID The record ID to set.
     */
    void setRecordId(Long recordID) {
        this.recordID = recordID;
    }

    /**
     * Gets the bounding box associated with this entry.
     * @return The bounding box of the entry.
     */
    public BoundingBox getBoundingBox() {return boundingBox;}

    /**
     * Gets the record ID associated with this entry.
     * @return The record ID of the entry.
     */
    public Long getRecordId() {return recordID;}

    /**
     * Gets the block ID of the child node associated with this entry.
     * @return The block ID of the child node.
     */
    public Long getChildNodeBlockID() {
        return childNodeBlockID;
    }


    /**
     * Adjusts the bounding box of this entry based on a list of entries.
     * @param entries The list of entries to consider for bounding box adjustment.
     */
    void adjustBoxEntries(ArrayList<Entry> entries)
    {
        boundingBox = new BoundingBox(Bounds.findMinBounds(entries));
    }


    /**
     * Adjusts the bounding box of this entry based on another entry's bounding box.
     * @param entry The entry to consider for bounding box adjustment.
     */
    void adjustBoxEntry(Entry entry)
    {
        boundingBox = new BoundingBox(Bounds.findMinBounds(boundingBox,entry.getBoundingBox()));
    }

    /**
     * Retrieves the record associated with this entry from a record map.
     * @param recordMap The map containing record IDs and corresponding records.
     * @return The Record object associated with this entry, or null if not found.
     */
    public Record getRecord(Map<Long, Record> recordMap) {
        if (recordMap != null && recordMap.containsKey(recordID)) {
            // Retrieve and return the Record object from the map based on recordId
            return recordMap.get(recordID);
        } else {
            // Return null or handle the case where the Record is not found
            return null;
        }
    }

    /**
     * Retrieves the child node associated with this entry from a node map.
     * @param nodeMap The map containing block IDs and corresponding nodes.
     * @return The Node object associated with this entry, or null if not found.
     */
    public Node getChildNode(Map<Long, Node> nodeMap) {
        if (nodeMap != null && nodeMap.containsKey(childNodeBlockID)) {
            // Retrieve and return the Node object from the map based on childNodeBlockID
            return nodeMap.get(childNodeBlockID);
        } else {
            // Return null or handle the case where the Node is not found
            return null;
        }
    }

    /**
     * Checks if this entry is in the skyline with respect to a query point.
     * @param queryPoint The query point for skyline comparison.
     * @return True if this entry is in the skyline, false otherwise.
     */
    public boolean isSkyline(ArrayList<Double> queryPoint) {
        if (boundingBox == null) {
            // Entry does not have a bounding box, cannot determine skyline status
            return false;
        }

        // Assume that lower values are preferred (minimization)
        boolean dominates = true;

        for (int i = 0; i < queryPoint.size(); i++) {
            double queryValue = queryPoint.get(i);
            double entryValue = boundingBox.getCenter().get(i);

            if (entryValue > queryValue) {
                // Entry is worse in at least one dimension, it does not dominate the query point
                dominates = false;
                break;
            }
        }

        return dominates;
    }
    /**
     * Checks if this entry dominates another entry
     * @param otherEntry the other entry that will be used for the comparison
     * @return True if the entry dominates the other entry, false otherwise.
     */

    public boolean dominates(Entry otherEntry) {
        if (boundingBox == null || otherEntry == null) {
            // Entries without bounding boxes cannot dominate
            return false;
        }

        ArrayList<Double> thisCenter = boundingBox.getCenter();
        ArrayList<Double> otherCenter = otherEntry.boundingBox.getCenter();

        boolean dominatesInAllDimensions = true;
        boolean equalsInAnyDimension = false;

        for (int i = 0; i < thisCenter.size(); i++) {
            double thisValue = thisCenter.get(i);
            double otherValue = otherCenter.get(i);

            // This entry is equal in this dimension
            if (thisValue < otherValue) {
                equalsInAnyDimension = false; // This entry is worse in this dimension
                dominatesInAllDimensions = false;
                break;
            } else equalsInAnyDimension = !(thisValue > otherValue); // This entry is better in this dimension
        }

        return equalsInAnyDimension || dominatesInAllDimensions;
    }
}
