import java.util.ArrayList;

// Class for skyline queries execution with the use of the RStarTree
class SkylineQuery extends Query {
    private ArrayList<Double> queryPoint; // The query point for skyline computation

    SkylineQuery(ArrayList<Double> queryPoint) {
        this.queryPoint = queryPoint;
    }

    @Override
    ArrayList<Long> getQueryRecordIds(Node node) {
        ArrayList<Long> skylineRecords = new ArrayList<>();
        getSkyline(node, skylineRecords);
        return skylineRecords;
    }

    // Recursive method to find skyline records in the RStarTree
    private void getSkyline(Node node, ArrayList<Long> skylineRecords) {
        if (node.isLeaf()) {
            // Leaf node, check each entry against the query point
            for (Entry entry : node.getEntries()) {
                if (entry.isSkyline(queryPoint)) {
                    skylineRecords.add(entry.getRecordId());
                }
            }
        } else {
            // Non-leaf node, recursively visit child nodes
            for (Entry entry : node.getEntries()) {
                Node childNode = FilesHelper.readIndexFileBlock(entry.getChildNodeBlockID());
                if (childNode != null && entry.isSkyline(queryPoint)) {
                    getSkyline(childNode, skylineRecords);
                }
            }
        }
    }
}