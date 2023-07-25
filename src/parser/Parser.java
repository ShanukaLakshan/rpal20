package parser;

import java.util.Stack;

import ast.AST;
import ast.ASTNode;
import ast.ASTNodeType;
import scanner.Scanner;
import scanner.Token;
import scanner.TokenType;

public class Parser{
  private Scanner s;
  private Token curToken;
  Stack<ASTNode> stack;

  public Parser(Scanner s){
    this.s = s;
    stack = new Stack<ASTNode>();
  }
  
  public AST buildAST(){
    Start();
    return new AST(stack.pop());
  }

  public void Start(){
    Read();
    procE(); 
    if(curToken!=null)
      throw new ParseException("Expected EOF.");
  }

  //
  private void Read(){
    do{
      curToken = s.NextToken(); 
    }while(curToken != null && curToken.getType() == TokenType.DELETE);
    if(null != curToken){
      if(curToken.getType()==TokenType.IDENTIFIER){
        terminalAstNode(ASTNodeType.IDENTIFIER, curToken.getValue());
      }
      else if(curToken.getType()==TokenType.INTEGER){
        terminalAstNode(ASTNodeType.INTEGER, curToken.getValue());
      } 
      else if(curToken.getType()==TokenType.STRING){
        terminalAstNode(ASTNodeType.STRING, curToken.getValue());
      }
    }
  }


  private boolean checkCurTokenType(TokenType type){
    if(curToken==null)
      return false;
    if(curToken.getType()==type)
      return true;
    return false;
  }
  

  private boolean checkCurToken(TokenType type, String value){
    if(curToken==null)
      return false;
    if(curToken.getType()!=type || !curToken.getValue().equals(value))
      return false;
    return true;
  }
  
  

  private void buildNAryASTNode(ASTNodeType type, int ariness){
    ASTNode node = new ASTNode();
    node.setType(type);
    while(ariness>0){
      ASTNode child = stack.pop();
      if(node.getChild()!=null)
        child.setSibling(node.getChild());
      node.setChild(child);
      node.setSourceLineNumber(child.getSourceLineNumber());
      ariness--;
    }
    stack.push(node);
  }


  private void terminalAstNode(ASTNodeType type, String value){
    
    ASTNode node = new ASTNode();
    node.setType(type);
    node.setValue(value);
    node.setSourceLineNumber(curToken.getSourceLineNumber());
    stack.push(node);
  }

  //E -> let D in E | fn Vb+ . E | Ew
  private void procE(){
    if(checkCurToken(TokenType.RESERVED, "let")){ 
      Read();
      procD();
      if(!checkCurToken(TokenType.RESERVED, "in"))
        throw new ParseException("E:  'in' expected");
      Read();
      procE(); 
      buildNAryASTNode(ASTNodeType.LET, 2);
    }
    else if(checkCurToken(TokenType.RESERVED, "fn")){ 
      int treesToPop = 0;
      
      Read();
      while(checkCurTokenType(TokenType.IDENTIFIER) || checkCurTokenType(TokenType.L_PAREN)){
        procVB(); 
        treesToPop++;
      }
      
      if(treesToPop==0)
        throw new ParseException("E: at least one 'Vb' expected");
      
      if(!checkCurToken(TokenType.OPERATOR, "."))
        throw new ParseException("E: '.' expected");
      
      Read();
      procE(); 
      
      buildNAryASTNode(ASTNodeType.LAMBDA, treesToPop+1); 
    }
    else 
      procEW();
  }

  //EW -> T { , T }
  private void procT(){
    procTA(); 
    int treesToPop = 0;
    while(checkCurToken(TokenType.OPERATOR, ",")){ 
      Read();
      procTA(); 
      treesToPop++;
    }
    if(treesToPop > 0) buildNAryASTNode(ASTNodeType.TAU, treesToPop+1);
  }

  //TC -> T [ ->
  private void procTC(){
    procB();
    if(checkCurToken(TokenType.OPERATOR, "->")){ 
      Read();
      procTC(); 
      if(!checkCurToken(TokenType.OPERATOR, "|"))
        throw new ParseException("TC: '|' expected");
      Read();
      procTC();  
      buildNAryASTNode(ASTNodeType.CONDITIONAL, 3);
    }
  }

  //TA -> TC { aug TC }
  private void procTA(){
    procTC(); 
   
    while(checkCurToken(TokenType.RESERVED, "aug")){ 
      Read();
      procTC(); 
      buildNAryASTNode(ASTNodeType.AUG, 2);
    }
  }

  //TA -> TC { aug TC }
  private void procEW(){
    procT(); 
    if(checkCurToken(TokenType.RESERVED, "where")){ 
      Read();
      procDR(); 
      buildNAryASTNode(ASTNodeType.WHERE, 2);
    }
  }
//BT -> BS { & BS }
  private void procBT(){
    procBS(); 
    while(checkCurToken(TokenType.OPERATOR, "&")){ 
      Read();
      procBS(); 
      buildNAryASTNode(ASTNodeType.AND, 2);
    }
  }
  
