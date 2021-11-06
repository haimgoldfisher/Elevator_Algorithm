package ex0.algo;

import ex0.Building;
import ex0.CallForElevator;
import ex0.Elevator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

public class MyElevAlgo implements ElevatorAlgo {
    private Building _building;
    private LinkedList<CallForElevator>[] calls_per_elev;

    public MyElevAlgo(Building b) { // constructor for a given building
        _building = b;
        int num_of_elev = b.numberOfElevetors();
        calls_per_elev = new LinkedList[num_of_elev]; // linked list of the elevators
        for (int i = 0; i < num_of_elev; i++) {
            calls_per_elev[i] = new LinkedList<CallForElevator>();
        } // every elevator has a list of calls
    }

    @Override
    public Building getBuilding() {
        return _building;
    }

    @Override
    public String algoName() {
        return "Haim & Or algo";
    }

    @Override
    /** this is the main function of our algo which uses inner function in order
     * to find the optimal elevator to answer to a given call for elevator.
     * @param - a call for an elevator.
     * @return the specific elevator's index to answer the call. */
    public int allocateAnElevator(CallForElevator call) {
        int n_elev = _building.numberOfElevetors();
        double min_time = Double.MAX_VALUE; // one of the calls must be lower than it
        int elev_index = 0; // generic
        for (int i = 0; i < n_elev; i++) {
            Elevator curr_elev = _building.getElevetor(i);
            double curr_time = calcTime2Src2Dest(curr_elev, call, i); // calculation of min time
            if (curr_time < min_time) {
                min_time = curr_time; // new min time
                elev_index = i; // new chosen elevator
            }
        }
        calls_per_elev[elev_index].add(call); // add the call to the chosen elevator's list
        return elev_index; // return the chosen elevator
    }

