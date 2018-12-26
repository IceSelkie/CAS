package com.hypereclipse.selkie.cas;

import com.hypereclipse.selkie.casbot.util.StringProcessing;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;

import static com.hypereclipse.selkie.casbot.util.BotStatic.atsr;



/**
 * class Int (extends Value)
 *
 * Represents an integer value in a little-endian array of hexadecimal values.
 * Lowest level representation of a value.
 *
 * All basic math operations can be applied.
 *
 * @author Selkie (Stanley S.)
 * @version 1.1
 * @since 1.0
 */
public class Int implements Value
{
  /**
   * A public constant representing the commonly used value of -1.
   *
   * @since 1.0
   */
  public static final Int NEGATIVEONE = new Int(-1).clean();
  /**
   * A public constant representing the commonly used value of 0.
   *
   * @since 1.0
   */
  public static final Int ZERO = new Int(0).clean();
  /**
   * A public constant representing the commonly used value of 1.
   *
   * @since 1.0
   */
  public static final Int ONE = new Int(1).clean();
  /**
   * A public constant representing the commonly used value of 2.
   *
   * @since 1.0
   */
  public static final Int TWO = new Int(2).clean();
  /**
   * A public constant representing the value 10.
   *
   * @since 1.0
   */
  public static final Int TEN = new Int(10).clean();
  /**
   * A public constant representing the commonly used value of 16.
   *
   * @since 1.0
   */
  public static final Int SIXTEEN = new Int(16).clean();
  /**
   * A public constant representing the maximum value that an integer can hold.
   *
   * @since 1.0
   */
  public static final Int INTMIN = new Int(Integer.MIN_VALUE).clean();
  /**
   * A public constant representing the minimum value that an integer can hold.
   *
   * @since 1.0
   */
  public static final Int INTMAX = new Int(Integer.MAX_VALUE).clean();
  /**
   * A public constant representing the maximum value that a long can hold.
   *
   * @since 1.0
   */
  public static final Int LONGMIN = new Int(Long.MIN_VALUE).clean();
  /**
   * A public constant representing the minimum value that a long can hold.
   *
   * @since 1.0
   */
  public static final Int LONGMAX = new Int(Long.MAX_VALUE).clean();


  /**
   * <code>true</code> if negative. <code>false</code> otherwise.
   *
   * @since 1.0
   */
  private boolean negative;
  /**
   * Represents the infinite precision integer in a little-endian array of hexadecimal values.
   * Value at the 0th index is the 0x1s place, and values in the 1st index are the 0x10s place, etc.
   * Note: It MAY be padded with zeros. See: {@link com.hypereclipse.selkie.cas.Int#isClean}.
   *
   * @since 1.0
   */
  private int[] value; //Hex: 0-F
  /**
   * Represents if the {@link com.hypereclipse.selkie.cas.Int#value} is "clean".
   * <code>true</code> asserts that the array has no extraneous tailing zeros.
   * Ex: 0x0010 would be <code>false</code>.
   * Ex: 0x10 should be <code>true</code>, but may not be if {@link com.hypereclipse.selkie.cas.Int#clean()} has not been called.
   *
   * @since 1.0
   */
  private boolean isClean = false;
  /**
   * A hashmap to store products once they are calculated so they don't need to be recalculated, since multiplication is decently expensive.
   *
   * @since 1.0
   */
  private static HashMap<Int, HashMap<Int,Int>> precalculatedMultiplications = new HashMap<>();
  // Should one be added for division/modulus? Probably.



  /**
   * Constructor for {@link com.hypereclipse.selkie.cas.Int}.
   *
   * Creates a new {@link com.hypereclipse.selkie.cas.Int} object from the given primitive value.
   *
   * @param value The input integer primitive to convert to an {@link com.hypereclipse.selkie.cas.Int}.
   * @since 1.0
   */
  public Int(long value)
  {
    // Deal with negatives
    if (value < 0)
    {
      negative = true;
      value = -value;
    }

    // Determine the length of the array by using the number of digits in the number in base ten.
    this.value = new int[((Long) value).toString().length()];

    // Fill in the digits in the value array.
    for (int i = 0; value > 0; i++)
    {
      this.value[i] = (int) (value % 0x10);
      value /= 0x10;
    }
  }

  /**
   * Private constructor for {@link com.hypereclipse.selkie.cas.Int}.
   *
   * This constructor is used internally by {@link com.hypereclipse.selkie.cas.Int} to create new {@link com.hypereclipse.selkie.cas.Int} objects given an existing set of instance variables.
   * Requires that the {@link int[]} will never be modified after the object is created, and thus is private.
   *
   * @param value The existing array of digits for a new {@link com.hypereclipse.selkie.cas.Int} object.
   * @param negative Whether the new {@link com.hypereclipse.selkie.cas.Int} is negative or not.
   * @since 1.0
   */
  private Int(int[] value, boolean negative)
  {
    this.value = value;
    this.negative = negative;
  }

