public class Distribution {
    private  DistributionGroup firstGroup;
    private  DistributionGroup secondGroup;

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
