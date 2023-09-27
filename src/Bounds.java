import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Bounds implements Serializable {
    private final double lower;
    private final double upper;

    public Bounds(double lower, double upper) {
        if(lower<=upper)
        {
            this.lower = lower;
            this.upper = upper;
        }
        else
            throw new IllegalArgumentException( "The lower bound value cannot be bigger than that of the upper bound.");
    }

    public double getLower() {
        return lower;
    }

    public double getUpper() {
        return upper;
    }

    static ArrayList<Bounds> findMinBounds(ArrayList<Entry> entries)
    {
        ArrayList<Bounds> minBounds = new ArrayList<>();
        for (int i=0; i < 1 ; i++) //TODO ADD FILES
        {
            Entry lowerEntry = Collections.min(entries, new EntryCompare.EntryBoundCompare(entries,i,false));
            Entry upperEntry = Collections.max(entries, new EntryCompare.EntryBoundCompare(entries,i,true));
            minBounds.add(new Bounds(lowerEntry.getBoundingBox().getBounds().get(i).getLower(),upperEntry.getBoundingBox().getBounds().get(i).getUpper()));
        }
        return minBounds;
    }

    static ArrayList<Bounds> findMinBounds(BoundingBox boundingBoxA, BoundingBox boundingBoxB)
    {
        ArrayList<Bounds> minBounds = new ArrayList<>();
        for (int i = 0; i < 1; i++) //TODO ADD FILES
        {
            double lower = Math.min(boundingBoxA.getBounds().get(i).getLower(), boundingBoxB.getBounds().get(i).getLower());
            double upper = Math.max(boundingBoxA.getBounds().get(i).getUpper(), boundingBoxB.getBounds().get(i).getUpper());
            minBounds.add(new Bounds(lower,upper));
        }
        return minBounds;
    }
}
