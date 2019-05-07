package flex.messaging.messages;

import com.exadel.flamingo.flex.messaging.security.SecurityServiceException;
import com.exadel.flamingo.flex.messaging.security.ServiceException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

























public class ErrorMessage
  extends AcknowledgeMessage
{
  private static final long serialVersionUID = 1L;
  private String faultCode = "Server.Call.Failed";
  
  private String faultDetail;
  private String faultString;
  private Object rootCause;
  private Map<String, Object> extendedData;
  private transient boolean loginError = false;
  

  public ErrorMessage() {}
  

  public ErrorMessage(Throwable t)
  {
    init(t);
  }
  
  public ErrorMessage(Message request, Throwable t) {
    super(request);
    if ((request instanceof CommandMessage)) {
      loginError = ((((CommandMessage)request).isLoginOperation()) && ((t instanceof SecurityServiceException)));
    }
    


    init(t);
  }
  
  private void init(Throwable t) {
    if ((t instanceof ServiceException)) {
      ServiceException se = (ServiceException)t;
      
      faultCode = se.getCode();
      faultString = se.getMessage();
      
      if ((t instanceof SecurityServiceException)) {
        faultDetail = se.getDetail();
      } else {
        faultDetail = (se.getDetail() + getStackTrace(t));
        extendedData = se.getExtendedData();
      }
    }
    else if (t != null) {
      faultString = t.getMessage();
      faultDetail = getStackTrace(t);
    }
    
    if (!(t instanceof SecurityServiceException)) {
      for (Throwable root = t; root != null; root = root.getCause())
        rootCause = root;
    }
  }
  
  private String getStackTrace(Throwable t) {
    StringWriter sw = new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    return sw.toString().replace("\r\n", "\n").replace('\r', '\n');
  }
  
  public String getFaultCode() {
    return faultCode;
  }
  
  public void setFaultCode(String faultCode) { this.faultCode = faultCode; }
  
  public String getFaultDetail()
  {
    return faultDetail;
  }
  
  public void setFaultDetail(String faultDetail) { this.faultDetail = faultDetail; }
  
  public String getFaultString()
  {
    return faultString;
  }
  
  public void setFaultString(String faultString) { this.faultString = faultString; }
  
  public Map<String, Object> getExtendedData()
  {
    return extendedData;
  }
  
  public void setExtendedData(Map<String, Object> extendedData) { this.extendedData = extendedData; }
  
  public Object getRootCause()
  {
    return rootCause;
  }
  
  public void setRootCause(Object rootCause) { this.rootCause = rootCause; }
  
  public boolean loginError()
  {
    return loginError;
  }
  
  public ErrorMessage copy(Message request)
  {
    ErrorMessage copy = new ErrorMessage(request, null);
    faultCode = faultCode;
    faultDetail = faultDetail;
    faultString = faultString;
    loginError = loginError;
    return copy;
  }
  
  public String toString()
  {
    return toString("");
  }
  
  public String toString(String indent)
  {
    StringBuilder sb = new StringBuilder(512);
    sb.append(getClass().getName()).append(" {");
    sb.append('\n').append(indent).append("  faultCode = ").append(faultCode);
    sb.append('\n').append(indent).append("  faultDetail = ").append(faultDetail);
    sb.append('\n').append(indent).append("  faultString = ").append(faultString);
    sb.append('\n').append(indent).append("  rootCause = ").append(rootCause);
    sb.append('\n').append(indent).append("  extendedData = ").append(extendedData);
    super.toString(sb, indent, null);
    sb.append('\n').append(indent).append('}');
    return sb.toString();
  }
}
