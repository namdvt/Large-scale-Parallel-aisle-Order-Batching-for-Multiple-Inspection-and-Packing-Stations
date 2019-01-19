import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
//import ilog.concert.*;
//import ilog.cplex.*;

import java.io.*;
import java.util.*;

/**
 * Created by nam on 1/13/2017.
 */
public class Main {
    public static List<Path> pathList = new ArrayList<Path>();
    public static List<Order> orderList = new ArrayList<Order>();
    public static List<Batch> batchList = new ArrayList<Batch>();
    public static int minNumberOfOrder = 0;
    public static int maxCapa = 25;
    public static int maxOrder = 12;
    public static int numRoute = 12;
    public static File resultFile;

    // For CPLEX
    public static int numOrder;
    public static int numAisle;
    public static int[] capa;
    public static int[][] Vrda;
    public static double cplexTimeLimit = 7200;
    public static int numBatch;

    public static void main(String args[]) throws Exception {
//        int depot = Integer.parseInt(args[0]);
//        resultFile = new File(args[0]);

        FileReaderHelper fileReaderHelper = new FileReaderHelper();
//        int depot = 0;
//        resultFile = new File("E:/baitap3/baitapmo3/result.xls");

        List<Result> resultList = new ArrayList<Result>();
        do {
            RandomGenerator generator = new RandomGenerator();
            RandomData randomData = generator.getRandomValues();

            // For CPLEX
            orderList = randomData.getOrderList();
            numOrder = randomData.getNumOrder();
            numAisle = randomData.getNumAisle();
            Vrda = randomData.getVrda();
            capa = randomData.getCapa();
        } while (checkCapa(numOrder, capa) == false);

        // not CPLEX
        List<Ordering> orderingList = new ArrayList<Ordering>();

        for (int d = 0; d <= 1; d++) {
            if (d != 4 && d != 5) {
                orderingList.clear();
                resultList.clear();

                getPathsAndOrders(fileReaderHelper, d);
//                orderingList.add(new CW_SWP(orderList, pathList, maxOrder, numAisle));
//                orderingList.add(new CW_PTS(orderList, pathList, maxCapa, numAisle));

//                orderingList.add(new FIFO_SWP(orderList, maxOrder));
//                orderingList.add(new FIFO_PTS(orderList, maxCapa));
                orderingList.add(new SEED_SWP(orderList, pathList, maxOrder, numAisle));
//                orderingList.add(new SEED_PTS(orderList, pathList, maxCapa, numAisle));

                for (Ordering ordering : orderingList) {
                    resultList.add(ordering.getResult());
                }

//                Hybrid_PTS hybrid_pts = new Hybrid_PTS();
//                Hybrid_SWP hybrid_swp = new Hybrid_SWP();
//                for (Result result : resultList) {
//                    if (Type.CW_PTS.equals(result.getType())) {
//                        hybrid_pts = new Hybrid_PTS(orderList, pathList, result.getBatchListResult(), maxCapa, numAisle);
//
//                    }
//                    if (Type.CW_SWP.equals(result.getType())) {
//                        hybrid_swp = new Hybrid_SWP(orderList, pathList, result.getBatchListResult(), maxOrder, numAisle);
//                        orderingList.add(hybrid_swp);
//
//                    }
//                }
//                resultList.add(hybrid_swp.getResult());
//                resultList.add(hybrid_pts.getResult());


//                if (numOrder < 50) {
//                    numBatch = 4;
//                } else if (numOrder < 100) {
//                    numBatch = 8;
//                } else if (numOrder < 150) {
//                    numBatch = 12;
//                } else if (numOrder < 200) {
//                    numBatch = 16;
//                } else if (numOrder < 250) {
//                    numBatch = 20;
//                } else if (numOrder < 300) {
//                    numBatch = 24;
//                } else if (numOrder < 400) {
//                    numBatch = 32;
//                } else if (numOrder < 500) {
//                    numBatch = 40;
//                } else if (numOrder < 600) {
//                    numBatch = 48;
//                } else {
//                    throw new Exception("Wrong numBatch Value");
//                }

//                resultList.add(CPLEX_SWP_DUP(d, numBatch));
//                resultList.add(CPLEX_PTS_DUP(d, numBatch));
//                resultList.add(CPLEX_SWP(d, numBatch));
//                resultList.add(CPLEX_PTS(d, numBatch));
//                resultList.add(CPLEX_SWP_DUP_LB(d, numBatch));
//                resultList.add(CPLEX_PTS_DUP_LB(d, numBatch));
//                resultList.add(CPLEX_SWP_LB(d, numBatch));
//                resultList.add(CPLEX_PTS_LB(d, numBatch));
//                resultList.add(CPLEX_SWP_DUP_LLB(d, numBatch));
//                resultList.add(CPLEX_PTS_DUP_LLB(d, numBatch));
//                resultList.add(CPLEX_SWP_LLB(d, numBatch));
//                resultList.add(CPLEX_PTS_LLB(d, numBatch));
                String fileName = d + ".xls";
//                exportToExcel(resultList, new File(new String(args[0] + fileName)));
                exportToExcel(resultList, new File(new String("D:/baitapmo3 - 20171122 LB/result" + d + ".xls")));
            }
        }
    }

    private static boolean checkCapa(int numOrder, int[] capa) {
        int totalCapa = 0;
        for (int i : capa) {
            totalCapa += i;
        }

        if (numOrder < 50 && totalCapa > 100) {
            return false;
        } else if (numOrder < 100 && totalCapa > 200) {
            return false;
        } else if (numOrder < 361 && totalCapa > 720) {
            return false;
        } else if (numOrder < 721 && totalCapa > 1440) {
            return false;
        } else if (numOrder < 1081 && totalCapa > 2160) {
            return false;
        } else if (numOrder < 1441 && totalCapa > 2880) {
            return false;
        } else if (numOrder < 1801 && totalCapa > 3600) {
            return false;
        } else if (numOrder < 2161 && totalCapa > 4320) {
            return false;
        }
        return true;
    }

