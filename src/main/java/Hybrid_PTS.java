import java.util.*;

/**
 * Created by nam on 8/15/2017.
 */
public class Hybrid_PTS implements Ordering {
    private List<Order> orderList;
    private List<Path> pathList;
    private int maxCapa;
    private int numAisle;
    private List<Batch> resultBatchList;
    private List<Order> removedOrderList;
    List<Batch> initialBatchList;
    private int sigma1 = 120;
    private int sigma2 = 80;
    private int sigma3 = 100;
    private double r = 0.5;
    private double p = 3;
    Set<List<List<Integer>>> tabuList = new HashSet<List<List<Integer>>>();
    Set<List<List<Integer>>> mainList = new HashSet<List<List<Integer>>>();
    int beta_randomRemove = 0;
    int beta_worstRemove = 0;
    int beta_shawRemove = 0;
    int beta_regretInsert = 0;
    int beta_greedyInsert = 0;
    int score_randomRemove = 0;
    int score_worstRemove = 0;
    int score_shawRemove = 0;
    int score_regretInsert = 0;
    int score_greedyInsert = 0;
    double weight_randomRemove = 1.0/3;
    double weight_worstRemove = 1.0/3;
    double weight_shawRemove = 1.0/3;
    double weight_regretInsert = 0.5;
    double weight_greedyInsert = 0.5;

    public Hybrid_PTS() {

    }

    public Hybrid_PTS(List<Order> orderList, List<Path> pathList, List<Batch> initialBatchList, int maxCapa, int numAisle) {
        this.orderList = orderList;
        this.pathList = pathList;
        this.maxCapa = maxCapa;
        this.numAisle = numAisle;
        this.resultBatchList = new ArrayList<Batch>();
        this.removedOrderList = new ArrayList<Order>();
        this.initialBatchList = initialBatchList;
    }

    private int getNumOrder(List<Batch> batchList) {
        int num = 0;
        for (Batch batch : batchList) {
            for (Order order : batch.getOrderList()) {
                num ++;
            }
        }
        return num;
    }

