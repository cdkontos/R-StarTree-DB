import java.util.ArrayList;

public class DistributionGroup {
    private final ArrayList<Entry> entries;
    private final BoundingBox boundingBox;

    public DistributionGroup(ArrayList<Entry> entries, BoundingBox boundingBox) {
        this.entries = entries;
        this.boundingBox = boundingBox;
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }
}