  /**
   * Protected constructor for {@link com.hypereclipse.selkie.cas.Int}.
   *
   * Duplicates an existing {@link com.hypereclipse.selkie.cas.Int}.
   *
   * Used by {@link com.hypereclipse.selkie.cas.Prime} to create a new {@link com.hypereclipse.selkie.cas.Prime} object without needing an array copy.
   *
   * @param val The {@link com.hypereclipse.selkie.cas.Int} object to duplicate.
   * @since 1.1
   */
  protected Int(Int val)
  {
    this.value = val.value;
    this.negative = val.negative;
  }

  /**
   *
   *
   * @param integerString
   * @param base
   * @return
   */
  public static Int create(String integerString, int base)
  {
    char[] baseCharset = CHARSET_36; // If 62 or less
    if(base == 2)
      baseCharset = CHARSET_BINARY;
    if(base == 8)
      baseCharset = CHARSET_OCTAL;
    if(base == 10)
      baseCharset = CHARSET_DECIMAL;
    if(base == 16)
      baseCharset = CHARSET_HEX;
    if(base == 64)
      baseCharset = CHARSET_BASE64;
    if (base == 63 || base > 64)
      throw new IllegalArgumentException("That base is not supported!");

    boolean isPosative = true;
    if (integerString.charAt(0) == '-' || integerString.charAt(0) == '+')
    {
      if (integerString.charAt(0) == '-')
        isPosative = false;
      integerString = integerString.substring(1);
    }

    Int baseInt = new Int(base);
    Int ret = ZERO;
    for (char c : integerString.toCharArray())
    {
      int value = StringProcessing.indexOf(baseCharset, c);
      ret = (Int)(ret.multiply(baseInt).add(new Int(value)));
    }
    if (!isPosative)
      ret = (Int)ret.negate();
    return ret;
  }



  /**
   * Adds a {@link com.hypereclipse.selkie.cas.Value} to this {@link com.hypereclipse.selkie.cas.Int}.
   *
   * Since {@link com.hypereclipse.selkie.cas.Int} is the lowest level representation of a value, if the <code>other</code> value is anything other than an {@link com.hypereclipse.selkie.cas.Int}, it's {@link com.hypereclipse.selkie.cas.Value#add(Value)} will be called instead.
   *
   * If the <code>other</code> value is an {@link com.hypereclipse.selkie.cas.Int}, it will add them together and create a new {@link com.hypereclipse.selkie.cas.Int} to represent them.
   *
   * @param other The other value to be added to this one.
   * @return A new {@link com.hypereclipse.selkie.cas.Value} representing the added value. If the <code>other</code> value is an {@link com.hypereclipse.selkie.cas.Int}, then it will return a new {@link com.hypereclipse.selkie.cas.Int} object with the added value.
   * @since 1.0
   */
  public Value add(Value other)
  {
    // We only want to process it if we know how to (when its also an Int). Otherwise, we'll have their class process it.
    if (!(other instanceof Int))
      return other.add(this);

    Int o = (Int)other;

    // Simple cases that require no processing
    if (o.isZero()) return this;
    if (isZero()) return o;
    // Special cases that require some processing before we can begin.
    if (o.isNegative()) return subtract(o.negate());
    if (isNegative()) return o.subtract(this.negate());

    // Determine the new Int's length. The longer length + 1 should be safe. (Incase of a carry)
    int lenMax = value.length < o.value.length ? o.value.length + 1 : value.length + 1;
    int[] ret = new int[lenMax];

    // Boolean to hold if there is a sixteen to carry.
    boolean carry = false;

    // For each digit, ...
    for (int i = 0; i < lenMax; i++)
    {
      // ... add each value together (and add one if carry), ...
      ret[i] = get(i) + o.get(i) + (carry ? 1 : 0);

      // ... and if its too large, carry.
      carry = false;
      if (ret[i] > 0xF)
      {
        ret[i] -= 0x10;
        carry = true;

        if (ret[i]>0xF)
          throw new IllegalStateException("Carrying a number does not solve out of bounds error. See Int#add(Int). " + atsr(this.value) + " " + atsr(o.value));
      }
    }
    // If there is another carry we haven't dealt with... Then something is broken and it needs to be fixed.
    if (carry)
      throw new IllegalStateException("Carrying a number beyond its bound. See Int#add(Int). " + atsr(this.value) + " " + atsr(o.value));
    return new Int(ret, false);
  }

