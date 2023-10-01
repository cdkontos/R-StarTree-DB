/**
 * A class representing a distribution of data into two groups.
 * @author Christos Kontos
 */
public class Distribution {
    private  DistributionGroup firstGroup;
    private  DistributionGroup secondGroup;

    /**
     * Constructs a Distribution object with the specified first and second groups.
     * @param firstGroup  The first group of the distribution.
     * @param secondGroup The second group of the distribution.
     */
    public Distribution(DistributionGroup firstGroup, DistributionGroup secondGroup) {
        this.firstGroup = firstGroup;
        this.secondGroup = secondGroup;
    }

    /**
     * Gets the first group of the distribution.
     * @return The first group of the distribution.
     */
    public DistributionGroup getFirstGroup() {
        return firstGroup;
    }

    /**
     * Gets the second group of the distribution.
     * @return The second group of the distribution.
     */
    public DistributionGroup getSecondGroup() {
        return secondGroup;
    }
}
