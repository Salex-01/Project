public class Person {
    double arrivalTime;
    int origin;
    int destination;
    double duration;
    double waitingTime = 0;
    Elevator targetTakenBy = null;

    public Person(double a, int o, int dest, double dur) {
        arrivalTime = a;
        origin = o;
        destination = dest;
        duration = dur;
    }
}