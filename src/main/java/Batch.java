import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nam on 1/11/2017.
 */
public class Batch{
    private int id;
    private List<Order> orderList;
    private int[] shortestPath;
    private Path path;

    public Batch(int id) {
        this.id = id;
        orderList = new ArrayList<Order>();
    }

    public Batch() {
        orderList = new ArrayList<Order>();
    }

    public Batch(Order ... orders) {
        orderList = new ArrayList<Order>();
        for (Order order : orders) {
            orderList.add(order);
        }
    }

    public Batch(Batch ... batches) {
        orderList = new ArrayList<Order>();
        for (Batch batch:batches) {
            orderList.addAll(batch.getOrderList());
        }
    }

    public Batch(List<Batch> batches) {
        orderList = new ArrayList<Order>();
        for (Batch batch:batches) {
            orderList.addAll(batch.getOrderList());
        }
    }

    public void addOrder(Order ...orders) {
        this.orderList.addAll(Arrays.asList(orders));
    }

    public void addOrder(List<Order> orders) {
        this.orderList.addAll(orders);
    }

    public void addOrder(Batch batch) {
        for (Order newOrder : batch.getOrderList()) {
            boolean added = false;
            for (Order order : this.orderList) {
                if (newOrder.equals(order)) {
                    added = true;
                    break;
                }
            }
            if(!added) {
                this.orderList.add(newOrder);
            }
        }
    }

    public List<Order> getOrderList() {
        return orderList;
    }

    public int[] getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(int[] shortestPath) {
        this.shortestPath = shortestPath;
    }

    public void print() {
        for (Order order : this.orderList) {
            for (int i=0;i<8;i++) {
                System.out.print(order.getPosition()[i]+ " ");
            }
            System.out.println();
        }
    }

    public int getCapa() {
        int sum = 0;
        for (Order order : orderList) {
            sum += order.getCapa();
        }
        return sum;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}