 //BS -> not BP | BP
  private void procBS(){
    if(checkCurToken(TokenType.RESERVED, "not")){ 
      Read();
      procBP(); 
      buildNAryASTNode(ASTNodeType.NOT, 1);
    }
    else
      procBP(); 
  }
  
//BS -> not BP | BP
  private void procB(){
    procBT(); 
    while(checkCurToken(TokenType.RESERVED, "or")){
      Read();
      procBT();
      buildNAryASTNode(ASTNodeType.OR, 2);
    }
  }

  //AF -> AP { ** AP }
  private void procAT(){
    procAF(); 

    boolean mult = true;
    while(checkCurToken(TokenType.OPERATOR, "*")||checkCurToken(TokenType.OPERATOR, "/")){
      if(curToken.getValue().equals("*"))
        mult = true;
      else if(curToken.getValue().equals("/"))
        mult = false;
      Read();
      procAF(); 
      if(mult) 
        buildNAryASTNode(ASTNodeType.MULT, 2);
      else 
        buildNAryASTNode(ASTNodeType.DIV, 2);
    }
  }
  
  //BP -> A [ > A | >= A | < A | <= A | = A | != A ]
  private void procBP(){
    procA(); 
    if(checkCurToken(TokenType.RESERVED,"gr")||checkCurToken(TokenType.OPERATOR,">")){ 
      Read();
      procA(); 
      buildNAryASTNode(ASTNodeType.GR, 2);
    }
    else if(checkCurToken(TokenType.RESERVED,"ge")||checkCurToken(TokenType.OPERATOR,">=")){ 
      Read();
      procA(); 
      buildNAryASTNode(ASTNodeType.GE, 2);
    }
    else if(checkCurToken(TokenType.RESERVED,"ls")||checkCurToken(TokenType.OPERATOR,"<")){ 
      Read();
      procA(); 
      buildNAryASTNode(ASTNodeType.LS, 2);
    }
    else if(checkCurToken(TokenType.RESERVED,"le")||checkCurToken(TokenType.OPERATOR,"<=")){ 
      Read();
      procA(); 
      buildNAryASTNode(ASTNodeType.LE, 2);
    }
    else if(checkCurToken(TokenType.RESERVED,"eq")){ 
      Read();
      procA(); 
      buildNAryASTNode(ASTNodeType.EQ, 2);
    }
    else if(checkCurToken(TokenType.RESERVED,"ne")){ 
      Read();
      procA(); 
      buildNAryASTNode(ASTNodeType.NE, 2);
    }
  }
  
//A -> A + AT | A - AT | AT
  private void procA(){
    if(checkCurToken(TokenType.OPERATOR, "+")){ 
      Read();
      procAT(); 
    }
    else if(checkCurToken(TokenType.OPERATOR, "-")){ 
      Read();
      procAT(); 
      buildNAryASTNode(ASTNodeType.NEG, 1);
    }
    else
      procAT(); 
    
    boolean plus = true;
    while(checkCurToken(TokenType.OPERATOR, "+")||checkCurToken(TokenType.OPERATOR, "-")){
      if(curToken.getValue().equals("+"))
        plus = true;
      else if(curToken.getValue().equals("-"))
        plus = false;
      Read();
      procAT(); 
      if(plus) 
        buildNAryASTNode(ASTNodeType.PLUS, 2);
      else 
        buildNAryASTNode(ASTNodeType.MINUS, 2);
    }
  }

//AF -> AP { ** AP }
  private void procAF(){
    procAP(); 
    
    if(checkCurToken(TokenType.OPERATOR, "**")){ 
      Read();
      procAF();
      buildNAryASTNode(ASTNodeType.EXP, 2);
    }
  }
  
  
//AP -> R { @ R }
  private void procAP(){
    procR(); 
    
    while(checkCurToken(TokenType.OPERATOR, "@")){ 
      Read();
      if(!checkCurTokenType(TokenType.IDENTIFIER))
        throw new ParseException("AP: expected Identifier");
      Read();
      procR(); 
      buildNAryASTNode(ASTNodeType.AT, 3);
    }
  }
  
  //R -> R Rn | Rn
  private void procR(){
    procRN(); 
    Read();
    while(checkCurTokenType(TokenType.INTEGER)||
        checkCurTokenType(TokenType.STRING)|| 
        checkCurTokenType(TokenType.IDENTIFIER)||
        checkCurToken(TokenType.RESERVED, "true")||
        checkCurToken(TokenType.RESERVED, "false")||
        checkCurToken(TokenType.RESERVED, "nil")||
        checkCurToken(TokenType.RESERVED, "dummy")||
        checkCurTokenType(TokenType.L_PAREN)){ //R -> R Rn => 'gamma'
      procRN(); 
      buildNAryASTNode(ASTNodeType.GAMMA, 2);
      Read();
    }
  }

