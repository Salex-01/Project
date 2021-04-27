import java.util.Comparator;
import java.util.LinkedList;

public class Elevator {
    ElevatorSim sim;
    LinkedList<Person> carrying = new LinkedList<>();
    Person target = null;
    double floor;
    double currentActionCompletionTime = 0;
    boolean direction = false;  // true = vers le haut

    public Elevator(ElevatorSim s) {
        sim = s;
        floor = (sim.nFloors - 1) / 2.;
    }

    public void work(double deltaTime, String scheduler, String idle, boolean log) {
        if (deltaTime < 0) {
            return;
        }
        if (carrying.isEmpty()) {
            if (target == null && sim.calls.isEmpty()) {
                moveToIdle(deltaTime, idle);
            } else {
                moveToNext(deltaTime, scheduler);
            }
        } else {
            carryPeople(deltaTime, scheduler, idle, log);
        }
    }

    private void moveToIdle(double deltaTime, String mode) {
        switch (mode) {
            case "low":
                direction = false;
                floor = Math.max(0, floor - deltaTime * sim.elevatorSpeed);
                currentActionCompletionTime = sim.time + floor / sim.elevatorSpeed;
                break;
            case "mid":
                direction = floor < (sim.nFloors - 1) / 2.;
                if (floor < (sim.nFloors - 1) / 2.) {
                    floor = Math.min((sim.nFloors - 1) / 2., floor + deltaTime * sim.elevatorSpeed);
                } else {
                    floor = Math.max((sim.nFloors - 1) / 2., floor - deltaTime * sim.elevatorSpeed);
                }
                currentActionCompletionTime = sim.time + Math.abs(floor - (sim.nFloors - 1) / 2.) / sim.elevatorSpeed;
                break;
            case "high":
                direction = true;
                floor = Math.min(sim.nFloors - 1, floor + deltaTime * sim.elevatorSpeed);
                currentActionCompletionTime = sim.time + (sim.nFloors - 1 - floor) / sim.elevatorSpeed;
                break;
            default:
                System.out.println("Mode d'attente inconnu : " + mode);
                System.exit(-1);
        }
    }

    private void moveToNext(double deltaTime, String mode) {
        if (target == null) {
            // fcfs,  sstf  ou  ls
            switch (mode) {
                case "fcfs":    // Premier arrivé premier servi
                    target = sim.calls.removeFirst();
                    currentActionCompletionTime = sim.time + Math.abs(floor - target.origin) / sim.elevatorSpeed;
                    return;
                case "sstf":    // Demande la plus proche
                    target = sim.calls.getFirst();
                    for (Person p : sim.calls) {
                        if (Math.abs(floor - p.origin) < Math.abs(floor - target.origin)) {
                            target = p;
                        }
                    }
                    sim.calls.remove(target);
                    currentActionCompletionTime = sim.time + Math.abs(floor - target.origin) / sim.elevatorSpeed;
                    return;
                case "ls":  // Premier arrivé premier servi dans la direction actuelle
                    if (direction) {
                        for (Person p : sim.calls) {
                            if (p.origin >= floor) {
                                target = p;
                                sim.calls.remove(p);
                                currentActionCompletionTime = sim.time + Math.abs(floor - target.origin) / sim.elevatorSpeed;
                                return;
                            }
                        }
                    } else {
                        for (Person p : sim.calls) {
                            if (p.origin <= floor) {
                                target = p;
                                sim.calls.remove(p);
                                currentActionCompletionTime = sim.time + Math.abs(floor - target.origin) / sim.elevatorSpeed;
                                return;
                            }
                        }
                    }
                    //noinspection ConstantConditions
                    if (target == null) {   // Quand il n'y a aucune demande dans la direction actuelle
                        target = sim.calls.removeFirst();
                        currentActionCompletionTime = sim.time + Math.abs(floor - target.origin) / sim.elevatorSpeed;
                        return;
                    }
                    break;
                default:
                    System.out.println("Planificateur inconnu : " + mode);
                    System.exit(-1);
            }
        }
        if (floor < target.origin) {
            floor = Math.min(target.origin, floor + deltaTime * sim.elevatorSpeed);
            currentActionCompletionTime = sim.time + (target.origin - floor) / sim.elevatorSpeed;
        } else if (floor > target.origin) {
            floor = Math.max(target.origin, floor - deltaTime * sim.elevatorSpeed);
            currentActionCompletionTime = sim.time + (floor - target.origin) / sim.elevatorSpeed;
        }
        if (Math.abs(floor - target.origin) < 0.000001) {  // Pour contourner les erreurs de précision des doubles
            carrying.addLast(target);
            target = null;
            currentActionCompletionTime = sim.time + Math.abs(carrying.getFirst().destination - floor) / sim.elevatorSpeed;
        }
    }

    private void carryPeople(double deltaTime, String s, String i, boolean log) {
        if (deltaTime <= 0) {
            return;
        }
        if (floor < carrying.getFirst().destination) {
            direction = true;
            floor = Math.min(carrying.getFirst().destination, floor + deltaTime * sim.elevatorSpeed);
            currentActionCompletionTime = sim.time + (carrying.getFirst().destination - floor) / sim.elevatorSpeed;
        } else if (floor > carrying.getFirst().destination) {
            direction = false;
            floor = Math.max(carrying.getFirst().destination, floor - deltaTime * sim.elevatorSpeed);
            currentActionCompletionTime = sim.time + (floor - carrying.getFirst().destination) / sim.elevatorSpeed;
        }
        if (Math.abs(floor - carrying.getFirst().destination) < 0.000001) {  // Pour contourner les erreurs de précision des doubles
            floor = carrying.getFirst().destination;
            LinkedList<Person> toRemove = new LinkedList<>();
            if (floor != 0) {
                for (Person p : carrying) {
                    if (p.destination == floor) {
                        toRemove.add(p);
                    }
                }
                for (Person p : toRemove) {
                    carrying.remove(p);
                    p.waitingTime = sim.time - p.arrivalTime;
                    p.arrivalTime = sim.time + p.duration;
                    p.origin = p.destination;
                    p.destination = 0;
                    p.duration = 0;
                    sim.working.add(p);
                }
                sim.working.sort(Comparator.comparingDouble(o -> o.arrivalTime));
            } else {
                for (Person p : carrying) {
                    if (p.destination == 0) {
                        toRemove.add(p);
                    }
                }
                for (Person p : toRemove) {
                    carrying.remove(p);
                    p.waitingTime += sim.time - p.arrivalTime;
                    sim.totalWaitingTime = sim.totalWaitingTime + p.waitingTime;
                    sim.served++;
                    if (log) {
                        System.out.println("Temps d'attente : " + p.waitingTime * 60 + " secondes");
                    }
                }
            }
            work(0, s, i, log);
        }
    }
}