  /**
   * Subtracts a {@link com.hypereclipse.selkie.cas.Value} from this {@link com.hypereclipse.selkie.cas.Int}.
   *
   * Since {@link com.hypereclipse.selkie.cas.Int} is the lowest level representation of a value, if the <code>other</code> value is anything other than an {@link com.hypereclipse.selkie.cas.Int}, it's {@link com.hypereclipse.selkie.cas.Value#subtract(Value)} will be called instead.
   *
   * If the <code>other</code> value is an {@link com.hypereclipse.selkie.cas.Int}, it will subtract it from this and create a new {@link com.hypereclipse.selkie.cas.Int} to represent them.
   *
   * @param other The other value to be subtracted from this one.
   * @return A new {@link com.hypereclipse.selkie.cas.Value} representing the subtracted value. If the <code>other</code> value is an {@link com.hypereclipse.selkie.cas.Int}, then it will return a new {@link com.hypereclipse.selkie.cas.Int} object with the subtracted value.
   * @since 1.0
   */
  public Value subtract(Value other)
  {
    // We only want to process it if we know how to (when its also an Int). Otherwise, we'll have their class process it.
    if (!(other instanceof Int))
      other.negate().add(this);

    Int o = (Int)other;

    // Simple cases that require no processing
    if (o.isZero()) return this;
    if (isZero()) return o.negate();
    if (equals(o)) return ZERO;
    // Special cases that require some processing before we can begin.
    if (o.isNegative()) return add(o.negate());
    if (isNegative()) return o.negate().add(this.negate());

    // We want our answer that we generate here to be positive, so should the number we are subtracting be larger, flip it so that cant happen.
    if (this.less(o))
      return o.subtract(this).negate();

    // ASSERTION: <code>this</code> must be greater than <code>o</code> now.

    // The new Int's length will be at most this Int's length, since this is greater than o. (Not adding, so the new value's magnitude wont be larger than the largest existing one)
    int lenMax = value.length;
    int[] ret = new int[lenMax];

    // The value being carried up (cuts out of the next digit)
    // should the digit being subtracted be larger than what its being subtracted from, that's a problem since our {@link #value} only holds positive values. We must carry "take" a 0x10 out of the next digit, so we can stay positive. This is our "carry".
    boolean carry = false;

    // For each digit, ...
    for (int i = 0; i < lenMax; i++)
    {
      // ... subtract the values (and a few more if needed by the carry), ...
      ret[i] = get(i) - o.get(i) - (carry ? 1 : 0);

      // ... and if its too small add 0x10 so its not and carry that to the next digit.
      carry = false;
      if (ret[i] < 0x0)
      {
        ret[i] += 0x10;
        carry = true;
        if (ret[i]<0x0)
          throw new IllegalStateException("Carrying a negative number does not solve negative. See Int#subtract(Int). " + atsr(this.value) + " " + atsr(o.value));
      }
    }
    // If there is another carry we haven't dealt with... Then something is broken and it needs to be fixed.
    if (carry)
      throw new IllegalStateException("Carrying a negative number beyond its bound. See Int#subtract(Int). " + atsr(this.value) + " " + atsr(o.value));
    return new Int(ret, false);
  }

  /**
   * Multiplies a {@link com.hypereclipse.selkie.cas.Value} by this {@link com.hypereclipse.selkie.cas.Int}.
   *
   * Since {@link com.hypereclipse.selkie.cas.Int} is the lowest level representation of a value, if the <code>other</code> value is anything other than an {@link com.hypereclipse.selkie.cas.Int}, it's {@link com.hypereclipse.selkie.cas.Value#multiply(Value)} will be called instead.
   *
   * If the <code>other</code> value is an {@link com.hypereclipse.selkie.cas.Int}, the product between the two will be taken and a new {@link com.hypereclipse.selkie.cas.Int} will be created to represent them.
   *
   * @param other The other value to be multiplied with this one.
   * @return A new {@link com.hypereclipse.selkie.cas.Value} representing the product. If the <code>other</code> value is an {@link com.hypereclipse.selkie.cas.Int}, then it will then return a new {@link com.hypereclipse.selkie.cas.Int} object representing the product.
   * @since 1.0
   */
  public Value multiply(Value other)
  {
    // We only want to process it if we know how to (when its also an Int). Otherwise, we'll have their class process it.
    if (!(other instanceof Int))
      return other.multiply(this);

    Int o = (Int)other;

    // Simple cases that require no processing
    // {@link #isZero()} calls {@link #clean()} in them, so this cleans them for us, which we need so they have the correct hash and can be used in the hashmap lookup.
    if (isZero() || o.isZero()) return ZERO;
    // Squaring is more efficient than long multiplication, so lets do that instead.
    //if (equals(o)) return square();

    // If we've done the math before, no need to do it again. Just look up last time's answer.
    if (precalculatedMultiplications.containsKey(this) && precalculatedMultiplications.get(this).containsKey(o))
      return precalculatedMultiplications.get(this).get(o);
    if (precalculatedMultiplications.containsKey(o) && precalculatedMultiplications.get(o).containsKey(this))
      return precalculatedMultiplications.get(o).get(this);

    // We want this's length shorter than o's, so the multiplication is done in less steps.
    if (o.value.length < value.length)
      return o.multiply(this);

    // Store if the result is negative, so we don't have to deal with negatives in the actual multiplication.
    boolean negative = isNegative() ^ o.isNegative();
    o = (Int)o.abs();
    // We only look at the digits from this, and the whole number of other, so we don't need to reset the sign of this.

    // Long multiplication: Multiply the longer value by each digit in the smaller one shifted over by its place, and add them all together.
    Int ret = ZERO;
    for (int i = 0; i < value.length; i++)
      ret = (Int)(ret.add(o.multiply(value[i]).shift(4 * i)));

    // Set the final sign of the product. And <code>ret</code> should always be positive to begin with, since o is set to positive and this's digits are all always positive.
    if (negative)
      ret = (Int)ret.negate();

    // Save this to the hashmap of previous multiplications to save us the work, should we be asked the same question again.
    if (!precalculatedMultiplications.containsKey(this))
      precalculatedMultiplications.put(this, new HashMap<>());
    precalculatedMultiplications.get(this).put(o, ret);

    return ret;
  }

