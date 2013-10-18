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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * Entity uniquely identified by an id, but with a state that vary over time.
 * 
 * @author Peter
 *
 */
public class TermItem<T extends TermState> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * The unique id for this kind of state.
     * @serial
     */
    private String id;
    /**
     * The time series of state.
     * @serial
     */
    private List<T> stateVector = new ArrayList<T>();
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public List<T> getStateVector() {
        return stateVector;
    }
    
    public void addState(final T termState) {
        stateVector.add(termState);
    }
    
    /**
     * Returns state valid for a specific date and time.
     * 
     * @param date the date and time.
     * @return the state or null if none found.
     */
    public T getState(final Date date) {
        for (final T state : stateVector) {
            if (state.isValid(date)) {
                return state;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        final String id = getId();
        return (id == null) ? super.hashCode() : id.hashCode();
    }
    
    @Override
    public boolean equals(Object another) {
        if (this == another) {
            return true;
        }
        if (another instanceof TermItem) { 
            return getId().equals(((TermItem<?>)another).getId());
        }
        return false;
    }
}