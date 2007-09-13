/*
 * Copyright 2004-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.web.servlet.mvc;

import java.util.Collection;
import java.util.HashMap;
//import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
/**
 * A parameter map class that allows mixing of request parameters and controller parameters. If a controller
 * parameter is set with the same name as a request parameter the controller parameter value is retrieved.
 * 
 * @author Graeme Rocher
 * @author Kate Rhodes
 * 
 * @since Oct 24, 2005
 */
public class GrailsParameterMap implements Map {

	private Map parameterMap;
	private HttpServletRequest request;

    /**
     * Creates a GrailsParameterMap populating from the given request object
     * @param request The request object
     */
    public GrailsParameterMap(HttpServletRequest request) {
		super();

		this.request = request;
		this.parameterMap = new HashMap();
        final Map requestMap = request.getParameterMap();
        for (Iterator it = requestMap.keySet().iterator(); it.hasNext(); ){
			String key = (String) it.next();
            Object paramValue = getParameterValue(requestMap, key);
            parameterMap.put(key, paramValue);
            processNestedKeys(request, requestMap, key, key ,parameterMap);
        }
	}

    private Object getParameterValue(Map requestMap, String key) {
        Object paramValue = requestMap.get(key);
        if(paramValue instanceof String[]) {
            String[] multiParams = (String[])paramValue;
            if(multiParams.length == 1) {
                paramValue = multiParams[0];
            }
        }
        return paramValue;
    }

    /*
     * This method builds up a multi dimensional hash structure from the parameters so that nested keys such as "book.author.name"
     * can be addressed like params['author'].name
     *
     * This also allows data binding to occur for only a subset of the properties in the parameter map
     */
    private void processNestedKeys(HttpServletRequest request, Map requestMap, String key, String nestedKey, Map nestedLevel) {
        final int nestedIndex = nestedKey.indexOf('.');
        if(nestedIndex > -1) {
            final String nestedPrefix = nestedKey.substring(0, nestedIndex);
            if(request.getParameter(nestedPrefix)==null) {
                Map nestedMap = (Map)nestedLevel.get(nestedPrefix);
                if(nestedMap == null) {
                    nestedMap = new GrailsParameterMap(new HashMap(), request);
                    nestedLevel.put(nestedPrefix, nestedMap);
                }
                if(nestedIndex < nestedKey.length()-1) {
                    final String remainderOfKey = nestedKey.substring(nestedIndex + 1, nestedKey.length());
                    nestedMap.put(remainderOfKey,getParameterValue(requestMap, key) );
                    if(remainderOfKey.indexOf('.') >-1) {
                        processNestedKeys(request, requestMap,key,remainderOfKey,nestedMap);
                    }
                }
            }
        }
    }

    /**
     * This constructor does not populate the GrailsParameterMap from the request but instead uses
     * the supplied values
     *
     * @param values The values to populate with
     * @param request The request object
     */
	public GrailsParameterMap(Map values,HttpServletRequest request) {
		super();

		this.request = request;
		this.parameterMap = values;
	}

    /**
	 * @return Returns the request.
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	public int size() {
		return parameterMap.size();
	}

	public boolean isEmpty() {			
		return parameterMap.isEmpty();
	}

	public boolean containsKey(Object key) {
		return parameterMap.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return parameterMap.containsValue(value);
	}

	public Object get(Object key) {
		// removed test for String key because there 
		// should be no limitations on what you shove in or take out
		if (parameterMap.get(key) instanceof String []){
			String[] valueArray = (String[])parameterMap.get(key);
			if(valueArray == null){
				return null;
			}
			
			if(valueArray.length == 1) {
				return valueArray[0];
			}
		}
		return parameterMap.get(key);
		
	}

	public Object put(Object key, Object value) {
		return parameterMap.put(key, value);
	}

	public Object remove(Object key) {
		return parameterMap.remove(key);
	}

	public void putAll(Map map) {
		parameterMap.putAll(map);
	}

	public void clear() {
		parameterMap.clear();
	}

	public Set keySet() {
		return parameterMap.keySet();
	}

	public Collection values() {
		return parameterMap.values();
	}

	public Set entrySet() {
		return parameterMap.entrySet();
	}		

}