  private void procDR(){
    if(checkCurToken(TokenType.RESERVED, "rec")){ 
      Read();
      procDB(); 
      buildNAryASTNode(ASTNodeType.REC, 1);
    }
    else{ 
      procDB(); 
    }
  }
  
//DB -> ( D ) | VB { , VB } = E | Identifier = E | ( D ) = E
  private void procDB(){
    if(checkCurTokenType(TokenType.L_PAREN)){ 
      procD();
      Read();
      if(!checkCurTokenType(TokenType.R_PAREN))
        throw new ParseException("DB: ')' expected");
      Read();
    }
    else if(checkCurTokenType(TokenType.IDENTIFIER)){
      Read();
      if(checkCurToken(TokenType.OPERATOR, ",")){ 
        Read();
        procVL(); 
        if(!checkCurToken(TokenType.OPERATOR, "="))
          throw new ParseException("DB: = expected.");
        buildNAryASTNode(ASTNodeType.COMMA, 2);
        Read();
        procE(); 
        buildNAryASTNode(ASTNodeType.EQUAL, 2);
      }
      else{ 
        if(checkCurToken(TokenType.OPERATOR, "=")){
          Read();
          procE(); 
          buildNAryASTNode(ASTNodeType.EQUAL, 2);
        }
        else{ 
          int treesToPop = 0;

          while(checkCurTokenType(TokenType.IDENTIFIER) || checkCurTokenType(TokenType.L_PAREN)){
            procVB(); 
            treesToPop++;
          }

          if(treesToPop==0)
            throw new ParseException("E: at least one 'Vb' expected");

          if(!checkCurToken(TokenType.OPERATOR, "="))
            throw new ParseException("DB: = expected.");

          Read();
          procE(); 

          buildNAryASTNode(ASTNodeType.FCNFORM, treesToPop+2); 
        }
      }
    }
  }

  //RN -> Identifier | Integer | String | true | false | nil | ( E ) | dummy
  private void procRN(){
    if(checkCurTokenType(TokenType.IDENTIFIER)|| 
       checkCurTokenType(TokenType.INTEGER)|| 
       checkCurTokenType(TokenType.STRING)){ 
    }
    else if(checkCurToken(TokenType.RESERVED, "true")){ 
      terminalAstNode(ASTNodeType.TRUE, "true");
    }
    else if(checkCurToken(TokenType.RESERVED, "false")){ 
      terminalAstNode(ASTNodeType.FALSE, "false");
    } 
    else if(checkCurToken(TokenType.RESERVED, "nil")){ 
      terminalAstNode(ASTNodeType.NIL, "nil");
    }
    else if(checkCurTokenType(TokenType.L_PAREN)){
      Read();
      procE(); 
      if(!checkCurTokenType(TokenType.R_PAREN))
        throw new ParseException("RN: ')' expected");
    }
    else if(checkCurToken(TokenType.RESERVED, "dummy")){ 
      terminalAstNode(ASTNodeType.DUMMY, "dummy");
    }
  }

//D -> DA [ within D ]
  private void procD(){
    procDA(); 
    if(checkCurToken(TokenType.RESERVED, "within")){ 
      Read();
      procD();
      buildNAryASTNode(ASTNodeType.WITHIN, 2);
    }
  }
  
  //DA -> DB { and DB }
  private void procDA(){
    procDR(); 
    int treesToPop = 0;
    while(checkCurToken(TokenType.RESERVED, "and")){ 
      Read();
      procDR(); 
      treesToPop++;
    }
    if(treesToPop > 0) buildNAryASTNode(ASTNodeType.SIMULTDEF, treesToPop+1);
  }
  

  
 //VB -> Identifier | ( VL )
  private void procVB(){
    if(checkCurTokenType(TokenType.IDENTIFIER)){ 
      Read();
    }
    else if(checkCurTokenType(TokenType.L_PAREN)){
      Read();
      if(checkCurTokenType(TokenType.R_PAREN)){ 
        terminalAstNode(ASTNodeType.PAREN, "");
        Read();
      }
      else{ 
        procVL(); 
        if(!checkCurTokenType(TokenType.R_PAREN))
          throw new ParseException("VB: ')' expected");
        Read();
      }
    }
  }


//VL -> Identifier { , Identifier } 
  private void procVL(){
    if(!checkCurTokenType(TokenType.IDENTIFIER))
      throw new ParseException("VL: Identifier expected");
    else{
      Read();
      int treesToPop = 0;
      while(checkCurToken(TokenType.OPERATOR, ",")){ 
        Read();
        if(!checkCurTokenType(TokenType.IDENTIFIER))
          throw new ParseException("VL: Identifier expected");
        Read();
        treesToPop++;
      }
      if(treesToPop > 0) buildNAryASTNode(ASTNodeType.COMMA, treesToPop+1); 
    }
  }

}

