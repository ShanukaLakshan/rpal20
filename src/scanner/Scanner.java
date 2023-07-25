package scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

// combination of screener and lexer 
public class Scanner{
  private BufferedReader buffer;
  private String extraCharRead;
  private final List<String> reservedIdentifiers = Arrays.asList(new String[]{"let","in","within","fn","where","aug","or",
                                                                              "not","gr","ge","ls","le","eq","ne","true",
                                                                              "false","nil","dummy","rec","and"});
  private int sourceLineNumber;
  
  public Scanner(String inputFile) throws IOException{
    sourceLineNumber = 1;
    buffer = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inputFile))));
  }
  
  // Return the next token
  public Token NextToken(){
    Token nextToken = null;
    String nextChar;
    if(extraCharRead!=null){
      nextChar = extraCharRead;
      extraCharRead = null;
    } else
      nextChar = NextChar();
    if(nextChar!=null)
      nextToken = buildToken(nextChar);
    return nextToken;
  }

  private String NextChar(){
    String nextChar = null;
    try{
      int c = buffer.read();
      if(c!=-1){
        nextChar = Character.toString((char)c);
        if(nextChar.equals("\n")) sourceLineNumber++;
      } else
          buffer.close();
    }catch(IOException e){
    }
    return nextChar;
  }

  // Build the tokens
  private Token buildToken(String currentChar){
    Token nextToken = null;
    if(LexicalRegexPatterns.LetterPattern.matcher(currentChar).matches()){
      nextToken = buildIdentifierToken(currentChar);
    }
    else if(LexicalRegexPatterns.DigitPattern.matcher(currentChar).matches()){
      nextToken = buildIntegerToken(currentChar);
    }
    else if(LexicalRegexPatterns.OpSymbolPattern.matcher(currentChar).matches()){ //comment tokens are also entered from here
      nextToken = buildOperatorToken(currentChar);
    }
    else if(currentChar.equals("\'")){
      nextToken = buildStringToken(currentChar);
    }
    else if(LexicalRegexPatterns.SpacePattern.matcher(currentChar).matches()){
      nextToken = buildSpaceToken(currentChar);
    }
    else if(LexicalRegexPatterns.PunctuationPattern.matcher(currentChar).matches()){
      nextToken = buildPunctuationPattern(currentChar);
    }
    return nextToken;
  }

  /*
   * Build the Identifier token
   * Identifier -> Letter (Letter | Digit | '_')*
   */
  private Token buildIdentifierToken(String currentChar){
    Token identifierToken = new Token();
    identifierToken.setType(TokenType.IDENTIFIER);
    identifierToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = NextChar();
    while(nextChar!=null){
      if(LexicalRegexPatterns.IdentifierPattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = NextChar();
      }
      else{
        extraCharRead = nextChar;
        break;
      }
    }
    
    String value = sBuilder.toString();
    if(reservedIdentifiers.contains(value))
      identifierToken.setType(TokenType.RESERVED);
    
    identifierToken.setValue(value);
    return identifierToken;
  }

  /*
   * Build the integer token.
   * Integer -> Digit+
   */
  private Token buildIntegerToken(String currentChar){
    Token integerToken = new Token();
    integerToken.setType(TokenType.INTEGER);
    integerToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = NextChar();
    while(nextChar!=null){ 
      if(LexicalRegexPatterns.DigitPattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = NextChar();
      }
      else{
        extraCharRead = nextChar;
        break;
      }
    }
    
    integerToken.setValue(sBuilder.toString());
    return integerToken;
  }

  /*
   * Build the operator token.
   * Operator -> Operator_symbol+
   */
  private Token buildOperatorToken(String currentChar){
    Token opSymbolToken = new Token();
    opSymbolToken.setType(TokenType.OPERATOR);
    opSymbolToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = NextChar();
    
    if(currentChar.equals("/") && nextChar.equals("/"))
      return buildCommentToken(currentChar+nextChar);
    
    while(nextChar!=null){ 
      if(LexicalRegexPatterns.OpSymbolPattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = NextChar();
      }
      else{
        extraCharRead = nextChar;
        break;
      }
    }
    
    opSymbolToken.setValue(sBuilder.toString());
    return opSymbolToken;
  }

  /*
   * Build the string token.
   * String -> '''' ('\' 't' | '\' 'n' | '\' '\' | '\' '''' |'(' | ')' | ';' | ',' |'' |Letter | Digit | Operator_symbol )* ''''
   */
  private Token buildStringToken(String currentChar){
    Token stringToken = new Token();
    stringToken.setType(TokenType.STRING);
    stringToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder("");
    
    String nextChar = NextChar();
    while(nextChar!=null){ 
      if(nextChar.equals("\'")){ 
        stringToken.setValue(sBuilder.toString());
        return stringToken;
      }
      else if(LexicalRegexPatterns.StringPattern.matcher(nextChar).matches()){ //match Letter | Digit | Operator_symbol
        sBuilder.append(nextChar);
        nextChar = NextChar();
      }
    }
    
    return null;
  }
  
  private Token buildSpaceToken(String currentChar){
    Token deleteToken = new Token();
    deleteToken.setType(TokenType.DELETE);
    deleteToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = NextChar();
    while(nextChar!=null){ 
      if(LexicalRegexPatterns.SpacePattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = NextChar();
      }
      else{
        extraCharRead = nextChar;
        break;
      }
    }
    
    deleteToken.setValue(sBuilder.toString());
    return deleteToken;
  }
  
  private Token buildCommentToken(String currentChar){
    Token commentToken = new Token();
    commentToken.setType(TokenType.DELETE);
    commentToken.setSourceLineNumber(sourceLineNumber);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = NextChar();
    while(nextChar!=null){ 
      if(LexicalRegexPatterns.CommentPattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = NextChar();
      }
      else if(nextChar.equals("\n"))
        break;
    }
    
    commentToken.setValue(sBuilder.toString());
    return commentToken;
  }

  private Token buildPunctuationPattern(String currentChar){
    Token punctuationToken = new Token();
    punctuationToken.setSourceLineNumber(sourceLineNumber);
    punctuationToken.setValue(currentChar);
    if(currentChar.equals("("))
      punctuationToken.setType(TokenType.L_PAREN);
    else if(currentChar.equals(")"))
      punctuationToken.setType(TokenType.R_PAREN);
    else if(currentChar.equals(";"))
      punctuationToken.setType(TokenType.SEMICOLON);
    else if(currentChar.equals(","))
      punctuationToken.setType(TokenType.COMMA);
    
    return punctuationToken;
  }
}