    /*
    public static Result CPLEX_SWP_DUP(int depot, int numBatch) throws Exception {
//        int numBatch = 4;
        int CapaSWP = 12;
        long executeTime = 0;
        double totalLength = 0;
        IloCplex cplex = new IloCplex();
        cplex.setParam(IloCplex.DoubleParam.TiLim, cplexTimeLimit);
        cplex.setParam(IloCplex.IntParam.Cliques, -1);
        cplex.setParam(IloCplex.IntParam.Covers, -1);
        cplex.setParam(IloCplex.IntParam.FlowCovers, -1);
        cplex.setParam(IloCplex.IntParam.ImplBd, -1);
        cplex.setParam(IloCplex.IntParam.GUBCovers, -1);
        cplex.setParam(IloCplex.IntParam.FracCuts, -1);  // gomory cut
        cplex.setParam(IloCplex.IntParam.FlowPaths, -1);
        cplex.setParam(IloCplex.IntParam.MIRCuts, -1);
        cplex.setParam(IloCplex.IntParam.DisjCuts, -1);
        cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, -1);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, 2);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Gomory, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Disjunctive, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Implied, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.LiftProj, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MCFCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MIRCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.PathCut, -1);
        cplex.setParam(IloCplex.BooleanParam.PreInd, false);
        List<Batch> resultBatchList = new ArrayList<Batch>();


        if (depot == 1) {
            int numRoute = 12;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0, Double.MAX_VALUE);
            }

            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            // suplementary data
            // Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }

            cplex.addMinimize(TotalObj);
            // Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }

            // ct3:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(_X[batch]), CapaSWP);
            }

            //  ct4:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }

            // ct5:
            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//            System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 2) {
//            int numDepot = 2;
            int numRoute = 17;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    32, 24, 32, 52, 32};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 0}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 0}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0, Double.MAX_VALUE);
            }

            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            // suplementary data
            // Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }

            cplex.addMinimize(TotalObj);
            // Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }

            // ct3:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(_X[batch]), CapaSWP);
            }

            //  ct4:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }

            // ct5:
            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//            System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 3) {
//            int numDepot = 3;
            int numRoute = 26;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    30, 32, 52, 24, 32, 52, 30,
                    40, 40, 60, 32, 32, 52, 24};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 0},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2},

                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0, Double.MAX_VALUE);
            }

            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            // suplementary data
            // Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }

            cplex.addMinimize(TotalObj);
            // Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }

            // ct3:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(_X[batch]), CapaSWP);
            }

            //  ct4:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }

            // ct5:
            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//            System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 6) {
//            int numDepot = 6;
            int numRoute = 47;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    24, 32, 52, 28, 36, 56, 36,
                    28, 32, 52, 24, 32, 52, 32,
                    32, 32, 52, 24, 32, 52, 32,
                    36, 36, 56, 28, 32, 52, 24,
                    40, 40, 60, 32, 32, 52, 24};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 0},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2},

                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3},

                    {1, 1, 0, 0, 0, 0, 4}, {1, 0, 0, 1, 0, 0, 4},
                    {1, 1, 1, 1, 0, 0, 4}, {0, 0, 1, 1, 0, 0, 4}, {0, 0, 1, 0, 0, 1, 4},
                    {0, 0, 1, 1, 1, 1, 4}, {0, 0, 0, 0, 1, 1, 4},

                    {1, 1, 0, 0, 0, 0, 5}, {1, 0, 0, 1, 0, 0, 5},
                    {1, 1, 1, 1, 0, 0, 5}, {0, 0, 1, 1, 0, 0, 5}, {0, 0, 1, 0, 0, 1, 5},
                    {0, 0, 1, 1, 1, 1, 5}, {0, 0, 0, 0, 1, 1, 5},

                    {1, 1, 0, 0, 0, 0, 6}, {1, 0, 0, 1, 0, 0, 6},
                    {1, 1, 1, 1, 0, 0, 6}, {0, 0, 1, 1, 0, 0, 6}, {0, 0, 1, 0, 0, 1, 6},
                    {0, 0, 1, 1, 1, 1, 6}, {0, 0, 0, 0, 1, 1, 6}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0, Double.MAX_VALUE);
            }

            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            // suplementary data
            // Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }

            cplex.addMinimize(TotalObj);
            // Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }

            // ct3:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(_X[batch]), CapaSWP);
            }

            //  ct4:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }

            // ct5:
            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//            System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 0) {
            int[] LT = {22, 28, 34, 48, 56, 56, 56, 22, 28, 48, 22, 76};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }


            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.numVarArray(numRoute, 0.0, Double.MAX_VALUE);
                }
            }


            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.numVarArray(numRoute, 0.0, Double.MAX_VALUE);
                }
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(_X[batch]), CapaSWP);
            }

            // ct4:

            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }

            // ct5:
            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));
                    }
                }
            }
//            for (int order = 0; order < numOrder; order++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int batch = 0; batch < numBatch; batch++) {
//                        for (int aisle = 0; aisle < numAisle; aisle++) {
//                            if (check(Rrda[route], Vrda.get(order)) == false) {
//                                cplex.addEq(X[batch][route], 0);
//                            }
//                            else{
//                                cplex.addEq(cplex.prod(_X[batch][order], Vrda.get(order)[aisle]), 0);
//                            }
//                        }
//                    }
//                }
//            }
//
//            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        cplex.addLe(cplex.sum(X[order]), cplex.sum(Y[batch]));
                    }
                }
            }
//


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
//                double[] YVals = cplex.getValues(Y);
//                System.out.println("YVals:");
//                for (int route = 0; route < numRoute; route++) {
//                    System.out.println(YVals);
//                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else {
            throw new Exception("Wrong depot value");
        }
        return new Result(Type.CPLEX_SWP_DUP, resultBatchList, executeTime, totalLength);
    }

    public static Result CPLEX_PTS_DUP(int depot, int numBatch) throws Exception {
//        int numRoute = 12;
//        int numBatch = 4;
        long executeTime = 0;
        double totalLength = 0;
        IloCplex cplex = new IloCplex();
        cplex.setParam(IloCplex.DoubleParam.TiLim, cplexTimeLimit);
        cplex.setParam(IloCplex.IntParam.Cliques, -1);
        cplex.setParam(IloCplex.IntParam.Covers, -1);
        cplex.setParam(IloCplex.IntParam.FlowCovers, -1);
        cplex.setParam(IloCplex.IntParam.ImplBd, -1);
        cplex.setParam(IloCplex.IntParam.GUBCovers, -1);
        cplex.setParam(IloCplex.IntParam.FracCuts, -1);  // gomory cut
        cplex.setParam(IloCplex.IntParam.FlowPaths, -1);
        cplex.setParam(IloCplex.IntParam.MIRCuts, -1);
        cplex.setParam(IloCplex.IntParam.DisjCuts, -1);
        cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, -1);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, 2);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Gomory, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Disjunctive, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Implied, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.LiftProj, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MCFCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MIRCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.PathCut, -1);
        cplex.setParam(IloCplex.BooleanParam.PreInd, false);
        List<Batch> resultBatchList = new ArrayList<Batch>();


        if (depot == 1) {
            int numRoute = 12;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][batch]), ct3[batch][order]);
                }
            }
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(ct3[batch]), maxCapa);
            }

            // ct4:

            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }


            // ct5:

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }


            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));

                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 2) {
            int numRoute = 17;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    32, 24, 32, 52, 32};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 0}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 0}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}};
            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][batch]), ct3[batch][order]);
                }
            }
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(ct3[batch]), maxCapa);
            }

            // ct4:

            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }


            // ct5:

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }


            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));

                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 3) {
//            int numDepot = 3;
            int numRoute = 26;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    30, 32, 52, 24, 32, 52, 30,
                    40, 40, 60, 32, 32, 52, 24};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 0},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2},

                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}};


            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][batch]), ct3[batch][order]);
                }
            }
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(ct3[batch]), maxCapa);
            }

            // ct4:

            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }


            // ct5:

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }


            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));

                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 6) {
//            int numDepot = 6;
            int numRoute = 47;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    24, 32, 52, 28, 36, 56, 36,
                    28, 32, 52, 24, 32, 52, 32,
                    32, 32, 52, 24, 32, 52, 32,
                    36, 36, 56, 28, 32, 52, 24,
                    40, 40, 60, 32, 32, 52, 24};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 0},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2},

                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3},

                    {1, 1, 0, 0, 0, 0, 4}, {1, 0, 0, 1, 0, 0, 4},
                    {1, 1, 1, 1, 0, 0, 4}, {0, 0, 1, 1, 0, 0, 4}, {0, 0, 1, 0, 0, 1, 4},
                    {0, 0, 1, 1, 1, 1, 4}, {0, 0, 0, 0, 1, 1, 4},

                    {1, 1, 0, 0, 0, 0, 5}, {1, 0, 0, 1, 0, 0, 5},
                    {1, 1, 1, 1, 0, 0, 5}, {0, 0, 1, 1, 0, 0, 5}, {0, 0, 1, 0, 0, 1, 5},
                    {0, 0, 1, 1, 1, 1, 5}, {0, 0, 0, 0, 1, 1, 5},

                    {1, 1, 0, 0, 0, 0, 6}, {1, 0, 0, 1, 0, 0, 6},
                    {1, 1, 1, 1, 0, 0, 6}, {0, 0, 1, 1, 0, 0, 6}, {0, 0, 1, 0, 0, 1, 6},
                    {0, 0, 1, 1, 1, 1, 6}, {0, 0, 0, 0, 1, 1, 6}};


            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][batch]), ct3[batch][order]);
                }
            }
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(ct3[batch]), maxCapa);
            }

            // ct4:

            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }


            // ct5:

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }


            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));

                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 0) {
            int[] LT = {22, 28, 34, 48, 56, 56, 56, 22, 28, 48, 22, 76};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.numVarArray(numRoute, 0.0, Double.MAX_VALUE);
                }
            }


            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.numVarArray(numRoute, 0.0, Double.MAX_VALUE);
                }
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][batch]), ct3[batch][order]);
                }
            }
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(ct3[batch]), maxCapa);
            }

            // ct4:

            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }


            // ct5:

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }


            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));

                    }
                }
            }

            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        cplex.addLe(cplex.sum(X[order]), cplex.sum(Y[batch]));
                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else {
            throw new Exception("Wrong depot value");
        }

        return new Result(Type.CPLEX_PTS_DUP, resultBatchList, executeTime, totalLength);
    }

    public static Result CPLEX_SWP(int depot, int numBatch) throws Exception {
//        int numRoute = 12;
//        int numBatch = 4;
        int CapaSWP = 12;
        long executeTime = 0;
        double totalLength = 0;
        IloCplex cplex = new IloCplex();
        cplex.setParam(IloCplex.DoubleParam.TiLim, cplexTimeLimit);
        cplex.setParam(IloCplex.IntParam.Cliques, -1);
        cplex.setParam(IloCplex.IntParam.Covers, -1);
        cplex.setParam(IloCplex.IntParam.FlowCovers, -1);
        cplex.setParam(IloCplex.IntParam.ImplBd, -1);
        cplex.setParam(IloCplex.IntParam.GUBCovers, -1);
        cplex.setParam(IloCplex.IntParam.FracCuts, -1);  // gomory cut
        cplex.setParam(IloCplex.IntParam.FlowPaths, -1);
        cplex.setParam(IloCplex.IntParam.MIRCuts, -1);
        cplex.setParam(IloCplex.IntParam.DisjCuts, -1);
        cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, -1);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, 2);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Gomory, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Disjunctive, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Implied, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.LiftProj, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MCFCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MIRCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.PathCut, -1);
        cplex.setParam(IloCplex.BooleanParam.PreInd, false);
        List<Batch> resultBatchList = new ArrayList<Batch>();


        if (depot == 1) {
            int numRoute = 12;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0}, {1, 0, 0, 1, 0, 0}, {1, 0, 0, 0, 0, 1},
                    {1, 1, 1, 1, 0, 0}, {1, 1, 1, 0, 0, 1}, {1, 1, 0, 0, 1, 1},
                    {1, 0, 0, 1, 1, 1}, {0, 0, 1, 1, 0, 0}, {0, 0, 1, 0, 0, 1},
                    {0, 0, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1}, {1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0, Double.MAX_VALUE);
            }

            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            // suplementary data
            // Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }

            cplex.addMinimize(TotalObj);
            // Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }

            // ct3:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(_X[batch]), CapaSWP);
            }

            //  ct4:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }

            // ct5:
            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//            System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 2) {
//            int numDepot = 2;
            int numRoute = 24;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    32, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2}};
            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0, Double.MAX_VALUE);
            }

            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            // suplementary data
            // Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }

            cplex.addMinimize(TotalObj);
            // Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }

            // ct3:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(_X[batch]), CapaSWP);
            }

            //  ct4:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }

            // ct5:
            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 3) {
//            int numDepot = 3;
            int numRoute = 36;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 24, 32, 52, 24, 80,
                    30, 32, 40, 52, 60, 60, 60, 24, 32, 52, 30, 80,
                    40, 40, 40, 60, 60, 60, 60, 32, 32, 52, 24, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2},
                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3}, {1, 0, 0, 0, 0, 1, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {1, 1, 1, 0, 0, 1, 3}, {1, 1, 0, 0, 1, 1, 3},
                    {1, 0, 0, 1, 1, 1, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}, {1, 1, 1, 1, 1, 1, 3}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0, Double.MAX_VALUE);
            }

            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            // suplementary data
            // Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }

            cplex.addMinimize(TotalObj);
            // Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }

            // ct3:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(_X[batch]), CapaSWP);
            }

            //  ct4:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }

            // ct5:
            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 6) {
//            int numDepot = 6;
            int numRoute = 72;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 28, 32, 52, 24, 80,
                    24, 32, 40, 52, 60, 60, 60, 28, 36, 56, 36, 80,
                    28, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    32, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    36, 36, 40, 56, 60, 60, 60, 28, 32, 52, 24, 80,
                    40, 40, 40, 60, 60, 60, 60, 32, 32, 52, 24, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2},
                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3}, {1, 0, 0, 0, 0, 1, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {1, 1, 1, 0, 0, 1, 3}, {1, 1, 0, 0, 1, 1, 3},
                    {1, 0, 0, 1, 1, 1, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}, {1, 1, 1, 1, 1, 1, 3},

                    {1, 1, 0, 0, 0, 0, 4}, {1, 0, 0, 1, 0, 0, 4}, {1, 0, 0, 0, 0, 1, 4},
                    {1, 1, 1, 1, 0, 0, 4}, {1, 1, 1, 0, 0, 1, 4}, {1, 1, 0, 0, 1, 1, 4},
                    {1, 0, 0, 1, 1, 1, 4}, {0, 0, 1, 1, 0, 0, 4}, {0, 0, 1, 0, 0, 1, 4},
                    {0, 0, 1, 1, 1, 1, 4}, {0, 0, 0, 0, 1, 1, 4}, {1, 1, 1, 1, 1, 1, 4},

                    {1, 1, 0, 0, 0, 0, 5}, {1, 0, 0, 1, 0, 0, 5}, {1, 0, 0, 0, 0, 1, 5},
                    {1, 1, 1, 1, 0, 0, 5}, {1, 1, 1, 0, 0, 1, 5}, {1, 1, 0, 0, 1, 1, 5},
                    {1, 0, 0, 1, 1, 1, 5}, {0, 0, 1, 1, 0, 0, 5}, {0, 0, 1, 0, 0, 1, 5},
                    {0, 0, 1, 1, 1, 1, 5}, {0, 0, 0, 0, 1, 1, 5}, {1, 1, 1, 1, 1, 1, 5},

                    {1, 1, 0, 0, 0, 0, 6}, {1, 0, 0, 1, 0, 0, 6}, {1, 0, 0, 0, 0, 1, 6},
                    {1, 1, 1, 1, 0, 0, 6}, {1, 1, 1, 0, 0, 1, 6}, {1, 1, 0, 0, 1, 1, 6},
                    {1, 0, 0, 1, 1, 1, 6}, {0, 0, 1, 1, 0, 0, 6}, {0, 0, 1, 0, 0, 1, 6},
                    {0, 0, 1, 1, 1, 1, 6}, {0, 0, 0, 0, 1, 1, 6}, {1, 1, 1, 1, 1, 1, 6}};
            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0, Double.MAX_VALUE);
            }

            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            // suplementary data
            // Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }

            cplex.addMinimize(TotalObj);
            // Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }

            // ct3:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(_X[batch]), CapaSWP);
            }

            //  ct4:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }

            // ct5:
            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 0) {
            int[] LT = {22, 28, 34, 48, 56, 56, 56, 22, 28, 48, 22, 76};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }


            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.numVarArray(numRoute, 0.0, Double.MAX_VALUE);
                }
            }


            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.numVarArray(numRoute, 0.0, Double.MAX_VALUE);
                }
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(_X[batch]), CapaSWP);
            }

            // ct4:

            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }

            // ct5:
            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));
                    }
                }
            }
//            for (int order = 0; order < numOrder; order++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int batch = 0; batch < numBatch; batch++) {
//                        for (int aisle = 0; aisle < numAisle; aisle++) {
//                            if (check(Rrda[route], Vrda.get(order)) == false) {
//                                cplex.addEq(X[batch][route], 0);
//                            }
//                            else{
//                                cplex.addEq(cplex.prod(_X[batch][order], Vrda.get(order)[aisle]), 0);
//                            }
//                        }
//                    }
//                }
//            }
//
//            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        cplex.addLe(cplex.sum(X[order]), cplex.sum(Y[batch]));
                    }
                }
            }
//


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
//                double[] YVals = cplex.getValues(Y);
//                System.out.println("YVals:");
//                for (int route = 0; route < numRoute; route++) {
//                    System.out.println(YVals);
//                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else {
            throw new Exception("Wrong depot value");
        }
        return new Result(Type.CPLEX_SWP, resultBatchList, executeTime, totalLength);
    }

    public static Result CPLEX_PTS(int depot, int numBatch) throws Exception {
//        int numRoute = 12;
//        int numBatch = 4;
        long executeTime = 0;
        double totalLength = 0;
        IloCplex cplex = new IloCplex();
        cplex.setParam(IloCplex.DoubleParam.TiLim, cplexTimeLimit);
        cplex.setParam(IloCplex.IntParam.Cliques, -1);
        cplex.setParam(IloCplex.IntParam.Covers, -1);
        cplex.setParam(IloCplex.IntParam.FlowCovers, -1);
        cplex.setParam(IloCplex.IntParam.ImplBd, -1);
        cplex.setParam(IloCplex.IntParam.GUBCovers, -1);
        cplex.setParam(IloCplex.IntParam.FracCuts, -1);  // gomory cut
        cplex.setParam(IloCplex.IntParam.FlowPaths, -1);
        cplex.setParam(IloCplex.IntParam.MIRCuts, -1);
        cplex.setParam(IloCplex.IntParam.DisjCuts, -1);
        cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, -1);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, 2);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Gomory, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Disjunctive, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Implied, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.LiftProj, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MCFCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MIRCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.PathCut, -1);
        cplex.setParam(IloCplex.BooleanParam.PreInd, false);
        List<Batch> resultBatchList = new ArrayList<Batch>();


        if (depot == 1) {
            int numRoute = 12;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

//
            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][batch]), ct3[batch][order]);
                }
            }
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(ct3[batch]), maxCapa);
            }

            // ct4:

            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }


            // ct5:

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }


            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));

                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 2) {
            //            int numDepot = 2;
            int numRoute = 24;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    32, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][batch]), ct3[batch][order]);
                }
            }
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(ct3[batch]), maxCapa);
            }

            // ct4:

            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }


            // ct5:

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }


            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));

                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 3) {
//            int numDepot = 3;
            int numRoute = 36;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 24, 32, 52, 24, 80,
                    30, 32, 40, 52, 60, 60, 60, 24, 32, 52, 30, 80,
                    40, 40, 40, 60, 60, 60, 60, 32, 32, 52, 24, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2},
                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3}, {1, 0, 0, 0, 0, 1, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {1, 1, 1, 0, 0, 1, 3}, {1, 1, 0, 0, 1, 1, 3},
                    {1, 0, 0, 1, 1, 1, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}, {1, 1, 1, 1, 1, 1, 3}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][batch]), ct3[batch][order]);
                }
            }
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(ct3[batch]), maxCapa);
            }

            // ct4:

            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }


            // ct5:

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }


            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));

                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 6) {
//            int numDepot = 6;
            int numRoute = 72;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 28, 32, 52, 24, 80,
                    24, 32, 40, 52, 60, 60, 60, 28, 36, 56, 36, 80,
                    28, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    32, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    36, 36, 40, 56, 60, 60, 60, 28, 32, 52, 24, 80,
                    40, 40, 40, 60, 60, 60, 60, 32, 32, 52, 24, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2},
                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3}, {1, 0, 0, 0, 0, 1, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {1, 1, 1, 0, 0, 1, 3}, {1, 1, 0, 0, 1, 1, 3},
                    {1, 0, 0, 1, 1, 1, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}, {1, 1, 1, 1, 1, 1, 3},

                    {1, 1, 0, 0, 0, 0, 4}, {1, 0, 0, 1, 0, 0, 4}, {1, 0, 0, 0, 0, 1, 4},
                    {1, 1, 1, 1, 0, 0, 4}, {1, 1, 1, 0, 0, 1, 4}, {1, 1, 0, 0, 1, 1, 4},
                    {1, 0, 0, 1, 1, 1, 4}, {0, 0, 1, 1, 0, 0, 4}, {0, 0, 1, 0, 0, 1, 4},
                    {0, 0, 1, 1, 1, 1, 4}, {0, 0, 0, 0, 1, 1, 4}, {1, 1, 1, 1, 1, 1, 4},

                    {1, 1, 0, 0, 0, 0, 5}, {1, 0, 0, 1, 0, 0, 5}, {1, 0, 0, 0, 0, 1, 5},
                    {1, 1, 1, 1, 0, 0, 5}, {1, 1, 1, 0, 0, 1, 5}, {1, 1, 0, 0, 1, 1, 5},
                    {1, 0, 0, 1, 1, 1, 5}, {0, 0, 1, 1, 0, 0, 5}, {0, 0, 1, 0, 0, 1, 5},
                    {0, 0, 1, 1, 1, 1, 5}, {0, 0, 0, 0, 1, 1, 5}, {1, 1, 1, 1, 1, 1, 5},

                    {1, 1, 0, 0, 0, 0, 6}, {1, 0, 0, 1, 0, 0, 6}, {1, 0, 0, 0, 0, 1, 6},
                    {1, 1, 1, 1, 0, 0, 6}, {1, 1, 1, 0, 0, 1, 6}, {1, 1, 0, 0, 1, 1, 6},
                    {1, 0, 0, 1, 1, 1, 6}, {0, 0, 1, 1, 0, 0, 6}, {0, 0, 1, 0, 0, 1, 6},
                    {0, 0, 1, 1, 1, 1, 6}, {0, 0, 0, 0, 1, 1, 6}, {1, 1, 1, 1, 1, 1, 6}};


            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }

            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.intVarArray(numRoute, 0, 1);
                }
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][batch]), ct3[batch][order]);
                }
            }
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(ct3[batch]), maxCapa);
            }

            // ct4:

            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }


            // ct5:

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }


            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));

                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 0) {
            int[] LT = {22, 28, 34, 48, 56, 56, 56, 22, 28, 48, 22, 76};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numBatch, 0, 1);
            }
            IloNumVar[][] _X = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                _X[batch] = cplex.intVarArray(numOrder, 0, 1);
            }
            IloNumVar[][] Y = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                Y[batch] = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);
            }

            IloNumVar[][] ct3 = new IloNumVar[numBatch][];
            for (int batch = 0; batch < numBatch; batch++) {
                ct3[batch] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            IloNumVar[][][] RAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    RAY[batch][aisle] = cplex.numVarArray(numRoute, 0.0, Double.MAX_VALUE);
                }
            }


            IloNumVar[][][] _DRAY = new IloNumVar[numBatch][numAisle][];
            for (int batch = 0; batch < numBatch; batch++) {
                for (int aisle = 0; aisle < numAisle; aisle++) {
                    _DRAY[batch][aisle] = cplex.numVarArray(numRoute, 0.0, Double.MAX_VALUE);
                }
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int batch = 0; batch < numBatch; batch++) {
                for (int route = 0; route < numRoute; route++) {
                    TotalObj.addTerm(LT[route], Y[batch][route]);
                }
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(X[order][batch], _X[batch][order]);
                }
            }

            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][batch]), ct3[batch][order]);
                }
            }
            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addLe(cplex.sum(ct3[batch]), maxCapa);
            }

            // ct4:

            for (int batch = 0; batch < numBatch; batch++) {
                cplex.addEq(cplex.sum(Y[batch]), 1);
            }


            // ct5:

            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int route = 0; route < numRoute; route++) {
                        cplex.addEq(cplex.prod(Y[batch][route], Rrda[route][aisle]), RAY[batch][aisle][route]);
                    }
                }
            }


            for (int aisle = 0; aisle < numAisle; aisle++) {
                for (int batch = 0; batch < numBatch; batch++) {
                    for (int order = 0; order < numOrder; order++) {
                        cplex.addLe(
                                cplex.prod(_X[batch][order], Vrda[order][aisle]),
                                cplex.sum(RAY[batch][aisle]));

                    }
                }
            }
//ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        cplex.addLe(cplex.sum(X[order]), cplex.sum(Y[batch]));
                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else {
            throw new Exception("Wrong depot value");
        }

        return new Result(Type.CPLEX_PTS, resultBatchList, executeTime, totalLength);
    }

    // LB
    public static Result CPLEX_SWP_DUP_LB(int depot, int numBatch) throws Exception {
//        int numBatch = 4;
        int CapaSWP = 12;
        long executeTime = 0;
        double totalLength = 0;
        IloCplex cplex = new IloCplex();
        cplex.setParam(IloCplex.DoubleParam.TiLim, cplexTimeLimit);
        cplex.setParam(IloCplex.IntParam.Cliques, -1);
        cplex.setParam(IloCplex.IntParam.Covers, -1);
        cplex.setParam(IloCplex.IntParam.FlowCovers, -1);
        cplex.setParam(IloCplex.IntParam.ImplBd, -1);
        cplex.setParam(IloCplex.IntParam.GUBCovers, -1);
        cplex.setParam(IloCplex.IntParam.FracCuts, -1);  // gomory cut
        cplex.setParam(IloCplex.IntParam.FlowPaths, -1);
        cplex.setParam(IloCplex.IntParam.MIRCuts, -1);
        cplex.setParam(IloCplex.IntParam.DisjCuts, -1);
        cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, -1);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, 2);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Gomory, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Disjunctive, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Implied, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.LiftProj, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MCFCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MIRCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.PathCut, -1);
        cplex.setParam(IloCplex.BooleanParam.PreInd, false);
        List<Batch> resultBatchList = new ArrayList<Batch>();


        if (depot == 1) {
            int numRoute = 12;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

//            // ct4:
//
//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct5
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//            System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numRoute];
                for (int order = 0; order < numOrder; order++) {
                    for (int route = 0; route < numRoute; route++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
//                double[] YVals = new double[numRoute];
//                for (int route = 0; route < numRoute; route++) {
//                    YVals = cplex.getValues(Y);
//                }
//
//                List<Batch> remainBatchList = new ArrayList<Batch>();
//                for (int route = 0; route < numRoute; route++) {
//                    if (YVals[route] > 0) {
//                        List<Order> orderListInRoute = new ArrayList<Order>();
//                        for (int order = 0; order < numOrder; order++) {
//                            if (XVals[order][route] > 0) {
//                                orderListInRoute.add(orderList.get(order));
//                            }
//                        }
//
//                        List<Order> tempOrders = new ArrayList<Order>();
//                        for (Order order : orderListInRoute) {
//                            tempOrders.add(order);
//                            if (tempOrders.size() ==12) {
//                                Batch newBatch = new Batch();
//                                newBatch.addOrder(tempOrders);
//                                resultBatchList.add(newBatch);
//                                tempOrders.clear();
//                            }
//                        }
//                        if (tempOrders.size() > 0) {
//                            Batch newBatch = new Batch();
//                            newBatch.addOrder(tempOrders);
//                            remainBatchList.add(newBatch);
//                        }
//                        System.out.println();
//                    }
//                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 2) {
//            int numDepot = 2;
            int numRoute = 17;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    32, 24, 32, 52, 32};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 0}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 0}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

//            // ct4:
//
//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct5
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//            System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 3) {
//            int numDepot = 3;
            int numRoute = 26;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    30, 32, 52, 24, 32, 52, 30,
                    40, 40, 60, 32, 32, 52, 24};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 0},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2},

                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//            System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 6) {
//            int numDepot = 6;
            int numRoute = 47;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    24, 32, 52, 28, 36, 56, 36,
                    28, 32, 52, 24, 32, 52, 32,
                    32, 32, 52, 24, 32, 52, 32,
                    36, 36, 56, 28, 32, 52, 24,
                    40, 40, 60, 32, 32, 52, 24};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 0},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2},

                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3},

                    {1, 1, 0, 0, 0, 0, 4}, {1, 0, 0, 1, 0, 0, 4},
                    {1, 1, 1, 1, 0, 0, 4}, {0, 0, 1, 1, 0, 0, 4}, {0, 0, 1, 0, 0, 1, 4},
                    {0, 0, 1, 1, 1, 1, 4}, {0, 0, 0, 0, 1, 1, 4},

                    {1, 1, 0, 0, 0, 0, 5}, {1, 0, 0, 1, 0, 0, 5},
                    {1, 1, 1, 1, 0, 0, 5}, {0, 0, 1, 1, 0, 0, 5}, {0, 0, 1, 0, 0, 1, 5},
                    {0, 0, 1, 1, 1, 1, 5}, {0, 0, 0, 0, 1, 1, 5},

                    {1, 1, 0, 0, 0, 0, 6}, {1, 0, 0, 1, 0, 0, 6},
                    {1, 1, 1, 1, 0, 0, 6}, {0, 0, 1, 1, 0, 0, 6}, {0, 0, 1, 0, 0, 1, 6},
                    {0, 0, 1, 1, 1, 1, 6}, {0, 0, 0, 0, 1, 1, 6}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//            System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 0) {
            int[] LT = {22, 28, 34, 48, 56, 56, 56, 22, 28, 48, 22, 76};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }



            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
//                double[] YVals = cplex.getValues(Y);
//                System.out.println("YVals:");
//                for (int route = 0; route < numRoute; route++) {
//                    System.out.println(YVals);
//                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else {
            throw new Exception("Wrong depot value");
        }
        return new Result(Type.CPLEX_SWP_DUP_LB, resultBatchList, executeTime, totalLength);
    }

    public static Result CPLEX_PTS_DUP_LB(int depot, int numBatch) throws Exception {
//        int numRoute = 12;
//        int numBatch = 4;

        long executeTime = 0;
        double totalLength = 0;
        IloCplex cplex = new IloCplex();
        cplex.setParam(IloCplex.DoubleParam.TiLim, cplexTimeLimit);
        cplex.setParam(IloCplex.IntParam.Cliques, -1);
        cplex.setParam(IloCplex.IntParam.Covers, -1);
        cplex.setParam(IloCplex.IntParam.FlowCovers, -1);
        cplex.setParam(IloCplex.IntParam.ImplBd, -1);
        cplex.setParam(IloCplex.IntParam.GUBCovers, -1);
        cplex.setParam(IloCplex.IntParam.FracCuts, -1);  // gomory cut
        cplex.setParam(IloCplex.IntParam.FlowPaths, -1);
        cplex.setParam(IloCplex.IntParam.MIRCuts, -1);
        cplex.setParam(IloCplex.IntParam.DisjCuts, -1);
        cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, -1);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, 2);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Gomory, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Disjunctive, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Implied, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.LiftProj, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MCFCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MIRCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.PathCut, -1);
        cplex.setParam(IloCplex.BooleanParam.PreInd, false);
        List<Batch> resultBatchList = new ArrayList<Batch>();


        if (depot == 1) {
            int numRoute = 12;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 2) {
            int numRoute = 17;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    32, 24, 32, 52, 32};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 0}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 0}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}};
            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 3) {
//            int numDepot = 3;
            int numRoute = 26;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    30, 32, 52, 24, 32, 52, 30,
                    40, 40, 60, 32, 32, 52, 24};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 0},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2},

                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}};


            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 6) {
//            int numDepot = 6;
            int numRoute = 47;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    24, 32, 52, 28, 36, 56, 36,
                    28, 32, 52, 24, 32, 52, 32,
                    32, 32, 52, 24, 32, 52, 32,
                    36, 36, 56, 28, 32, 52, 24,
                    40, 40, 60, 32, 32, 52, 24};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 0},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2},

                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3},

                    {1, 1, 0, 0, 0, 0, 4}, {1, 0, 0, 1, 0, 0, 4},
                    {1, 1, 1, 1, 0, 0, 4}, {0, 0, 1, 1, 0, 0, 4}, {0, 0, 1, 0, 0, 1, 4},
                    {0, 0, 1, 1, 1, 1, 4}, {0, 0, 0, 0, 1, 1, 4},

                    {1, 1, 0, 0, 0, 0, 5}, {1, 0, 0, 1, 0, 0, 5},
                    {1, 1, 1, 1, 0, 0, 5}, {0, 0, 1, 1, 0, 0, 5}, {0, 0, 1, 0, 0, 1, 5},
                    {0, 0, 1, 1, 1, 1, 5}, {0, 0, 0, 0, 1, 1, 5},

                    {1, 1, 0, 0, 0, 0, 6}, {1, 0, 0, 1, 0, 0, 6},
                    {1, 1, 1, 1, 0, 0, 6}, {0, 0, 1, 1, 0, 0, 6}, {0, 0, 1, 0, 0, 1, 6},
                    {0, 0, 1, 1, 1, 1, 6}, {0, 0, 0, 0, 1, 1, 6}};


            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 0) {
            int[] LT = {22, 28, 34, 48, 56, 56, 56, 22, 28, 48, 22, 76};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else {
            throw new Exception("Wrong depot value");
        }

        return new Result(Type.CPLEX_PTS_DUP_LB, resultBatchList, executeTime, totalLength);
    }

    public static Result CPLEX_SWP_LB(int depot, int numBatch) throws Exception {
//        int numRoute = 12;
//        int numBatch = 4;

        int CapaSWP = 12;
        long executeTime = 0;
        double totalLength = 0;
        IloCplex cplex = new IloCplex();
        cplex.setParam(IloCplex.DoubleParam.TiLim, cplexTimeLimit);
        cplex.setParam(IloCplex.IntParam.Cliques, -1);
        cplex.setParam(IloCplex.IntParam.Covers, -1);
        cplex.setParam(IloCplex.IntParam.FlowCovers, -1);
        cplex.setParam(IloCplex.IntParam.ImplBd, -1);
        cplex.setParam(IloCplex.IntParam.GUBCovers, -1);
        cplex.setParam(IloCplex.IntParam.FracCuts, -1);  // gomory cut
        cplex.setParam(IloCplex.IntParam.FlowPaths, -1);
        cplex.setParam(IloCplex.IntParam.MIRCuts, -1);
        cplex.setParam(IloCplex.IntParam.DisjCuts, -1);
        cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, -1);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, 2);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Gomory, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Disjunctive, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Implied, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.LiftProj, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MCFCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MIRCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.PathCut, -1);
        cplex.setParam(IloCplex.BooleanParam.PreInd, false);
        List<Batch> resultBatchList = new ArrayList<Batch>();


        if (depot == 1) {
            int numRoute = 12;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//            System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 2) {
//            int numDepot = 2;
            int numRoute = 24;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    32, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2}};
            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 3) {
//            int numDepot = 3;
            int numRoute = 36;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 24, 32, 52, 24, 80,
                    30, 32, 40, 52, 60, 60, 60, 24, 32, 52, 30, 80,
                    40, 40, 40, 60, 60, 60, 60, 32, 32, 52, 24, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2},
                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3}, {1, 0, 0, 0, 0, 1, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {1, 1, 1, 0, 0, 1, 3}, {1, 1, 0, 0, 1, 1, 3},
                    {1, 0, 0, 1, 1, 1, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}, {1, 1, 1, 1, 1, 1, 3}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 6) {
//            int numDepot = 6;
            int numRoute = 72;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 28, 32, 52, 24, 80,
                    24, 32, 40, 52, 60, 60, 60, 28, 36, 56, 36, 80,
                    28, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    32, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    36, 36, 40, 56, 60, 60, 60, 28, 32, 52, 24, 80,
                    40, 40, 40, 60, 60, 60, 60, 32, 32, 52, 24, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2},
                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3}, {1, 0, 0, 0, 0, 1, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {1, 1, 1, 0, 0, 1, 3}, {1, 1, 0, 0, 1, 1, 3},
                    {1, 0, 0, 1, 1, 1, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}, {1, 1, 1, 1, 1, 1, 3},

                    {1, 1, 0, 0, 0, 0, 4}, {1, 0, 0, 1, 0, 0, 4}, {1, 0, 0, 0, 0, 1, 4},
                    {1, 1, 1, 1, 0, 0, 4}, {1, 1, 1, 0, 0, 1, 4}, {1, 1, 0, 0, 1, 1, 4},
                    {1, 0, 0, 1, 1, 1, 4}, {0, 0, 1, 1, 0, 0, 4}, {0, 0, 1, 0, 0, 1, 4},
                    {0, 0, 1, 1, 1, 1, 4}, {0, 0, 0, 0, 1, 1, 4}, {1, 1, 1, 1, 1, 1, 4},

                    {1, 1, 0, 0, 0, 0, 5}, {1, 0, 0, 1, 0, 0, 5}, {1, 0, 0, 0, 0, 1, 5},
                    {1, 1, 1, 1, 0, 0, 5}, {1, 1, 1, 0, 0, 1, 5}, {1, 1, 0, 0, 1, 1, 5},
                    {1, 0, 0, 1, 1, 1, 5}, {0, 0, 1, 1, 0, 0, 5}, {0, 0, 1, 0, 0, 1, 5},
                    {0, 0, 1, 1, 1, 1, 5}, {0, 0, 0, 0, 1, 1, 5}, {1, 1, 1, 1, 1, 1, 5},

                    {1, 1, 0, 0, 0, 0, 6}, {1, 0, 0, 1, 0, 0, 6}, {1, 0, 0, 0, 0, 1, 6},
                    {1, 1, 1, 1, 0, 0, 6}, {1, 1, 1, 0, 0, 1, 6}, {1, 1, 0, 0, 1, 1, 6},
                    {1, 0, 0, 1, 1, 1, 6}, {0, 0, 1, 1, 0, 0, 6}, {0, 0, 1, 0, 0, 1, 6},
                    {0, 0, 1, 1, 1, 1, 6}, {0, 0, 0, 0, 1, 1, 6}, {1, 1, 1, 1, 1, 1, 6}};
            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 0) {
//            int numRoute = 12;
//            int[] LT = {22, 28, 34, 48, 56, 56, 56, 22, 28, 48, 22, 76};
//            int[][] Rrda = {{1, 1, 0, 0, 0, 0}, {1, 0, 0, 1, 0, 0}, {1, 0, 0, 0, 0, 1},
//                    {1, 1, 1, 1, 0, 0}, {1, 1, 1, 0, 0, 1}, {1, 1, 0, 0, 1, 1},
//                    {1, 0, 0, 1, 1, 1}, {0, 0, 1, 1, 0, 0}, {0, 0, 1, 0, 0, 1},
//                    {0, 0, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1}, {1, 1, 1, 1, 1, 1}};
//
//            long startTime = System.currentTimeMillis();
//
//            // lower bound
////            List<Path> lowerBoundPathList = new ArrayList<Path>();
////            for (Order order : orderList) {
////                for (Path path : order.getAcceptedPathList()) {
////                    if (lowerBoundPathList.contains(path) == false) {
////                        lowerBoundPathList.add(path);
////                    }
////                }
////            }
////
////            int LT[] = new int[lowerBoundPathList.size()];
////            int Rrda[][] = new int[lowerBoundPathList.size()][];
////            int i = 0;
////            for (Path path : lowerBoundPathList) {
////                LT[i] = path.getLength();
////                Rrda[i] = path.getPosition();
////                i ++;
////            }
//
//
//            IloNumVar[][] X = new IloNumVar[numOrder][];
//            for (int order = 0; order < numOrder; order++) {
//                X[order] = cplex.intVarArray(numRoute, 0, 1);
//            }
//
//            IloNumVar[][] _X = new IloNumVar[numRoute][];
//            for (int route = 0; route < numRoute; route++) {
//                _X[route] = cplex.intVarArray(numOrder, 0, 1);
//            }
//
//            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);
//
//
//
//            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
//            for (int route = 0; route < numBatch; route++) {
//                ct3[route] = cplex.numVarArray(numOrder,0.0,Double.MAX_VALUE);
//            }
//
//
//
//
//            // suplementary data
//
//
//// Obj
//            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
//            for (int route = 0; route < numRoute; route++) {
//                    TotalObj.addTerm(LT[route], Y[route]);
//                }
//
//
//
//
//            cplex.addMinimize(TotalObj);
//// Constraints
//            // X supplementary
//            for (int order = 0; order < numOrder; order++) {
//                for (int route = 0; route < numRoute; route++) {
//                    cplex.addEq(X[order][route],_X[route][order]);
//                }
//            }
//
//
//            // ct2:
//            for (int order = 0; order < numOrder; order++) {
//                cplex.addEq(cplex.sum(X[order]), 1);
//            }
//            // ct3:
//            for (int batch = 0; batch < numBatch; batch++) {
//                for (int route = 0; route < numRoute; route++) {
//                    cplex.addLe(cplex.sum(_X[batch]), cplex.prod(CapaSWP,Y[route]));
//                }
//            }
//
//            // ct4:
//
//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
//            //ct6
////            for (int order = 0; order < numOrder; order++) {
////                for (int route = 0; route < numRoute; route++) {
////                    if (check(Rrda[route], Vrda[order]) == false) {
////                                cplex.addEq(X[order][route], 0);
////                    }
////                    else {
////                        cplex.addEq(X[order][route], 1);
////                    }
////                }
////            }
//
//
//
//
//            if (cplex.solve()) {
//                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
//                    System.out.println("No Solution");
//                    return null;
//                }
            int numRoute = 12;
            int[] LT = {22, 28, 34, 48, 56, 56, 56, 22, 28, 48, 22, 76};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();

            // lower bound
//            List<Path> lowerBoundPathList = new ArrayList<Path>();
//            for (Order order : orderList) {
//                for (Path path : order.getAcceptedPathList()) {
//                    if (lowerBoundPathList.contains(path) == false) {
//                        lowerBoundPathList.add(path);
//                    }
//                }
//            }
//
//            int LT[] = new int[lowerBoundPathList.size()];
//            int Rrda[][] = new int[lowerBoundPathList.size()][];
//            int i = 0;
//            for (Path path : lowerBoundPathList) {
//                LT[i] = path.getLength();
//                Rrda[i] = path.getPosition();
//                i ++;
//            }


            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }


            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numRoute];
                for (int order = 0; order < numOrder; order++) {
                    for (int route = 0; route < numRoute; route++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
//                double[] YVals = cplex.getValues(Y);
//                System.out.println("YVals:");
//                for (int route = 0; route < numRoute; route++) {
//                    System.out.println(YVals);
//                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else {
            throw new Exception("Wrong depot value");
        }
        return new Result(Type.CPLEX_SWP_LB, resultBatchList, executeTime, totalLength);
    }


    public static Result CPLEX_PTS_LB(int depot, int numBatch) throws Exception {
//        int numRoute = 12;
//        int numBatch = 4;

        long executeTime = 0;
        double totalLength = 0;
        IloCplex cplex = new IloCplex();
        cplex.setParam(IloCplex.DoubleParam.TiLim, cplexTimeLimit);
        cplex.setParam(IloCplex.IntParam.Cliques, -1);
        cplex.setParam(IloCplex.IntParam.Covers, -1);
        cplex.setParam(IloCplex.IntParam.FlowCovers, -1);
        cplex.setParam(IloCplex.IntParam.ImplBd, -1);
        cplex.setParam(IloCplex.IntParam.GUBCovers, -1);
        cplex.setParam(IloCplex.IntParam.FracCuts, -1);  // gomory cut
        cplex.setParam(IloCplex.IntParam.FlowPaths, -1);
        cplex.setParam(IloCplex.IntParam.MIRCuts, -1);
        cplex.setParam(IloCplex.IntParam.DisjCuts, -1);
        cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, -1);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, 2);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Gomory, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Disjunctive, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Implied, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.LiftProj, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MCFCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MIRCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.PathCut, -1);
        cplex.setParam(IloCplex.BooleanParam.PreInd, false);
        List<Batch> resultBatchList = new ArrayList<Batch>();


        if (depot == 1) {
            int numRoute = 12;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

//
            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 2) {
            //            int numDepot = 2;
            int numRoute = 24;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    32, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 3) {
//            int numDepot = 3;
            int numRoute = 36;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 24, 32, 52, 24, 80,
                    30, 32, 40, 52, 60, 60, 60, 24, 32, 52, 30, 80,
                    40, 40, 40, 60, 60, 60, 60, 32, 32, 52, 24, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2},
                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3}, {1, 0, 0, 0, 0, 1, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {1, 1, 1, 0, 0, 1, 3}, {1, 1, 0, 0, 1, 1, 3},
                    {1, 0, 0, 1, 1, 1, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}, {1, 1, 1, 1, 1, 1, 3}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }
            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 6) {
//            int numDepot = 6;
            int numRoute = 72;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 28, 32, 52, 24, 80,
                    24, 32, 40, 52, 60, 60, 60, 28, 36, 56, 36, 80,
                    28, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    32, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    36, 36, 40, 56, 60, 60, 60, 28, 32, 52, 24, 80,
                    40, 40, 40, 60, 60, 60, 60, 32, 32, 52, 24, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2},
                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3}, {1, 0, 0, 0, 0, 1, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {1, 1, 1, 0, 0, 1, 3}, {1, 1, 0, 0, 1, 1, 3},
                    {1, 0, 0, 1, 1, 1, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}, {1, 1, 1, 1, 1, 1, 3},

                    {1, 1, 0, 0, 0, 0, 4}, {1, 0, 0, 1, 0, 0, 4}, {1, 0, 0, 0, 0, 1, 4},
                    {1, 1, 1, 1, 0, 0, 4}, {1, 1, 1, 0, 0, 1, 4}, {1, 1, 0, 0, 1, 1, 4},
                    {1, 0, 0, 1, 1, 1, 4}, {0, 0, 1, 1, 0, 0, 4}, {0, 0, 1, 0, 0, 1, 4},
                    {0, 0, 1, 1, 1, 1, 4}, {0, 0, 0, 0, 1, 1, 4}, {1, 1, 1, 1, 1, 1, 4},

                    {1, 1, 0, 0, 0, 0, 5}, {1, 0, 0, 1, 0, 0, 5}, {1, 0, 0, 0, 0, 1, 5},
                    {1, 1, 1, 1, 0, 0, 5}, {1, 1, 1, 0, 0, 1, 5}, {1, 1, 0, 0, 1, 1, 5},
                    {1, 0, 0, 1, 1, 1, 5}, {0, 0, 1, 1, 0, 0, 5}, {0, 0, 1, 0, 0, 1, 5},
                    {0, 0, 1, 1, 1, 1, 5}, {0, 0, 0, 0, 1, 1, 5}, {1, 1, 1, 1, 1, 1, 5},

                    {1, 1, 0, 0, 0, 0, 6}, {1, 0, 0, 1, 0, 0, 6}, {1, 0, 0, 0, 0, 1, 6},
                    {1, 1, 1, 1, 0, 0, 6}, {1, 1, 1, 0, 0, 1, 6}, {1, 1, 0, 0, 1, 1, 6},
                    {1, 0, 0, 1, 1, 1, 6}, {0, 0, 1, 1, 0, 0, 6}, {0, 0, 1, 0, 0, 1, 6},
                    {0, 0, 1, 1, 1, 1, 6}, {0, 0, 0, 0, 1, 1, 6}, {1, 1, 1, 1, 1, 1, 6}};


            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 0) {
            int[] LT = {22, 28, 34, 48, 56, 56, 56, 22, 28, 48, 22, 76};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.intVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.intVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    }
                    else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else {
            throw new Exception("Wrong depot value");
        }

        return new Result(Type.CPLEX_PTS_LB, resultBatchList, executeTime, totalLength);
    }

    //LLB
    public static Result CPLEX_SWP_DUP_LLB(int depot, int numBatch) throws Exception {
//        int numBatch = 4;

        int CapaSWP = 12;
        long executeTime = 0;
        double totalLength = 0;
        IloCplex cplex = new IloCplex();
        cplex.setParam(IloCplex.DoubleParam.TiLim, cplexTimeLimit);
        cplex.setParam(IloCplex.IntParam.Cliques, -1);
        cplex.setParam(IloCplex.IntParam.Covers, -1);
        cplex.setParam(IloCplex.IntParam.FlowCovers, -1);
        cplex.setParam(IloCplex.IntParam.ImplBd, -1);
        cplex.setParam(IloCplex.IntParam.GUBCovers, -1);
        cplex.setParam(IloCplex.IntParam.FracCuts, -1);  // gomory cut
        cplex.setParam(IloCplex.IntParam.FlowPaths, -1);
        cplex.setParam(IloCplex.IntParam.MIRCuts, -1);
        cplex.setParam(IloCplex.IntParam.DisjCuts, -1);
        cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, -1);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, 2);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Gomory, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Disjunctive, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Implied, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.LiftProj, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MCFCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MIRCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.PathCut, -1);
        cplex.setParam(IloCplex.BooleanParam.PreInd, false);
        List<Batch> resultBatchList = new ArrayList<Batch>();


        if (depot == 1) {
            int numRoute = 12;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//            System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 2) {
//            int numDepot = 2;
            int numRoute = 17;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    32, 24, 32, 52, 32};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 0}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 0}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//            System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 3) {
//            int numDepot = 3;
            int numRoute = 26;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    30, 32, 52, 24, 32, 52, 30,
                    40, 40, 60, 32, 32, 52, 24};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 0},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2},

                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//            System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 6) {
//            int numDepot = 6;
            int numRoute = 47;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    24, 32, 52, 28, 36, 56, 36,
                    28, 32, 52, 24, 32, 52, 32,
                    32, 32, 52, 24, 32, 52, 32,
                    36, 36, 56, 28, 32, 52, 24,
                    40, 40, 60, 32, 32, 52, 24};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 0},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2},

                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3},

                    {1, 1, 0, 0, 0, 0, 4}, {1, 0, 0, 1, 0, 0, 4},
                    {1, 1, 1, 1, 0, 0, 4}, {0, 0, 1, 1, 0, 0, 4}, {0, 0, 1, 0, 0, 1, 4},
                    {0, 0, 1, 1, 1, 1, 4}, {0, 0, 0, 0, 1, 1, 4},

                    {1, 1, 0, 0, 0, 0, 5}, {1, 0, 0, 1, 0, 0, 5},
                    {1, 1, 1, 1, 0, 0, 5}, {0, 0, 1, 1, 0, 0, 5}, {0, 0, 1, 0, 0, 1, 5},
                    {0, 0, 1, 1, 1, 1, 5}, {0, 0, 0, 0, 1, 1, 5},

                    {1, 1, 0, 0, 0, 0, 6}, {1, 0, 0, 1, 0, 0, 6},
                    {1, 1, 1, 1, 0, 0, 6}, {0, 0, 1, 1, 0, 0, 6}, {0, 0, 1, 0, 0, 1, 6},
                    {0, 0, 1, 1, 1, 1, 6}, {0, 0, 0, 0, 1, 1, 6}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//            System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 0) {
            int[] LT = {22, 28, 34, 48, 56, 56, 56, 22, 28, 48, 22, 76};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
//                double[] YVals = cplex.getValues(Y);
//                System.out.println("YVals:");
//                for (int route = 0; route < numRoute; route++) {
//                    System.out.println(YVals);
//                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else {
            throw new Exception("Wrong depot value");
        }
        return new Result(Type.CPLEX_SWP_DUP_LLB, resultBatchList, executeTime, totalLength);
    }

    public static Result CPLEX_PTS_DUP_LLB(int depot, int numBatch) throws Exception {
//        int numRoute = 12;
//        int numBatch = 4;

        long executeTime = 0;
        double totalLength = 0;
        IloCplex cplex = new IloCplex();
        cplex.setParam(IloCplex.DoubleParam.TiLim, cplexTimeLimit);
        cplex.setParam(IloCplex.IntParam.Cliques, -1);
        cplex.setParam(IloCplex.IntParam.Covers, -1);
        cplex.setParam(IloCplex.IntParam.FlowCovers, -1);
        cplex.setParam(IloCplex.IntParam.ImplBd, -1);
        cplex.setParam(IloCplex.IntParam.GUBCovers, -1);
        cplex.setParam(IloCplex.IntParam.FracCuts, -1);  // gomory cut
        cplex.setParam(IloCplex.IntParam.FlowPaths, -1);
        cplex.setParam(IloCplex.IntParam.MIRCuts, -1);
        cplex.setParam(IloCplex.IntParam.DisjCuts, -1);
        cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, -1);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, 2);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Gomory, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Disjunctive, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Implied, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.LiftProj, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MCFCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MIRCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.PathCut, -1);
        cplex.setParam(IloCplex.BooleanParam.PreInd, false);
        List<Batch> resultBatchList = new ArrayList<Batch>();


        if (depot == 1) {
            int numRoute = 12;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();


            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 2) {
            int numRoute = 17;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    32, 24, 32, 52, 32};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 0}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 0}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}};
            long startTime = System.currentTimeMillis();


            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 3) {
//            int numDepot = 3;
            int numRoute = 26;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    30, 32, 52, 24, 32, 52, 30,
                    40, 40, 60, 32, 32, 52, 24};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 0},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2},

                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}};


            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }

            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 6) {
//            int numDepot = 6;
            int numRoute = 47;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80,
                    24, 32, 52, 28, 36, 56, 36,
                    28, 32, 52, 24, 32, 52, 32,
                    32, 32, 52, 24, 32, 52, 32,
                    36, 36, 56, 28, 32, 52, 24,
                    40, 40, 60, 32, 32, 52, 24};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 0},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 0}, {1, 1, 0, 0, 1, 1, 0},
                    {1, 0, 0, 1, 1, 1, 0}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 0},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2},

                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3},

                    {1, 1, 0, 0, 0, 0, 4}, {1, 0, 0, 1, 0, 0, 4},
                    {1, 1, 1, 1, 0, 0, 4}, {0, 0, 1, 1, 0, 0, 4}, {0, 0, 1, 0, 0, 1, 4},
                    {0, 0, 1, 1, 1, 1, 4}, {0, 0, 0, 0, 1, 1, 4},

                    {1, 1, 0, 0, 0, 0, 5}, {1, 0, 0, 1, 0, 0, 5},
                    {1, 1, 1, 1, 0, 0, 5}, {0, 0, 1, 1, 0, 0, 5}, {0, 0, 1, 0, 0, 1, 5},
                    {0, 0, 1, 1, 1, 1, 5}, {0, 0, 0, 0, 1, 1, 5},

                    {1, 1, 0, 0, 0, 0, 6}, {1, 0, 0, 1, 0, 0, 6},
                    {1, 1, 1, 1, 0, 0, 6}, {0, 0, 1, 1, 0, 0, 6}, {0, 0, 1, 0, 0, 1, 6},
                    {0, 0, 1, 1, 1, 1, 6}, {0, 0, 0, 0, 1, 1, 6}};


            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 0) {
            int[] LT = {22, 28, 34, 48, 56, 56, 56, 22, 28, 48, 22, 76};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else {
            throw new Exception("Wrong depot value");
        }

        return new Result(Type.CPLEX_PTS_DUP_LLB, resultBatchList, executeTime, totalLength);
    }

    public static Result CPLEX_SWP_LLB(int depot, int numBatch) throws Exception {
//        int numRoute = 12;
//        int numBatch = 4;

        int CapaSWP = 12;
        long executeTime = 0;
        double totalLength = 0;
        IloCplex cplex = new IloCplex();
        cplex.setParam(IloCplex.DoubleParam.TiLim, cplexTimeLimit);
        cplex.setParam(IloCplex.IntParam.Cliques, -1);
        cplex.setParam(IloCplex.IntParam.Covers, -1);
        cplex.setParam(IloCplex.IntParam.FlowCovers, -1);
        cplex.setParam(IloCplex.IntParam.ImplBd, -1);
        cplex.setParam(IloCplex.IntParam.GUBCovers, -1);
        cplex.setParam(IloCplex.IntParam.FracCuts, -1);  // gomory cut
        cplex.setParam(IloCplex.IntParam.FlowPaths, -1);
        cplex.setParam(IloCplex.IntParam.MIRCuts, -1);
        cplex.setParam(IloCplex.IntParam.DisjCuts, -1);
        cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, -1);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, 2);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Gomory, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Disjunctive, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Implied, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.LiftProj, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MCFCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MIRCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.PathCut, -1);
        cplex.setParam(IloCplex.BooleanParam.PreInd, false);
        List<Batch> resultBatchList = new ArrayList<Batch>();


        if (depot == 1) {
            int numRoute = 12;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct5
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//            System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 2) {
//            int numDepot = 2;
            int numRoute = 24;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    32, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2}};
            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 3) {
//            int numDepot = 3;
            int numRoute = 36;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 24, 32, 52, 24, 80,
                    30, 32, 40, 52, 60, 60, 60, 24, 32, 52, 30, 80,
                    40, 40, 40, 60, 60, 60, 60, 32, 32, 52, 24, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2},
                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3}, {1, 0, 0, 0, 0, 1, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {1, 1, 1, 0, 0, 1, 3}, {1, 1, 0, 0, 1, 1, 3},
                    {1, 0, 0, 1, 1, 1, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}, {1, 1, 1, 1, 1, 1, 3}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 6) {
//            int numDepot = 6;
            int numRoute = 72;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 28, 32, 52, 24, 80,
                    24, 32, 40, 52, 60, 60, 60, 28, 36, 56, 36, 80,
                    28, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    32, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    36, 36, 40, 56, 60, 60, 60, 28, 32, 52, 24, 80,
                    40, 40, 40, 60, 60, 60, 60, 32, 32, 52, 24, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2},
                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3}, {1, 0, 0, 0, 0, 1, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {1, 1, 1, 0, 0, 1, 3}, {1, 1, 0, 0, 1, 1, 3},
                    {1, 0, 0, 1, 1, 1, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}, {1, 1, 1, 1, 1, 1, 3},

                    {1, 1, 0, 0, 0, 0, 4}, {1, 0, 0, 1, 0, 0, 4}, {1, 0, 0, 0, 0, 1, 4},
                    {1, 1, 1, 1, 0, 0, 4}, {1, 1, 1, 0, 0, 1, 4}, {1, 1, 0, 0, 1, 1, 4},
                    {1, 0, 0, 1, 1, 1, 4}, {0, 0, 1, 1, 0, 0, 4}, {0, 0, 1, 0, 0, 1, 4},
                    {0, 0, 1, 1, 1, 1, 4}, {0, 0, 0, 0, 1, 1, 4}, {1, 1, 1, 1, 1, 1, 4},

                    {1, 1, 0, 0, 0, 0, 5}, {1, 0, 0, 1, 0, 0, 5}, {1, 0, 0, 0, 0, 1, 5},
                    {1, 1, 1, 1, 0, 0, 5}, {1, 1, 1, 0, 0, 1, 5}, {1, 1, 0, 0, 1, 1, 5},
                    {1, 0, 0, 1, 1, 1, 5}, {0, 0, 1, 1, 0, 0, 5}, {0, 0, 1, 0, 0, 1, 5},
                    {0, 0, 1, 1, 1, 1, 5}, {0, 0, 0, 0, 1, 1, 5}, {1, 1, 1, 1, 1, 1, 5},

                    {1, 1, 0, 0, 0, 0, 6}, {1, 0, 0, 1, 0, 0, 6}, {1, 0, 0, 0, 0, 1, 6},
                    {1, 1, 1, 1, 0, 0, 6}, {1, 1, 1, 0, 0, 1, 6}, {1, 1, 0, 0, 1, 1, 6},
                    {1, 0, 0, 1, 1, 1, 6}, {0, 0, 1, 1, 0, 0, 6}, {0, 0, 1, 0, 0, 1, 6},
                    {0, 0, 1, 1, 1, 1, 6}, {0, 0, 0, 0, 1, 1, 6}, {1, 1, 1, 1, 1, 1, 6}};
            long startTime = System.currentTimeMillis();

            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }

                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 0) {
//            int numRoute = 12;
//            int[] LT = {22, 28, 34, 48, 56, 56, 56, 22, 28, 48, 22, 76};
//            int[][] Rrda = {{1, 1, 0, 0, 0, 0}, {1, 0, 0, 1, 0, 0}, {1, 0, 0, 0, 0, 1},
//                    {1, 1, 1, 1, 0, 0}, {1, 1, 1, 0, 0, 1}, {1, 1, 0, 0, 1, 1},
//                    {1, 0, 0, 1, 1, 1}, {0, 0, 1, 1, 0, 0}, {0, 0, 1, 0, 0, 1},
//                    {0, 0, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1}, {1, 1, 1, 1, 1, 1}};
//
//            long startTime = System.currentTimeMillis();
//
//            // lower bound
////            List<Path> lowerBoundPathList = new ArrayList<Path>();
////            for (Order order : orderList) {
////                for (Path path : order.getAcceptedPathList()) {
////                    if (lowerBoundPathList.contains(path) == false) {
////                        lowerBoundPathList.add(path);
////                    }
////                }
////            }
////
////            int LT[] = new int[lowerBoundPathList.size()];
////            int Rrda[][] = new int[lowerBoundPathList.size()][];
////            int i = 0;
////            for (Path path : lowerBoundPathList) {
////                LT[i] = path.getLength();
////                Rrda[i] = path.getPosition();
////                i ++;
////            }
//
//
//            IloNumVar[][] X = new IloNumVar[numOrder][];
//            for (int order = 0; order < numOrder; order++) {
//                X[order] = cplex.intVarArray(numRoute, 0, 1);
//            }
//
//            IloNumVar[][] _X = new IloNumVar[numRoute][];
//            for (int route = 0; route < numRoute; route++) {
//                _X[route] = cplex.intVarArray(numOrder, 0, 1);
//            }
//
//            IloNumVar[] Y = cplex.intVarArray(numRoute, 0, Integer.MAX_VALUE);
//
//
//
//            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
//            for (int route = 0; route < numBatch; route++) {
//                ct3[route] = cplex.numVarArray(numOrder,0.0,Double.MAX_VALUE);
//            }
//
//
//
//
//            // suplementary data
//
//
//// Obj
//            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
//            for (int route = 0; route < numRoute; route++) {
//                    TotalObj.addTerm(LT[route], Y[route]);
//                }
//
//
//
//
//            cplex.addMinimize(TotalObj);
//// Constraints
//            // X supplementary
//            for (int order = 0; order < numOrder; order++) {
//                for (int route = 0; route < numRoute; route++) {
//                    cplex.addEq(X[order][route],_X[route][order]);
//                }
//            }
//
//
//            // ct2:
//            for (int order = 0; order < numOrder; order++) {
//                cplex.addEq(cplex.sum(X[order]), 1);
//            }
//            // ct3:
//            for (int batch = 0; batch < numBatch; batch++) {
//                for (int route = 0; route < numRoute; route++) {
//                    cplex.addLe(cplex.sum(_X[batch]), cplex.prod(CapaSWP,Y[route]));
//                }
//            }
//
//            // ct4:
//
//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
//            //ct6
////            for (int order = 0; order < numOrder; order++) {
////                for (int route = 0; route < numRoute; route++) {
////                    if (check(Rrda[route], Vrda[order]) == false) {
////                                cplex.addEq(X[order][route], 0);
////                    }
////                    else {
////                        cplex.addEq(X[order][route], 1);
////                    }
////                }
////            }
//
//
//
//
//            if (cplex.solve()) {
//                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
//                    System.out.println("No Solution");
//                    return null;
//                }
            int numRoute = 12;
            int[] LT = {22, 28, 34, 48, 56, 56, 56, 22, 28, 48, 22, 76};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();

            // lower bound
//            List<Path> lowerBoundPathList = new ArrayList<Path>();
//            for (Order order : orderList) {
//                for (Path path : order.getAcceptedPathList()) {
//                    if (lowerBoundPathList.contains(path) == false) {
//                        lowerBoundPathList.add(path);
//                    }
//                }
//            }
//
//            int LT[] = new int[lowerBoundPathList.size()];
//            int Rrda[][] = new int[lowerBoundPathList.size()][];
//            int i = 0;
//            for (Path path : lowerBoundPathList) {
//                LT[i] = path.getLength();
//                Rrda[i] = path.getPosition();
//                i ++;
//            }


            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(_X[route]), cplex.prod(CapaSWP, Y[route]));
            }


            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//            System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numRoute];
                for (int order = 0; order < numOrder; order++) {
                    for (int route = 0; route < numRoute; route++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
//                double[] YVals = cplex.getValues(Y);
//                System.out.println("YVals:");
//                for (int route = 0; route < numRoute; route++) {
//                    System.out.println(YVals);
//                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else {
            throw new Exception("Wrong depot value");
        }
        return new Result(Type.CPLEX_SWP_LLB, resultBatchList, executeTime, totalLength);
    }


    public static Result CPLEX_PTS_LLB(int depot, int numBatch) throws Exception {
//        int numRoute = 12;
//        int numBatch = 4;

        long executeTime = 0;
        double totalLength = 0;
        IloCplex cplex = new IloCplex();
        cplex.setParam(IloCplex.DoubleParam.TiLim, cplexTimeLimit);
        cplex.setParam(IloCplex.IntParam.Cliques, -1);
        cplex.setParam(IloCplex.IntParam.Covers, -1);
        cplex.setParam(IloCplex.IntParam.FlowCovers, -1);
        cplex.setParam(IloCplex.IntParam.ImplBd, -1);
        cplex.setParam(IloCplex.IntParam.GUBCovers, -1);
        cplex.setParam(IloCplex.IntParam.FracCuts, -1);  // gomory cut
        cplex.setParam(IloCplex.IntParam.FlowPaths, -1);
        cplex.setParam(IloCplex.IntParam.MIRCuts, -1);
        cplex.setParam(IloCplex.IntParam.DisjCuts, -1);
        cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, -1);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, 2);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Gomory, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Disjunctive, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.Implied, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.LiftProj, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MCFCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.MIRCut, -1);
//        cplex.setParam(IloCplex.IntParam.MIP.Cuts.PathCut, -1);
        cplex.setParam(IloCplex.BooleanParam.PreInd, false);
        List<Batch> resultBatchList = new ArrayList<Batch>();


        if (depot == 1) {
            int numRoute = 12;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 32, 40, 60, 40, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

//
            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 2) {
            //            int numDepot = 2;
            int numRoute = 24;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    32, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 3) {
//            int numDepot = 3;
            int numRoute = 36;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 24, 32, 52, 24, 80,
                    30, 32, 40, 52, 60, 60, 60, 24, 32, 52, 30, 80,
                    40, 40, 40, 60, 60, 60, 60, 32, 32, 52, 24, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2},
                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3}, {1, 0, 0, 0, 0, 1, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {1, 1, 1, 0, 0, 1, 3}, {1, 1, 0, 0, 1, 1, 3},
                    {1, 0, 0, 1, 1, 1, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}, {1, 1, 1, 1, 1, 1, 3}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }
            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 6) {
//            int numDepot = 6;
            int numRoute = 72;
            int[] LT = {24, 32, 40, 52, 60, 60, 60, 28, 32, 52, 24, 80,
                    24, 32, 40, 52, 60, 60, 60, 28, 36, 56, 36, 80,
                    28, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    32, 32, 40, 52, 60, 60, 60, 24, 32, 52, 32, 80,
                    36, 36, 40, 56, 60, 60, 60, 28, 32, 52, 24, 80,
                    40, 40, 40, 60, 60, 60, 60, 32, 32, 52, 24, 80};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1},

                    {1, 1, 0, 0, 0, 0, 2}, {1, 0, 0, 1, 0, 0, 2}, {1, 0, 0, 0, 0, 1, 2},
                    {1, 1, 1, 1, 0, 0, 2}, {1, 1, 1, 0, 0, 1, 2}, {1, 1, 0, 0, 1, 1, 2},
                    {1, 0, 0, 1, 1, 1, 2}, {0, 0, 1, 1, 0, 0, 2}, {0, 0, 1, 0, 0, 1, 2},
                    {0, 0, 1, 1, 1, 1, 2}, {0, 0, 0, 0, 1, 1, 2}, {1, 1, 1, 1, 1, 1, 2},
                    {1, 1, 0, 0, 0, 0, 3}, {1, 0, 0, 1, 0, 0, 3}, {1, 0, 0, 0, 0, 1, 3},
                    {1, 1, 1, 1, 0, 0, 3}, {1, 1, 1, 0, 0, 1, 3}, {1, 1, 0, 0, 1, 1, 3},
                    {1, 0, 0, 1, 1, 1, 3}, {0, 0, 1, 1, 0, 0, 3}, {0, 0, 1, 0, 0, 1, 3},
                    {0, 0, 1, 1, 1, 1, 3}, {0, 0, 0, 0, 1, 1, 3}, {1, 1, 1, 1, 1, 1, 3},

                    {1, 1, 0, 0, 0, 0, 4}, {1, 0, 0, 1, 0, 0, 4}, {1, 0, 0, 0, 0, 1, 4},
                    {1, 1, 1, 1, 0, 0, 4}, {1, 1, 1, 0, 0, 1, 4}, {1, 1, 0, 0, 1, 1, 4},
                    {1, 0, 0, 1, 1, 1, 4}, {0, 0, 1, 1, 0, 0, 4}, {0, 0, 1, 0, 0, 1, 4},
                    {0, 0, 1, 1, 1, 1, 4}, {0, 0, 0, 0, 1, 1, 4}, {1, 1, 1, 1, 1, 1, 4},

                    {1, 1, 0, 0, 0, 0, 5}, {1, 0, 0, 1, 0, 0, 5}, {1, 0, 0, 0, 0, 1, 5},
                    {1, 1, 1, 1, 0, 0, 5}, {1, 1, 1, 0, 0, 1, 5}, {1, 1, 0, 0, 1, 1, 5},
                    {1, 0, 0, 1, 1, 1, 5}, {0, 0, 1, 1, 0, 0, 5}, {0, 0, 1, 0, 0, 1, 5},
                    {0, 0, 1, 1, 1, 1, 5}, {0, 0, 0, 0, 1, 1, 5}, {1, 1, 1, 1, 1, 1, 5},

                    {1, 1, 0, 0, 0, 0, 6}, {1, 0, 0, 1, 0, 0, 6}, {1, 0, 0, 0, 0, 1, 6},
                    {1, 1, 1, 1, 0, 0, 6}, {1, 1, 1, 0, 0, 1, 6}, {1, 1, 0, 0, 1, 1, 6},
                    {1, 0, 0, 1, 1, 1, 6}, {0, 0, 1, 1, 0, 0, 6}, {0, 0, 1, 0, 0, 1, 6},
                    {0, 0, 1, 1, 1, 1, 6}, {0, 0, 0, 0, 1, 1, 6}, {1, 1, 1, 1, 1, 1, 6}};


            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }

            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else if (depot == 0) {
            int[] LT = {22, 28, 34, 48, 56, 56, 56, 22, 28, 48, 22, 76};
            int[][] Rrda = {{1, 1, 0, 0, 0, 0, 1}, {1, 0, 0, 1, 0, 0, 1}, {1, 0, 0, 0, 0, 1, 1},
                    {1, 1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 1, 1}, {1, 1, 0, 0, 1, 1, 1},
                    {1, 0, 0, 1, 1, 1, 1}, {0, 0, 1, 1, 0, 0, 1}, {0, 0, 1, 0, 0, 1, 1},
                    {0, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

            long startTime = System.currentTimeMillis();
            IloNumVar[][] X = new IloNumVar[numOrder][];
            for (int order = 0; order < numOrder; order++) {
                X[order] = cplex.numVarArray(numRoute, 0, 1);
            }

            IloNumVar[][] _X = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                _X[route] = cplex.numVarArray(numOrder, 0, 1);
            }

            IloNumVar[] Y = cplex.numVarArray(numRoute, 0, Double.MAX_VALUE);


            IloNumVar[][] ct3 = new IloNumVar[numRoute][];
            for (int route = 0; route < numRoute; route++) {
                ct3[route] = cplex.numVarArray(numOrder, 0.0, Double.MAX_VALUE);
            }


            // suplementary data


// Obj
            IloLinearNumExpr TotalObj = cplex.linearNumExpr();
            for (int route = 0; route < numRoute; route++) {
                TotalObj.addTerm(LT[route], Y[route]);
            }


            cplex.addMinimize(TotalObj);
// Constraints
            // X supplementary
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(X[order][route], _X[route][order]);
                }
            }


            // ct2:
            for (int order = 0; order < numOrder; order++) {
                cplex.addEq(cplex.sum(X[order]), 1);
            }
            // ct3:
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    cplex.addEq(cplex.prod(capa[order], X[order][route]), ct3[route][order]);
                }
            }
            for (int route = 0; route < numRoute; route++) {
                cplex.addLe(cplex.sum(ct3[route]), cplex.prod(maxCapa, Y[route]));
            }

            // ct4:

//            for (int aisle = 0; aisle < numAisle; aisle++) {
//                for (int route = 0; route < numRoute; route++) {
//                    for (int order = 0; order < numOrder; order++) {
//                        cplex.addLe(
//                                cplex.prod(_X[route][order], Vrda[order][aisle]),
//                                cplex.prod(Y[route], Rrda[route][aisle]));
//                    }
//                }
//            }
            //ct6
            for (int order = 0; order < numOrder; order++) {
                for (int route = 0; route < numRoute; route++) {
                    if (check(Rrda[route], Vrda[order]) == false) {
                        cplex.addEq(X[order][route], 0);
                    } else {
                        //cplex.addEq(X[order][route], 1);
                    }
                }
            }


            if (cplex.solve()) {
                if (cplex.getStatus().equals(IloCplex.Status.Infeasible)) {
                    System.out.println("No Solution");
                    return null;
                }

//                System.out.println("Objective:" + cplex.getObjValue());
//                System.out.println("XVals:");
                double[][] XVals = new double[numOrder][numBatch];
                for (int order = 0; order < numOrder; order++) {
                    for (int batch = 0; batch < numBatch; batch++) {
                        XVals[order] = cplex.getValues(X[order]);
                    }
                }
                long endTime = System.currentTimeMillis();
                executeTime = endTime - startTime;

                totalLength = cplex.getObjValue();
                resultBatchList = getBatchListFormCplexResult(XVals);
            }
        } else {
            throw new Exception("Wrong depot value");
        }

        return new Result(Type.CPLEX_PTS_LLB, resultBatchList, executeTime, totalLength);
    }

*/
    private static List<Batch> getBatchListFormCplexResult(double[][] XVals) {
        List<Batch> result = new ArrayList<Batch>();
        // add 10 empty
        for (int i = 0; i < XVals[0].length; i++) {
            result.add(new Batch());
        }

        // add order
        for (int i = 0; i < XVals.length; i++) {
            for (int j = 0; j < XVals[0].length; j++) {
                if (XVals[i][j] > 0.5) {
                    result.get(j).addOrder(orderList.get(i));
                }
            }
        }
        return result;
    }

