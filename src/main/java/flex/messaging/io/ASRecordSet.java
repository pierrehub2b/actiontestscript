package flex.messaging.io;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;















public class ASRecordSet
  extends ASObject
{
  private static final long serialVersionUID = 1L;
  private static final Log log = LogFactory.getLog(ASRecordSet.class);
  
  public static final String SERVICE_NAME = "OpenAMFPageableRecordSet";
  
  private static final String SI = "serverInfo";
  
  private static final String SI_ID = "id";
  
  private static final String SI_TOTAL_COUNT = "totalCount";
  private static final String SI_INITIAL_DATA = "initialData";
  private static final String SI_CURSOR = "cursor";
  private static final String SI_SERVICE_NAME = "serviceName";
  private static final String SI_COLUMN_NAMES = "columnNames";
  private static final String SI_VERSION = "version";
  private static int count = 0;
  private Map<String, Object> serverInfo;
  private List<List<Object>> rows;
  private int initialRowCount;
  
  public ASRecordSet()
  {
    super("RecordSet");
    serverInfo = new HashMap();
    put("serverInfo", serverInfo);
    
    synchronized (ASRecordSet.class)
    {
      count += 1;
      setId("RS" + count);
    }
    
    setInitialData(new ArrayList());
    setServiceName("OpenAMFPageableRecordSet");
    setCursor(1);
    setVersion(1.0D);
    rows = new ArrayList();
    initialRowCount = 0;
  }
  
  public String getId() {
    return (String)serverInfo.get("id");
  }
  
  public void setId(String id) { serverInfo.put("id", id); }
  
  public int getTotalCount()
  {
    Object value = serverInfo.get("totalCount");
    if (value != null)
      return ((Integer)value).intValue();
    return 0;
  }
  
  public void setTotalCount(int totalCount) { serverInfo.put("totalCount", Integer.valueOf(totalCount)); }
  
  public List<?> getInitialData()
  {
    return (List)serverInfo.get("initialData");
  }
  
  public void setInitialData(List<?> initialData) { serverInfo.put("initialData", initialData); }
  

  public Map<String, Object> getRecords(int from, int count)
  {
    List<List<Object>> page = rows.subList(from - 1, from - 1 + count);
    
    Map<String, Object> records = new HashMap();
    records.put("Page", page);
    records.put("Cursor", Integer.valueOf(from + 1));
    
    return records;
  }
  
  public int getCursor()
  {
    Object value = serverInfo.get("cursor");
    if (value != null)
      return ((Integer)value).intValue();
    return 0;
  }
  
  public void setCursor(int cursor) { serverInfo.put("cursor", Integer.valueOf(cursor)); }
  
  public String getServiceName()
  {
    return (String)serverInfo.get("serviceName");
  }
  
  public void setServiceName(String serviceName) { serverInfo.put("serviceName", serviceName); }
  
  public String[] getColumnNames()
  {
    return (String[])serverInfo.get("columnNames");
  }
  
  public void setColumnNames(String[] columnNames) { serverInfo.put("columnNames", columnNames); }
  
  public double getVersion()
  {
    Object value = serverInfo.get("version");
    if (value != null)
      return ((Double)value).doubleValue();
    return 0.0D;
  }
  
  public void setVersion(double version) { serverInfo.put("version", new Double(version)); }
  
  public List<List<Object>> rows()
  {
    return rows;
  }
  
  public void populate(ResultSet rs) throws IOException
  {
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int columnCount = rsmd.getColumnCount();
      String[] columnNames = new String[columnCount];
      
      int rowIndex = 0;
      List<List<Object>> initialData = new ArrayList();
      while (rs.next()) {
        rowIndex++;
        List<Object> row = new ArrayList();
        for (int column = 0; column < columnCount; column++) {
          if (rowIndex == 1) {
            columnNames[column] = rsmd.getColumnName(column + 1);
          }
          row.add(rs.getObject(column + 1));
        }
        if (rowIndex == 1) {
          setColumnNames(columnNames);
        }
        rows.add(row);
        if (rowIndex <= initialRowCount) {
          initialData.add(row);
        }
      }
      setTotalCount(rowIndex);
      setInitialData(initialData);
      setColumnNames(columnNames);
    } catch (SQLException e) {
      throw new IOException(e.getMessage());
    }
  }
  




  public void populate(String[] columnNames, List<List<Object>> rows)
  {
    this.rows = rows;
    
    List<List<Object>> initialData = rows.subList(0, initialRowCount > rows.size() ? rows.size() : initialRowCount);
    




    setInitialData(initialData);
    setTotalCount(rows.size());
    setColumnNames(columnNames);
  }
  







  public void populate(List<?> list, String[] ignoreProperties)
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
  {
    List<String> names = new ArrayList();
    Object firstBean = list.get(0);
    PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(firstBean);
    
    for (int i = 0; i < properties.length; i++) {
      PropertyDescriptor descriptor = properties[i];
      if (!ignoreProperty(descriptor, ignoreProperties)) {
        names.add(descriptor.getDisplayName());
      }
    }
    String[] columnNames = new String[names.size()];
    columnNames = (String[])names.toArray(columnNames);
    setColumnNames(columnNames);
    
    int rowIndex = 0;
    List<List<Object>> initialData = new ArrayList();
    Iterator<?> iterator = list.iterator();
    while (iterator.hasNext()) {
      rowIndex++;
      Object bean = iterator.next();
      List<Object> row = new ArrayList();
      for (int i = 0; i < properties.length; i++) {
        PropertyDescriptor descriptor = properties[i];
        if (!ignoreProperty(descriptor, ignoreProperties)) {
          Object value = null;
          Method readMethod = descriptor.getReadMethod();
          if (readMethod != null) {
            value = readMethod.invoke(bean, new Object[0]);
          }
          row.add(value);
        }
      }
      rows.add(row);
      if (rowIndex <= initialRowCount) {
        initialData.add(row);
      }
    }
    setInitialData(initialData);
    setTotalCount(rows.size());
    log.debug(this);
  }
  


  private boolean ignoreProperty(PropertyDescriptor descriptor, String[] ignoreProperties)
  {
    boolean ignore = false;
    if (descriptor.getName().equals("class")) {
      ignore = true;
    } else {
      for (int i = 0; i < ignoreProperties.length; i++) {
        String ignoreProp = ignoreProperties[i];
        if (ignoreProp.equals(descriptor.getName())) {
          log.debug("Ignoring " + descriptor.getName());
          ignore = true;
          break;
        }
      }
    }
    return ignore;
  }
  

  public String toString()
  {
    StringBuffer info = new StringBuffer();
    addInfo(info, "id", getId());
    addInfo(info, "totalCount", getTotalCount());
    addInfo(info, "cursor", getCursor());
    addInfo(info, "serviceName", getServiceName());
    addInfo(info, "version", getVersion());
    StringBuffer names = new StringBuffer();
    String[] columnNames = getColumnNames();
    if (columnNames != null) {
      for (int i = 0; i < columnNames.length; i++) {
        String name = columnNames[i];
        if (i > 0) {
          names.append(", ");
        }
        names.append(name);
      }
    }
    addInfo(info, "columnNames", names);
    addInfo(info, "initialData", getInitialData().toString());
    return info.toString();
  }
  
  private void addInfo(StringBuffer info, String name, int value) {
    addInfo(info, name, new Integer(value));
  }
  
  private void addInfo(StringBuffer info, String name, double value) {
    addInfo(info, name, new Double(value));
  }
  
  private void addInfo(StringBuffer info, String name, Object value) {
    info.append(name);
    info.append(" = ");
    info.append(value);
    info.append('\n');
  }
}
