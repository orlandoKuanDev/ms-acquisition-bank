package com.example.msacquisitionbank.utils;

import java.util.Random;

/**
 * Peruvian generator account number
 * Author Orlando Kuan Becerra
 */
public class IbanGenerator {
    private Random random = new Random(System.currentTimeMillis());

    /**
     *
     * @param account the account number of the acquisition
     * @return  A randomly generated, account number.
     */
    public String generate(String account) {
        StringBuilder builder = new StringBuilder();
        builder.append("PE");
        Random value = new Random();
        builder.append(value.nextInt(10));
        builder.append(value.nextInt(10));
        builder.append("-");
        builder.append(account);
        return builder.toString();
    }
}