    // Read orders, add all accepted paths for each order
    public static void getPathsAndOrders(FileReaderHelper fileReaderHelper, int depot) throws Exception {
        int[] pathLengthList = fileReaderHelper.readLengthPathListFormFile(depot);
        List<int[]> pathPositionList = fileReaderHelper.readPathPositionListFormFile(depot);
        pathList.clear();

//        int[] capaList = fileReaderHelper.readCapaFromFile();

        // get paths
        if (depot == 0) {
            for (int i = 0; i < pathPositionList.size(); i++) {
                pathList.add(new Path(0, pathPositionList.get(i), pathLengthList[i]));
            }
        } else {
            int depotNum = 1;
            for (int i = 0; i < pathPositionList.size(); i++) {
                pathList.add(new Path(depotNum, pathPositionList.get(i), pathLengthList[i]));
                if ((i + 1) % numRoute == 0) {
                    depotNum++;
                }
            }
        }

        // get orders
//        List<int[]> orderPathList = fileReaderHelper.readOrderPathListFromFile();
//        List<int[]> oofList = fileReaderHelper.readOofListFromFile();
//        Order newOrder;
//        for (int i = 0; i < orderPathList.size(); i++) {
//            newOrder = new Order(i, orderPathList.get(i), oofList.get(i), capaList[i]);
////            newOrder = new Order(i, orderPathList.get(i), capaList[i]);
//            orderList.add(newOrder);
//        }

        // Set acceptedPathList for orders
        for (Order order : orderList) {
            List<Path> acceptedPathList = new ArrayList<Path>();
            for (Path path : pathList) {
                boolean isAccepted = true;
                for (int i = 0; i < numAisle; i++) {
                    if (order.getPosition()[i] > path.getPosition()[i]) {
                        isAccepted = false;
                        break;
                    }
                }
                if (isAccepted) {
                    acceptedPathList.add(path);
                }
            }
            order.setAcceptedPathList(acceptedPathList);
        }
    }

