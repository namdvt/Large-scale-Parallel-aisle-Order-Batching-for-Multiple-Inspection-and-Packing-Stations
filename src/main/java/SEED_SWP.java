import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nguyen on 7/1/2017.
 */
public class SEED_SWP implements Ordering {
    private List<Order> orderList;
    private List<Path> pathList;
    private int maxOrder;
    private int numAisle;

    public SEED_SWP(List<Order> orderList,List<Path> pathList, int maxOrder, int numAisle) {
        this.orderList = orderList;
        this.pathList = pathList;
        this.maxOrder = maxOrder;
        this.numAisle = numAisle;
    }

    public Result getResult() {
        Batch newBatch = new Batch();
        Order orderPicked;
        List<Batch> batchList = new ArrayList<Batch>();
        List<Order> orderList_ = new ArrayList<Order>(orderList);
        long startTime = System.currentTimeMillis();

        // Execute ordering
        while (orderList_.size() != 0) {
            if (newBatch.getOrderList().size() == 0) {
                if (orderList_.size() != 1) {
                    List<Order> pairSeed = getPairOfOrderWithMaxAcceptedLength(orderList_);
                    newBatch.addOrder(pairSeed.get(0), pairSeed.get(1));
                    orderList_.remove(pairSeed.get(0));
                    orderList_.remove(pairSeed.get(1));
                } else {
                    newBatch.addOrder(orderList_);
                    orderList_.clear();
                }
            } else if (newBatch.getOrderList().size() == maxOrder) {
                batchList.add(newBatch);
                newBatch = new Batch();
            } else {
                orderPicked = getOrderForBatchToHaveMinPathLength(newBatch, orderList_);
                newBatch.addOrder(orderPicked);
                orderList_.remove(orderPicked);
            }
        }
        batchList.add(newBatch);

        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;

        return new Result(Type.SEED_SWP, batchList, executeTime);
    }

    private Order getOrderForBatchToHaveMinPathLength(Batch batch, List<Order> orderList) {

        Order orderPicked = orderList.get(0);
        int pathLength = getShortedAcceptedPathForOrders(extractOrders(batch, orderPicked)).getLength();
        for (Order order : orderList) {
            if (pathLength > getShortedAcceptedPathForOrders(extractOrders(batch, order)).getLength()) {
                orderPicked = order;
                pathLength = getShortedAcceptedPathForOrders(extractOrders(batch, order)).getLength();
            }
        }
        return orderPicked;
    }

    private static List<Order> extractOrders(Batch batch, Order order) {
        List<Order> orders = new ArrayList<Order>(batch.getOrderList());
        orders.add(order);
        return orders;
    }

    private Path getShortedAcceptedPathForOrders(List<Order> orders) {
        // Get shortest Path or Orders in Batch
        int[] shortestPath = {0, 0, 0, 0, 0, 0, 0, 0};
        for (Order order : orders) {
            for (int i = 0; i < numAisle; i++) {
                shortestPath[i] |= order.getPosition()[i];
            }
        }

        // Get accepted paths
        List<Path> acceptedPathList = new ArrayList<Path>();
        for (Path path : pathList) {
            boolean isAccepted = true;
            for (int i = 0; i < numAisle; i++) {
                if (shortestPath[i] > path.getPosition()[i]) {
                    isAccepted = false;
                    break;
                }
            }
            if (isAccepted) {
                acceptedPathList.add(path);
            }
        }

        // Get shortest form accepted paths and print
        Path shortestAcceptedPath = acceptedPathList.get(0);
        for (Path path : acceptedPathList) {
            if (shortestAcceptedPath.getLength() > path.getLength()) {
                shortestAcceptedPath = path;
            }
        }
        return shortestAcceptedPath;
    }

    private List<Order> getPairOfOrderWithMaxAcceptedLength(List<Order> orderList) {
        List<Batch> orderPairList = new ArrayList<Batch>();
        for (int i = 0; i < orderList.size() - 1; i++) {
            for (int j = i + 1; j < orderList.size(); j++) {
                orderPairList.add(new Batch(orderList.get(i), orderList.get(j)));
            }
        }

        Batch selectedBatch = orderPairList.get(0);
        int selectedBatchLength = getShortedAcceptedPathForOrders(selectedBatch.getOrderList()).getLength();
        for (Batch batch : orderPairList) {
            int batchLength = getShortedAcceptedPathForOrders(batch.getOrderList()).getLength();
            if (selectedBatchLength > batchLength) {
                selectedBatch = batch;
                selectedBatchLength = batchLength;
            }
        }
        return selectedBatch.getOrderList();
    }
}
