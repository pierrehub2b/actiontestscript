package flex.messaging.io;

import java.util.HashMap;





























public class ASObject
  extends HashMap<String, Object>
{
  private static final long serialVersionUID = 1L;
  private String type;
  
  public ASObject() {}
  
  public ASObject(String type)
  {
    this.type = type;
  }
  





  public String getType()
  {
    return type;
  }
  







  public void setType(String type)
  {
    this.type = type;
  }
  









  public boolean containsKey(Object key)
  {
    return super.containsKey(toLowerCase(key));
  }
  















  public Object get(Object key)
  {
    return super.get(toLowerCase(key));
  }
  














  public Object put(String key, Object value)
  {
    return super.put((String)toLowerCase(key), value);
  }
  










  public Object remove(Object key)
  {
    return super.remove(toLowerCase(key));
  }
  










  private Object toLowerCase(Object key)
  {
    return key;
  }
  



  public Object instantiate()
  {
    Object ret;
    

    try
    {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> clazz = loader.loadClass(type);
      ret = clazz.newInstance();
    } catch (Exception e) {
      ret = null;
    }
    return ret;
  }
  
  public String toString()
  {
    return "ASObject[type=" + getType() + "," + super.toString() + "]";
  }
}