    /**
     * this function calculate for a given elevator the time it will take it to answer the new call considering its current.
     * in other words the function calculate the time will take this elevator to pass on the source floor if its on the way of the calls that already
     * allocated to this elevator and then the time will take it to pass on the destination floor. if the source floor is not
     * on the current rout it will calculate the time to finish the current rout and then will add the time to answer the new call from its position.
     * the function will also calculate the "regret time" that means that it will check how many calls will be delayed by allocating
     * this elevator to this call and will sum up the delay time of each call into 1 value. this value will be used as penalty to the answering time.
     * at the end it will sum up the rout time and the regret time and will return it.
     * the elevator that will return the lowest value will be allocated to the new call.
     *
     * @param : the elevator we would like to calculate its time, the call and its index in the elevs list
     * @return the global time this call will waste to the other current calls
     */
    private double calcTime2Src2Dest(Elevator e, CallForElevator c, int elev_index) {
        double time_per_floor = 1 / e.getSpeed();
        double time_of_first = 0;
        int num_calls = calls_per_elev[elev_index].size();
        if (num_calls == 0) {// if the elevator dont have any other calls it will simply calculate the time to get to the source and from the sorce to the destination
            int floors_to_src = Math.abs(c.getSrc() - e.getPos());
            double x = time_per_floor * floors_to_src;
            double time_to_src = e.getStartTime() + x + e.getStopTime() + e.getTimeForOpen() + e.getTimeForClose();
            int floors_to_dest = Math.abs(c.getDest() - c.getSrc());
            double y = time_per_floor * floors_to_dest;
            double src_to_dest = e.getStartTime() + y + e.getStopTime() + e.getTimeForOpen();
            double new_call_rout_time = time_to_src + src_to_dest;
            return new_call_rout_time;
        }
        CallForElevator curr_call = calls_per_elev[elev_index].get(0);

        //if the code reached this point it means that this elevator have at least 1 call
        //the next if and else block checks if the current call is on its way to the source or the destination and if the new call is already on its way.
        if (c.getType() == curr_call.getType() && c.getType() == CallForElevator.UP) {
            if (curr_call.getState() == CallForElevator.GOING2SRC && e.getPos() < c.getSrc() && c.getSrc() < curr_call.getSrc()) {
                time_of_first += Math.abs(e.getPos() - c.getSrc()) * time_per_floor + e.getStopTime() + e.getTimeForOpen() + e.getTimeForClose();
                return time_of_first;
            }
            if (curr_call.getState() == CallForElevator.GOIND2DEST && e.getPos() < c.getSrc() && c.getSrc() < curr_call.getDest()) {
                time_of_first += Math.abs(e.getPos() - c.getSrc()) * time_per_floor + e.getStopTime() + e.getTimeForOpen() + e.getTimeForClose();
                return time_of_first;
            }
        } else if (c.getType() == curr_call.getType() && c.getType() == CallForElevator.DOWN) {
            if (curr_call.getState() == CallForElevator.GOING2SRC && e.getPos() > c.getSrc() && c.getSrc() > curr_call.getSrc()) {
                time_of_first += Math.abs(e.getPos() - c.getSrc()) * time_per_floor + e.getStopTime() + e.getTimeForOpen() + e.getTimeForClose();
                return time_of_first;
            }
            if (curr_call.getState() == CallForElevator.GOIND2DEST && e.getPos() > c.getSrc() && c.getSrc() > curr_call.getDest()) {
                time_of_first += Math.abs(e.getPos() - c.getSrc()) * time_per_floor + e.getStopTime() + e.getTimeForOpen() + e.getTimeForClose();
                return time_of_first;
            }
        }
        else {
            if (curr_call.getState() == CallForElevator.GOIND2DEST)
                time_of_first += Math.abs(e.getPos() - curr_call.getDest());
            if (curr_call.getState() == CallForElevator.GOING2SRC)
                time_of_first += Math.abs(e.getPos() - curr_call.getSrc());
        }
        ArrayList<Integer> rout = new ArrayList<>();
        int start;
        int max_min;
        if (curr_call.getState() == CallForElevator.GOING2SRC) {
            rout.add(curr_call.getSrc());
            max_min = curr_call.getDest();
            start = 0;
        } else {
            rout.add(curr_call.getDest());
            max_min = curr_call.getDest();
            start = 1;
        }
        boolean arrived_src = false, arrived_dest = false;
        int i = start;
        /*
        this loop purpose is to add the destination floors in which the elevator change its direction.
        by that we can understand how many floors the elevator actually passing and calculate the time to pass them.
         */
        while (i < num_calls - 1) {
            int curr_type = calls_per_elev[elev_index].get(i).getType();
            int next_type = calls_per_elev[elev_index].get(i + 1).getType();
            if (next_type == curr_type) {
                if (curr_type == CallForElevator.UP) {
                    max_min = Math.max(max_min, calls_per_elev[elev_index].get(i + 1).getDest());
                    if (c.getType() == CallForElevator.UP && c.getSrc() < max_min) {
                        rout.add(c.getSrc());
                        arrived_src = true;
                    }
                    if (c.getType() == CallForElevator.UP && c.getDest() < max_min && arrived_src) {
                        rout.add(c.getDest());
                        arrived_dest = true;
                        break;
                    }
                } else {
                    max_min = Math.min(max_min, calls_per_elev[elev_index].get(i + 1).getDest());
                    if (c.getType() == CallForElevator.DOWN && c.getSrc() > max_min) {
                        rout.add(c.getSrc());
                        arrived_src = true;
                    }
                    if (c.getType() == CallForElevator.DOWN && c.getDest() > max_min && arrived_src) {
                        rout.add(c.getDest());
                        arrived_dest = true;
                        break;
                    }
                }
            }
//  this block represents changing directions cases of calls list of elevator
            if (next_type != curr_type) {
                if (curr_type == CallForElevator.UP) { // UP to DOWN
                    max_min = Math.max(max_min, calls_per_elev[elev_index].get(i + 1).getSrc());
                    if (c.getType() == CallForElevator.UP && c.getSrc() < max_min) {
                        rout.add(c.getSrc());
                        arrived_src = true;
                    }
                    if (c.getType() == CallForElevator.UP && c.getDest() < max_min && arrived_src) {
                        rout.add(c.getDest());
                        arrived_dest = true;
                        break;
                    }
                } else { // DOWN to UP
                    max_min = Math.min(max_min, calls_per_elev[elev_index].get(i + 1).getSrc());
                    if (c.getType() == CallForElevator.DOWN && c.getSrc() > max_min) {
                        rout.add(c.getSrc());
                        arrived_src = true;
                    }
                    if (c.getType() == CallForElevator.DOWN && c.getDest() > max_min && arrived_src) {
                        rout.add(c.getDest());
                        arrived_dest = true;
                        break;
                    }
                }
                rout.add(max_min);
                max_min = calls_per_elev[elev_index].get(i + 1).getDest();
            }
            if (i + 1 == num_calls && next_type == curr_type) {
                rout.add(max_min);
            }
            i++;
        }
        if (!arrived_src) {
            rout.add(c.getSrc());
            rout.add(c.getDest());
        }
        if (arrived_src && !arrived_dest)
            rout.add(c.getDest());
        int floors_passing = 0;
        for (int j = 0; j < rout.size() - 1; j++) {
            int x = rout.get(j);
            int y = rout.get(j + 1);
            floors_passing += Math.abs(x - y);
        }
        double rout_time = floors_passing * time_per_floor + i * (e.getStartTime() + e.getStopTime() + e.getTimeForOpen() + e.getTimeForClose()) + time_of_first;
        double regret_time = (num_calls - i) * (e.getStartTime() + e.getStopTime() + e.getTimeForOpen() + e.getTimeForClose());
        return rout_time + regret_time;
    }

