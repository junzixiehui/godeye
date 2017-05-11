package com.ziroom.godeye.util;

import org.junit.Test;

import java.util.Random;

/**
 * Description:
 *
 * @author: by qlb
 * @date: 2017/4/22 18:36
 * @version: 1.0
 */
public class RandomTest {

  private int sampleRate = 100;

  private final Random randIntGen = new Random();
  private int BASE = 100;

  @Test
  public void testRandom() {
    int randomValue = randIntGen.nextInt(BASE);
    System.out.println(randomValue);
  }
}
