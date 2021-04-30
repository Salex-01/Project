import javax.swing.*;
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
    LinkedList<Person> calls = new LinkedList<>();
    LinkedList<Person> working = new LinkedList<>();
    ElevatorRandom r = new ElevatorRandom();
    double arrivalLambda = 1. / 2;
    double workLambda = 1. / 60;
    double totalWaitingTime = 0;
    int served = 0;
    long maxPeople = Long.MAX_VALUE;
    boolean log = false;

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
                case "maxPeople":
                case "mp":
                    maxPeople = Long.parseLong(args[i + 1]);
                    i++;
                    break;
                case "arrivalLambda":
                case "al":
                    arrivalLambda = Double.parseDouble(args[i + 1]);
                    i++;
                    break;
                case "workLambda":
                case "wl":
                    workLambda = Double.parseDouble(args[i + 1]);
                    i++;
                    break;
                case "speed":
                    elevatorSpeed = Double.parseDouble(args[i + 1]);
                    i++;
                    break;
                case "log":
                    log = true;
                    break;
                default:
                    System.out.println("Argument inconnu : " + args[i]);
                    System.exit(-1);
            }
        }
        if (nFloors < 2) {
            System.out.println("Au moins 2 étages");
            System.exit(-1);
        }
        elevators = new Elevator[nElevators];
        for (int i = 0; i < nElevators; i++) {
            elevators[i] = new Elevator(this);
        }
    }

    @Override
    public void run() {
        new Stopper(this, true).start();
        Stopper s = new Stopper(this, false);
        s.start();
        working.addLast(new Person(time + r.nextExponential(arrivalLambda), 0, r.nextInt(nFloors - 1) + 1, r.nextExponential(workLambda)));
        while (!stopped && served < maxPeople) {
            if (time >= working.getFirst().arrivalTime) {   // Normalement, si vrai, arrivalTime==time
                Person c = working.removeFirst();
                if (c.origin == 0) {
                    working.addLast(new Person(time + r.nextExponential(arrivalLambda), 0, r.nextInt(nFloors - 1) + 1, r.nextExponential(workLambda)));
                    working.sort(Comparator.comparingDouble(o -> o.arrivalTime));
                }
                calls.addLast(c);
            }
            elevatorTargets();
            for (Elevator e : elevators) {
                e.work(time - oldTime, schedulerMode, idleMode, log);
            }
            oldTime = time;
            time = nextEvent();
        }
        System.out.println("Temps d'attente moyen sur "
                + served + (served > 1 ? " personnes et " : " personne et ")
                + nFloors + " étages avec "
                + nElevators + (nElevators > 1 ? " ascenseurs : " : " ascenseur : ")
                + totalWaitingTime * 60 / served + " secondes");
        String[] button = {"OK"};
        JOptionPane.showOptionDialog(null, "Temps d'attente moyen sur "
                        + served + (served > 1 ? " personnes et " : " personne et ")
                        + nFloors + " étages avec "
                        + nElevators + (nElevators > 1 ? " ascenseurs : " : " ascenseur : ")
                        + totalWaitingTime * 60 / served + " secondes",
                "", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, button, button[0]);
        System.exit(0);
    }

    private void elevatorTargets() {
        Elevator[] e = new Elevator[nFloors];
        for (Person p : calls) {
            if (p.targetTakenBy != null) {
                e[p.origin] = p.targetTakenBy;
            }
        }
        for (Person p : calls) {
            p.targetTakenBy = e[p.origin];
        }
    }

    public double nextEvent() {
        double min = Double.MAX_VALUE;
        if (working.getFirst().arrivalTime < min) {
            min = working.getFirst().arrivalTime;
        }
        for (Elevator e : elevators) {
            if (e.currentActionCompletionTime < min && (e.currentActionCompletionTime > time || e.target != null)) {
                min = e.currentActionCompletionTime;
            }
        }
        return min;
    }

    private static class Stopper extends Thread {
        ElevatorSim sim;
        boolean mode;

        public Stopper(ElevatorSim s, boolean b) {
            sim = s;
            mode = b;
        }

        @Override
        @SuppressWarnings("BusyWait")
        public void run() {
            if (mode) {
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
            } else {
                String[] ObjButtons = {"STOP"};
                JOptionPane.showOptionDialog(null, "", "", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, ObjButtons, ObjButtons[0]);
            }
            sim.stopped = true;
        }
    }
}