import ex0.Building;
import ex0.algo.*;
import ex0.simulator.Builging_A;
import ex0.simulator.Simulator_A;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class tester_algo {

        @Test
        void AlgoTester()
        {
            for (int i = 0; i < 10; i++) {
                int stage = i;  // any case in [0,9]
                System.out.println("Ex0 Simulator: isStarting, stage="+stage+") ... =  ");
                Simulator_A.initData(stage, null);  // init the simulator data: {building, calls}.
                ElevatorAlgo myAlgo = new MyElevAlgo(Simulator_A.getBuilding());
                Simulator_A.initAlgo(myAlgo); // init the algorithm to be used by the simulator
                Simulator_A.runSim(); // run the simulation
                long time = System.currentTimeMillis();
                String report_name = "out/Ex0_report_case_"+stage+"_"+time+"_ID_.log";
                Simulator_A.report(report_name); // print the algorithm results in the given case, and save the log to a file.
            }
        }
}
