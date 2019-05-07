package flex.messaging.messages;

import java.io.Serializable;
import java.util.Map;

public abstract interface Message
  extends Serializable
{
  public static final String DESTINATION_CLIENT_ID_HEADER = "DSDstClientId";
  public static final String ENDPOINT_HEADER = "DSEndpoint";
  public static final String REMOTE_CREDENTIALS_HEADER = "DSRemoteCredentials";
  public static final String DS_ID_HEADER = "DSId";
  public static final String HIDDEN_CREDENTIALS = "****** (credentials)";
  
  public abstract Object getBody();
  
  public abstract Object getClientId();
  
  public abstract String getDestination();
  
  public abstract Object getHeader(String paramString);
  
  public abstract Map<String, Object> getHeaders();
  
  public abstract String getMessageId();
  
  public abstract long getTimestamp();
  
  public abstract long getTimeToLive();
  
  public abstract boolean headerExists(String paramString);
  
  public abstract void setBody(Object paramObject);
  
  public abstract void setClientId(Object paramObject);
  
  public abstract void setDestination(String paramString);
  
  public abstract void setHeader(String paramString, Object paramObject);
  
  public abstract void setHeaders(Map<String, Object> paramMap);
  
  public abstract void setMessageId(String paramString);
  
  public abstract void setTimestamp(long paramLong);
  
  public abstract void setTimeToLive(long paramLong);
  
  public abstract String toString(String paramString);
}