    public Result getResult() {
        int randomRemove = 0;
        int worstRemove = 0;
        int shawRemove = 0;
        int regretInsert = 0;
        int greedyInsert = 0;

        boolean pickLowerSolution = false;

        int qmin = (int)(0.175*orderList.size());
        int qmax = (int)(0.35*orderList.size());
        int q = (new Random()).nextInt(qmin) + qmax - qmin;
        resultBatchList = this.initialBatchList;
        q = q/resultBatchList.size();
        System.out.println(q);

        long startTime = System.currentTimeMillis();
        boolean f = false;

        Iteration firstIteration = new Iteration(1,1,1,1,1,1,1,1,1,1);
        firstIteration.solution = getSolution(resultBatchList);
        firstIteration.totalLength = Calculation.getTotalLength(resultBatchList, pathList);

        List<Segment> segmentList = new ArrayList<Segment>();
        List<Iteration> iterationList = new ArrayList<Iteration>();


        // Create random list
        DistributedRandomNumberGenerator removeHeuristics = new DistributedRandomNumberGenerator();
        DistributedRandomNumberGenerator insertHeuristics = new DistributedRandomNumberGenerator();
        removeHeuristics.addNumber(1,weight_randomRemove);
        removeHeuristics.addNumber(2,weight_worstRemove);
        removeHeuristics.addNumber(3,weight_shawRemove);
        insertHeuristics.addNumber(1,weight_regretInsert);
        insertHeuristics.addNumber(2,weight_greedyInsert);

        // create first segment
        Segment segment = new Segment(100);
        segment.iterationList.add(firstIteration);
        Iteration bestGlobalSolution = new Iteration();
        bestGlobalSolution.totalLength = Calculation.getTotalLength(initialBatchList, pathList);
        bestGlobalSolution.solution = getSolution(initialBatchList);
        List<List<Integer>> solutionForTabu = bestGlobalSolution.solution;

        for (int i = 0; i<10*orderList.size(); i++) {
            if (!pickLowerSolution) {
                resultBatchList = getBatchListFromSolution(bestGlobalSolution);
            }
            // count
            int remove; // remove == 1: randomRemoval; // remove == 2: worstRemoval
            int insert; // remove == 1: regretInsert; // remove == 2: greedyInsert
            int randomRemoveHeuristic = removeHeuristics.getDistributedRandomNumber();
            int randomInsertHeuristic = insertHeuristics.getDistributedRandomNumber();

            // randomly select remove heuristic
            if (randomRemoveHeuristic == 1) {
                remove = 1;
                for (Batch batch : resultBatchList) {
                    if (batch.getOrderList().size() > 1) {
                        randomRemoval(batch, 4);
                    }
                }
                randomRemove ++;
                beta_randomRemove ++;
            } else if (randomRemoveHeuristic == 2) {
                remove = 2;
                for (Batch batch : resultBatchList) {
                    if (batch.getOrderList().size() > 1) {
                        worstRemoval(batch, 4);
                    }
                }
                worstRemove ++;
                beta_worstRemove ++;
            } else {
                remove = 3;
                shawRemoval(resultBatchList, 4);
                shawRemove ++;
                beta_shawRemove ++;
            }

            // randomly select insert heuristic
            if (randomInsertHeuristic == 1) {
                insert = 1;  // regret
                regretInsertion();
                regretInsert ++;
                beta_regretInsert ++;
            } else {
                insert = 2;
                greedyInsertion();
                greedyInsert ++;
                beta_greedyInsert ++;
            }


            List<List<Integer>> solution = getSolution(resultBatchList);
            solutionForTabu = solution;
            // update score
            // new best solution
            if (Calculation.getTotalLength(resultBatchList, pathList) == bestGlobalSolution.totalLength) {
                updateScore(remove, insert, 100);
            }

            if (Calculation.getTotalLength(resultBatchList, pathList) > bestGlobalSolution.totalLength) {
                double rand = Math.random();
                if (rand < 0.1) {
                    pickLowerSolution = true;
                } else {
                    pickLowerSolution = false;
                }
            }

            // better solution
            if (Calculation.getTotalLength(resultBatchList, pathList) < bestGlobalSolution.totalLength) {
                updateScore(remove, insert, 120);
                bestGlobalSolution.solution = getSolution(resultBatchList);
                bestGlobalSolution.totalLength = Calculation.getTotalLength(resultBatchList, pathList);
            }

            // new solution
            if (!mainList.contains(solution)) {
                updateScore(remove, insert, 80);
            }
            mainList.add(solution);

            if(i%100 ==0 && i !=0) {
                // update weight
                // randomRemoval
                double newWeightRandomRemove = (1-r)*weight_randomRemove + r*score_randomRemove/beta_randomRemove;
                if (!Double.isNaN(newWeightRandomRemove)) {
                    removeHeuristics.addNumber(1, newWeightRandomRemove);
                    weight_randomRemove = newWeightRandomRemove;
                }

                // worstRemoval
                double newWeightWorstRemove = (1-r)*weight_worstRemove + r*score_worstRemove/beta_worstRemove;
                if(!Double.isNaN(newWeightWorstRemove)) {
                    removeHeuristics.addNumber(2, newWeightWorstRemove);
                    weight_worstRemove = newWeightWorstRemove;
                }

                // shawRemoval
                double newWeightShawRemove = (1-r)*weight_shawRemove + r*score_shawRemove/beta_shawRemove;
                if(!Double.isNaN(newWeightShawRemove)) {
                    removeHeuristics.addNumber(3, newWeightShawRemove);
                    weight_shawRemove = newWeightShawRemove;
                }

                // regretInsert
                double newWeightRegretInsert = (1-r)*weight_regretInsert + r*score_regretInsert/beta_regretInsert;
                if(!Double.isNaN(newWeightRegretInsert)) {
                    insertHeuristics.addNumber(1, newWeightRegretInsert);
                    weight_regretInsert = newWeightRegretInsert;
                }
                // greedyInsert
                double newWeightGreedyInsert = (1-r)*weight_greedyInsert + r*score_greedyInsert/beta_greedyInsert;
                if(!Double.isNaN(newWeightGreedyInsert)) {
                    insertHeuristics.addNumber(2, newWeightGreedyInsert);
                    weight_greedyInsert = newWeightGreedyInsert;
                }

                //reset score and beta
                beta_randomRemove = 1;
                beta_worstRemove = 1;
                beta_shawRemove = 1;
                beta_regretInsert = 1;
                beta_greedyInsert = 1;
                score_randomRemove = 0;
                score_worstRemove = 0;
                score_shawRemove = 0;
                score_regretInsert = 0;
                score_greedyInsert = 0;
            }

            //Tabu
            List<Batch> neighborhoodBatch;
            if (i%100 == 0) {
                Set<List<Batch>> neighborhoodList;
                neighborhoodList = createNeighborhoodList(resultBatchList);

                for (List<Batch> neiborhood : neighborhoodList) {
                    if (Calculation.getTotalLength(neiborhood, pathList) < bestGlobalSolution.totalLength) {
                        bestGlobalSolution.solution = getSolution(neiborhood);
                        bestGlobalSolution.totalLength = Calculation.getTotalLength(neiborhood, pathList);
                        resultBatchList = getBatchListFromSolution(solution);
                        System.out.println("TABU");
                    }
                }
            }
        }
        resultBatchList = getBatchListFromSolution(bestGlobalSolution);

        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;

//        System.out.println(Calculation.getTotalLength(resultBatchList, pathList));

        System.out.println(randomRemove);
        System.out.println(worstRemove);
        System.out.println(shawRemove);
        System.out.println(regretInsert);
        System.out.println(greedyInsert);
        return new Result(Type.HYBRID_PTS, resultBatchList, executeTime);
    }


