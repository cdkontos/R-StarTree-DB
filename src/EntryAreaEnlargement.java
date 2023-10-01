/**
 * A class representing an entry along with its area enlargement value, used for comparison.
 *
 * @author Christos Kontos
 */
public class EntryAreaEnlargement implements Comparable{

    private final Entry entry;
    private final double areaEnlargement;

    /**
     * Constructs an EntryAreaEnlargement object with the specified entry and area enlargement value.
     *
     * @param entry           The entry to associate with area enlargement.
     * @param areaEnlargement The area enlargement value for the associated entry.
     */
    public EntryAreaEnlargement(Entry entry, double areaEnlargement) {
        this.entry = entry;
        this.areaEnlargement = areaEnlargement;
    }

    /**
     * Gets the entry associated with this EntryAreaEnlargement object.
     *
     * @return The entry associated with this EntryAreaEnlargement.
     */
    public Entry getEntry() {
        return entry;
    }

    /**
     * Gets the area enlargement value associated with this EntryAreaEnlargement object.
     *
     * @return The area enlargement value.
     */
    private double getAreaEnlargement() {
        return areaEnlargement;
    }

    /**
     * Compares this EntryAreaEnlargement object to another object for ordering.
     *
     * @param o The object to compare to.
     * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater
     *         than the specified object.
     */
    @Override
    public int compareTo(Object o) {
        EntryAreaEnlargement ob = (EntryAreaEnlargement) o;
        if(this.getAreaEnlargement() == ob.getAreaEnlargement())
        {
            return Double.compare(this.getEntry().getBoundingBox().getArea(),ob.getEntry().getBoundingBox().getArea());
        }
        else
            return Double.compare(this.getAreaEnlargement(),ob.getAreaEnlargement());
    }
}
