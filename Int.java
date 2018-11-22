package com.hypereclipse.selkie.cas;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;

import static com.hypereclipse.selkie.casbot.util.BotStatic.ats;
import static com.hypereclipse.selkie.casbot.util.BotStatic.atsr;

public class Int implements Value
{
  public static final Int NEGATIVEONE = new Int(-1);
  public static final Int ZERO = new Int(0);
  public static final Int ONE = new Int(1);
  public static final Int TWO = new Int(2);
  public static final Int SIXTEEN = new Int(16);
  public static final Int INTMIN = new Int(Integer.MIN_VALUE);
  public static final Int INTMAX = new Int(Integer.MAX_VALUE);
  public static final Int LONGMIN = new Int(Long.MIN_VALUE);
  public static final Int LONGMAX = new Int(Long.MAX_VALUE);


  // SmallEndian Hex Int
  boolean negative;
  int[] value; //Hex: 0-F

  public Int(long value)
  {
    this.value = new int[((Long) value).toString().length()];
    if (value < 0)
    {
      negative = true;
      value = -value;
    }
    int i = 0;
    while (value > 0)
    {
      this.value[i++] = (int) (value % 0x10);
      value /= 0x10;
    }
  }

  private Int(int[] value, boolean negative)
  {
    this.value = value;
    this.negative = negative;
  }

  public Value add(Value other)
  {
    if (other instanceof Int)
    {
      Int o = (Int) other;

      if (o.isZero()) return this;
      if (isZero()) return o;
      if (o.isNegative()) return subtract(o.negate());
      if (isNegative()) return o.subtract(this.negate());

      int lenMax = value.length < o.value.length ? o.value.length + 1 : value.length + 1;
      int[] ret = new int[lenMax];
      boolean carry = false;
      for (int i = 0; i < lenMax; i++)
      {
        ret[i] = get(i) + o.get(i) + (carry ? 1 : 0);
        if (ret[i] > 0xF)
        {
          ret[i] -= 0x10;
          carry = true;
        }
        else
          carry = false;
      }
      return new Int(ret, false);
    }
    else
      return other.add(this);
  }

  public Value subtract(Value other)
  {
    if (other instanceof Int)
    {
      Int o = (Int) other;

      if (o.isZero()) return this;
      if (isZero()) return o.negate();
      if (equals(o)) return new Int(new int[0], false);
      if (o.isNegative()) return add(o.negate());
      if (isNegative()) return o.add(this.negate());

      if (this.less(o))
        return o.subtract(this).negate();

      // This must be greater than o now.

      int lenMax = value.length < o.value.length ? o.value.length + 1 : value.length + 1;
      int[] ret = new int[lenMax];
      int carry = 0;

      for (int i = 0; i < lenMax; i++)
      {
        ret[i] = get(i) - o.get(i) - carry;
        carry = 0;
        while (ret[i] < 0x0)
        {
          ret[i] += 0x10;
          carry++;
        }
      }
      if (carry > 0)
        throw new IllegalArgumentException("Carrying a negative number beyond its bound. See Int#subtract(Int). "+atsr(this.value)+" "+atsr(o.value));
      return new Int(ret, false);
    }
    return other.add(this);
  }

  HashMap<Int, HashMap<Int,Int>> precalculated = new HashMap<>();
  public Value multiply(Value other)
  {
    if (other instanceof Int)
    {
      Int o = (Int) other;

      clean();
      o.clean();

      if (precalculated.containsKey(this) && precalculated.get(this).containsKey(o))
        return precalculated.get(this).get(o);

      if (o.value.length < value.length)
        return o.multiply(this);

      int lenMax = value.length + o.value.length;

      Int ret = ZERO;
      for (int i = 0; i < value.length; i++)
        ret = (Int) (ret.add(o.multiply(value[i]).shift(4*i)));

      ret = (Int)ret.abs();
      if (isNegative()^o.isNegative())
        ret = (Int)ret.negate();

      if (!precalculated.containsKey(this))
        precalculated.put(this,new HashMap<>());
      precalculated.get(this).put(o,ret);

      return ret;
    }
    else
      return other.multiply(this);
  }

