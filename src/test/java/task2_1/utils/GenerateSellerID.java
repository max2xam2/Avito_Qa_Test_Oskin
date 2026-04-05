package task2_1.utils;

import java.util.Random;

public class GenerateSellerID {
  private static final int MIN = 111111;
  private static final int MAX = 999999;

  public static int generateSellerId() {
    Random random = new Random();
    return random.nextInt(MAX - MIN + 1) + MIN;
  }
}
