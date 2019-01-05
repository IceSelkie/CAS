package com.hypereclipse.selkie.cas;

public class ExpressionElement
{
  public final String value;

  public String getString()
  {
    return value;
  }

  public boolean equals(Object obj)
  {
    if (! (obj instanceof ExpressionElement))
      return false;
    ExpressionElement ee = (ExpressionElement)obj;
    return (ee.getClass().equals(this.getClass())&&ee.value.equals(value)); //TODO what if they are the same but different type? Ie: negate and subtract
  }

  public ExpressionElement(String value)
  {
    this.value = value;
  }

  public class ExpressionElementNumeral extends ExpressionElement
  {
    public ExpressionElementNumeral(String value) {super(value);}
  }

  public class ExpressionElementNumeralNotation extends ExpressionElement
  {
    public ExpressionElementNumeralNotation(String value) {super(value);}

    public class ExpressionElementNumeralNotationDecimal extends ExpressionElementNumeralNotation
    {
      public ExpressionElementNumeralNotationDecimal(String value) {super(value);}
    }
    public class ExpressionElementNumeralNotationScientificNotation extends ExpressionElementNumeralNotation
    {
      public ExpressionElementNumeralNotationScientificNotation(String value) {super(value);}
    }
  }

  public class ExpressionElementBaseIndicator extends ExpressionElement
  {
    public ExpressionElementBaseIndicator(String value) {super(value);}
  }

  public class ExpressionElementBinaryOperator extends ExpressionElement
  {
    public ExpressionElementBinaryOperator(String value) {super(value);}
  }

  public class ExpressionElementUnary extends ExpressionElement
  {
    public ExpressionElementUnary(String value) {super(value);}

    public class ExpressionElementUnaryPre extends ExpressionElementUnary
    {
      public ExpressionElementUnaryPre(String value) {super(value);}
    }
    public class ExpressionElementUnaryPost extends ExpressionElementUnary
    {
      public ExpressionElementUnaryPost(String value) {super(value);}
    }
  }

  public class ExpressionElementGrouping extends ExpressionElement
  {
    public ExpressionElementGrouping(String value) {super(value);}

    public class ExpressionElementGroupingOpen extends ExpressionElementGrouping
    {
      public ExpressionElementGroupingOpen(String value) {super(value);}
    }
    public class ExpressionElementGroupingClose extends ExpressionElementGrouping
    {
      public ExpressionElementGroupingClose(String value) {super(value);}
    }
  }
}