    public Batch randomRemoval(Batch batch, int q) {
        while (q >0 && batch.getOrderList().size() > 1) {
            Random rand = new Random();
            int randomNum = 0;
            try {
                randomNum = rand.nextInt((batch.getOrderList().size()));
            } catch (Exception e) {
                System.out.println(e);
            }
            Order randomOrder = batch.getOrderList().get(randomNum);

            this.removedOrderList.add(randomOrder);
            batch.getOrderList().remove(randomOrder);
            q --;
        }
        return batch;
    }

    private List<Batch> getInitialBatchList() {
        List<Batch> initialBatchList;
//        FIFO_PTS initialSolution = new FIFO_PTS(orderList, maxCapa);
//        SEED_PTS initialSolution = new SEED_PTS(orderList, pathList, maxCapa, numAisle);
        CW_PTS initialSolution = new CW_PTS(orderList, pathList, maxCapa, numAisle);
        initialBatchList = initialSolution.getResult().getBatchListResult();
        return initialBatchList;
    }

    public Batch worstRemoval(Batch batch, int q) {
        int fs = Calculation.getPath(batch, pathList).getLength();
        while (q > 0 && batch.getOrderList().size() > 1) {
            // Calculate cost
            for (Order order : batch.getOrderList()) {
                Batch batchWithoutOrderi = new Batch(batch);
                batchWithoutOrderi.getOrderList().remove(order);
                int cost = fs - Calculation.getPath(batchWithoutOrderi, pathList).getLength();
                order.setCost(cost);
            }

            // Sort
            Collections.sort(batch.getOrderList(), new Comparator<Order>() {
                public int compare(Order o1, Order o2) {
                    return o2.getCost() - o1.getCost();
                }
            });

            // Remove randomly
//            double u = Math.random();
//            int L = batch.getOrderList().size();
//            Double randomPosition = Math.pow(u, p)*L;
//            Order randomOrder = batch.getOrderList().get(randomPosition.intValue());

            // List with max cost
            int maxCost = batch.getOrderList().get(0).getCost();
            List<Order> maxCostOrderList = new ArrayList<Order>();
            for (Order order : batch.getOrderList()) {
                if (order.getCost() == maxCost) {
                    maxCostOrderList.add(order);
                }
            }

            Order randomOrder = maxCostOrderList.get((int) (Math.random() * maxCostOrderList.size()));


            this.removedOrderList.add(randomOrder);
            batch.getOrderList().remove(randomOrder);
            q --;
        }

        return batch;
    }

    public void greedyInsertion() {
        for (Batch batch : resultBatchList) {
            // clear all cost
            for (Order order : removedOrderList) {
                order.setCost(0);
            }

            // set cost for each order
            for (Order order : this.removedOrderList) {
                if (batch.getCapa() + order.getCapa() <= maxCapa) {
                    Batch temp = new Batch(batch);
                    temp.addOrder(order);
                    order.setCost(Calculation.getPath(temp, this.pathList).getLength());
                } else {
                    order.setCost(Integer.MAX_VALUE);
                }
            }

            // sort to get order with min cost
            Collections.sort(removedOrderList, new Comparator<Order>() {
                public int compare(Order o1, Order o2) {
                    return o1.getCost() - o2.getCost();
                }
            });

            outerloop:
            while (true){
                if (removedOrderList.isEmpty()) {
                    return;
                }
                // get list order with min cost
                int minCost = removedOrderList.get(0).getCost();
                List<Order> minCostOrderList = new ArrayList<Order>();
                for (Order order : removedOrderList) {
                    if (order.getCost() == minCost) {
                        minCostOrderList.add(order);
                    }
                }
                while (true){
                    Order order = minCostOrderList.get((int)(Math.random()*minCostOrderList.size()));
                    batch.addOrder(order);
                    if (batch.getCapa() > maxCapa) {
                        batch.getOrderList().remove(order);
                        break outerloop;
                    }

                    removedOrderList.remove(order);
                    minCostOrderList.remove(order);
                    if (minCostOrderList.isEmpty()) {
                        break;
                    }
                }
            }

//            // add order with minimum cost to batch
//            Iterator iterator = removedOrderList.iterator();
//            while (iterator.hasNext()) {
//                Order order = (Order) iterator.next();
//                if (batch.getCapa() + order.getCapa() <= maxCapa) {
//                    batch.addOrder(order);
//                    iterator.remove();
//                } else {
//                    break;
//                }
//            }
        }
    }

