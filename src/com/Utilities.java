/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import com.components.LinkButton;

import java.util.Vector;
import java.util.Stack;
import java.util.Enumeration;

import com.sun.lwuit.Button;
import com.sun.lwuit.Label;
import com.sun.lwuit.Container;
import com.sun.lwuit.Component;
import com.sun.lwuit.events.ActionListener;
/**
 *
 * @author caxthelm
 */
public class Utilities {
    
    public static String replace(String _sNeedle, String _sReplacement, String _sHaystack) {
        String result = "";
        int index = _sHaystack.indexOf(_sNeedle);
        if(index == 0) {
            result = _sReplacement+_sHaystack.substring(_sNeedle.length());
            return replace(_sNeedle, _sReplacement, result);
        }else if(index > 0) {
            result = _sHaystack.substring(0,index)+ _sReplacement +_sHaystack.substring(index+_sNeedle.length());
            return replace(_sNeedle, _sReplacement, result);
        }else {
            return _sHaystack;
        }
    }//end replace(String needle, String replacement, String haystack)
    
    public static String deletePart(String _sNeedle, String _sHaystack) {
        String result = "";
        int index = _sHaystack.indexOf(_sNeedle);
        while(index > 0) {
            String temp = _sHaystack.substring(0, index);
            temp += _sHaystack.substring(index + _sNeedle.length());
            _sHaystack = temp;
            index = _sHaystack.indexOf(_sNeedle);
        }
        return _sHaystack;
    }//end replace(String needle, String replacement, String haystack)
    
    public static String stripSlash(String _sHaystack) {
        String result = "";
        int index = _sHaystack.indexOf('\\');
        while(index > 0 && index + 1 < _sHaystack.length()) {
            String temp = _sHaystack.substring(0, index);
            boolean foundU = false;
            switch((char)_sHaystack.charAt(index+1)) {
                case '\"':
                    temp += '\"';
                    break;
                case '\'':
                    temp += '\'';
                    break;
                case 'n':
                    temp += '\n';
                    break;
                case 't':
                    temp += '\t';
                    break;
                case 'r':
                    temp += '\r';
                    break;
                case '\\':
                    temp += '\\';
                    break;
                case '/':
                    temp += '/';
                    break;
                case 'u':
                    foundU = true;
                    break;
            }
            if(index > 0) {
                if(foundU) {
                    temp += com.sun.lwuit.html.HTMLUtils.convertHTMLCharEntity("#x" + _sHaystack.substring(index + 2, index + 6));
                    temp += _sHaystack.substring(index + 6);
                } else {
                    temp += _sHaystack.substring(index + 2);
                }
                _sHaystack = temp;
                index = _sHaystack.indexOf('\\');
            }
        }
        return _sHaystack;
    }//end replace(String needle, String replacement, String haystack)
    
    //Used to strip out html from small sections of text.
    public static String stripHTML(String _sHaystack) {
        boolean done = false;
        StringBuffer retString = new StringBuffer();
        while(!done && _sHaystack.length() > 0) {
            int startIdx = _sHaystack.indexOf('<');
            int endIdx = _sHaystack.indexOf('>');
            int nextStartIdx = _sHaystack.indexOf('<');
            
            //we have no more real tags.
            if(startIdx == -1 || endIdx == -1){
                retString.append(_sHaystack);
                done = true;
            }
            //there was a non-tag '<' to deal with.
            if(endIdx < startIdx) {
                startIdx = _sHaystack.indexOf('<', startIdx);
                retString.append(_sHaystack.substring(0, startIdx + 1));
                _sHaystack = _sHaystack.substring(startIdx + 1);
                continue;
            }
            retString.append(_sHaystack.substring(0, startIdx));
            _sHaystack = _sHaystack.substring(endIdx + 1);
        }
        return retString.toString();
    }//end stripHTML(String _sHaystack)
    
    public static Vector getSectionsFromJSON(JsonObject _oJson) {
        Vector vReturnVec = null;
        if(_oJson == null)
            return null;
        
        Object oMobileView = _oJson.get("mobileview");
        if(oMobileView != null && oMobileView instanceof JsonObject) {
            Object oSections = ((JsonObject)oMobileView).get("sections");
            if(oSections != null && oSections instanceof Vector) {
                vReturnVec = (Vector)oSections;
            }
        }
        return vReturnVec;
    }//end getSectionsFromJSON(JsonObject _oJson)
    
    public static Vector getQueryResultsFromJSON(JsonObject _oJson) {
        Vector vReturnVec = null;
        if(_oJson == null)
            return null;
        
        Object oQuery = _oJson.get("query");
        if(oQuery != null && oQuery instanceof JsonObject) {
            Object oSearch = ((JsonObject)oQuery).get("search");
            if(oSearch != null && oSearch instanceof Vector) {
                vReturnVec = (Vector)oSearch;
            }
        }
        return vReturnVec;
    }//end getQueryResultsFromJSON(JsonObject _oJson)
    
    public static Vector getLanguagesFromJSON(JsonObject _oJson) {
        /*This structure is a little weird. it has base object -> query->pages
         * 
         */
        Vector vReturnVec = null;
        if(_oJson == null)
            return null;
        
        Object oQuery = _oJson.get("query");
        if(oQuery != null && oQuery instanceof JsonObject) {
            Object oPages = ((JsonObject)oQuery).get("pages");
            if(oPages != null && oPages instanceof JsonObject) {
                Enumeration pages = ((JsonObject)oPages).elements();
                while (pages.hasMoreElements()) {
                    JsonObject page = (JsonObject) pages.nextElement();
                    if(page.isEmpty()) {
                        continue;
                    }
                    vReturnVec = (Vector)page.get("langlinks");
                }
            }
        }
        return vReturnVec;
    }//end getSectionsFromJSON(JsonObject _oJson)
    
    public static String getNormalizedTitleFromJSON(JsonObject _oJson) {
        return ((JsonObject)((JsonObject)_oJson).get("mobileview")).getString("normalizedtitle");
    }//end getNormalizedTitleFromJSON(JsonObject _oJson)
}