  public Int divideInteger(Value other)
  {
    if (other instanceof Int)
    {
      Int o = (Int) other;
      if (isNegative())
        if (o.isNegative())
          return negate().divideInteger(o.negate());
        else
          return (Int) negate().divideInteger(o).negate();
      if (o.isNegative())
        return (Int) divideInteger(o.negate()).negate();

      if (this.isZero()) return this;
      if (o.isZero()) throw new IllegalArgumentException("Divide by zero.");
      if (this.less(o))
        return ZERO;

      clean();
      o.clean();

      //     ret
      //  o / this

      int[] ret = new int[this.value.length];
      int indecieOfCompare = value.length-o.value.length;
      Int temp = this;
      while (temp.greaterEqual(o))
      {
        //temp.clean();
        //System.out.println(indecieOfCompare+" "+atsr(ret)+" "+atsr(temp.value)+" "+atsr(o.value));
        // If its divisable at this point, do so.
        while (temp.shift(-4*indecieOfCompare).greaterEqual(o))
        {
          ret[indecieOfCompare]++;
          temp = (Int)temp.subtract(o.shift(4*indecieOfCompare));

          //temp.clean();
          //System.out.println(indecieOfCompare+" "+atsr(ret)+" "+atsr(temp.value)+" "+atsr(o.value));
        }
        indecieOfCompare--;
      }

      return new Int(ret,false);

    }
    else
      throw new IllegalArgumentException("That isnt implemented yet!");
  }

  public Value mod(Value other)
  {
    return subtract(divideInteger(other).multiply(other));
  }

  private Int multiply(int coeff)
  {
    if (coeff < 0 || coeff > 0xF)
      throw new IllegalArgumentException("factor invalid");
    if (coeff == 0)
      return ZERO;
    if (coeff == 1)
      return this;

    int[] ret = new int[value.length + 1];
    int carry = 0;
    for (int i = 0; i < ret.length; i++)
    {
      ret[i] = coeff * get(i) + carry;
      carry = ret[i] / 0x10;
      ret[i] %= 0x10;
    }
    return new Int(ret, isNegative());
  }

  public Value negate()
  {
    return new Int(value, isZero() ? false : !negative);
  }

  public Value abs()
  {
    return isNegative() ? new Int(value, false) : this;
  }

  public String exact()
  {
    clean();
    StringBuilder sb = new StringBuilder(value.length);
    for (int i = value.length - 1; i >= 0; i--)
      if (value[i] > 9) sb.append((char) ('A' + value[i] - 0xA));
      else sb.append(value[i]);
    if (isZero()) sb.append('0');
    return (isNegative() ? "-0x" : "0x") + sb.toString();
  }

  public static final char[] CHARSET_BINARY = new char[]{'0', '1'};
  public static final char[] CHARSET_OCTAL = "01234567".toCharArray();
  public static final char[] CHARSET_DECIMAL = "0123456789".toCharArray();
  public static final char[] CHARSET_HEX = "0123456789ABCDEF".toCharArray();
  public static final char[] CHARSET_BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
  public static final char[] CHARSET_36 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

  public String exact(int base, char[] charset)
  {
    if (base == 0x10 && (charset == null || charset.equals(CHARSET_HEX)))
      return exact();
    if (charset.length < base)
      throw new IllegalArgumentException("There must be enough characters in the charset for each value in that base!");
    StringBuilder sb = new StringBuilder((value.length * 0x10) / base + 5);
    String prefix = "";
    if (isNegative()) {prefix = "-"; }
    if (base == 0x2) prefix += "0b";
    if (base == 0x8) prefix += "0o";
    if (base == 0x10) prefix += "0x";
    if (base != 0x10 && base != 10 && base != 0x8 && base != 0x2) prefix += "(b-" + base + ")";

    Int baseI = new Int(base);
    Int temp = (Int) this.abs();

    if (temp.isZero())
      sb.append('0');
    else
      while (!temp.isZero())
      {
        Int val = temp.divideInteger(baseI);
        Int indexI = (Int) temp.subtract(val.multiply(baseI));
        int index = indexI.toInt();
        sb.append(index < charset.length ? charset[index] : '#');
        temp = val;
      }
    return prefix + sb.reverse().toString();
  }

  public Boolean equals(Value other)
  {
    if (other instanceof Int)
    {
      Int o = (Int) other;

      if (isPositive() ^ isPositive())
        return (isZero() && o.isZero());

      clean();
      o.clean();
      if (value.length - o.value.length!=0)
        return false;

      for (int i = value.length - 1; i >= 0; i--)
      {
        if (get(i) != o.get(i)) return false;
      }
      return true;
    }
    else
      return other.equals(this);
  }

