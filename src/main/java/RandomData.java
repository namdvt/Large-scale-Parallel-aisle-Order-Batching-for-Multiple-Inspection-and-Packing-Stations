import java.util.List;

/**
 * Created by Nguyen on 7/9/2017.
 */
public class RandomData {
    private List<Order> orderList;
    private int numOrder;
    private int numAisle;
    private int [][]Vrda;
    private int []capa;

    public RandomData(List<Order> orderList, int numOrder, int numAisle, int[][] vrda, int[] capa) {
        this.orderList = orderList;
        this.numOrder = numOrder;
        this.numAisle = numAisle;
        Vrda = vrda;
        this.capa = capa;
    }

    public List<Order> getOrderList() {
        return orderList;
    }

    public int getNumOrder() {
        return numOrder;
    }

    public int getNumAisle() {
        return numAisle;
    }

    public int[][] getVrda() {
        return Vrda;
    }

    public int[] getCapa() {
        return capa;
    }
}
