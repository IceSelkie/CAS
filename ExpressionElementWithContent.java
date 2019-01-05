package com.hypereclipse.selkie.cas;

public class ExpressionElementWithContent
{
  ExpressionElement type;
  StringBuilder content;

  public ExpressionElementWithContent(ExpressionElement expect, String string)
  {
    type = expect;
    content = new StringBuilder(string);
  }

  public ExpressionElementWithContent(ExpressionElement expect, StringBuilder string)
  {
    type = expect;
    content = string;
  }
}
