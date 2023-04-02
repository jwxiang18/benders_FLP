import gurobi.*;

public class FLP_callback extends GRBCallback {
    private int iter;
    private int nwarehouse;
    private int nstore;
    private double[] supply;
    private double[] demand;
    private GRBConstr[] csupply;
    private GRBConstr[] cdemand;
    private GRBVar[] vmbuild;
    private GRBVar maxShipCost;
    private GRBModel sub;
    public FLP_callback(int iter , int nwarehouse , int nstore , double[] supply ,
                        double[] demand , GRBConstr[] csupply , GRBConstr[] cdemand,
                        GRBVar[] vmbuild , GRBVar maxShipCost , GRBModel sub){
        this.iter = iter;
        this.nwarehouse = nwarehouse;
        this.nstore = nstore;
        this.supply = supply;
        this.demand = demand;
        this.cdemand = cdemand;
        this.csupply = csupply;
        this.vmbuild = vmbuild;
        this.maxShipCost = maxShipCost;
        this.sub = sub;
    }

    protected void callback(){
        try {
            if(where == GRB.Callback.MIPSOL){
                if(iter >= 1){
                    for (int i = 0; i < nwarehouse; i++) {
                        csupply[i].set(GRB.DoubleAttr.RHS , getSolution(vmbuild[i]) * supply[i]);
                    }
                }
                sub.optimize();

                if(sub.get(GRB.IntAttr.Status) == GRB.Status.INFEASIBLE){
                    System.out.println("Iteration : " + iter);
                    System.out.println("Adding feasibility cut");
                    System.out.println();

                    GRBLinExpr exp = new GRBLinExpr();
                    for (int i = 0; i < nwarehouse; i++) {
                        exp.addTerm(csupply[i].get(GRB.DoubleAttr.FarkasDual)*supply[i] , vmbuild[i]);
                    }
                    double sum = 0;
                    for (int i = 0; i < nstore; i++) {
                        sum += cdemand[i].get(GRB.DoubleAttr.FarkasDual)*demand[i];
                    }
                    addLazy(exp , GRB.LESS_EQUAL , -1*sum);
                    iter++;
                } else if (sub.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL){
                    System.out.println("Iteration : " + iter);
                    System.out.println("Adding optimality cut");
                    System.out.println();

                    GRBLinExpr exp = new GRBLinExpr();
                    for (int i = 0; i < nwarehouse; i++) {
                        exp.addTerm(-1*csupply[i].get(GRB.DoubleAttr.Pi)*supply[i] , vmbuild[i]);
                    }
                    exp.addTerm(1, maxShipCost);
                    double sum = 0;
                    for (int i = 0; i < nstore; i++) {
                        sum += cdemand[i].get(GRB.DoubleAttr.Pi)*demand[i];
                    }
                    addLazy(exp , GRB.GREATER_EQUAL , sum );
                    iter++;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
