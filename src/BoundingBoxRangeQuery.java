import java.util.ArrayList;
/**
 * The BoundingBoxRangeQuery class is used for executing a range query within a specific bounding box
 * using the RStarTree index. It searches for records that fall within the specified bounding box.
 *
 * @author Akompian Georgios
 */
class BoundingBoxRangeQuery extends Query {
    private ArrayList<Long> qualifyingRecordIds; // Record ids used for queries
    private BoundingBox searchBoundingBox; // BoundingBox used for range queries

    /**
     * Constructs a new BoundingBoxRangeQuery with the specified searchBoundingBox.
     *
     * @param searchBoundingBox The BoundingBox defining the range query area.
     */
    BoundingBoxRangeQuery(BoundingBox searchBoundingBox) {
        this.searchBoundingBox = searchBoundingBox;
    }



    /**
     * Returns the IDs of the records that fall within the specified bounding box.
     *
     * @param node The root node of the RStarTree to start the query from.
     * @return An ArrayList containing the qualifying record IDs.
     */
    @Override
    ArrayList<Long> getQueryRecordIds(Node node) {
        qualifyingRecordIds = new ArrayList<>();
        search(node); // Start the search from the root node of the RStarTree.
        return qualifyingRecordIds;
    }



    /**
     * Recursively searches for records within the searchBoundingBox.
     *
     * @param node The current node being examined in the RStarTree.
     */
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
