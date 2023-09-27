public class Distribution {
    private final DistributionGroup firstGroup;
    private final DistributionGroup secondGroup;

    public Distribution(DistributionGroup firstGroup, DistributionGroup secondGroup) {
        this.firstGroup = firstGroup;
        this.secondGroup = secondGroup;
    }

    public DistributionGroup getFirstGroup() {
        return firstGroup;
    }

    public DistributionGroup getSecondGroup() {
        return secondGroup;
    }
}
