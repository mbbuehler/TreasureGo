package ch.mbuehler.eth.mgis.treasurego;

/**
 * Calculates the reward for a particular game situation. The calculated reward depends on the average speed and the current temperature.
 */

class RewardCalculator {

    /**
     * In milliseconds
     */
    private final static float[] TIME_RANGE = new float[]{0, 60 * 1000};

    /**
     * Calculates the reward for the given combination of variables.
     * The reward is calculated as a linear combination of the averageSpeed and currentTemperature.
     * Returns the reward as number of coins (int): 0 <= reward <= maxReward
     *
     * @param treasure           Treasure
     * @param gemCollectionTimeMillis in milliseconds
     * @return int
     */
    static int calculateReward(Treasure treasure, double gemCollectionTimeMillis) {
        if(gemCollectionTimeMillis < 0){
            // Variable has not been set.
            return 0;
        }

        // Calculate normalized values for speed and temperature
        double normalizedCollectionTime = getNormalizedCollectionTime(gemCollectionTimeMillis);

        // maximum achievable reward for this treasure
        int maxReward = treasure.getReward();
        // The user receives at least some coins
        int minReward = (int)((float)maxReward / 2);
        int restReward = maxReward - minReward;

        // If the user was fast he can get additional points.
        double preciseReward = minReward + restReward * normalizedCollectionTime;

        // Reward is measured in coins, so we need an int.
        return (int) Math.round(preciseReward);
    }

    /**
     * Returns the normalized temperature.
     *
     * @param timeMillis double
     * @return float
     */
    private static double getNormalizedCollectionTime(double timeMillis) {
        return normalizeValue(timeMillis, TIME_RANGE[0], TIME_RANGE[1]);
    }

    /**
     * Returns the normalized value (range [0,1]).
     * If value < min this function returns 0.
     * If value > max this funciton returns 1.
     *
     * @param value float
     * @param min   minimum of range considered
     * @param max   maximum of range considered
     * @return float
     */
    private static double normalizeValue(double value, double min, double max) {
        double valueInRange = Math.max(value, min);
        valueInRange = Math.min(value, max);

        double normalizedValue = valueInRange / max;
        return normalizedValue;
    }


}