    public void regretInsertion() {
        for (Batch batch : resultBatchList) {
            // clear all cost
            for (Order order : removedOrderList) {
                order.setCost(0);
            }

            // set cost for each order
            for (Order order : this.removedOrderList) {
                if (batch.getCapa() + order.getCapa() <= maxCapa) {
                    Batch temp = new Batch(batch);
                    temp.addOrder(order);
                    order.setCost(Calculation.getPath(temp, this.pathList).getLength());
                } else {
                    order.setCost(Integer.MAX_VALUE);
                }
            }

            // calculate k regret value
            for (Order order : removedOrderList) {
                int sumCost = 0;
                for (Batch b : resultBatchList) {
                    if (b.equals(batch)) {
                        continue;
                    } else {
                        Batch temp = new Batch(b);
                        temp.addOrder(order);
                        sumCost += Calculation.getPath(temp, this.pathList).getLength() - order.getCost();
                    }
                }
                order.setCost(sumCost);
            }

            Collections.sort(removedOrderList, new Comparator<Order>() {
                public int compare(Order o1, Order o2) {
                    return o2.getCost() - o1.getCost();
                }
            });


            outerloop:
            while (true){
                if (removedOrderList.isEmpty()) {
                    return;
                }
                // get list order with min cost
                int minCost = removedOrderList.get(0).getCost();
                List<Order> minCostOrderList = new ArrayList<Order>();
                for (Order order : removedOrderList) {
                    if (order.getCost() == minCost) {
                        minCostOrderList.add(order);
                    }
                }
                while (true){
                    Order order = minCostOrderList.get((int)(Math.random()*minCostOrderList.size()));
                    batch.addOrder(order);
                    if (batch.getCapa() > maxCapa) {
                        batch.getOrderList().remove(order);
                        break outerloop;
                    }

                    removedOrderList.remove(order);
                    minCostOrderList.remove(order);
                    if (minCostOrderList.isEmpty()) {
                        break;
                    }
                }
            }
        }
    }

    public class DistributedRandomNumberGenerator {

        private HashMap<Integer, Double> distribution;
        private double distSum;

        public DistributedRandomNumberGenerator() {
            distribution = new HashMap<Integer, Double>();
        }

        public void addNumber(int value, double distribution) {
            if (this.distribution.get(value) != null) {
                distSum -= this.distribution.get(value);
            }
            this.distribution.put(value, distribution);
            distSum += distribution;
        }

        public int getDistributedRandomNumber() {
            double rand = Math.random();
            double ratio = 1.0f / distSum;
            double tempDist = 0;
            for (Integer i : distribution.keySet()) {
                tempDist += distribution.get(i);
                if (rand / ratio <= tempDist) {
                    return i;
                }
            }
            return 0;
        }

        public HashMap<Integer, Double> getDistribution() {
            return distribution;
        }
    }

    private class Segment {
        int numIteration;
        Set<List<Integer>> solutionList = new HashSet<List<Integer>>();
        List<Iteration> iterationList;
        double weightRandomRemoval;
        double weightWorstRemoval;
        double weightRegretInsert;
        double weightGreedyInsert;
        double weightShawRemoval;

        public Segment(int numIteration) {
            this.numIteration = numIteration;
            this.iterationList = new ArrayList<Iteration>();
        }

        double getTotalScoreRandomRemoval() {
            double sum = 0;
            for (Iteration iteration : iterationList) {
                sum += iteration.scoreRandomRemoval;
            }
            return sum;
        }

        double getTotalScoreWorstRemoval() {
            double sum = 0;
            for (Iteration iteration : iterationList) {
                sum += iteration.scoreWorstRemoval;
            }
            return sum;
        }

        double getTotalScoreShawRemoval() {
            double sum = 0;
            for (Iteration iteration : iterationList) {
                sum += iteration.scoreShawRemoval;
            }
            return sum;
        }

        double getTotalScoreRegretInsert() {
            double sum = 0;
            for (Iteration iteration : iterationList) {
                sum += iteration.scoreRegretInsert;
            }
            return sum;
        }

        double getTotalScoreGreedyInsert() {
            double sum = 0;
            for (Iteration iteration : iterationList) {
                sum += iteration.scoreGreedyInsert;
            }
            return sum;
        }

        double getTotalNumRandomRemoval() {
            double sum = 0;
            for (Iteration iteration : iterationList) {
                sum += iteration.numRandomRemoval;
            }
            return sum;
        }

