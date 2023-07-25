package csemachine;

import ast.ASTNode;
import ast.ASTNodeType;

// ASTNode for tuple
public class Tuple extends ASTNode{
  
  public Tuple(){
    setType(ASTNodeType.TUPLE);
  }
  
  // returns the value of the tuple
  @Override
  public String getValue(){
    ASTNode childNode = getChild();
    // when child node is null
    if(childNode==null)
      return "nil";
    
    // when child node is not null
    String printValue = "(";
    while(childNode.getSibling()!=null){
      printValue += childNode.getValue() + ", ";
      childNode = childNode.getSibling();
    }
    printValue += childNode.getValue() + ")";
    return printValue;
  }
  
  // copy the tuple
  public Tuple accept(NodeCopier nodeCopier){
    return nodeCopier.copy(this);
  }
  
}
