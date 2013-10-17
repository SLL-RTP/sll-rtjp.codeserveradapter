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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author Peter
 *
 */
public class AbstractTermItem<T extends State> {
    private String id;
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
    
    public void addState(final T state) {
        stateVector.add(state);
    }
    
    public T getState(final Date date) {
        for (final T state : stateVector) {
            if (state.getValidFrom().before(date) && state.getValidTo().after(date)) {
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
        if (another instanceof AbstractTermItem) { 
            return getId().equals(((AbstractTermItem<?>)another).getId());
        }
        return false;
    }
}