        double getTotalNumWorstRemoval() {
            double sum = 0;
            for (Iteration iteration : iterationList) {
                sum += iteration.numWorstRemoval;
            }
            return sum;
        }

        double getTotalNumShawRemoval() {
            double sum = 0;
            for (Iteration iteration : iterationList) {
                sum += iteration.numShawRemoval;
            }
            return sum;
        }

        double getTotalNumRegretInsert() {
            double sum = 0;
            for (Iteration iteration : iterationList) {
                sum += iteration.numRegretInsert;
            }
            return sum;
        }

        double getTotalNumGreedyInsert() {
            double sum = 0;
            for (Iteration iteration : iterationList) {
                sum += iteration.numGreedyInsert;
            }
            return sum;
        }
    }

    private class Iteration {
        public Iteration() {

        }

        public Iteration(int scoreRandomRemoval, int scoreWorstRemoval, int scoreShawRemoval, int scoreRegretInsert, int scoreGreedyInsert, int numRandomRemoval, int numWorstRemoval, int numShawRemoval, int numRegretInsert, int numGreedyInsert) {
            this.scoreRandomRemoval = scoreRandomRemoval;
            this.scoreWorstRemoval = scoreWorstRemoval;
            this.scoreShawRemoval = scoreShawRemoval;
            this.scoreRegretInsert = scoreRegretInsert;
            this.scoreGreedyInsert = scoreGreedyInsert;
            this.numRandomRemoval = numRandomRemoval;
            this.numWorstRemoval = numWorstRemoval;
            this.numShawRemoval = numShawRemoval;
            this.numRegretInsert = numRegretInsert;
            this.numGreedyInsert = numGreedyInsert;
        }

        int scoreRandomRemoval;
        int scoreWorstRemoval;
        int scoreShawRemoval;
        int scoreRegretInsert;
        int scoreGreedyInsert;
        int totalLength;
        int numRandomRemoval;
        int numWorstRemoval;
        int numShawRemoval;
        int numRegretInsert;
        int numGreedyInsert;
        List<List<Integer>> solution;
    }

    // solution is the numid or orders
    private List<List<Integer>> getSolution(List<Batch> batchList) {
        List<List<Integer>> solution = new ArrayList<List<Integer>>();
        for (Batch batch : batchList) {
            List<Integer> batchSolution = new ArrayList<Integer>();
            for (Order order : batch.getOrderList()) {
                batchSolution.add(order.getId());
            }
            solution.add(batchSolution);
        }

        return solution;
    }

    private void updateNumber(Iteration iteration, int remove, int insert) {
        if (remove == 1) {
            iteration.numRandomRemoval ++;
        }
        if (remove == 2) {
            iteration.numWorstRemoval ++;
        }
        if (remove == 3) {
            iteration.numShawRemoval ++;
        }

        if (insert == 1) {
            iteration.numRegretInsert ++;
        } else {
            iteration.numGreedyInsert ++;
        }
    }

    private void updateScore(Iteration iteration, int remove, int insert, int score) {
        if (remove == 1) {
            iteration.scoreRandomRemoval += score;
        }
        if (remove == 2) {
            iteration.scoreWorstRemoval += score;
        }
        if (remove == 3) {
            iteration.scoreShawRemoval += score;
        }

        if (insert == 1) {
            iteration.scoreRegretInsert += score;
        } else {
            iteration.scoreGreedyInsert += score;
        }
    }

    private List<Batch> getBatchListFromSolution(Iteration iteration) {
        List<Batch> batchList = new ArrayList<Batch>();
        for (List<Integer> batchSolution : iteration.solution) {
            Batch batch = new Batch();
            for (Integer id : batchSolution) {
                batch.addOrder(orderList.get(id));
            }
            batchList.add(batch);
        }
        return batchList;
    }

    private List<Batch> getBatchListFromSolution(List<List<Integer>> solution) {
        List<Batch> batchList = new ArrayList<Batch>();
        for (List<Integer> batchSolution : solution) {
            Batch batch = new Batch();
            for (Integer id : batchSolution) {
                batch.addOrder(orderList.get(id));
            }
            batchList.add(batch);
        }
        return batchList;
    }

