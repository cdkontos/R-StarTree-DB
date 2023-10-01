import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

// Class used for executing a skyline query without any use of an index.
class SeqSkylineQuery extends SeqScanQuery {
    private BoundingBox queryBoundingBox;
    private PriorityQueue<Record> skylineCandidates;
    private int dimensions;

    public SeqSkylineQuery(int dimensions) {
        this.dimensions = dimensions;
        this.skylineCandidates = new PriorityQueue<>(new Comparator<Record>() {
            @Override
            public int compare(Record record1, Record record2) {
                // Compare records based on dominance relationship.
                if (dominates(record1, record2)) {
                    return 1; // record1 dominates record2, so put it in front.
                } else if (dominates(record2, record1)) {
                    return -1; // record2 dominates record1, so put it in front.
                } else {
                    return 0; // No dominance relationship, the order doesn't matter.
                }
            }
        });
    }

    @Override
    public ArrayList<Long> getQueryRecordIds() {
        ArrayList<Long> skylineRecordIds = new ArrayList<>();
        int blockId = 1;

        while (blockId <= FilesHelper.getTotalBlocksInDatafile()) {
            ArrayList<Record> recordsInBlock = FilesHelper.readDataFileBlock(blockId);

            if (recordsInBlock != null) {
                for (Record record : recordsInBlock) {
                    if (isSkyline(record)) {
                        skylineRecordIds.add(record.getId());
                        // Add the record to the skyline candidates.
                        skylineCandidates.add(record);
                    }
                }
            } else {
                throw new IllegalStateException("Could not read records properly from the datafile");
            }

            blockId++;
        }

        return skylineRecordIds;
    }

    private boolean isSkyline(Record record) {
        for (Record skylineRecord : skylineCandidates) {
            if (dominates(skylineRecord, record)) {
                return false;
            }
        }
        return true;
    }

    private boolean dominates(Record record1, Record record2) {
        for (int i = 0; i < dimensions; i++) {
            double value1 = record1.getCoordinate(i);
            double value2 = record2.getCoordinate(i);
            if (value1 > value2) {
                return false;
            }
        }
        return true;
    }
}
