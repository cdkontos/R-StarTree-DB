import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

// Class used for executing a k-nearest neighbors query of a specific search point without any use of an index.
// Finds the k closest records to that search point.
class SeqNearestNeighbourQuery extends SeqScanQuery {
    private ArrayList<Double> searchPoint; // The point for which nearest neighbors are sought.
    private int k; // The number of nearest neighbors to find.
    private PriorityQueue<IdDistancePair> nearestNeighbours; // Priority queue to store the nearest neighbors.

    // Constructor for SequentialNearestNeighboursQuery.
    SeqNearestNeighbourQuery(ArrayList<Double> searchPoint, int k) {
        if (k < 0)
            throw new IllegalArgumentException("Parameter 'k' for the nearest neighbors must be a positive integer.");
        this.searchPoint = searchPoint;
        this.k = k;

        // Initialize the priority queue with a custom comparator to maintain the k-nearest neighbors.
        this.nearestNeighbours = new PriorityQueue<>(k, new Comparator<IdDistancePair>() {
            @Override
            public int compare(IdDistancePair recordDistancePairA, IdDistancePair recordDistancePairB) {
                // In order to make a MAX heap, compare distances in reverse order.
                return Double.compare(recordDistancePairB.getDistanceFromItem(), recordDistancePairA.getDistanceFromItem());
            }
        });
    }

    // Returns the IDs of the query's records, sorted by distance from the search point.
    @Override
    ArrayList<Long> getQueryRecordIds() {
        ArrayList<Long> qualifyingRecordIds = new ArrayList<>();
        findNeighbours();

        // Pop elements from the priority queue (max heap) to get the closest neighbors first.
        while (nearestNeighbours.size() != 0) {
            IdDistancePair recordDistancePair = nearestNeighbours.poll();
            qualifyingRecordIds.add(recordDistancePair.getRecordId());
        }

        // Reverse the list to return the closest neighbors first instead of farthest.
        Collections.reverse(qualifyingRecordIds);
        return qualifyingRecordIds;
    }

    // Method to find the k-nearest neighbors.
    private void findNeighbours() {
        int blockId = 1;
        while (blockId < FilesHelper.getTotalBlocksInDatafile()) {
            ArrayList<Record> recordsInBlock;
            recordsInBlock = FilesHelper.readDataFileBlock(blockId);
            ArrayList<LeafEntry> entries = new ArrayList<>();

            if (recordsInBlock != null) {
                for (Record record : recordsInBlock) {
                    ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();

                    // Since we have points as records, we set the lower and upper bounds to be the same.
                    for (int d = 0; d < FilesHelper.getDataDimensions(); d++)
                        boundsForEachDimension.add(new Bounds(record.getCoordinate(d), record.getCoordinate(d)));

                    entries.add(new LeafEntry(record.getId(), blockId, boundsForEachDimension));
                }
                int i = 0;
                while (i < entries.size()) {
                    double distanceFromPoint = entries.get(i).getBoundingBox().findMinPointDistance(searchPoint);

                    // Maintain the k-nearest neighbors in the priority queue.
                    if (nearestNeighbours.size() == k) {
                        if (distanceFromPoint < nearestNeighbours.peek().getDistanceFromItem()) {
                            nearestNeighbours.poll();
                            nearestNeighbours.add(new IdDistancePair(entries.get(i).getRecordID(), distanceFromPoint));
                        }
                    } else {
                        nearestNeighbours.add(new IdDistancePair(entries.get(i).getRecordID(), distanceFromPoint));
                    }
                    i++;
                }
            } else {
                throw new IllegalStateException("Could not read records properly from the datafile");
            }
            blockId++;
        }
    }
}

