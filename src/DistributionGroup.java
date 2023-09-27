import java.util.ArrayList;

public class DistributionGroup {
    private ArrayList<Entry> entries;
    private BoundingBox boundingBox;

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
