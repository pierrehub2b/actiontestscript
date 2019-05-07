package flex.messaging.io;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;

public class ArrayCollection
  extends ArrayList<Object>
  implements Externalizable
{
  private static final long serialVersionUID = 1L;
  
  public ArrayCollection() {}
  
  public ArrayCollection(int capacity)
  {
    super(capacity);
  }
  
  public ArrayCollection(Collection<?> col) {
    super(col);
  }
  
  public ArrayCollection(Object[] array)
  {
    addAll(array);
  }
  
  public void addAll(Object[] array) {
    for (Object o : array)
      add(o);
  }
  
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(toArray());
  }
  
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    Object o = in.readObject();
    if (o != null) {
      if ((o instanceof Collection)) {
        addAll((Collection)o);
      } else if (o.getClass().isArray()) {
        addAll((Object[])o);
      } else {
        add(o);
      }
    }
  }
  
  public String toString() {
    return super.toString();
  }
}
