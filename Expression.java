package com.hypereclipse.selkie.cas;

import com.hypereclipse.selkie.casbot.util.Util;

import java.util.ArrayList;
import java.util.List;

import static com.hypereclipse.selkie.casbot.util.StringProcessing.*;
import static com.hypereclipse.selkie.casbot.util.Util.olts;

public class Expression
{
  public static final String[] operators_binary = new String[]{".", "E+", "E-", "**", "*", "/", "%", "+", "-", "|", "&", "^"};
  public static final String[] operators_unary_pre = new String[]{"++", "--", "+", "-", "!"};
  public static final String[] operators_unary_post = new String[]{"++", "--", "!"};
  public static final String[] indicator_base = new String[]{"0b", "0o", "0x"};
  public static final int[] indicator_base_value = new int[]{2, 8, 16};
  public static final String[] operators_all = new String[]{"**", "E+", "E-", "*", "/", "%", "+", "-", "|", "&", "^", "!"};
  public static final String[] grouping_open = new String[]{"(", "["};
  public static final String[] grouping_close = new String[]{")", "]"};
  public static final char[] whitespace = new char[]{' ', '\t', '\r', '\n', };//TODO: &nsp ?

  // Example: "-5 + 0x3"
  public Expression(String expression)
  {
    StringBuilder sb = sb(expression);
    ArrayList<ExpressionElementWithContent> splitExpression = new ArrayList<ExpressionElementWithContent>();

    ExpressionElementWithContent prev = null; // In some cases (life if whitespace is taking up last, or need to remember decimal/scinote)
    ExpressionElementWithContent last = null;
    boolean isWhitespace;

    while (sb.length()>0)
    {
      isWhitespace = detectAndNoteWhitespace(sb);
      List<ExpressionElement> expected = determineExpectedAndAllowed(last.type, prev.type, isWhitespace);
      splitExpression.add(readAndCutAsExpected(sb, expected));
      //combineLastTwoIfNeeded(expected.getLast);

      prev = last;
      //last = temp; // TODO: What will we fill it with?
    }
  }

  private ExpressionElementWithContent readAndCutAsExpected(StringBuilder sb, List<ExpressionElement> expected)
  {
    for (ExpressionElement expect : expected)
      if (isAt(sb, expect.getString(),0))
        return readAndCut(sb, expect);
    throw new IllegalArgumentException("Expected token not found. Expected any: "+olts(expected)+"; Found: \""+sb+"\"");
  }

  private ExpressionElementWithContent readAndCut(StringBuilder sb, ExpressionElement expect)
  {
    sb.replace(0,expect.getString().length()-1,"");
    return new ExpressionElementWithContent(expect, expect.getString());
  }

  private boolean detectAndNoteWhitespace(StringBuilder sb)
  {
    boolean ret = false;
    while (firstIndexOfAny(sb, whitespace)==0)
    {
      sb.deleteCharAt(0);
      ret = true;
    }
    return ret;
  }

  private List<ExpressionElement> determineExpectedAndAllowed(ExpressionElement last, ExpressionElement prev, boolean isWhitespace)
  {
    // Expect: opUnPre, grouping, or base/number. WS
    // If opUnPre: expect opUnPre, grouping, or base/number.
    // If groupingOpen: expect opUnPre, grouping, or base/number. WS
    // If base: expect number.
    // If number: expect number(!WS), decimal(!WS), sciNote(!WS), opUnPost, opBi, grouping. WS
    // If decimal or numDecimal: expect number(!WS), sciNote(!WS), opUnPost, opBi, grouping. WS
    // If sciNote: expect (signed)num.
    // If numSciNote: expect opUnPost, opBi, grouping. WS
    // If numWS: expect opBi, grouping
    // If opBi: expect base/number, opUnPre, grouping.
    // If opUnPost: expect opUnPost, opBi, grouping.
    // If groupingClose: expect opUnPost, opBi, grouping. WS

    List<ExpressionElement> ret = new ArrayList<>();

    if (last == null)
    {
      // Expect: opUnPre, grouping, or base/number. WS

    }
    else if (last instanceof ExpressionElement.ExpressionElementUnary.ExpressionElementUnaryPre)
    {
      // If opUnPre: expect opUnPre, grouping, or base/number.

    }
    else if (last instanceof ExpressionElement.ExpressionElementGrouping.ExpressionElementGroupingOpen)
    {
      // If groupingOpen: expect opUnPre, grouping, or base/number. WS

    }
    else if (last instanceof ExpressionElement.ExpressionElementBaseIndicator)
    {
      // If base: expect number.

      return null;//TODO replace this with <code>numeralAny;</code>
    }
    else if (last instanceof ExpressionElement.ExpressionElementNumeral)
    {
      if (prev instanceof ExpressionElement.ExpressionElementNumeralNotation.ExpressionElementNumeralNotationDecimal)
      {
        // If decimal or numDecimal: expect number(!WS), sciNote(!WS), opUnPost, opBi, grouping. WS

      }
      else if (prev instanceof ExpressionElement.ExpressionElementNumeralNotation.ExpressionElementNumeralNotationScientificNotation)
      {
        // If numSciNote: expect opUnPost, opBi, grouping. WS

      }
      else
      {
        // If number: expect number(!WS), decimal(!WS), sciNote(!WS), opUnPost, opBi, grouping. WS
        // If numWS: expect opBi, grouping

      }
    }
    else if (last instanceof ExpressionElement.ExpressionElementNumeralNotation.ExpressionElementNumeralNotationDecimal)
    {
      // If decimal or numDecimal: expect number(!WS), sciNote(!WS), opUnPost, opBi, grouping. WS

    }
    else if (last instanceof ExpressionElement.ExpressionElementNumeralNotation.ExpressionElementNumeralNotationScientificNotation)
    {
      // If sciNote: expect (signed)num.

    }
    else if (last instanceof ExpressionElement.ExpressionElementBinaryOperator)
    {
      // If opBi: expect base/number, opUnPre, grouping.

    }
    else if (last instanceof ExpressionElement.ExpressionElementUnary.ExpressionElementUnaryPost)
    {
      // If opUnPost: expect opUnPost, opBi, grouping.

    }
    else if (last instanceof ExpressionElement.ExpressionElementGrouping.ExpressionElementGroupingClose)
    {
      // If groupingClose: expect opUnPost, opBi, grouping. WS

    }
    else
      throw new IllegalArgumentException("Unable to interpret value and predict next element.");
    return ret;
  }
}

// Number is value/expression (decimal/scinote is part of number)
// Grouping is expression
// Operator is function