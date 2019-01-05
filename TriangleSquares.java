package com.hypereclipse.selkie.cas;

import com.hypereclipse.selkie.casbot.util.BotStatic;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

import static com.hypereclipse.selkie.cas.Int.ONE;
import static com.hypereclipse.selkie.cas.Int.TWO;
import static com.hypereclipse.selkie.cas.Int.ZERO;

public class TriangleSquares
{
  /*public static void main(String[] args)
  {
    Int a = ZERO;
    Int tri = ZERO;
    Int sqb = (Int)ONE.add(TWO);
    Int sq = ONE;
    int ct = 0;
    Date start = new Date();

    while (true)
    {
      //System.out.println("a:"+o(a)+" tri:"+o(tri)+ " sqb:" +o(sqb)+" sq:"+o(sq));

      int cmp = (tri.compareTo(sq));
      if (cmp == 0)
      {
        System.out.println(++ct + " - " + o(tri)+ "\t\ttdiff: "+BotStatic.tdif(start,new Date()));
        sq = (Int)sq.add(sqb);
        sqb = (Int)sqb.add(TWO);
        tri = (Int)tri.add(a);
        a = (Int)a.add(ONE);
      }
      if (cmp>0)
      {
        sq = (Int)sq.add(sqb);
        sqb = (Int)sqb.add(TWO);
      }
      else
      {
        tri = (Int)tri.add(a);
        a = (Int)a.add(ONE);
      }
    }
  }*/
  public static String o(Int v)
  {
    return v.exact(10, Int.CHARSET_DECIMAL);
  }


  public static void main(String[] args)
  {
    Int five = new Int(5);
    Date orig = new Date();
    Int val = getSqTri(300);
    /*for (int i =0; i<11; i++)
    {
      System.out.println("5^" + i + ": " + s(five.pow(i)));
      orig = new Date();
      val = getSqTri(i*100);
      System.out.print(BotStatic.tdif(orig,new Date()));
      System.out.println("\t"+(i*100) + " - " + s(val)+"\t\t"+BotStatic.tdif(orig,new Date()));
    }*/

    for (int i = 0; true; i++)
    {
      Date start = new Date();
       val = getSqTri(i);
      Date stop = new Date();

      System.out.println(i + " - " + s(val)+ "\t\tctime: "+BotStatic.tdif(start,stop)+"\t\tptime: "+BotStatic.tdif(stop,new Date())+"\t\trtime: "+BotStatic.tdif(orig,new Date()));
    }
  }

  public static double root2 = Math.sqrt(2);//(double)665857/470832
  public static double a = 3 + 2 * root2;
  public static double b = 3 - 2 * root2;
  public static double c = Math.sqrt(32);

  static Int getSqTri(int base)
  {
    Int a = new Int(17);
    Int b = new Int(288);
    Int ret = ZERO;
    for (int i = 0; i <= base; i += 2)
    {
      Int temp = (Int) a.powOld(base-i).multiply(b.powOld(i / 2)).multiply(choose(base, i));
      ret = (Int) ret.add(temp);
    }
    //
    Int ret2 = ret.subtract(ONE).divideInteger(Int.SIXTEEN);

    return ret2;
  }
  static Int choose(int set, int loc)
  {
    //       set!
    // ----------------    *(loc+1->set)/*(1,set-loc)
    //  loc!(set-loc)!

    if (set<0||loc<0||loc>set)
      return Int.ZERO;
    if (loc==0||loc==set)
      return ONE;

    Int iset = new Int(set);
    Int iloc = new Int(loc);
    Int idif = new Int(set-loc);

    Int ret = ONE;

    for (Int v = iset; v.greater(iloc); v = (Int)v.subtract(ONE))
      ret = (Int)ret.multiply(v);
    for (Int v = ONE; v.lessEqual(idif); v = (Int)v.add(ONE))
      ret = ret.divideInteger(v);

    //for (Int o = (Int)a.subtract(ONE); o.isPositive()&&!o.isZero(); o=(Int)o.subtract(ONE)) { a = (Int)a.multiply(o); }
    //for (Int o = (Int)b.subtract(ONE); o.isPositive()&&!o.isZero(); o=(Int)o.subtract(ONE)) { b = (Int)b.multiply(o); }
    //for (Int o = (Int)c.subtract(ONE); o.isPositive()&&!o.isZero(); o=(Int)o.subtract(ONE)) { c = (Int)c.multiply(o); }

    //System.out.println(set+" "+loc+" : "+s(ret));
    return ret;
  }

  static double sqrt(int num)
  {
    double root = ((double) num) / 2;
    double complement;
    do
    {
      complement = root;
      root = (complement + (num / complement)) / 2;
    }
    while ((complement - root) != 0);

    return root;
  }

  static long pow(long base, int exponent)
  {
    long val = 1;
    for (int i = 0; i < exponent; i++)
    {
      val *= base;
    }
    return val;
  }

  static String s(Value i)
  {
    return i.exact(10,Int.CHARSET_DECIMAL);
  }
}
