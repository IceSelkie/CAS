package com.hypereclipse.selkie.cas;

import org.jetbrains.annotations.NotNull;

public interface Value extends Comparable
{
  public static final char[] CHARSET_BINARY = new char[]{'0', '1'};
  public static final char[] CHARSET_OCTAL = "01234567".toCharArray();
  public static final char[] CHARSET_DECIMAL = "0123456789".toCharArray();
  public static final char[] CHARSET_HEX = "0123456789ABCDEF".toCharArray();
  public static final char[] CHARSET_BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
  public static final char[] CHARSET_36 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

  public Value add(Value other);
  public Value subtract(Value other);
  public Value multiply(Value other);
  //public Value divide(Value other);
  //public Value pow(Value other);
  public Int divideInteger(Value other);
  public Value mod(Value other);
  public Value negate();
  public Value abs();
  public String exact();
  public String exact(int base, char[] charset);
  //public String approx(int sigfig);
  //public String approx(int sigfig, int base, char[] charset);

  public Boolean equals(Value other);
  //public int compareTo(Value other);
  public Boolean isNumber();
  public Boolean isPositive();
  public Boolean isZero();
  public Boolean isNegative();
  public Boolean greater(Value other);
  public Boolean less(Value other);
  public Boolean greaterEqual(Value other);
  public Boolean lessEqual(Value other);

  public int compareTo(@NotNull Object other);
}
