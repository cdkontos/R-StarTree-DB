import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        boolean reconstructingTree = UserInterface.startApplication();
        boolean bulk = false;
        System.out.println("Do you want bulk? (y/n)");
        Scanner scan = new Scanner(System.in);
        String answer;
        while (true) {
            answer = scan.nextLine().trim().toLowerCase();
            System.out.println();
            // In case the user wants to reset the files
            if (answer.equals("y")) {
                bulk = true;
                break;
            } else if (answer.equals("n")) {
                break;
            } else {
                System.out.println("Please answer with y/n: ");
            }
        }
        RStarTree rStarTree;
        if(bulk)
        {
            long startTreeTime = System.nanoTime();
            rStarTree = new RStarTree(reconstructingTree, true);
            long stopTreeTime = System.nanoTime();
            System.out.println("Time taken for R*Tree Bulk Loading with Zorder time: " + (double) (stopTreeTime - startTreeTime) / 1000000 + " ms");
        }
        else
        {
            long startTreeTime = System.nanoTime();
            rStarTree = new RStarTree(reconstructingTree);
            long stopTreeTime = System.nanoTime();
            System.out.println("Time taken for normal R*Tree: " + (double) (stopTreeTime - startTreeTime) / 1000000 + " ms");
        }
        UserInterface.runApplication(rStarTree);
    }
}