import java.util.*;

// Class used for executing a k-nearest neighbors query of a specific search point with the use of the RStarTree.
// Finds the k closest records of that search point.
class NearestNeighboursQuery extends Query {
    private ArrayList<Double> searchPoint; // The coordinates of the point used for radius queries
    private double searchPointRadius; // The reference radius that is used as a bound
    private int k; // The number of nearest neighbors to be found
    private PriorityQueue<IdDistancePair> nearestNeighbours; // Using a max heap for the nearest neighbors

    NearestNeighboursQuery(ArrayList<Double> searchPoint, int k) {
        if (k < 0)
            throw new IllegalArgumentException("Parameter 'k' for the nearest neighbors must be a positive integer.");
        this.searchPoint = searchPoint;
        this.k = k;
        this.searchPointRadius = Double.MAX_VALUE;
        this.nearestNeighbours = new PriorityQueue<>(k, (recordDistancePairA, recordDistancePairB) -> {
            return Double.compare(recordDistancePairB.getDistanceFromItem(), recordDistancePairA.getDistanceFromItem()); // In order to make a MAX heap
        });
    }

    // Returns the IDs of the query's records.
    @Override
    ArrayList<Long> getQueryRecordIds(Node node) {
        ArrayList<Long> qualifyingRecordIds = new ArrayList<>();
        findNeighbours(node);
        while (nearestNeighbours.size() != 0) {
            IdDistancePair recordDistancePair = nearestNeighbours.poll();
            qualifyingRecordIds.add(recordDistancePair.getRecordId());
        }
        Collections.reverse(qualifyingRecordIds); // In order to return the closest neighbors first instead of the farthest
        return qualifyingRecordIds;
    }

    // Finds the nearest neighbors by using a branch and bound algorithm
    // with the help of the RStarTree.
    private void findNeighbours(Node node) {
        node.getEntries().sort(new EntryComparator.EntryDistanceFromPointComparator(node.getEntries(), searchPoint));
        int i = 0;
        if (node.getLevel() != RStarTree.getLeafLevel()) {
            while (i < node.getEntries().size() && (nearestNeighbours.size() < k || node.getEntries().get(i).getBoundingBox().findMinPointDistance(searchPoint) <= searchPointRadius)) {
                findNeighbours(FilesHelper.readIndexFileBlock(node.getEntries().get(i).getChildNodeBlockID()));
                i++;
            }
        } else {
            while (i < node.getEntries().size() && (nearestNeighbours.size() < k || node.getEntries().get(i).getBoundingBox().findMinPointDistance(searchPoint) <= searchPointRadius)) {
                if (nearestNeighbours.size() >= k)
                    nearestNeighbours.poll();
                LeafEntry leafEntry = (LeafEntry) node.getEntries().get(i);
                double minDistance = leafEntry.getBoundingBox().findMinPointDistance(searchPoint);
                nearestNeighbours.add(new IdDistancePair(leafEntry.getRecordID(), minDistance));
                searchPointRadius = nearestNeighbours.peek().getDistanceFromItem();
                i++;
            }
        }
    }
}

