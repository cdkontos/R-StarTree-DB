import java.util.ArrayList;
/**
 * A class representing a group of entries and their associated bounding box within a distribution.
 *
 * @author Christos Kontos
 */
public class DistributionGroup {
    private final ArrayList<Entry> entries;
    private final BoundingBox boundingBox;

    /**
     * Constructs a DistributionGroup object with the specified entries and bounding box.
     *
     * @param entries     The list of entries in the distribution group.
     * @param boundingBox The bounding box that encompasses the entries in the group.
     */
    public DistributionGroup(ArrayList<Entry> entries, BoundingBox boundingBox) {
        this.entries = entries;
        this.boundingBox = boundingBox;
    }

    /**
     * Gets the list of entries in the distribution group.
     *
     * @return The list of entries in the distribution group.
     */
    public ArrayList<Entry> getEntries() {
        return entries;
    }

    /**
     * Gets the bounding box that encompasses the entries in the distribution group.
     *
     * @return The bounding box of the distribution group.
     */
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }
}
