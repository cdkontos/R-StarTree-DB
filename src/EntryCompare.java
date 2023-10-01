import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
/**
 * A utility class containing nested comparator classes for comparing entries based on various criteria.
 *
 * @author Christos Kontos
 */
public class EntryCompare {
    /**
     * A comparator class for comparing entries based on their bounding box bounds in a specific dimension.
     */
    static class EntryBoundCompare implements Comparator<Entry> {
        private HashMap<Entry, Double> entryCompareMap;

        EntryBoundCompare(List<Entry> entries, int dimension, boolean compareByUpper) {
            this.entryCompareMap = new HashMap<>();
            if (compareByUpper) {
                for (Entry entry : entries) {
                    entryCompareMap.put(entry, entry.getBoundingBox().getBounds().get(dimension).getUpper());
                }
            } else {
                for (Entry entry : entries) {
                    entryCompareMap.put(entry, entry.getBoundingBox().getBounds().get(dimension).getLower());
                }
            }
        }

        public int compare(Entry entry1, Entry entry2) {
            return Double.compare(entryCompareMap.get(entry1), entryCompareMap.get(entry2));
        }
    }

    /**
     * A comparator class for comparing entries based on their distance from a specified point.
     */
    static class EntryDistanceFromPointCompare implements Comparator<Entry> {
        // Hash-map  used for mapping the comparison value of the Entries during the compare method
        // Key of the hash-map is the given Entry
        // Value of the hash-map is the given Entry's BoundingBox distance from the given point
        private HashMap<Entry, Double> entryComparisonMap;

        EntryDistanceFromPointCompare(List<Entry> entriesToCompare, ArrayList<Double> point) {
            // Initialising Hash-map
            this.entryComparisonMap = new HashMap<>();

            for (Entry entry : entriesToCompare)
                entryComparisonMap.put(entry, entry.getBoundingBox().findMinPointDistance(point));
        }

        public int compare(Entry entryA, Entry entryB) {
            return Double.compare(entryComparisonMap.get(entryA), entryComparisonMap.get(entryB));
        }
    }

    /**
     * A comparator class for comparing entries based on their area enlargement when combined with a bounding box.
     */
    static class EntryEnlargementCompare implements Comparator<Entry> {
            private HashMap<Entry, ArrayList<Double>> entryCompareMap;

            public EntryEnlargementCompare(List<Entry> entries, BoundingBox boundingBox) {
                this.entryCompareMap = new HashMap<>();
                for (Entry entry : entries) {
                    BoundingBox entryNewBox = new BoundingBox(Bounds.findMinBounds(entry.getBoundingBox(), boundingBox));
                    ArrayList<Double> values = new ArrayList<>();
                    values.add(entry.getBoundingBox().getArea());
                    double areaEnlargement = entryNewBox.getArea() - entry.getBoundingBox().getArea();
                    if (areaEnlargement < 0) {
                        throw new IllegalStateException("The enlargement cannot be negative");
                    }
                    values.add(areaEnlargement);
                    entryCompareMap.put(entry, values);
                }
            }

            @Override
            public int compare(Entry o1, Entry o2) {
                double areaEnlargement1 = entryCompareMap.get(o1).get(1);
                double areaEnlargement2 = entryCompareMap.get(o2).get(1);
                if (areaEnlargement1 == areaEnlargement2) {
                    return Double.compare(entryCompareMap.get(o1).get(0), entryCompareMap.get(o2).get(0));
                } else {
                    return Double.compare(areaEnlargement1, areaEnlargement2);
                }
            }
        }


    /**
     * A comparator class for comparing entries based on their overlap enlargement with a bounding box.
     */
        static class EntryEnlargementOverlapCompare implements Comparator<Entry> {
            private BoundingBox boundingBox;
            private ArrayList<Entry> entries;

            private HashMap<Entry, Double> entryCompareMap;

            public EntryEnlargementOverlapCompare(List<Entry> entriesCompare, BoundingBox boundingBox, ArrayList<Entry> entries) {
                this.boundingBox = boundingBox;
                this.entries = entries;
                this.entryCompareMap = new HashMap<>();

                for (Entry entry : entriesCompare) {
                    double overlapEntry = calculateEntryOverlap(entry, entry.getBoundingBox());
                    Entry newEntry = new Entry(new BoundingBox(Bounds.findMinBounds(entry.getBoundingBox(), boundingBox)));
                    double newEntryOverlap = calculateEntryOverlap(entry, newEntry.getBoundingBox());
                    double overlapEnlargementEntry = newEntryOverlap - overlapEntry;

                    if (overlapEnlargementEntry < 0) {
                        throw new IllegalStateException("The enlargement cannot be negative.");
                    }
                    entryCompareMap.put(entry, overlapEnlargementEntry);
                }
            }


            private double calculateEntryOverlap(Entry entry, BoundingBox boundingBox) {
                double sum = 0;
                for (Entry entry1 : entries) {
                    if (entry1 != entry) {
                        sum += BoundingBox.calcOverlapVal(boundingBox, entry1.getBoundingBox());
                    }
                }
                return sum;
            }

            @Override
            public int compare(Entry o1, Entry o2) {
                double overlapEnlargementEntry1 = entryCompareMap.get(o1);
                double overlapEnlargementEntry2 = entryCompareMap.get(o2);

                if (overlapEnlargementEntry1 == overlapEnlargementEntry2) {
                    ArrayList<Entry> entriesCompare = new ArrayList<>();
                    entriesCompare.add(o1);
                    entriesCompare.add(o2);
                    return new EntryEnlargementCompare(entriesCompare, boundingBox).compare(o1, o2);
                } else {
                    return Double.compare(overlapEnlargementEntry1, overlapEnlargementEntry2);
                }
            }
        }

    /**
     * A comparator class for comparing entries based on their distance from the center of a bounding box.
     */
        static class EntryDistanceCenterCompare implements Comparator<Entry> {
            private HashMap<Entry, Double> entryCompareMap;

            public EntryDistanceCenterCompare(List<Entry> entries, BoundingBox boundingBox) {
                this.entryCompareMap = new HashMap<>();
                for (Entry entry : entries) {
                    entryCompareMap.put(entry, BoundingBox.findBoundBoxDist(entry.getBoundingBox(), boundingBox));
                }
            }

            public int compare(Entry o1, Entry o2) {
                return Double.compare(entryCompareMap.get(o1), entryCompareMap.get(o2));
            }
        }


    }

