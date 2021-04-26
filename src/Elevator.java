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

    public void work(double deltaTime, String scheduler, String idle) {
        if (deltaTime <= 0) {
            return;
        }
        if (carrying.isEmpty()) {
            if (sim.people.isEmpty()) {
                moveToIdle(deltaTime, idle);
            } else {
                moveToNext(deltaTime, scheduler);
            }
        } else {
            carryPeople(deltaTime);
        }
    }

    private void moveToIdle(double deltaTime, String mode) {
        switch (mode) {
            case "low":
                floor = Math.max(0, floor - deltaTime * sim.elevatorSpeed);
                currentActionCompletionTime = sim.time + floor / sim.elevatorSpeed;
                break;
            case "mid":
                if (floor < (sim.nFloors - 1) / 2.) {
                    floor = Math.min((sim.nFloors - 1) / 2., floor + deltaTime * sim.elevatorSpeed);
                } else {
                    floor = Math.max((sim.nFloors - 1) / 2., floor - deltaTime * sim.elevatorSpeed);
                }
                currentActionCompletionTime = sim.time + Math.abs(floor - (sim.nFloors - 1) / 2.) / sim.elevatorSpeed;
                break;
            case "high":
                floor = Math.min(sim.nFloors - 1, floor + deltaTime * sim.elevatorSpeed);
                currentActionCompletionTime = sim.time + (sim.nFloors - 1 - floor) / sim.elevatorSpeed;
                break;
            default:
                System.out.println("Mode d'attente inconnu : " + mode);
                System.exit(-1);
        }
    }

    private void moveToNext(double deltaTime, String mode) {
        // fcfs,  sstf  ou  ls
        switch (mode) {
            case "fcfs":    // Premier arrivé premier servi
                if (target == null) {
                    target = sim.people.removeFirst();
                }
                break;
            case "sstf":    // Demande la plus proche
                if (target != null) {
                    target = sim.people.getFirst();
                    for (Person p : sim.people) {
                        if (Math.abs(floor - p.origin) < Math.abs(floor - target.origin)) {
                            target = p;
                        }
                    }
                    sim.people.remove(target);
                }
                break;
            case "ls":  // Premier arrivé premier servi dans la direction actuelle
                if (target == null) {
                    if (direction) {
                        for (Person p : sim.people) {
                            if (p.origin >= floor) {
                                target = p;
                                sim.people.remove(p);
                                break;
                            }
                        }
                    } else {
                        for (Person p : sim.people) {
                            if (p.origin <= floor) {
                                target = p;
                                sim.people.remove(p);
                                break;
                            }
                        }
                    }
                }
                if (target == null) {
                    target = sim.people.removeFirst();
                }
                break;
            default:
                System.out.println("Planificateur inconnu : " + mode);
                System.exit(-1);
        }
        //noinspection ConstantConditions
        if (floor < target.origin) {
            floor = Math.min(target.origin, floor + deltaTime * sim.elevatorSpeed);
            currentActionCompletionTime = sim.time + (target.origin - floor) / sim.elevatorSpeed;
        } else if (floor > target.origin) {
            floor = Math.max(target.origin, floor - deltaTime * sim.elevatorSpeed);
            currentActionCompletionTime = sim.time + (floor - target.origin) / sim.elevatorSpeed;
        } else {
            currentActionCompletionTime = sim.time;
        }
        if (floor == target.origin) {
            carrying.addLast(target);
            target = null;
        }
    }

    private void carryPeople(double deltaTime) {
        if (floor < carrying.getFirst().destination) {
            floor = Math.min(carrying.getFirst().destination, floor + deltaTime * sim.elevatorSpeed);
            currentActionCompletionTime = sim.time + (carrying.getFirst().destination - floor) / sim.elevatorSpeed;
        } else if (floor > carrying.getFirst().destination) {
            floor = Math.max(carrying.getFirst().destination, floor - deltaTime * sim.elevatorSpeed);
            currentActionCompletionTime = sim.time + (floor - carrying.getFirst().destination) / sim.elevatorSpeed;
        } else {
            currentActionCompletionTime = sim.time;
        }
        if (floor == carrying.getFirst().destination) {
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
                    sim.meanWaitingTime = (sim.meanWaitingTime * sim.served + p.waitingTime) / (sim.served + 1);
                    sim.served++;
                    System.out.println("Temps d'attente : " + p.waitingTime + "minutes");
                    System.out.println("Temps d'attente moyen sur " + sim.served + " personnes : " + sim.meanWaitingTime + "minutes");
                }
            }
        }
    }
}