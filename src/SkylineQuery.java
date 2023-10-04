import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
/**
 * Class for skyline queries execution with the use of the RStarTree
 *
 * @author Akompian Georgios
*/
 class SkylineQuery extends Query {
    //private ArrayList<Double> queryPoint; // The query point for skyline computation

    SkylineQuery() {
        //this.querypoint=querypoint;
    }

    @Override
    ArrayList<Long> getQueryRecordIds(Node node) {
        ArrayList<Long> skylineRecords = new ArrayList<>();
        getSkyline(node, skylineRecords);
        return skylineRecords;
    }

    private void getSkyline(Node node, ArrayList<Long> skylineRecords) {
        Set<Entry> skylineSet = new HashSet<>(); // Maintain a set of skyline points

        if (node.isLeaf()) {
            // Leaf node, check each entry against the query point
            for (Entry entry : node.getEntries()) {
                boolean potentialSkyline = true;

                // Check if entry is dominated by any existing skyline points
                Iterator<Entry> iterator = skylineSet.iterator();
                while (iterator.hasNext()) {
                    Entry skylineEntry = iterator.next();
                    if (entry.dominates(skylineEntry)) {
                        iterator.remove(); // Remove dominated skyline point
                    } else if (skylineEntry.dominates(entry)) {
                        potentialSkyline = false; // Entry is dominated, skip it
                        break;
                    }
                }

                if (potentialSkyline) {
                    skylineSet.add(entry); // Add the entry to the skyline set
                }
            }
        } else {
            // Non-leaf node, recursively visit child nodes
            for (Entry entry : node.getEntries()) {
                Node childNode = FilesHelper.readIndexFileBlock(entry.getChildNodeBlockID());
                if (childNode != null) {
                    getSkyline(childNode, skylineRecords);
                }
            }
        }

        // Add the skyline records from the set to the result list
        for (Entry skylineEntry : skylineSet) {
            skylineRecords.add(skylineEntry.getRecordId());
        }
    }


}