    private List<Batch> shawRemoval(List<Batch> batchList, int q) {
        for (int j=1; j<=q; j++) {
            int numRelaxed = batchList.size();

            while (removedOrderList.size() < numRelaxed) {
                // random order in relaxed list
                int randBatchId = 0;
                Batch randBatch = new Batch();
                while (randBatch.getOrderList().size() == 0) {
                    randBatchId = getRandomPosition(batchList.size());
                    randBatch = batchList.get(randBatchId);
                }
                int randOrderId = getRandomPosition(randBatch.getOrderList().size());

                Order randOrder = new Order(1);
                try {
                    randOrder = randBatch.getOrderList().get(randOrderId);
                } catch (Exception e) {
                    System.out.println(e);
                }

                removedOrderList.add(randOrder);
                randBatch.getOrderList().remove(randOrder);

                // set relatedness to orders
                for (int i = 0; i < batchList.size(); i++) {
                    if (i == randBatchId) {
                        for (Order order : batchList.get(i).getOrderList()) {
                            order.setRelatedness(getRelatedness(randOrder, order, 1));
                        }
                    } else {
                        for (Order order : batchList.get(i).getOrderList()) {
                            order.setRelatedness(getRelatedness(randOrder, order, 0));
                        }
                    }
                }

                // get min relatedness
                List<Order> orderList = new ArrayList<Order>();
                for (Batch batch : batchList) {
                    orderList.addAll(batch.getOrderList());
                }

                try {
                    Collections.sort(orderList, new Comparator<Order>() {
                        public int compare(Order o1, Order o2) {
                            if (o1.getRelatedness() < o2.getRelatedness())
                                return -1;
                            else if (o1.getRelatedness() > o2.getRelatedness())
                                return 1;
                            else
                                return 0;
                        }
                    });
                } catch (Exception e) {
                    System.out.println(e);
                }

                // random remove
                int removeOrderId = getRandomPosition(orderList.size());
                Order removeOrder = orderList.get(removeOrderId);

                // add to remove
                removedOrderList.add(removeOrder);

                // remove from list
                boolean f;
                for (Batch batch : batchList) {
                    f = false;
                    for (Order order : batch.getOrderList()) {
                        if (removeOrder.equals(order)) {
                            f = true;
//                        break;
                        }
                    }
                    if (f) {
                        batch.getOrderList().remove(removeOrder);
                        break;
                    }
                }
            }
        }
        return new ArrayList<Batch>();
    }

    private double getRelatedness(Order i, Order j, int b) {
        // calculate saving
        Batch batchi = new Batch(i);
        Batch batchj = new Batch(j);
        Batch batchij = new Batch(i,j);

        double di = Calculation.getPath(batchi, pathList).getLength();
        double dj = Calculation.getPath(batchj, pathList).getLength();
        double dij = Calculation.getPath(batchij, pathList).getLength();

        double saving = (di + dj - dij)/Math.min(di,dj);

        // calculate relatedness
        double r = 1/(saving + b);

        return Math.abs(r);
    }

    private int getRandomPosition(int L) {
        Double randomPosition = Math.pow(Math.random(),3)*L;
        return randomPosition.intValue();
    }


//    private List<List<Batch>> createNeighborhoodList(List<Batch> resultBatchList) {
//        List<List<Batch>> result = new ArrayList<List<Batch>>();
//        for (int i=0;i<=999;i++) {
//            List<Batch> neighborhood = new ArrayList<Batch>();
//            for (Batch batch : resultBatchList) {
//                neighborhood.add(new Batch(batch));
//            }
//
//            // order 1
//            int batchId1;
//            do {
//                batchId1 = (int) (Math.floor((Math.random()) * resultBatchList.size()));
//            } while (resultBatchList.get(batchId1).getOrderList().isEmpty());
//
//            int orderId1 = (int)(Math.floor((Math.random())*(resultBatchList.get(batchId1).getOrderList().size())));
//            Order order1 = resultBatchList.get(batchId1).getOrderList().get(orderId1);
//
//
//            // order 2
//            int batchId2;
//            int orderId2;
//            Order order2;
//            do {
//                do {
//                    batchId2 = (int) (Math.floor((Math.random()) * resultBatchList.size()));
//                } while (resultBatchList.get(batchId2).getOrderList().isEmpty());
//
//                orderId2 = (int)(Math.floor((Math.random())*(resultBatchList.get(batchId2).getOrderList().size())));
//                order2 = resultBatchList.get(batchId2).getOrderList().get(orderId2);
//
//            } while (batchId1 == batchId2 || order1.getCapa() != order2.getCapa());
//
//            neighborhood.get(batchId1).getOrderList().remove(order1);
//            neighborhood.get(batchId1).getOrderList().add(order2);
//            neighborhood.get(batchId2).getOrderList().remove(order2);
//            neighborhood.get(batchId2).getOrderList().add(order1);
//
//            result.add(neighborhood);
//        }
//        return result;
//    }



