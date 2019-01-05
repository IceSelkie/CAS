package com.hypereclipse.selkie.cas;

public class Prime extends Int
{
  private Prime(Int val)
  {
    super(val);
  }

  public static boolean isPrime(Int value)
  {
    return false;
  }

  public static Int toPrime(Int value)
  {
    if (!isPrime(value))
      return value;
    return new Prime(value);
  }
}
