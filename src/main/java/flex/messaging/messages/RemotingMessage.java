package flex.messaging.messages;






public class RemotingMessage
  extends AsyncMessage
{
  private static final long serialVersionUID = 1L;
  




  private String source;
  




  private String operation;
  





  public RemotingMessage() {}
  




  public String getSource()
  {
    return source;
  }
  
  public void setSource(String source) {
    this.source = source;
  }
  
  public String getOperation() {
    return operation;
  }
  
  public void setOperation(String operation) {
    this.operation = operation;
  }
  
  public String toString()
  {
    return toString("");
  }
  
  public String toString(String indent) {
    StringBuilder sb = new StringBuilder(512);
    sb.append(getClass().getName()).append(" {");
    sb.append('\n').append(indent).append("  source = ").append(source);
    sb.append('\n').append(indent).append("  operation = ").append(operation);
    super.toString(sb, indent, null);
    sb.append('\n').append(indent).append('}');
    return sb.toString();
  }
}
