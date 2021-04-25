import java.util.Random;

public class ElevatorRandom extends Random {
    public double nextPoisson(double lambda) {
        //TODO
        return 0;
    }

    public double nextExponential(double lambda) {
        return -Math.log(1 - nextDouble()) / lambda;
    }
}