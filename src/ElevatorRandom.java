import java.util.Comparator;
import java.util.LinkedList;
import java.util.Random;

public class ElevatorRandom extends Random {
    LinkedList<Double> nextPoissonValues = new LinkedList<>();
    double time = 0;

    public double nextPoissonTime(double lambda) {
        while (nextPoissonValues.isEmpty()) {
            int n = nextPoisson(lambda);
            for (int i = 0; i < n; i++) {
                nextPoissonValues.add(time + nextDouble());
            }
            time += 1;
            nextPoissonValues.sort(Comparator.naturalOrder());
        }
        return nextPoissonValues.removeFirst();
    }

    public int nextPoisson(double lambda) {
        double u = nextDouble();
        int i = 0;
        double c = Math.exp(-lambda);
        while (u >= c) {
            i++;
            c += Math.exp(-lambda) * Math.pow(lambda, i) / factorial(i);
        }
        return i;
    }

    private long factorial(int n) {
        long fact = 1;
        for (int i = 2; i <= n; i++) {
            fact = fact * i;
        }
        return fact;
    }

    public double nextExponential(double lambda) {
        return -Math.log(1 - nextDouble()) / lambda;
    }
}