public class ElevatorSim extends Thread {
    int nElevators = 1;
    int nFloors = 3;
    String schedulerMode = "FCFS";  // FCFS,  SSTF  ou  LS
    String idleMode = "mid";    // mid, high ou low
    Elevator[] elevators;
    Floor[] floors;


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
        floors = new Floor[nFloors];
    }

    @Override
    public void run() {
        // TODO
    }
}
