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

import java.io.Serializable;

import se.sll.codeserveradapter.parser.TermItem;
import se.sll.codeserveradapter.parser.TermState;

/**
 * HSA Mapping from HSA ID to facilities. The id id corresponds to HSA ID.
 * 
 * @author Peter
 *
 */
public class HSAMappingState extends TermState implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * The facility.
     * @serial
     */
    private TermItem<FacilityState> facility;

    public TermItem<FacilityState> getFacility() {
        return facility;
    }

    public void setFacility(TermItem<FacilityState> facility) {
        this.facility = facility;
    }
}