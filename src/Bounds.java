import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
/**
 * A class representing bounds in an n-dimensional space. Bounds consist of a lower and upper value for a dimension.
 * @author Christos Kontos
 */
public class Bounds implements Serializable {
    private double lower;
    private double upper;

    /**
     * Constructs Bounds with the specified lower and upper values.
     * @param lower The lower bound value for the dimension.
     * @param upper The upper bound value for the dimension.
     * @throws IllegalArgumentException If the lower bound value is greater than the upper bound value.
     */
    public Bounds(double lower, double upper) {
        if(lower<=upper)
        {
            this.lower = lower;
            this.upper = upper;
        }
        else
            throw new IllegalArgumentException( "The lower bound value cannot be bigger than that of the upper bound.");
    }

    /**
     * Gets the lower bound value.
     * @return The lower bound value for the dimension.
     */
    public double getLower() {
        return lower;
    }

    /**
     * Gets the upper bound value.
     * @return The upper bound value for the dimension.
     */
    public double getUpper() {
        return upper;
    }

    /**
     * Finds the minimum bounds for a list of entries along each dimension.
     * @param entries A list of entries to find minimum bounds from.
     * @return An ArrayList of Bounds, where each Bounds object represents the minimum bounds along a dimension.
     */
    static ArrayList<Bounds> findMinBounds(ArrayList<Entry> entries)
    {
        ArrayList<Bounds> minBounds = new ArrayList<>();
        for (int i=0; i < FilesHelper.getDataDimensions() ; i++)
        {
            Entry lowerEntry = Collections.min(entries, new EntryCompare.EntryBoundCompare(entries,i,false));
            Entry upperEntry = Collections.max(entries, new EntryCompare.EntryBoundCompare(entries,i,true));
            minBounds.add(new Bounds(lowerEntry.getBoundingBox().getBounds().get(i).getLower(),upperEntry.getBoundingBox().getBounds().get(i).getUpper()));
        }
        return minBounds;
    }

    /**
     * Finds the minimum bounds between two bounding boxes along each dimension.
     * @param boundingBoxA The first bounding box.
     * @param boundingBoxB The second bounding box.
     * @return An ArrayList of Bounds, where each Bounds object represents the minimum bounds between the two bounding boxes along a dimension.
     */
    static ArrayList<Bounds> findMinBounds(BoundingBox boundingBoxA, BoundingBox boundingBoxB)
    {
        ArrayList<Bounds> minBounds = new ArrayList<>();
        for (int i = 0; i < FilesHelper.getDataDimensions(); i++)
        {
            double lower = Math.min(boundingBoxA.getBounds().get(i).getLower(), boundingBoxB.getBounds().get(i).getLower());
            double upper = Math.max(boundingBoxA.getBounds().get(i).getUpper(), boundingBoxB.getBounds().get(i).getUpper());
            minBounds.add(new Bounds(lower,upper));
        }
        return minBounds;
    }
}
