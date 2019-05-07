package flex.messaging.messages;









public class AcknowledgeMessage
  extends AsyncMessage
{
  private static final long serialVersionUID = 1L;
  








  public AcknowledgeMessage() {}
  







  public AcknowledgeMessage(Message request)
  {
    super(request);
  }
  
  public String toString()
  {
    return toString("");
  }
  
  public String toString(String indent) {
    StringBuilder sb = new StringBuilder(512);
    sb.append(getClass().getName()).append(" {");
    super.toString(sb, indent, null);
    sb.append('\n').append(indent).append('}');
    return sb.toString();
  }
}
