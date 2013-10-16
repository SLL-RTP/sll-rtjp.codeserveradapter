package se.sll.codeserveradapter.paymentresponsible.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import se.sll.codeserveradapter.paymentresponsible.model.HSAMappingBean;
import se.sll.codeserveradapter.paymentresponsible.util.HSAMappingIndexBuilder;

@Service
public class HSAMappingService {
    
    private Map<String, List<HSAMappingBean>> currentIndex;

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
