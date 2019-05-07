package flex.messaging.messages;



public class CommandMessage
  extends AsyncMessage
{
  private static final long serialVersionUID = 1L;
  

  public static final int SUBSCRIBE_OPERATION = 0;
  

  public static final int UNSUBSCRIBE_OPERATION = 1;
  

  public static final int POLL_OPERATION = 2;
  

  public static final int CLIENT_SYNC_OPERATION = 4;
  

  public static final int CLIENT_PING_OPERATION = 5;
  

  public static final int CLUSTER_REQUEST_OPERATION = 7;
  

  public static final int LOGIN_OPERATION = 8;
  

  public static final int LOGOUT_OPERATION = 9;
  

  public static final int SESSION_INVALIDATE_OPERATION = 10;
  
  public static final int UNKNOWN_OPERATION = 10000;
  
  private String messageRefType;
  
  private int operation;
  

  public CommandMessage() {}
  

  public String getMessageRefType()
  {
    return messageRefType;
  }
  
  public void setMessageRefType(String messageRefType) { this.messageRefType = messageRefType; }
  
  public int getOperation()
  {
    return operation;
  }
  
  public void setOperation(int operation) { this.operation = operation; }
  
  public boolean isSecurityOperation()
  {
    return (isLoginOperation()) || (isLogoutOperation());
  }
  
  public boolean isLoginOperation() { return operation == 8; }
  
  public boolean isLogoutOperation() {
    return operation == 9;
  }
  
  public boolean isClientPingOperation() {
    return operation == 5;
  }
  
  public String toString()
  {
    return toString("");
  }
  
  public String toString(String indent) {
    StringBuilder sb = new StringBuilder(512);
    sb.append(getClass().getName()).append(" {");
    sb.append('\n').append(indent).append("  messageRefType: ").append(messageRefType);
    sb.append('\n').append(indent).append("  operation: ").append(getReadableOperation(operation));
    super.toString(sb, indent, isLoginOperation() ? "****** (credentials)" : null);
    sb.append('\n').append(indent).append('}');
    return sb.toString();
  }
  
  private static String getReadableOperation(int operation) {
    switch (operation) {
    case 0: 
      return "SUBSCRIBE";
    case 1: 
      return "UNSUBSCRIBE";
    case 2: 
      return "POLL";
    case 4: 
      return "CLIENT_SYNC";
    case 5: 
      return "CLIENT_PING";
    case 7: 
      return "CLUSTER_REQUEST";
    case 8: 
      return "LOGIN";
    case 9: 
      return "LOGOUT";
    case 10: 
      return "SESSION_INVALIDATE";
    case 10000: 
      return "UNKNOWN";
    }
    return "REALLY UNKNOWN: 0x" + Integer.toBinaryString(operation);
  }
}
