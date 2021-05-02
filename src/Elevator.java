import java.util.Comparator;
import java.util.LinkedList;

public class Elevator {
    ElevatorSim sim;
    LinkedList<Person> carrying = new LinkedList<>();   // Personnes dans l'ascenseur
    Person target = null;   // Personne dont la demande va être traitée
    double floor;   // Étage actuel
    double currentActionCompletionTime = 0; // Heure à laquelle l'action en cours sera terminée
                                            // (heure du prochain évènement lié à cet ascenseur)
    boolean direction = false;  // true = vers le haut

    public Elevator(ElevatorSim s) {
        sim = s;
        floor = (sim.nFloors - 1) / 2.;
    }

    public void work(double deltaTime, String scheduler, String idle, boolean log) {
        if (deltaTime < 0) {
            return;
        }
        // Si il n'y a personne dans l'ascenseur
        if (carrying.isEmpty()) {
            // Si il n'y a aucune demande à traiter
            if (sim.calls.stream().noneMatch(person -> (person.targetTakenBy == this || person.targetTakenBy == null))) {
                target = null;
                moveToIdle(deltaTime, idle);    // Appliquer la politique d'attente
            } else {    // Sinon
                moveToNextTarget(deltaTime, scheduler); // Acquisition et déplacement vers la prochaine cible
            }
        } else {    // Sinon
            carryPeople(deltaTime, scheduler, idle, log);   // Traiter les demandes des personnes à bord
        }
    }

    // Application de la politique d'attente
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

    // Trouve une personne dont la demande n'est pas prise en compte et l'assigne comme cible
    // (si elle existe, dans l'ordre d'arrivée des demandes)
    private void findFreeTarget() {
        for (Person p : sim.calls) {
            if (p.targetTakenBy == null) {
                target = p;
                break;
            }
        }
    }

    // Acquisition/déplacement vers la prochaine cible
    private void moveToNextTarget(double deltaTime, String mode) {
        // Si la cible n'est pas valide, acquisition d'une nouvelle cible
        if (target == null || !sim.calls.contains(target)) {
            target = null;
            // fcfs,  sstf  ou  ls
            switch (mode) {
                case "fcfs":    // Premier arrivé premier servi
                    findFreeTarget();
                    target.targetTakenBy = this;
                    currentActionCompletionTime = sim.time + Math.abs(floor - target.origin) / sim.elevatorSpeed;
                    return;
                case "sstf":    // Demande la plus proche
                    for (Person p : sim.calls) {
                        if ((target == null || Math.abs(floor - p.origin) < Math.abs(floor - target.origin)) && p.targetTakenBy == null) {
                            target = p;
                        }
                    }
                    //noinspection ConstantConditions
                    target.targetTakenBy = this;
                    currentActionCompletionTime = sim.time + Math.abs(floor - target.origin) / sim.elevatorSpeed;
                    return;
                case "ls":  // Premier arrivé premier servi dans la direction actuelle
                    for (Person p : sim.calls) {
                        if ((p.origin == floor || (p.origin > floor) == direction) && p.targetTakenBy == null) {
                            target = p;
                            break;
                        }
                    }
                    if (target == null) {  // Quand il n'y a aucune demande dans la direction actuelle
                        findFreeTarget();
                    }
                    target.targetTakenBy = this;
                    currentActionCompletionTime = sim.time + Math.abs(floor - target.origin) / sim.elevatorSpeed;
                    return;
                default:
                    System.out.println("Planificateur inconnu : " + mode);
                    System.exit(-1);
            }
        }
        // Prise en compte des demandes de toutes les personnes au même étage que la cible
        for (Person p : sim.calls) {
            if (p.origin == target.origin && p.targetTakenBy == null) {
                p.targetTakenBy = this;
            }
        }
        // Déplacement vers la cible
        if (floor < target.origin) {
            floor = Math.min(target.origin, floor + deltaTime * sim.elevatorSpeed);
            currentActionCompletionTime = sim.time + (target.origin - floor) / sim.elevatorSpeed;
        } else if (floor > target.origin) {
            floor = Math.max(target.origin, floor - deltaTime * sim.elevatorSpeed);
            currentActionCompletionTime = sim.time + (floor - target.origin) / sim.elevatorSpeed;
        }
        // Si on a atteint l'étage de départ de la cible
        if (Math.abs(floor - target.origin) < 1E-6) {  // Pour contourner les erreurs de précision des doubles
            floor = target.origin;
            target = null;
            // Montée de toutes les personnes en attente à cet étage
            for (Person p : sim.calls) {
                if (p.origin == floor) {
                    carrying.addLast(p);
                }
            }
            for (Person p : carrying) {
                sim.calls.remove(p);
                p.targetTakenBy = null;
            }
            currentActionCompletionTime = sim.time + Math.abs(carrying.getFirst().destination - floor) / sim.elevatorSpeed;
        }
    }

    // Traitement des demandes des personnes à bord (dans l'ordre des demandes)
    private void carryPeople(double deltaTime, String s, String i, boolean log) {
        if (deltaTime <= 0) {
            if (!carrying.isEmpty()) {
                currentActionCompletionTime = sim.time + Math.abs(floor - carrying.getFirst().destination) / sim.elevatorSpeed;
            }
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
        // Si on a atteint la destination de la première demande
        if (Math.abs(floor - carrying.getFirst().destination) < 1E-6) {  // Pour contourner les erreurs de précision des doubles
            floor = carrying.getFirst().destination;
            LinkedList<Person> toRemove = new LinkedList<>();
            // Débarquement de toutes les personnes voulant descendre à cet étage
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
                // Log des temps d'attente des personnes qui descendent
                for (Person p : toRemove) {
                    carrying.remove(p);
                    p.waitingTime += sim.time - p.arrivalTime;
                    sim.totalWaitingTime = sim.totalWaitingTime + p.waitingTime;
                    sim.served++;
                    if (log) {
                        System.out.println("Temps d'attente : " + p.waitingTime * 60 + " secondes pour l'aller retour à l'étage " + p.origin);
                    }
                }
            }
            work(0, s, i, log);
        }
    }
}