public class EntryAreaEnlargement implements Comparable{

    private final Entry entry;
    private final double areaEnlargement;

    public EntryAreaEnlargement(Entry entry, double areaEnlargement) {
        this.entry = entry;
        this.areaEnlargement = areaEnlargement;
    }

    public Entry getEntry() {
        return entry;
    }

    private double getAreaEnlargement() {
        return areaEnlargement;
    }

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
