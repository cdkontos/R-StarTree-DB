import java.util.ArrayList;
/**
 * The SeqScanBoundingBoxRangeQuery class is used for executing a range query within a specific bounding box
 * without the use of an index. It searches for records within the specified bounding box.
 *
 * @author Akompian Georgios
 */
class SeqScanBoundingBoxRangeQuery extends SeqScanQuery {

    private ArrayList<Long> qualifyingRecordIds; // Record ids used for queries
    private BoundingBox searchBoundingBox; // Bounding box used for range queries

    /**
     * Constructs a SeqScanBoundingBoxRangeQuery with the given search bounding box.
     *
     * @param searchBoundingBox The bounding box used for range queries.
     */
    SeqScanBoundingBoxRangeQuery(BoundingBox searchBoundingBox) {
        this.searchBoundingBox = searchBoundingBox;
    }

    /**
     * Returns the IDs of the query's records that fall within the specified bounding box.
     *
     * @return The IDs of the records within the bounding box.
     */
    @Override
    ArrayList<Long> getQueryRecordIds() {
        qualifyingRecordIds = new ArrayList<>();
        search();
        return qualifyingRecordIds;
    }

    /** Method to search for records within the bounding box.
     *
     */
    private void search(){
        int blockId = 1;
        while(blockId < FilesHelper.getTotalBlocksInDatafile())
        {
            ArrayList<Record> recordsInBlock;
            recordsInBlock = FilesHelper.readDataFileBlock(blockId);
            ArrayList<LeafEntry> entries = new ArrayList<>();

            if (recordsInBlock != null)
            {
                for (Record record : recordsInBlock)
                {
                    ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
                    // Since we have to do with points as records we set low and upper to be same
                    for (int d = 0; d < FilesHelper.getDataDimensions(); d++)
                        boundsForEachDimension.add(new Bounds(record.getCoordinate(d), record.getCoordinate(d)));

                    entries.add(new LeafEntry(record.getId(), blockId, boundsForEachDimension));
                }

                for(Entry entry : entries)
                {
                    if(BoundingBox.checkBoxOverlap(entry.getBoundingBox(), searchBoundingBox)){
                        LeafEntry leafEntry = (LeafEntry) entry;
                        qualifyingRecordIds.add(leafEntry.getRecordID());
                    }
                }
            }
            else
                throw new IllegalStateException("Could not read records properly from the datafile");
            blockId++;
        }
    }
}

