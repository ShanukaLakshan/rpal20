package csemachine;

import java.util.HashMap;
import java.util.Map;

import ast.ASTNode;

// Environment class to store the mapping of variables to their values
public class Environment{
  private Environment parent;
  private Map<String, ASTNode> nameValueMap;
  
  public Environment(){
    nameValueMap = new HashMap<String, ASTNode>();
  }

  public Environment getParent(){
    return parent;
  }

  public void setParent(Environment parent){
    this.parent = parent;
  }
  
  // returns the value of the variable
  public ASTNode lookup(String key){
    ASTNode retValue = null;
    Map<String, ASTNode> map = nameValueMap;
    
    retValue = map.get(key);
    
    if(retValue!=null)
      return retValue.accept(new NodeCopier());
    
    if(parent!=null)
      return parent.lookup(key);
    else
      return null;
  }
  
  public void addMapping(String key, ASTNode value){
    nameValueMap.put(key, value);
  }
}
