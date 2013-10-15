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
package se.sll.codeserveradapter.paymentresponsible.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.sll.codeserveradapter.paymentresponsible.model.HSAMappingBean;

public class HSAMappingIndexBuilderTest {
   

    @Test
    public void parse_success() {
        HSAMappingIndexBuilder builder = new HSAMappingIndexBuilder()
        .withCommissionFile("src/test/resources/test-files/SAMVERKS-REL.xml")
        .withCommissionTypeFile("src/test/resources/test-files/UPPDRAGSTYP.xml")
        .withFacilityFile("src/test/resources/test-files/AVD-REL.xml")
        .withMekFile("src/test/resources/test-files/MEK.xml");
        
        final Map<String, List<HSAMappingBean>> index = builder.build();
      
        assertTrue(index.size() > 4000);
        
        for (List<HSAMappingBean> list : index.values()) {
            assertFalse(list.size() == 0);
            for (HSAMappingBean mapping : list) {
                assertNotNull(mapping.getFacility());
                assertTrue(mapping.getValidFrom().before(mapping.getValidTo()));
            }
        }
    }
}
