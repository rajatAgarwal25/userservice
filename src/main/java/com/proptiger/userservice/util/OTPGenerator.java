package com.proptiger.userservice.util;

import java.util.Random;

/**
 * Random number generator with fixed length, by default 4 digits but could be
 * initialised to use any no no digits with a exception of max value of a int.
 * 
 * So if not passed randomNumDigits then length of all OTP generated should be
 * 4.
 * 
 * @author Rajeev Pandey
 *
 */
public class OTPGenerator {

    private int    BASE   = 1000;
    private int    MAX    = 8999;

    private Random random = new Random();

    public OTPGenerator() {

    }

    public OTPGenerator(int randomNumDigits) {
        BASE = 1;
        for (int i = 0; i < randomNumDigits - 1; i++) {
            BASE = BASE * 10;
        }
        MAX = BASE * 10 - 1 - BASE;

    }

    public int getRandomInt() {
        return BASE + random.nextInt(MAX);
    }
}
