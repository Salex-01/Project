public class Person {
    double arrivalTime; // Heure à laquelle la personne appelle un ascenseur
    int origin; // Étage de départ
    int destination;    // Étage d'arrivée
    double duration;    // Durée du travail
    double waitingTime = 0; // Temps d'attente
    Elevator targetTakenBy = null;  // Pour savoir si sa demande est prise en compte par un ascenseur

    public Person(double a, int o, int dest, double dur) {
        arrivalTime = a;
        origin = o;
        destination = dest;
        duration = dur;
    }
}