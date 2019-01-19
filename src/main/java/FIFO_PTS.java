import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nguyen on 7/1/2017.
 */
public class FIFO_PTS implements Ordering {
    private List<Order> orderList;
    private int maxCapa;

    public FIFO_PTS(List<Order> orderList, int maxCapa) {
        this.orderList = orderList;
        this.maxCapa = maxCapa;
    }

    public Result getResult() {
        Batch newBatch = new Batch();
        List<Batch> batchList = new ArrayList<Batch>();
        List<Order> orderList_ = new ArrayList<Order>(orderList);
        long startTime = System.currentTimeMillis();

        // Execute ordering
        for (Order order : orderList_) {
            if (newBatch.getCapa() + order.getCapa() <= maxCapa) {
                newBatch.addOrder(order);
            } else {
                batchList.add(newBatch);
                newBatch = new Batch(order);
            }
        }
        batchList.add(newBatch);

        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;

        return new Result(Type.FIFO_PTS, batchList, executeTime);
    }
}
