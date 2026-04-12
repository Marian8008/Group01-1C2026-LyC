package lyc.compiler;

import java_cup.runtime.Symbol;
import lyc.compiler.ParserSym;
import lyc.compiler.model.*;
import static lyc.compiler.constants.Constants.*;

%%

%public
%class Lexer
%unicode
%cup
%line
%column
%throws CompilerException
%eofval{
  return symbol(ParserSym.EOF);
%eofval}


%{
  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
%}


LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
Identation =  [ \t\f]

Plus = "+"
Mult = "*"
Sub = "-"
Div = "/"
Assig = ":="
Colon = ":"
Comma = ","
OpenBracket = "("
CloseBracket = ")"
OpenCurlyBracket = "{"
CloseCurlyBracket = "}"
GreaterThan = ">"
LessThan = "<"
GreaterThanOrEqual = ">="
LessThanOrEqual = "<="
Equal = "=="
NotEqual = "!="

Letter = [a-zA-Z]
Digit = [0-9]

WhiteSpace = ({LineTerminator} | {Identation})+

// Cualquier caracter menos salto de linea (comments de una linea) o ningun caracter
CommentCharacters = ([^\r\n])*

Comment = "#+" {CommentCharacters} "+#"
Identifier = {Letter} ({Letter}|{Digit})*

IntegerConstant = {Digit}+
FloatConstant =  {Digit}+"."{Digit}+ | "."{Digit}+ | {Digit}+"."
StringConstant = "\"" ([^\"])* "\""

/* palabras reservadas */
Int = "Int"
Float = "Float"
String = "String"
Init = "init"
And = "AND"
Or = "OR"
Not = "NOT"
Read = "read"
Write = "write"
If = "if"
Else = "else"
While = "while"
%%


/* keywords */

<YYINITIAL> {
  /* los token se reconocen en el orden en el que se escriben aca, primero en la lista tiene mas prioridad */
  /* palabras reservadas */
  {Write}                                  { return symbol(ParserSym.WRITE); }
  {Init}                                   { return symbol(ParserSym.INIT); }
  {And}                                    { return symbol(ParserSym.AND); }
  {Or}                                     { return symbol(ParserSym.OR); }
  {Not}                                    { return symbol(ParserSym.NOT); }
  {Read}                                   { return symbol(ParserSym.READ); }
  {If}                                     { return symbol(ParserSym.IF); }
  {Else}                                   { return symbol(ParserSym.ELSE); }
  {While}                                  { return symbol(ParserSym.WHILE); }
  /* tipos de dato */
  {Int}                                   { return symbol(ParserSym.INT); }
  {Float}                                 { return symbol(ParserSym.FLOAT); }
  {String}                                { return symbol(ParserSym.STRING); }
  /* identifiers */
  {Identifier}                             {
      String value = yytext();
      if (value.length() > MAX_LENGTH) {
          throw new InvalidLengthException("La longitud del ID "+value +" supera lo permitido.");
      }
      return symbol(ParserSym.IDENTIFIER, value);
  }
  /* Constants */
  {FloatConstant}                          {
      String value = yytext();
      try {
          float num = Float.parseFloat(value);
          if (num < MIN_FLOAT || num > MAX_FLOAT) {
              throw new InvalidFloatException("El valor FLOAT "+value +" supera lo permitido.");
          }
          return symbol(ParserSym.FLOAT_CONSTANT, value);
      } catch (NumberFormatException e) {
          throw new InvalidFloatException("El valor FLOAT "+value +" es invalido.");
      }
  }
  {IntegerConstant}                        {
      String value = yytext();
      try {
          int num = Integer.parseInt(value);
          if (num < MIN_INT || num > MAX_INT) {
              throw new InvalidIntegerException("El valor INT "+value +" supera lo permitido.");
          }
          return symbol(ParserSym.INTEGER_CONSTANT, value);
      } catch (NumberFormatException e) {
          throw new InvalidIntegerException("El valor INT "+value +" es invalido.");
      }
  }
  {StringConstant}                         {
      String value = yytext().substring(1, yytext().length() - 1);
      if (value.length() > MAX_LENGTH) {
          throw new InvalidLengthException("La longitud del STRING "+value +" supera lo permitido.");
      }
      return symbol(ParserSym.STRING_CONSTANT, value);
  }

  /* operators */
  {Plus}                                    { return symbol(ParserSym.PLUS); }
  {Sub}                                     { return symbol(ParserSym.SUB); }
  {Mult}                                    { return symbol(ParserSym.MULT); }
  {Div}                                     { return symbol(ParserSym.DIV); }
  {Assig}                                   { return symbol(ParserSym.ASSIG); }
  {OpenBracket}                             { return symbol(ParserSym.OPEN_BRACKET); }
  {CloseBracket}                            { return symbol(ParserSym.CLOSE_BRACKET); }
  {OpenCurlyBracket}                        { return symbol(ParserSym.OPEN_CURLY_BRACKET); }
  {CloseCurlyBracket}                       { return symbol(ParserSym.CLOSE_CURLY_BRACKET); }
  {Colon}                                   { return symbol(ParserSym.COLON); }
  {Comma}                                   { return symbol(ParserSym.COMMA); }
  {GreaterThan}                             { return symbol(ParserSym.GREATER_THAN); }
  {LessThan}                                { return symbol(ParserSym.LESS_THAN); }
  {GreaterThanOrEqual}                      { return symbol(ParserSym.GREATER_THAN_OR_EQUAL); }
  {LessThanOrEqual}                         { return symbol(ParserSym.LESS_THAN_OR_EQUAL); }
  {Equal}                                   { return symbol(ParserSym.EQUAL); }
  {NotEqual}                                { return symbol(ParserSym.NOT_EQUAL); }
  /* whitespace, comment */
  {WhiteSpace}                   { /* ignore */ }
  {Comment}                      {
        String value = yytext().substring(1, yytext().length() - 1);;
        if (value.indexOf('#') != -1) {
            throw new UnknownCharacterException(yytext());
        }
    }
}


/* error fallback */
[^]                              { throw new UnknownCharacterException(yytext()); }