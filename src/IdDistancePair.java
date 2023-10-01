/**
 * Represents a pair containing the ID of a record and its distance from a specific item.
 * @author Christos Kontos
 */
class IdDistancePair {
    private long recordId; // The id of the record
    private double distanceFromItem; // The distance from an item

    /**
     * Constructs an IdDistancePair with the specified record ID and distance from an item.
     * @param recordId          The ID of the record.
     * @param distanceFromItem  The distance from the specific item.
     */
    IdDistancePair(long recordId, double distanceFromItem) {
        this.recordId = recordId;
        this.distanceFromItem = distanceFromItem;
    }

    /**
     * Get the record ID stored in this pair.
     * @return The record ID.
     */
    long getRecordId() {
        return recordId;
    }

    /**
     * Get the distance from the specific item stored in this pair.
     * @return The distance from the item.
     */
    double getDistanceFromItem() {
        return distanceFromItem;
    }
}

