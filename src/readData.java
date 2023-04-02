import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class readData {
    public static Instance readData(String filename) throws IOException {
        Instance instance = new Instance();
        try(BufferedReader reader = Files.newBufferedReader(Paths.get(filename))){
            instance.nwareHouse = Integer.parseInt(reader.readLine());
            instance.nstore = Integer.parseInt(reader.readLine());
            String[] line = reader.readLine().trim().split("\\s+");
            instance.supply = new double[instance.nwareHouse];
            for(int i = 0; i < instance.nwareHouse ; i++){
                instance.supply[i] = Double.parseDouble(line[i]);
            }
            line = reader.readLine().trim().split("\\s+");
            instance.demand = new double[instance.nstore];
            for (int i = 0; i < instance.nstore; i++) {
                instance.demand[i] = Double.parseDouble(line[i]);
            }
            line = reader.readLine().trim().split("\\s+");
            instance.fixCost = new double[instance.nwareHouse];
            for (int i = 0; i < instance.nwareHouse; i++) {
                instance.fixCost[i] = Double.parseDouble(line[i]);
            }
            instance.varCost = new double[instance.nwareHouse][instance.nstore];
            for (int i = 0; i < instance.nwareHouse; i++) {
                line = reader.readLine().trim().split("\\s+");
                for (int j = 0; j < instance.nstore; j++) {
                    instance.varCost[i][j] = Double.parseDouble(line[j]);
                }
            }
            return instance;
        }

    }
}
