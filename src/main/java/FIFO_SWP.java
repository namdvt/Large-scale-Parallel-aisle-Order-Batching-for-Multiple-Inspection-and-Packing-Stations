import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nguyen on 7/1/2017.
 */
public class FIFO_SWP implements Ordering {
    private List<Order> orderList;
    private int maxOrder;

    public FIFO_SWP(List<Order> orderList, int maxOrder) {
        this.orderList = orderList;
        this.maxOrder = maxOrder;
    }

    public Result getResult() {
        Batch newBatch = new Batch();
        List<Batch> batchList = new ArrayList<Batch>();
        List<Order> orderList_ = new ArrayList<Order>(orderList);
        long startTime = System.currentTimeMillis();

        // Execute ordering
        for (Order order : orderList_) {
            if (newBatch.getOrderList().size() < maxOrder) {
                newBatch.addOrder(order);
            } else {
                batchList.add(newBatch);
                newBatch = new Batch(order);
            }
        }
        batchList.add(newBatch);

        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;

        return new Result(Type.FIFO_SWP, batchList, executeTime);
    }
}