    public static void exportToExcel(List<Result> resultList, File file) throws Exception {
        //Create or update file
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet resultSheet;

        Row row;
        if (file.exists()) {
            FileInputStream inputStream = new FileInputStream(file);
            workbook = new HSSFWorkbook(inputStream);
            resultSheet = workbook.getSheet("Result");
        } else {
            resultSheet = workbook.createSheet("Result");

            Row detailRow = resultSheet.createRow(0);
            detailRow.createCell(0).setCellValue("Time");
            detailRow.createCell(1).setCellValue("cw_swp");
            detailRow.createCell(2).setCellValue("Time");
            detailRow.createCell(3).setCellValue("cw_pts");
            detailRow.createCell(4).setCellValue("Time");
            detailRow.createCell(5).setCellValue("fifo_swp");
            detailRow.createCell(6).setCellValue("Time");
            detailRow.createCell(7).setCellValue("fifo_pts");
            detailRow.createCell(8).setCellValue("Time");
            detailRow.createCell(9).setCellValue("seed_swp");
            detailRow.createCell(10).setCellValue("Time");
            detailRow.createCell(11).setCellValue("seed_pts");
            detailRow.createCell(12).setCellValue("Time");
            detailRow.createCell(13).setCellValue("Hybrid_swp");
            detailRow.createCell(14).setCellValue("Time");
            detailRow.createCell(15).setCellValue("Hybrid_pts");
            detailRow.createCell(16).setCellValue("Time");
            detailRow.createCell(17).setCellValue("dup_swp");
            detailRow.createCell(18).setCellValue("Time");
            detailRow.createCell(19).setCellValue("dup_pts");
            detailRow.createCell(20).setCellValue("Time");
            detailRow.createCell(21).setCellValue("cplex_swp");
            detailRow.createCell(22).setCellValue("Time");
            detailRow.createCell(23).setCellValue("cplex_pts");

            detailRow.createCell(24).setCellValue("Time");
            detailRow.createCell(25).setCellValue("dup_swp_lb");
            detailRow.createCell(26).setCellValue("Time");
            detailRow.createCell(27).setCellValue("dup_pts_lb");
            detailRow.createCell(28).setCellValue("Time");
            detailRow.createCell(29).setCellValue("cplex_swp_lb");
            detailRow.createCell(30).setCellValue("Time");
            detailRow.createCell(31).setCellValue("cplex_pts_lb");

            detailRow.createCell(32).setCellValue("Time");
            detailRow.createCell(33).setCellValue("dup_swp_llb");
            detailRow.createCell(34).setCellValue("Time");
            detailRow.createCell(35).setCellValue("dup_pts_llb");
            detailRow.createCell(36).setCellValue("Time");
            detailRow.createCell(37).setCellValue("cplex_swp_llb");
            detailRow.createCell(38).setCellValue("Time");
            detailRow.createCell(39).setCellValue("cplex_pts_llb");
        }

        // Calculate and export
        // Export Result
        row = resultSheet.createRow(resultSheet.getLastRowNum() + 1);
        int cellNum = 0;
        for (Result result : resultList) {
            row.createCell(cellNum++).setCellValue(result.getExecuteTime());
            row.createCell(cellNum++).setCellValue(result.getTotalLength(pathList));
        }

        // Export position 80/60
        HSSFSheet newSheet;
        for (Result result : resultList) {
            if (file.exists()) {
                newSheet = workbook.getSheet(result.getType().toString());
            } else {
                newSheet = workbook.createSheet(result.getType().toString());
            }
//            for (Batch batch : result.getBatchListResult()) {
//                int[] position = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//                for (Order order : batch.getOrderList()) {
//                    for (int i = 0; i < 60; i++) {
//                        position[i] += order.getPosition80()[i];
//                    }
//                }
//
//                // Export to excel
//                cellNum = 0;
//                row = newSheet.createRow(newSheet.getLastRowNum()+1);
//                row.createCell(cellNum++).setCellValue(batch.getPath().getDepot());
//                for (int i = 0; i < 60; i++) {
//                    Cell cell = row.createCell(cellNum++);
//                    cell.setCellValue(position[i]);
//                }
//            }
        }

        // Write to excel
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.close();
            System.out.println("Excel written successfully..");

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println();
    }

    public static boolean check(int path[], int order[]) {
        for (int i = 0; i < order.length; i++) {
            if (path[i] - order[i] < 0) {
                return false;
            }
        }
        return true;
    }
}