    private Set<List<Batch>> createNeighborhoodList(List<Batch> resultBatchList) {
        Set<List<Batch>> result = new HashSet<List<Batch>>();

        // Swap
        outerloop:
        for (int i=0; i<resultBatchList.size()-1; i++) {
            if (resultBatchList.get(i).getOrderList().isEmpty()) {
                continue;
            }
            for (int j=i+1; j<resultBatchList.size(); j++) {
                if (resultBatchList.get(j).getOrderList().isEmpty()) {
                    continue;
                }
                for (int a=0; a<resultBatchList.get(i).getOrderList().size(); a++) {
                    for (int b=0; b<resultBatchList.get(j).getOrderList().size(); b++) {
                        List<Batch> neighborhood = new ArrayList<Batch>();
                        for (Batch batch : resultBatchList) {
                            neighborhood.add(new Batch(batch));
                        }
                        //swap

                        Order order1 = neighborhood.get(i).getOrderList().get(a);
                        Order order2 = neighborhood.get(j).getOrderList().get(b);
                        if (order1.getCapa() == order2.getCapa()) {
                            neighborhood.get(i).getOrderList().remove(order1);
                            neighborhood.get(i).getOrderList().add(order2);
                            neighborhood.get(j).getOrderList().remove(order2);
                            neighborhood.get(j).getOrderList().add(order1);

                            List<List<Integer>> solution = getSolution(neighborhood);
                            //add to neighborhood
                            if (tabuList.size() >= 500000) {
                                tabuList.remove(0);
                                tabuList.add(solution);
                            }
                            if (result.size() >= 10000) {
                                break outerloop;
                            }
                        }
                    }
                }
            }
        }

        // Shift3
//        outerloop2:
//        for (int i=0; i<resultBatchList.size()-2; i++) {
//            if (resultBatchList.get(i).getOrderList().isEmpty()) {
//                continue;
//            }
//            for (int j=i+1; j<resultBatchList.size()-1; j++) {
//                if (resultBatchList.get(j).getOrderList().isEmpty()) {
//                    continue;
//                }
//                for (int k=j+1; k<resultBatchList.size(); k++) {
//                    if (resultBatchList.get(k).getOrderList().isEmpty()) {
//                        continue;
//                    }
//                    for (int a = 0; a < resultBatchList.get(i).getOrderList().size(); a++) {
//                        for (int b = 0; b < resultBatchList.get(j).getOrderList().size(); b++) {
//                            for (int c = 0; c < resultBatchList.get(k).getOrderList().size(); c++) {
//                                // create new neighborhood
//                                List<Batch> neighborhood1 = new ArrayList<Batch>();
//                                for (Batch batch : resultBatchList) {
//                                    neighborhood1.add(new Batch(batch));
//                                }
//
//                                List<Batch> neighborhood2 = new ArrayList<Batch>();
//                                for (Batch batch : resultBatchList) {
//                                    neighborhood2.add(new Batch(batch));
//                                }
//
//                                // Shift
//                                Order order1 = neighborhood1.get(i).getOrderList().get(a);
//                                Order order2 = neighborhood1.get(j).getOrderList().get(b);
//                                Order order3 = neighborhood1.get(k).getOrderList().get(c);
//                                if (order1.getCapa() == order2.getCapa() && order2.getCapa() == order3.getCapa()) {
//                                    neighborhood1.get(i).getOrderList().remove(order1);
//                                    neighborhood1.get(j).getOrderList().remove(order2);
//                                    neighborhood1.get(k).getOrderList().remove(order3);
//                                    neighborhood1.get(i).getOrderList().add(order2);
//                                    neighborhood1.get(j).getOrderList().add(order3);
//                                    neighborhood1.get(k).getOrderList().add(order1);
//
//                                    neighborhood2.get(i).getOrderList().remove(order1);
//                                    neighborhood2.get(j).getOrderList().remove(order2);
//                                    neighborhood2.get(k).getOrderList().remove(order3);
//                                    neighborhood2.get(i).getOrderList().add(order3);
//                                    neighborhood2.get(j).getOrderList().add(order1);
//                                    neighborhood2.get(k).getOrderList().add(order2);
//
//                                    //add to neighborhood
//                                    try {
//                                        List<List<Integer>> solution1 = getSolution(neighborhood1);
//                                        List<List<Integer>> solution2 = getSolution(neighborhood2);
//                                        if (!tabuList.contains(solution1)) {
//                                            result.add(neighborhood1);
//                                            tabuList.add(solution1);
//                                        }
//                                        if (!tabuList.contains(solution2)) {
//                                            result.add(neighborhood2);
//                                            tabuList.add(solution2);
//                                        }
//                                        if (result.size() >= 500000) {
//                                            break outerloop2;
//                                        }
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }


        return result;
    }