  /**
   * Divides this {@link com.hypereclipse.selkie.cas.Int} by a {@link com.hypereclipse.selkie.cas.Value} using integer division.
   *
   * Since {@link com.hypereclipse.selkie.cas.Int} is the lowest level representation of a value, if the <code>other</code> value is anything other than an {@link com.hypereclipse.selkie.cas.Int}, it'll throw an {@link java.lang.IllegalArgumentException} because we don't know how to deal with that yet.
   *
   * If the <code>other</code> value is an {@link com.hypereclipse.selkie.cas.Int}, the quotient between the two will be taken and a new {@link com.hypereclipse.selkie.cas.Int} will be created to represent them.
   *
   * This method uses long division to
   *
   * @param other The other value to divided this by.
   * @return A new {@link com.hypereclipse.selkie.cas.Value} representing the product. If the <code>other</code> value is an {@link com.hypereclipse.selkie.cas.Int}, then it will then return a new {@link com.hypereclipse.selkie.cas.Int} object representing the product.
   * @since 1.0
   */
  public Int divideInteger(Value other)
  {
    // We only want to process it if we know how to (when its also an Int). Otherwise, we'll have their class process it.
    if (!(other instanceof Int))
      // TODO: We need some other way to take care of integer division, if the value we are dividing by is not an integer.
      throw new IllegalArgumentException("That isnt implemented yet!");

    Int o = (Int)other;

    // Simple cases that require no processing
    // checks isZero on both, which cleans for us.
    if (this.isZero()) return this;
    // Can't divide by zero. Yet.
    if (o.isZero()) throw new IllegalArgumentException("Divide by zero.");
    if (o.equals(ONE)) return this;

    // Special cases that require some processing before we can begin.
    // Take care of negatives
    if (this.isNegative())
      if (o.isNegative())
        return negate().divideInteger(o.negate());
      else
        return (Int)negate().divideInteger(o).negate();
    if (o.isNegative())
      return (Int)divideInteger(o.negate()).negate();

    // Obvious zero case
    if (this.less(o))
      return ZERO;

    // We are doing long division, and if it were on paper, it would look something like this:
    //     ret
    //  o / this

    // The length will be less than the original number.
    int[] ret = new int[this.value.length];
    int indecieOfCompare = value.length - o.value.length;
    Int temp = this;
    while (temp.greaterEqual(o))
    {
      //temp.clean();
      //System.out.println(indecieOfCompare+" "+atsr(ret)+" "+atsr(temp.value)+" "+atsr(o.value));
      // If its divisable at this point, do so.
      while (temp.shift(-4 * indecieOfCompare).greaterEqual(o))
      {
        ret[indecieOfCompare]++;
        temp = (Int)temp.subtract(o.shift(4 * indecieOfCompare));

        //temp.clean();
        //System.out.println(indecieOfCompare+" "+atsr(ret)+" "+atsr(temp.value)+" "+atsr(o.value));
      }
      indecieOfCompare--;
    }

    return new Int(ret, false);
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

  private Int clean()
  {
    if (isClean) return this;
    int i = value.length;
    while (get(--i) == 0 && i>=0) ;
    int[] ret = new int[++i];
    System.arraycopy(value, 0, ret, 0, i);
    value = ret;
    isClean = true;
    return this;
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