    @Override
    public void cmdElevator(int elev_num) {
        while (!calls_per_elev[elev_num].isEmpty() &&
                calls_per_elev[elev_num].getFirst().getState() == CallForElevator.DONE) {
            calls_per_elev[elev_num].removeFirst();
        } // because we want our lists to contain only undone calls
        Elevator e = _building.getElevetor(elev_num);
        if (calls_per_elev[elev_num].isEmpty()) { // no calls
            // sleeping_mode(e);
            return; } // do nothing
        CallForElevator c = calls_per_elev[elev_num].getFirst();
        int stop_floor;
        if (c.getState() == CallForElevator.GOIND2DEST) // same as above
            stop_floor = c.getDest();
        else // (c.getState() == CallForElevator.GOING2SRC)
            stop_floor = c.getSrc();
        if (e.getState() == Elevator.LEVEL)
            e.goTo(stop_floor);
        else { // this elevator has call(s), lets deal them
            ListIterator<CallForElevator> li = calls_per_elev[elev_num].listIterator();
            int new_stop_floor = stop_floor;
            while (li.hasNext()) { // can to be improved by not stopping if its can't reach dest floor without changing direction.
                CallForElevator next_call = li.next();
                if (next_call.getType() == c.getType() && c.getType() == CallForElevator.UP) {
                    if (next_call.getState() == CallForElevator.GOING2SRC && e.getPos() < next_call.getSrc() && next_call.getSrc() < new_stop_floor)
                        new_stop_floor = next_call.getSrc();
                    else if (next_call.getState() == CallForElevator.GOIND2DEST && e.getPos() < next_call.getDest() && next_call.getDest() < new_stop_floor)
                        new_stop_floor = next_call.getDest();
                }
                if (next_call.getType() == c.getType() && c.getType() == CallForElevator.DOWN) {
                    if (next_call.getState() == CallForElevator.GOING2SRC && e.getPos() > next_call.getSrc() && next_call.getSrc() > new_stop_floor)
                        new_stop_floor = next_call.getSrc();
                    else if (next_call.getState() == CallForElevator.GOIND2DEST && e.getPos() > next_call.getDest() && next_call.getDest() > new_stop_floor)
                        new_stop_floor = next_call.getDest();
                }
            }
            if (new_stop_floor != stop_floor)
                e.stop(new_stop_floor);
        }
    }
    /**
     *  our former version of the algorithm, without regret time calculation:
     */
//    private double calcCurrentRoutTime(Elevator elev, int elev_index) {
//        double time_per_floor = 1 / elev.getSpeed();
//        int num_calls = calls_per_elev[elev_index].size();
//        if (num_calls == 0)
//            return 0;
//        ArrayList<Integer> rout = new ArrayList<>();
//        rout.add(calls_per_elev[elev_index].get(0).getSrc());
//        int max_min = calls_per_elev[elev_index].get(0).getDest();
//        for (int i = 0; i < num_calls - 1; i++) {
//            int curr_type = calls_per_elev[elev_index].get(i).getType();
//            int next_type = calls_per_elev[elev_index].get(i + 1).getType();
//            if (next_type == curr_type) {
//                if (curr_type == CallForElevator.UP)
//                    max_min = Math.max(max_min, calls_per_elev[elev_index].get(i + 1).getDest());
//                else
//                    max_min = Math.min(max_min, calls_per_elev[elev_index].get(i + 1).getDest());
//            }
//            if (next_type != curr_type) {
//                if (curr_type == CallForElevator.UP)
//                    max_min = Math.max(max_min, calls_per_elev[elev_index].get(i + 1).getSrc());
//                else
//                    max_min = Math.min(max_min, calls_per_elev[elev_index].get(i + 1).getSrc());
//                rout.add(max_min);
//                max_min = calls_per_elev[elev_index].get(i + 1).getDest();
//            }
//            if (i + 1 == num_calls && next_type == curr_type)
//                rout.add(max_min);
//        }
//        int floors_passing = 0;
//        for (int i = 0; i < rout.size() - 1; i++) {
//            int x = rout.get(i);
//            int y = rout.get(i + 1);
//            floors_passing += Math.abs(x - y);
//        }
//        double rout_time = floors_passing * time_per_floor + num_calls * (elev.getStartTime() +
//                elev.getStopTime() + elev.getTimeForOpen() + elev.getTimeForClose());
//        return rout_time;
//    }
}