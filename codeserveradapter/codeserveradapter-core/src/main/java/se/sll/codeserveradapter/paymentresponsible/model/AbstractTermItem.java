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
package se.sll.codeserveradapter.paymentresponsible.model;

import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * 
 * @author Peter
 *
 */
public class AbstractTermItem implements Comparable<AbstractTermItem> {
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

    private String id;
    private String name;
    private Date validFrom = MIN_DATE;
    private Date validTo = MAX_DATE;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
        this.validFrom = (validFrom == null) ? MIN_DATE :validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = (validTo == null) ? MAX_DATE : validTo;
    }
    
    
    public boolean isNewerThan(AbstractTermItem anotherItem) {
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
    public int compareTo(AbstractTermItem other) {
        return getValidFrom().compareTo(other.getValidFrom());
    }
}