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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sll.codeserveradapter.paymentresponsible.model.AbstractTermItem;
import se.sll.codeserveradapter.paymentresponsible.model.CodeServiceEntry;
import se.sll.codeserveradapter.paymentresponsible.model.Commission;
import se.sll.codeserveradapter.paymentresponsible.model.CommissionType;
import se.sll.codeserveradapter.paymentresponsible.model.Facility;
import se.sll.codeserveradapter.paymentresponsible.model.HSAMapping;

import static se.sll.codeserveradapter.paymentresponsible.util.CodeServiceXMLParser.CodeServiceEntryCallback;
import static se.sll.codeserveradapter.paymentresponsible.util.SimpleXMLElementParser.ElementMatcherCallback;

/**
 * Builds HSA Mapping index.
 * 
 * @author Peter
 */
public class HSAMappingIndexBuilder {
    // attribute and element names.
    private static final String ABBREVIATION = "abbreviation";
    private static final String UPPDRAGSTYP = "UPPDRAGSTYP";
    private static final String NO_COMMISSION_ID = "0000";
    private static final String SAMVERKS = "SAMVERKS";
    private static final String SHORTNAME = "shortname";

    private static final Logger log = LoggerFactory.getLogger(HSAMappingIndexBuilder.class);

    private String mekFile;
    private String facilityFIle;
    private String commissionFile;
    private String commissionTypeFile;
    private Date newerThan = CodeServiceXMLParser.ONE_YEAR_BACK;
    
    /**
     * Input file for mapping (MEK) data (mandatory).
     * 
     * @param mekFile the input XML file name.
     * @return the builder.
     */
    public HSAMappingIndexBuilder withMekFile(String mekFile) {
        this.mekFile = mekFile;
        return this;
    }

    /**
     * Input file for facility (AVD) data (mandatory).
     * 
     * @param facilityFile the input XML file name.
     * @return the builder.
     */
    public HSAMappingIndexBuilder withFacilityFile(String facilityFile) {
        this.facilityFIle = facilityFile;
        return this;
    }

    /**
     * Input file for commission (SAMVERKS) mapping data (mandatory).
     * 
     * @param commissionFile the input XML file name.
     * @return the builder.
     */
    public HSAMappingIndexBuilder withCommissionFile(String commissionFile) {
        this.commissionFile = commissionFile;
        return this;
    }
    
    /**
     * Input file for commission type (UPPDRAGSTYP) data (mandatory).
     * 
     * @param commissionTypeFile the input XML file name.
     * @return the builder.
     */
    public HSAMappingIndexBuilder withCommissionTypeFile(String commissionTypeFile) {
        this.commissionTypeFile = commissionTypeFile;
        return this;
    }
    
    /**
     * Indicates how to filter out old data items, default setting is to keep one year old data, i.e.
     * expiration date is less than one year back in time.
     * 
     * @param newerThan the date that data must be newer than to be stored in index, otherwise it's ignored.
     * @return the builder.
     */
    public HSAMappingIndexBuilder newerThan(Date newerThan) {
        this.newerThan = newerThan;
        return this;
    }
    
    /**
     * Builds the index.
     * 
     * @return a map with HSA ID as keys and {@link HSAMapping} as value objects.
     */
    public Map<String, List<HSAMapping>> build() {
        log.info("build commissionTypeIndex from: {}", commissionTypeFile);
        final HashMap<String, CommissionType> commissionTypeIndex = createCommissionTypeIndex();
        log.info("commissionTypeIndex size: {}", commissionTypeIndex.size());

        log.info("build commissionIndex from: {}", commissionFile);
        final HashMap<String, Commission> commissionIndex = createCommissionIndex(commissionTypeIndex);
        log.info("commissionIndex size: {}", commissionIndex.size());
        
        log.info("build facilityIndex from: {}", facilityFIle);
        final HashMap<String, Facility> facilityIndex = createFacilityIndex(commissionIndex);
        log.info("facilityIndex size: {}", facilityIndex.size());

        log.info("build hsaMappingIndex from: {}", mekFile);
        final Map<String, List<HSAMapping>> hsaIndex = createHSAIndex(facilityIndex);
        log.info("hsaMappingIndex size: {}", hsaIndex.size());

        return hsaIndex;
    }
    
    //
    protected Map<String, List<HSAMapping>> createHSAIndex(final HashMap<String, Facility> avdIndex) {
        SimpleXMLElementParser elementParser = new SimpleXMLElementParser(this.mekFile);
        final Map<String, List<HSAMapping>> map = new HashMap<String, List<HSAMapping>>();

        final Map<String, Integer> elements = new HashMap<String, Integer>();
        elements.put("Kombikakod", 1);
        elements.put("HSAId", 2);
        elements.put("FromDatum", 3);
        elements.put("TillDatum", 4);


        elementParser.parse("mappning", elements, new ElementMatcherCallback() {
            private HSAMapping mapping = null;
            @Override
            public void match(int element, String data) {
                switch (element) {
                case 1:
                    mapping.setFacility(avdIndex.get(data));
                    break;
                case 2:
                    mapping.setId(data);
                    break;
                case 3:
                    mapping.setValidFrom(AbstractTermItem.toDate(data));
                    break;
                case 4:
                    mapping.setValidTo(AbstractTermItem.toDate(data));
                    break;
                }
            }

            @Override
            public void end() {
                if (mapping.isNewerThan(newerThan) && mapping.getFacility() != null) {
                    List<HSAMapping> list = map.get(mapping.getId());
                    if (list == null) {
                        list = new ArrayList<HSAMapping>();
                        map.put(mapping.getId(), list);
                    }
                    list.add(mapping);
                }
            }

            @Override
            public void begin() {
                mapping = new HSAMapping();
            }
        });

        for (List<HSAMapping> l : map.values()) {
            Collections.sort(l);
        }

        return map;
    }
    

