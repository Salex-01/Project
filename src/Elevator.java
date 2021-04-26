import java.util.LinkedList;

public class Elevator {
    ElevatorSim sim;
    LinkedList<Call> carrying = new LinkedList<>();
    double floor;
    int nextTarget = 0;
    double currentActionCompletionTime = 0;

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
            boolean direction = nextTarget > floor; // true = vers le haut
            carryPeople(time, deltaTime, direction, scheduler, idle);
        }
    }

    private void moveToIdle(double deltaTime, String mode){
        switch (mode){
            case "low":

                break;
            case "mid":
                break;
            case "high":
                break;
        }
    }

    private void moveToNext(double time, double deltaTime, String mode){
        //TODO
    }

    private void carryPeople(double time, double deltaTime, boolean direction, String scheduler, String idle){
        //TODO
    }
}