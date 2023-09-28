// Class which is used to hold an id of a record and its distance from a specific item.
class IdDistancePair {
    private long recordId; // The id of the record
    private double distanceFromItem; // The distance from an item

    // Constructor to initialize the IdDistancePair with a record ID and distance.
    IdDistancePair(long recordId, double distanceFromItem) {
        this.recordId = recordId;
        this.distanceFromItem = distanceFromItem;
    }

    // Get the record ID stored in this pair.
    long getRecordId() {
        return recordId;
    }

    // Get the distance from the specific item stored in this pair.
    double getDistanceFromItem() {
        return distanceFromItem;
    }
}

