package csemachine;

// class to print the error message
public class EvaluationError{
  
  public static void printError(int sourceLineNumber, String message){
    System.out.println(":"+sourceLineNumber+": "+message);
    System.exit(1);
  }

}
