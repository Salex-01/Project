import java.util.LinkedList;

public class ElevatorSim extends Thread {
    boolean stopped = false;
    int nElevators = 1;
    int nFloors = 3;
    String schedulerMode = "FCFS";  // FCFS,  SSTF  ou  LS
    String idleMode = "mid";    // mid, high ou low
    Elevator[] elevators;
    double oldTime = 0;
    double time = 0;
    LinkedList<Call> calls = new LinkedList<>();
    LinkedList<Integer> floors = new LinkedList<>();
    LinkedList<Call> working = new LinkedList<>();
    ElevatorRandom r = new ElevatorRandom();

    public ElevatorSim(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "elevators":
                case "e":
                    nElevators = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                case "floors":
                case "f":
                    nFloors = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                case "scheduler":
                case "s":
                    schedulerMode = args[i + 1];
                    i++;
                    break;
                case "idle":
                case "i":
                    idleMode = args[i + 1];
                    i++;
                    break;
                default:
                    System.out.println("Argument inconnu : " + args[i]);
                    System.exit(-1);
            }
        }
        elevators = new Elevator[nElevators];
        for (int i = 0; i < nElevators; i++) {
            elevators[i] = new Elevator(this);
        }
    }

    @Override
    public void run() {
        calls.addLast(new Call(0, 0, r.nextInt(nFloors - 1) + 1, r.nextExponential(60)));
        working.addLast(new Call(r.nextPoisson(0.5), 0, r.nextInt(nFloors - 1) + 1, r.nextExponential(60)));
        while (!stopped) {
            if (working.getFirst().arrivalTime == time) {
                calls.addLast(working.removeFirst());
            }
            for (Elevator e : elevators) {
                e.work(time, time - oldTime, schedulerMode, idleMode);
            }
            oldTime = time;
            time = nextEvent();
        }
    }

    public double nextEvent() {
        double min = Double.MAX_VALUE;
        for (Call c : working) {
            if (c.arrivalTime < min) {
                min = c.arrivalTime;
            }
        }
        return min;
    }
}