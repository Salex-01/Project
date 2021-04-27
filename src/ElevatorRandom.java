import java.util.Random;

public class ElevatorRandom extends Random {
        public double nextExponential(double lambda) {
        double rand;
        do {
            rand = nextDouble();
        } while (rand == 0);
        return -Math.log(rand) / lambda;
    }
}