package se.sll.codeserveradapter.parser;

import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public abstract class State implements Comparable<State> {
    
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

    private String name;
    
    private Date validFrom = MIN_DATE;
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
        this.validFrom = (validFrom == null) ? MIN_DATE :validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = (validTo == null) ? MAX_DATE : validTo;
    }
    
    public boolean isValid(Date date) {
        return getValidFrom().before(date) && getValidTo().after(date);
    }
    
    public boolean isNewerThan(State anotherItem) {
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
    public int compareTo(State other) {
        return getValidFrom().compareTo(other.getValidFrom());
    }
}
