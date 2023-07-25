package csemachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import ast.ASTNode;
import ast.ASTNodeType;


// Delta function is a function that takes a list of arguments and returns a function that takes the next argument in the list
public class Delta extends ASTNode{
  private List<String> boundVars;
  private Environment linkedEnv; 
  private Stack<ASTNode> body;
  private int index;
  
  public Delta(){
    setType(ASTNodeType.DELTA);
    boundVars = new ArrayList<String>();
  }
  
  public Delta accept(NodeCopier nodeCopier){
    return nodeCopier.copy(this);
  }
  
  // returns the value of the delta function
  @Override
  public String getValue(){
    return "[lambda closure: "+boundVars.get(0)+": "+index+"]";
  }

  // returns the list of bound variables
  public List<String> getBoundVars(){
    return boundVars;
  }
  
  // adds a bound variable to the list of bound variables
  public void addBoundVars(String boundVar){
    boundVars.add(boundVar);
  }
  
  // sets the list of bound variables
  public void setBoundVars(List<String> boundVars){
    this.boundVars = boundVars;
  }
  
  // returns the body of the delta function
  public Stack<ASTNode> getBody(){
    return body;
  }

  // sets the index of the delta function
  public void setIndex(int index){
    this.index = index;
  }

  // returns the environment linked to the delta function
  public Environment getLinkedEnv(){
    return linkedEnv;
  }
  
  // sets the body of the delta function
  public void setBody(Stack<ASTNode> body){
    this.body = body;
  }
  
  // returns the index of the delta function
  public int getIndex(){
    return index;
  }

  // sets the environment linked to the delta function
  public void setLinkedEnvironment(Environment linkedEnv){
    this.linkedEnv = linkedEnv;
  }
}
