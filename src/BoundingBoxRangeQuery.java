import java.util.ArrayList;

// Class used for executing a range query within a specific bounding box with the use of the RStarTree.
// Searches for records within that bounding box.
class BoundingBoxRangeQuery extends Query {
    private ArrayList<Long> qualifyingRecordIds; // Record ids used for queries
    private BoundingBox searchBoundingBox; // BoundingBox used for range queries

    BoundingBoxRangeQuery(BoundingBox searchBoundingBox) {
        this.searchBoundingBox = searchBoundingBox;
    }

    // Returns the IDs of the query's records.
    @Override
    ArrayList<Long> getQueryRecordIds(Node node) {
        qualifyingRecordIds = new ArrayList<>();
        search(node); // Start the search from the root node of the RStarTree.
        return qualifyingRecordIds;
    }

    // Search for records within searchBoundingBox.
    private void search(Node node) {
        // [Search subtrees]
        // If the current node is not a leaf, check each entry to determine whether it overlaps with the searchBoundingBox.
        if (node.getLevel() != RStarTree.getLeafLevel()) {
            for (Entry entry : node.getEntries()) {
                // For all overlapping entries, invoke the search on the tree whose root is
                // pointed to by E.childPTR.
                if (BoundingBox.checkBoxOverlap(entry.getBoundingBox(), searchBoundingBox)) {
                    search(FilesHelper.readIndexFileBlock(entry.getChildNodeBlockID()));
                }
            }
        }
        // [Search leaf node]
        // If the current node is a leaf, check all entries to determine whether they overlap with S.
        // If so, E is a qualifying record.
        else {
            for (Entry entry : node.getEntries()) {
                // For all overlapping entries, invoke the search on the tree whose root is
                // pointed to by E.childPTR.
                if (BoundingBox.checkBoxOverlap(entry.getBoundingBox(), searchBoundingBox)) {
                    LeafEntry leafEntry = (LeafEntry) entry;
                    qualifyingRecordIds.add(leafEntry.getRecordID());
                }
            }
        }
    }
}
