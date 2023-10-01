import java.io.Serializable;
import java.util.ArrayList;

/**
 * The Record class represents a point in n-dimensional space.
 *
 * @author Akompian Georgios
 */
class Record implements Serializable {
    private long id; // The unique id of the record
    private ArrayList<Double> coordinates; // ArrayList with the coordinates of the Record's point

    /**
     * Constructs a Record with the given ID and coordinates.
     *
     * @param id          The unique ID of the record.
     * @param coordinates ArrayList with the coordinates of the Record's point.
     */
    Record(long id, ArrayList<Double> coordinates) {
        this.id = id;
        this.coordinates = coordinates;
    }

    /**
     * Constructs a Record from a string representation.
     *
     * @param recordInString A string containing the record's data.
     * @throws IllegalArgumentException If the string format is incorrect.
     */
    Record(String recordInString) throws IllegalArgumentException
    {
        String[] stringArray;
        stringArray = recordInString.split(FilesHelper.getDELIMITER()); // given string will be split by the argument delimiter provided

        if (stringArray.length != FilesHelper.getDataDimensions() + 1)
            throw new IllegalArgumentException("In order to convert a String to a Record, a Long and a total amount of coordinates for each dimension must be given");

        id = Long.parseLong(stringArray[0]);
        coordinates = new ArrayList<>();
        for (int i = 1; i < stringArray.length ; i++)
            coordinates.add(Double.parseDouble(stringArray[i]));
    }

    /**
     * Get the unique ID of the record.
     *
     * @return The ID of the record.
     */
    long getId() {
        return id;
    }

    /**
     * Returns the coordinate on the specified dimension.
     *
     * @param dimension The dimension (axis) for which to retrieve the coordinate.
     * @return The coordinate on the specified dimension.
     */
    double getCoordinate(int dimension)
    {
        return coordinates.get(dimension);
    }

    /**
     * Converts the Record to its string representation.
     *
     * @return A string representing the Record.
     */
    @Override
    public String toString() {
        StringBuilder recordToString = new StringBuilder(id + "," + coordinates.get(0));
        for(int i = 1; i < coordinates.size(); i++)
            recordToString.append(",").append(coordinates.get(i));
        return String.valueOf(recordToString);
    }
    /**
     * Checks if this Record dominates another Record in all dimensions.
     *
     * @param other The other Record to compare.
     * @return True if this Record dominates the other, false otherwise.
     */
    boolean dominates(Record other) {
        if (other == null) {
            return false; // Handle the case where 'other' is null
        }

        boolean isBetterInSomeDimension = false;

        for (int dimension = 0; dimension < coordinates.size(); dimension++) {
            double thisCoordinate = coordinates.get(dimension);
            double otherCoordinate = other.getCoordinate(dimension);

            if (thisCoordinate > otherCoordinate) {
                // 'this' is better in this dimension
                isBetterInSomeDimension = true;
            } else if (thisCoordinate < otherCoordinate) {
                // 'this' is worse in this dimension
                return false; // 'this' does not dominate 'other'
            }
            // If thisCoordinate == otherCoordinate, they are equal in this dimension, continue to the next dimension
        }

        return isBetterInSomeDimension;
    }


}

