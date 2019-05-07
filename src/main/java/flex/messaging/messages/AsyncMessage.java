package flex.messaging.messages;

import java.util.HashMap;
























public abstract class AsyncMessage
  extends AbstractMessage
{
  private String correlationId;
  
  public AsyncMessage()
  {
    setHeaders(new HashMap());
  }
  
  public AsyncMessage(Message request) {
    super(request);
    
    setHeaders(new HashMap());
    correlationId = request.getMessageId();
  }
  
  public String getCorrelationId() {
    return correlationId;
  }
  
  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }
  
  protected void toString(StringBuilder sb, String indent, String bodyMessage)
  {
    sb.append('\n').append(indent).append("  correlationId = ").append(correlationId);
    super.toString(sb, indent, bodyMessage);
  }
}
