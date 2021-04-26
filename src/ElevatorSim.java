import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;

public class ElevatorSim extends Thread {
    boolean stopped = false;
    int nElevators = 1;
    int nFloors = 3;
    String schedulerMode = "fcfs";  // fcfs,  sstf  ou  ls
    String idleMode = "mid";    // mid, high ou low
    double elevatorSpeed = 6;   // En étages par minute
    Elevator[] elevators;
    double oldTime = 0;
    double time = 0;
    LinkedList<Person> people = new LinkedList<>();
    LinkedList<Person> working = new LinkedList<>();
    ElevatorRandom r = new ElevatorRandom();
    double poissonLambda = 0.5;
    double meanWaitingTime = 0;
    int served = 0;

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
                case "speed":
                    elevatorSpeed = Double.parseDouble(args[i + 1]);
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
        new Stopper(this).start();
        people.addLast(new Person(0, 0, r.nextInt(nFloors - 1) + 1, r.nextExponential(60)));
        working.addLast(new Person(r.nextPoissonTime(poissonLambda), 0, r.nextInt(nFloors - 1) + 1, r.nextExponential(60)));
        while (!stopped) {
            if (working.getFirst().arrivalTime == time) {
                Person c = working.removeFirst();
                if (c.origin == 0) {
                    working.addLast(new Person(time + r.nextPoissonTime(poissonLambda), 0, r.nextInt(nFloors - 1) + 1, r.nextExponential(60)));
                    working.sort(Comparator.comparingDouble(o -> o.arrivalTime));
                }
                people.addLast(c);
            }
            for (Elevator e : elevators) {
                e.work(time - oldTime, schedulerMode, idleMode);
            }
            oldTime = time;
            time = nextEvent();
        }
        System.out.println("Temps d'attente moyen sur " + served + " personnes : " + meanWaitingTime + "minutes");
    }

    public double nextEvent() {
        double min = Double.MAX_VALUE;
        for (Person p : working) {
            if (p.arrivalTime < min) {
                min = p.arrivalTime;
            }
        }
        for (Elevator e : elevators) {
            if (e.currentActionCompletionTime < min && e.currentActionCompletionTime > time) {
                min = e.currentActionCompletionTime;
            }
        }
        return min;
    }

    private static class Stopper extends Thread {
        ElevatorSim sim;

        public Stopper(ElevatorSim s) {
            sim = s;
        }

        @Override
        @SuppressWarnings("BusyWait")
        public void run() {
            while (true) {
                try {
                    if (System.in.available() != 0) break;
                } catch (IOException ignored) {
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
            sim.stopped = true;
        }
    }
}