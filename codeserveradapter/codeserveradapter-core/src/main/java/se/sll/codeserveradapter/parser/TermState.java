/**
 *  Copyright (c) 2013 SLL <http://sll.se/>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package se.sll.codeserveradapter.parser;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * State valid for a particular period of time.
 * 
 * @author Peter
 *
 */
public abstract class TermState implements Comparable<TermState>, Serializable {
    private static final long serialVersionUID = 1L;

    static Date MAX_DATE = new Date(Long.MAX_VALUE);
    static Date MIN_DATE = new Date(0L);

    static DatatypeFactory datatypeFactory;
    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The name.
     * @serial
     */
    private String name;
    /**
     * Valid from date and time.
     * @serial
     */
    private Date validFrom = MIN_DATE;
    /**
     * Valid to date and time.
     * @serial
     */
    private Date validTo = MAX_DATE;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = removeTime((validFrom == null) ? MIN_DATE :validFrom);
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = removeTime((validTo == null) ? MAX_DATE : validTo);
    }
    
    public static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    public boolean isValid(Date date) {
        return beforeOrEquals(date) && afterOrEquals(date);
    }
    
    private boolean beforeOrEquals(Date date) {
    	return getValidFrom().getTime() <= date.getTime();
    }
    
    private boolean afterOrEquals(Date date) {
    	return getValidTo().getTime() >= date.getTime();
    }
    
    public boolean isNewerThan(TermState anotherItem) {
        if (anotherItem == null) {
            return true;
        }        
        return isNewerThan(anotherItem.getValidTo());
    }
    
    public boolean isNewerThan(Date date) {
        return getValidTo().after(date);
    }
    
    public static Date toDate(String xmlDateTime) {
        final XMLGregorianCalendar cal = datatypeFactory.newXMLGregorianCalendar(xmlDateTime);
        return cal.toGregorianCalendar().getTime();    
    }

    @Override
    public int compareTo(TermState other) {
        return getValidFrom().compareTo(other.getValidFrom());
    }
}
