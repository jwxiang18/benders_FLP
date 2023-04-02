import gurobi.GRBException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, GRBException {
        Instance instance = readData.readData("warehouse.dat");
        Benders bd = new Benders(instance);
        bd.main();
    }
}