    //
    protected HashMap<String, Facility> createFacilityIndex(final HashMap<String, Commission> samverksIndex) {
        final HashMap<String, Facility> index = new HashMap<String, Facility>();

        CodeServiceXMLParser parser = new CodeServiceXMLParser(this.facilityFIle, new CodeServiceEntryCallback() {
            @Override
            public void onCodeServiceEntry(CodeServiceEntry codeServiceEntry) {
                final List<String> codes = codeServiceEntry.getCodes(SAMVERKS);
                if (codes != null) {
                    // filter out non-existing SAMVERKS associations 
                    if (codes.size() == 1 && NO_COMMISSION_ID.equals(codes.get(0))) {
                        return;
                    }
                    final Facility prev = index.get(codeServiceEntry.getId());
                    if (codeServiceEntry.isNewerThan(prev)) {
                        final Facility avd = new Facility();
                        avd.setId(codeServiceEntry.getId());
                        avd.setName(codeServiceEntry.getAttribute(SHORTNAME));
                        avd.setValidFrom(codeServiceEntry.getValidFrom());
                        avd.setValidTo(codeServiceEntry.getValidTo());
                        for (final String id : codes) {
                            final Commission samverks = samverksIndex.get(id);
                            if (samverks != null) {
                                avd.getCommissions().add(samverks);
                            }
                        }
                        index.put(codeServiceEntry.getId(), avd);
                    }
                }
            }
        });

        parser.extractAttribute(SHORTNAME);
        parser.extractCodeSystem(SAMVERKS);
        parser.setNewerThan(newerThan);
        
        parser.parse();

        return index;
    }

    //
    protected HashMap<String, Commission> createCommissionIndex(final HashMap<String, CommissionType> uppdragstypIndex) {
        final HashMap<String, Commission> index = new HashMap<String, Commission>();

        CodeServiceXMLParser parser = new CodeServiceXMLParser(this.commissionFile, new CodeServiceEntryCallback() {
            @Override
            public void onCodeServiceEntry(CodeServiceEntry codeServiceEntry) {
                final Commission prev = index.get(codeServiceEntry.getId());
                if (codeServiceEntry.isNewerThan(prev)) {
                    CommissionType uppdragstyp = null;
                    List<String> ul = codeServiceEntry.getCodes(UPPDRAGSTYP);
                    if (ul != null && ul.size() == 1) {
                        uppdragstyp = uppdragstypIndex.get(ul.get(0));
                    }
                    if (uppdragstyp != null) {
                        final Commission samverks = new Commission();
                        samverks.setId(codeServiceEntry.getId());
                        samverks.setName(codeServiceEntry.getAttribute(ABBREVIATION));
                        samverks.setCommissionType(uppdragstyp);
                        samverks.setValidFrom(codeServiceEntry.getValidFrom());
                        samverks.setValidTo(codeServiceEntry.getValidTo());
                        index.put(codeServiceEntry.getId(), samverks);
                    }
                }
            }
        });

        parser.extractAttribute(ABBREVIATION);
        parser.extractCodeSystem(UPPDRAGSTYP);
        parser.setNewerThan(newerThan);
        
        parser.parse();


        return index;
    }

    //
    protected HashMap<String, CommissionType> createCommissionTypeIndex() {
        final HashMap<String, CommissionType> index = new HashMap<String, CommissionType>();

        CodeServiceXMLParser parser = new CodeServiceXMLParser(this.commissionTypeFile, new CodeServiceEntryCallback() {
            @Override
            public void onCodeServiceEntry(CodeServiceEntry codeServiceEntry) {
                final CommissionType prev = index.get(codeServiceEntry.getId());
                if (codeServiceEntry.isNewerThan(prev)) {
                    final CommissionType uppdragstyp = new CommissionType();
                    uppdragstyp.setId(codeServiceEntry.getId());
                    uppdragstyp.setName(codeServiceEntry.getAttribute(SHORTNAME));
                    uppdragstyp.setValidFrom(codeServiceEntry.getValidFrom());
                    uppdragstyp.setValidTo(uppdragstyp.getValidTo());
                    index.put(codeServiceEntry.getId(), uppdragstyp);
                }
            }
        });

        parser.extractAttribute(SHORTNAME);
        parser.setNewerThan(newerThan);

        parser.parse();
        
        return index;
    }    
}