  public Boolean isNumber()
  {
    return true;
  }

  public Boolean isPositive()
  {
    return isZero() || !negative;
  }

  public Boolean isZero()
  {
    clean();
    return value.length == 0 || (value.length == 1 && value[0] == 0);
  }

  public Boolean isNegative()
  {
    return !isZero() && negative;
  }

  public Boolean greater(Value other)
  {
    if (other instanceof Int)
    {
      Int o = (Int) other;

      if (isPositive() && o.isNegative())
        return true;
      if (isNegative() && o.isPositive())
        return false;

      boolean ret = isPositive();
      this.clean();
      o.clean();
      if (value.length - o.value.length != 0)
        return value.length > o.value.length ? ret : !ret;

      for (int i = value.length - 1; i >= 0; i--)
      {
        if (get(i) > o.get(i)) return ret;
        if (get(i) < o.get(i)) return !ret;
      }
      return false;
    }
    else
      return !equals(other) && other.less(this);
  }

  public Boolean less(Value other)
  {
    return !equals(other) && !greater(other);
  }

  public Boolean greaterEqual(Value other)
  {
    return greater(other) || equals(other);
  }

  public Boolean lessEqual(Value other)
  {
    return !greater(other);
  }



  public Int shift(int qty)
  {
    if (qty % 4 == 0)
    {
      qty /= 4;
      if (value.length + qty <= 0)
        return ZERO;
      int[] ret = new int[value.length + qty];
      if (qty > 0)
        System.arraycopy(value, 0, ret, qty, value.length);
      else
      {
        //System.out.println("System.arraycopy("+value.length+", "+qty+", "+ret.length+", 0, "+(value.length - qty)+");");
        System.arraycopy(value, -qty, ret, 0, value.length + qty);
      }
      return new Int(ret, negative);
    }
    throw new IllegalArgumentException("Shifting by non-4 not implemented yet!");
  }
  public Int pow(int exp)
  {
    if (exp<0)
      throw new IllegalArgumentException("Negative exponents are not supported yet. They will be once quotients are added.");
    if (exp==0)
      return ONE;
    if (exp==1)
      return this;
    if (exp%2==0)
      return ((Int)this.multiply(this)).pow(exp/2);
    else
      return (Int)this.multiply(((Int)this.multiply(this)).pow(exp/2));
  }
  public Int powOld(int exp)
  {
    if (exp < 0)
      throw new IllegalArgumentException("Negative exponents are not supported yet. They will be once quotients are added.");
    Int ret = ONE;
    for (int i = 0; i < exp; i++)
      ret = (Int) ret.multiply(this);
    return ret;
  }




  private int get(int index)
  {
    if (index < 0 || index >= value.length) return 0;
    return value[index];
  }

  private boolean isClean = false;

  private void clean()
  {
    if (isClean) return;
    int i = value.length;
    while (get(--i) == 0 && i>=0) ;
    int[] ret = new int[++i];
    System.arraycopy(value, 0, ret, 0, i);
    value = ret;
    isClean = true;
  }

  public Integer toInt()
  {
    if (this.lessEqual(INTMAX)&&this.greaterEqual(INTMIN))
    {
      int ret = 0;
      int pow = 1;
      for (int i =0; i<value.length; i++)
      {
        ret += value[i]*pow;
        pow*=16;
      }
      return ret;
    }
    else
      return null;
  }

  public Long toLong()
  {
    if (this.lessEqual(LONGMAX)&&this.greaterEqual(LONGMIN))
    {
      long ret = 0;
      long pow = 1;
      for (int i =0; i<value.length; i++)
      {
        ret += value[i]*pow;
        pow*=16;
      }
      return ret;
    }
    else
      return null;
  }

  public BigInteger toBigInt()
  {
    return new BigInteger(toString());
  }

  public int compareTo(@NotNull Object other)
  {
    if (other instanceof Int)
    {
      Int o = (Int) other;
      Int ret = (Int)subtract(o);
      if (ret.isZero()) return 0;
      return ret.isPositive()?1:-1;
    }
    throw new IllegalArgumentException();
  }

  public String toString()
  {
    return exact();
  }

  public int hashCode()
  {
    clean();
    return Arrays.hashCode(value)^(negative?0xFFFF:0);
  }
}
