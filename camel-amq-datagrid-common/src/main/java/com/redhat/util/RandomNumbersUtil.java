package com.redhat.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class RandomNumbersUtil {
    static Random random = new Random();

    public static int randomInt() {
        return random.nextInt(1000) + 1;
    }

    public static double randomDouble() {
        return BigDecimal.valueOf(random.nextDouble(1000) + 1).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
}
