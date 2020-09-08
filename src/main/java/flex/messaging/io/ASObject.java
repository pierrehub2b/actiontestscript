/*
 * www.openamf.org
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */

package flex.messaging.io;

import java.util.HashMap;

public class ASObject extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    private String type;

    public ASObject() {
        super();
    }

    public ASObject(String type) {
        super();
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(toLowerCase(key));
    }

    @Override
    public Object get(Object key) {
        return super.get(toLowerCase(key));
    }

    @Override
    public Object put(String key, Object value) {
        return super.put((String)toLowerCase(key), value);
    }

    @Override
    public Object remove(Object key) {
        return super.remove(toLowerCase(key));
    }

    private Object toLowerCase(Object key) {
        return key;
    }

    public Object instantiate() {
        Object ret;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> clazz = loader.loadClass(type);
            ret = clazz.newInstance();
        } catch (Exception e) {
            ret = null;
        }
        return ret;
    }

    @Override
    public String toString() {
        return "ASObject[type=" + getType() + "," + super.toString() + "]";
    }
}