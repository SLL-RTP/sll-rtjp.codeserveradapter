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
package se.sll.codeserveradapter.paymentresponsible.service;

import java.util.List;
import java.util.Map;

import se.sll.codeserveradapter.paymentresponsible.model.HSAMappingBean;
import se.sll.codeserveradapter.paymentresponsible.util.HSAMappingIndexBuilder;

public class HSAMappingService {
    
    private static Map<String, List<HSAMappingBean>> currentIndex = null;

    public HSAMappingService() {
    }
    
    public Map<String, List<HSAMappingBean>> build() {
        HSAMappingIndexBuilder builder = new HSAMappingIndexBuilder()
        .withCommissionFile("src/test/resources/test-files/SAMVERKS-REL.xml")
        .withCommissionTypeFile("src/test/resources/test-files/UPPDRAGSTYP.xml")
        .withFacilityFile("src/test/resources/test-files/AVD-REL.xml")
        .withMekFile("src/test/resources/test-files/MEK.xml");
        
        final Map<String, List<HSAMappingBean>> index = builder.build();

        return index;
    }
    
    public void revalidate() {
        
    }
    
    public static HSAMappingService getInstance() {
        return new HSAMappingService();
    }
}
