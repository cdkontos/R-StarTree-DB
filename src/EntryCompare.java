import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class EntryCompare {
    static class EntryBoundCompare implements Comparator<Entry>
    {
        private final HashMap<Entry,Double> entryCompareMap;

        EntryBoundCompare(List<Entry> entries, int dimension, boolean compareByUpper)
        {
            this.entryCompareMap = new HashMap<>();
            if(compareByUpper)
            {
                for (Entry entry : entries) {
                    entryCompareMap.put(entry,entry.getBoundingBox().getBounds().get(dimension).getUpper());
                }
            }
            else
            {
                for (Entry entry : entries) {
                    entryCompareMap.put(entry,entry.getBoundingBox().getBounds().get(dimension).getLower());
                }
            }
        }

        public int compare(Entry entry1, Entry entry2) {
            return Double.compare(entryCompareMap.get(entry1),entryCompareMap.get(entry2));
        }
    }


}
