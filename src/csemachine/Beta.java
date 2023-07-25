package csemachine;

import java.util.Stack;

import ast.ASTNode;
import ast.ASTNodeType;

// Compare this snippet from src\ast\ASTNode.java:
public class Beta extends ASTNode{
  // thenBlock and elseBlock are stacks of ASTNodes that represent the then and else blocks of the beta function
  private Stack<ASTNode> elseBlock;
  private Stack<ASTNode> thenBlock;
  
  //  beta function is a conditional function to check if the first argument is true or false
  public Beta(){
    setType(ASTNodeType.BETA);
    thenBlock = new Stack<ASTNode>();
    elseBlock = new Stack<ASTNode>();
  }
  

  public Beta accept(NodeCopier nodeCopier){
    return nodeCopier.copy(this);
  }

  public Stack<ASTNode> getThenBody(){
    return thenBlock;
  }

  public Stack<ASTNode> getElseBody(){
    return elseBlock;
  }

  public void setThenBody(Stack<ASTNode> thenBlock){
    this.thenBlock = thenBlock;
  }

  public void setElseBody(Stack<ASTNode> elseBlock){
    this.elseBlock = elseBlock;
  }
  
}
