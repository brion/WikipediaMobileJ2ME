/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/**
 *
 * @author caxthelm
 * 
 * JsonObjects are an extension of Hashtables with extra methods to make sure memory
 * has been nulled and ready for garbage collection (failsafe for low end devices).
 */
public class JsonObject extends Hashtable {
    public JsonObject() {
        super();
    }
    
    public Object get(Object _key) {
        if(this.containsKey(_key)) {
            return super.get(_key);
        }
        return null;                
    }
    
    public void cleanChildren() {
        Enumeration keys = keys();
        while(keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            Object child = this.get(key);
            cleanChild(child);
            this.remove(key);
        }
    }//end cleanChildren()
    
    public static void cleanChild(Object _oChild) {
        if(_oChild instanceof Vector) {
            Vector vecChild = (Vector)_oChild;
            while(!vecChild.isEmpty()) {
                cleanChild(vecChild.firstElement());
                vecChild.removeElementAt(0);
            }
        }else if(_oChild instanceof JsonObject) {
            ((JsonObject)_oChild).cleanChildren();
        }
    }//end cleanChild()
    
    public String getString(String _sKey)
    {
        Object value = get(_sKey);
        if(value != null && value instanceof String) {
            return (String)value;
        }else if(value != null && value instanceof Integer) {
            return ((Integer)value).toString();
        }
        return "";
    }
    
    public Integer getInteger(String _sKey)
    {
        Object value = get(_sKey);
        if(value != null && value instanceof Integer) {
            return (Integer)value;
        }
        return null;
    }
}
