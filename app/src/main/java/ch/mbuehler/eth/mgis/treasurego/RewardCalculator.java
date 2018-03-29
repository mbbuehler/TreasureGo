package ch.mbuehler.eth.mgis.treasurego;

/**
 * Calculates the reward for a particular game situation. The calculated reward depends on the average speed and the current temperature.
 */

public class RewardCalculator {

    /**
     * In degrees Celsius
     */
    final static float[] TEMPERATURE_RANGE = new float[]{0, 30};
    /**
     * In m/s: 60 km/h * 1000m / 3600s = m/s
     */
    final static float[] SPEED_RANGE = new float[]{0, 60 * 1000 / 3600};

    /**
     * Calculates the reward for the given combination of variables.
     * The reward is calculated as a linear combination of the averageSpeed and currentTemperature.
     * Returns the reward as number of coins (int): 0 <= reward <= maxReward
     *
     * @param treasure           Treasure
     * @param avgSpeed           Double
     * @param currentTemperature float
     * @return int
     */
    static int calculateReward(Treasure treasure, Double avgSpeed, float currentTemperature) {

        // Calculate normalized values for speed and temperature
        float normalizedSpeed = getNormalizedSpeed(avgSpeed.floatValue());
        float normalizedTemperature = getNormalizedTemperature(currentTemperature);

        // maximum achievable reward for this treasure
        int maxReward = treasure.getReward();

        // Calculate the rewards for each aspect (speed and temperature)
        float speedReward = normalizedSpeed * maxReward;
        float temperatureReward = normalizedTemperature * maxReward;

        // Calculate the precise reward as a linear combination of the contributions from speed and temperature
        float preciseReward = (float) 2 / 3 * speedReward + (float) 1 / 3 * temperatureReward;

        // Reward is measured in coins, so we need an int.
        return Math.round(preciseReward);
    }

    /**
     * Returns the normalized temperature.
     *
     * @param temperature float
     * @return float
     */
    private static float getNormalizedTemperature(float temperature) {
        return normalizeValue(temperature, TEMPERATURE_RANGE[0], TEMPERATURE_RANGE[1]);
    }

    /**
     * Returns the normalized speed
     *
     * @param speed float
     * @return float
     */
    private static float getNormalizedSpeed(float speed) {
        return normalizeValue(speed, SPEED_RANGE[0], SPEED_RANGE[1]);
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
    private static float normalizeValue(float value, float min, float max) {
        float valueInRange = Math.max(value, min);
        valueInRange = Math.min(value, max);

        float normalizedValue = valueInRange / max;
        return normalizedValue;
    }


}
