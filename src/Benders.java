
import gurobi.*;

import java.util.List;


public class Benders {
    int nwareHouse , nstore;
    Instance instance;
    GRBModel master , sub;
    GRBVar[] vmbuild;
    GRBVar[][] vship;
    GRBVar maxShipCost;
    GRBConstr[] csupply;
    GRBConstr[] cdemand;

    public Benders(Instance instance){
        this.instance = instance;
        nwareHouse = instance.nwareHouse;
        nstore = instance.nstore;
    }
    public void main() throws GRBException {
        init();
        buildMp();
        buildSup();
        master.setCallback(new FLP_callback(0,nwareHouse,nstore,instance.supply,instance.demand,csupply,cdemand,vmbuild,maxShipCost,sub));
        System.out.println("         *** Benders Decomposition ***");
        master.optimize();
        System.out.println("         *** end ***");

        for (int i = 0; i < nwareHouse; i++) {
            csupply[i].set(GRB.DoubleAttr.RHS , vmbuild[i].get(GRB.DoubleAttr.X) * instance.supply[i]);
        }
        sub.optimize();
        report();
    }

    public void report() throws GRBException {
        System.out.println("         *** Report ***");
        System.out.println("         *** Master ***");
        System.out.println("         *** Objective : " + master.get(GRB.DoubleAttr.ObjVal));
        for (int i = 0; i < nwareHouse; i++) {
            if(vmbuild[i].get(GRB.DoubleAttr.X) > 1e-6)
                System.out.println("         *** Warehouse " + i + " is built");
        }
        System.out.println("         maxShipCost : " + maxShipCost.get(GRB.DoubleAttr.X));
        System.out.println("         *** Sub ***");
        System.out.println("         *** Objective : " + sub.get(GRB.DoubleAttr.ObjVal));
        for (int i = 0; i < nstore; i++) {
            for (int j = 0; j < nwareHouse; j++) {
                if(vship[j][i].get(GRB.DoubleAttr.X) > 1e-6)
                    System.out.println("         *** Warehouse " + j + " ship to store " + i + " : " + vship[j][i].get(GRB.DoubleAttr.X));
            }
        }
        System.out.println("         *** end ***");
    }

    private void buildMp() throws GRBException {
        vmbuild = new GRBVar[nwareHouse];
        for (int i = 0; i < nwareHouse; i++) {
            vmbuild[i] = master.addVar(0,1,0,GRB.BINARY,"y"+i);
        }
        maxShipCost = master.addVar(0,GRB.INFINITY , 0, GRB.CONTINUOUS , "q");
        GRBLinExpr expr = new GRBLinExpr();
        for (int i = 0; i < nwareHouse; i++) {
            expr.addTerm(instance.fixCost[i] , vmbuild[i]);
        }
        expr.addTerm(1,maxShipCost);
        master.setObjective(expr , GRB.MINIMIZE);

    }

    private void buildSup() throws GRBException {
        vship = new GRBVar[nwareHouse][nstore];
        for (int i = 0; i < nwareHouse; i++) {
            for (int j = 0; j < nstore; j++) {
                vship[i][j] = sub.addVar(0,GRB.INFINITY , 0, GRB.CONTINUOUS,"x"+i+"_"+j);
            }
        }
        csupply = new GRBConstr[nwareHouse];
        cdemand = new GRBConstr[nstore];
        for (int i = 0; i < nwareHouse; i++) {
            GRBLinExpr expr1 = new GRBLinExpr();
            for (int j = 0; j < nstore; j++) {
                expr1.addTerm(1,vship[i][j]);
            }
            csupply[i] = sub.addConstr(expr1,GRB.LESS_EQUAL,instance.supply[i],"csupply"+i);
        }

        for (int i = 0; i < nstore; i++) {
            GRBLinExpr expr2 = new GRBLinExpr();
            for (int j = 0; j < nwareHouse; j++) {
                expr2.addTerm(1,vship[j][i]);
            }
            cdemand[i] = sub.addConstr(expr2, GRB.EQUAL,instance.demand[i] , "cdemand"+i);
        }

        GRBLinExpr expr = new GRBLinExpr();
        for (int i = 0; i < nwareHouse; i++) {
            for (int j = 0; j < nstore; j++) {
                expr.addTerm(instance.varCost[i][j] , vship[i][j]);
            }
        }
        sub.setObjective(expr , GRB.MINIMIZE);
    }
    private void init() throws GRBException {
        GRBEnv env = new GRBEnv();
        master = new GRBModel(env);
        sub = new GRBModel(env);
        master.set(GRB.IntParam.OutputFlag , 0);
        sub.set(GRB.IntParam.OutputFlag , 0);

        master.set(GRB.IntParam.LazyConstraints , 1);

        sub.set(GRB.IntParam.Presolve , 0);

        sub.set(GRB.IntParam.InfUnbdInfo , 1);

        sub.set(GRB.IntParam.Method , 1);
    }
}
