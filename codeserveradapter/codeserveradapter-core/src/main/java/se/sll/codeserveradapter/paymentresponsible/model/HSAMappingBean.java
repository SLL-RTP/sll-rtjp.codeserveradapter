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


public class HSAMappingBean extends AbstractTermItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private FacilityBean facility;

    public FacilityBean getFacility() {
        return facility;
    }

    public void setFacility(FacilityBean facility) {
        this.facility = facility;
    }

    @Override
    public String toString() {
        return String.format("HSAId: %s, Kombika: %s, Period: %3$tY-%3$tm-%3$td - %4$tY-%4$tm-%4$td", getId(), facility.getId(), getValidFrom(), getValidTo());
    }
}
