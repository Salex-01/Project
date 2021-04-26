import java.util.LinkedList;

public class Elevator {
    ElevatorSim sim;
    LinkedList<Call> carrying = new LinkedList<>();
    double floor;
    int nextTarget = 0;
    double currentActionCompletionTime = 0;
    boolean direction;  // true = vers le haut

    public Elevator(ElevatorSim s) {
        sim = s;
        floor = (sim.nFloors - 1) / 2.;
    }

    public void work(double time, double deltaTime, String scheduler, String idle) {
        if (deltaTime <= 0) {
            return;
        }
        if (carrying.isEmpty()) {
            if (sim.calls.isEmpty()) {
                moveToIdle(deltaTime, idle);
            } else {
                moveToNext(time, deltaTime, scheduler);
            }
        } else {
            carryPeople(time, deltaTime, direction, scheduler, idle);
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
        }
    }

    private void moveToNext(double time, double deltaTime, String mode) {
        //TODO
    }

    private void carryPeople(double time, double deltaTime, boolean direction, String scheduler, String idle) {
        //TODO
    }
}