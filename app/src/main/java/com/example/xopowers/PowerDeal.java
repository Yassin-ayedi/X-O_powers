package com.example.xopowers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Deals 3 random powers (from the full pool, excluding NONE) to a player each turn.
 * NONE is always included as one option to keep the game fair.
 */
public class PowerDeal {

    private static final Power[] POWER_POOL = {
        Power.BOMB, Power.SHIELD, Power.STEAL , Power.DOUBLE,
        Power.CHAIN , Power.MIRROR, Power.WILDCARD
    };

    private final Random random = new Random();

    /**
     * Returns 3 powers: always includes NONE as one choice + 2 random real powers.
     */
    public Power[] deal() {
        List<Power> pool = new ArrayList<>();
        for (Power p : POWER_POOL) pool.add(p);
        Collections.shuffle(pool, random);

        Power[] result = new Power[3];
        result[0] = Power.NONE;
        result[1] = pool.get(0);
        result[2] = pool.get(1);

        // Shuffle the 3 so NONE isn't always first
        List<Power> resultList = new ArrayList<>();
        for (Power p : result) resultList.add(p);
        Collections.shuffle(resultList, random);

        return resultList.toArray(new Power[0]);
    }
}