//    private List<List<List<Integer>>> createNeighborhoodList(List<Batch> resultBatchList) {
//        // random 3 batches
//        List<List<Integer>> batchPositions = new ArrayList<List<Integer>>();
//        List<List<List<Integer>>> neighborhoodList = new ArrayList<List<List<Integer>>>();
//        List<List<Integer>> solution;
//
//        for (int j=0;j <5000;j++) {
//            int capa = 1;
//            int batchId1;
//            int batchId2;
//            int batchId3;
//            do {
//                batchId1 = (int) (Math.floor((Math.random()) * resultBatchList.size()));
//                batchId2 = (int) (Math.floor((Math.random()) * resultBatchList.size()));
//                batchId3 = (int) (Math.floor((Math.random()) * resultBatchList.size()));
//            } while (batchId1 == batchId2 || batchId1 == batchId3 || batchId2 == batchId3);
//
//
//            Batch batch1 = resultBatchList.get(batchId1);
//            Batch batch2 = resultBatchList.get(batchId2);
//            Batch batch3 = resultBatchList.get(batchId3);
//
//            if (batch1.getOrderList().isEmpty() || batch2.getOrderList().isEmpty() || batch3.getOrderList().isEmpty()) {
//                continue;
//            }
//
//            for (int i = 0; i < 3; i++) {
//                // 1
//                Order order1 = null;
//                Order order2 = null;
//                Order order3 = null;
//                for (Order order : batch1.getOrderList()) {
//                    if (order.getCapa() == capa) {
//                        order1 = order;
//                        break;
//                    }
//                }
//                for (Order order : batch2.getOrderList()) {
//                    if (order.getCapa() == capa) {
//                        order2 = order;
//                        break;
//                    }
//                }
//                for (Order order : batch3.getOrderList()) {
//                    if (order.getCapa() == capa) {
//                        order3 = order;
//                        break;
//                    }
//                }
//                capa++;
//
//                if (order1 == null || order2 == null || order3 == null) {
//                    continue;
//                }
//                // 132
//                batch2.getOrderList().remove(order2);
//                batch2.getOrderList().add(order3);
//                batch3.getOrderList().remove(order3);
//                batch3.getOrderList().add(order2);
//
//                solution = getSolution(resultBatchList);
//                if (!tabuList.contains(solution)) {
//                    neighborhoodList.add(solution);
//                    if (tabuList.size() >= 50000) {
//                        tabuList.remove(0);
//                        tabuList.add(solution);
//                    }
//                }
//
//                // 231
//                batch1.getOrderList().remove(order1);
//                batch1.getOrderList().add(order2);
//                batch3.getOrderList().remove(order2);
//                batch3.getOrderList().add(order1);
//
//                solution = getSolution(resultBatchList);
//                if (tabuList.size() >= 50000) {
//                    tabuList.remove(0);
//                    tabuList.add(solution);
//                }
//
//                // 213
//                batch2.getOrderList().remove(order3);
//                batch2.getOrderList().add(order1);
//                batch3.getOrderList().remove(order1);
//                batch3.getOrderList().add(order3);
//
//                solution = getSolution(resultBatchList);
//                if (tabuList.size() >= 50000) {
//                    tabuList.remove(0);
//                    tabuList.add(solution);
//                }
//
//                // 312
//                batch1.getOrderList().remove(order2);
//                batch1.getOrderList().add(order3);
//                batch3.getOrderList().remove(order3);
//                batch3.getOrderList().add(order2);
//
//                solution = getSolution(resultBatchList);
//                if (tabuList.size() >= 50000) {
//                    tabuList.remove(0);
//                    tabuList.add(solution);
//                }
//
//                // 321
//                batch2.getOrderList().remove(order1);
//                batch2.getOrderList().add(order2);
//                batch3.getOrderList().remove(order2);
//                batch3.getOrderList().add(order1);
//
//                solution = getSolution(resultBatchList);
//                if (tabuList.size() >= 50000) {
//                    tabuList.remove(0);
//                    tabuList.add(solution);
//                }
//
//            }
//        }
//
//        return neighborhoodList;
//    }

    private List<Order> swappedList(List<Order> orderList) {
        for (int i=1;i<orderList.size();i++) {
            Collections.swap(orderList, i, i-1);
        }
        return orderList;
    }
    private void updateScore(int remove, int insert, int score) {
        if (remove == 1) {
            score_randomRemove += score;
        }
        if (remove == 2) {
            score_worstRemove += score;
        }
        if (remove == 3) {
            score_shawRemove += score;
        }
        if (insert == 1) {
            score_regretInsert += score;
        }
        if (insert == 2) {
            score_greedyInsert += score;
        }
    }
}
