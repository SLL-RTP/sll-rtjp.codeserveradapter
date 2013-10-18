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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sll.codeserveradapter.parser.CodeServiceEntry;
import se.sll.codeserveradapter.parser.CodeServiceXMLParser;
import se.sll.codeserveradapter.parser.CodeServiceXMLParser.CodeServiceEntryCallback;
import se.sll.codeserveradapter.parser.SimpleXMLElementParser;
import se.sll.codeserveradapter.parser.SimpleXMLElementParser.ElementMatcherCallback;
import se.sll.codeserveradapter.parser.TermItem;
import se.sll.codeserveradapter.parser.TermState;
import se.sll.codeserveradapter.paymentresponsible.model.CommissionState;
import se.sll.codeserveradapter.paymentresponsible.model.CommissionTypeState;
import se.sll.codeserveradapter.paymentresponsible.model.FacilityState;
import se.sll.codeserveradapter.paymentresponsible.model.HSAMappingState;

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
     * @return a map with HSA ID as keys and {@link HSAMappingBean} as value objects.
     */
    public Map<String, List<TermItem<HSAMappingState>>> build() {
        log.info("build commissionTypeIndex from: {}", commissionTypeFile);
        final HashMap<String, TermItem<CommissionTypeState>> commissionTypeIndex = createCommissionTypeIndex();
        log.info("commissionTypeIndex size: {}", commissionTypeIndex.size());

        log.info("build commissionIndex from: {}", commissionFile);
        final HashMap<String, TermItem<CommissionState>> commissionIndex = createCommissionIndex(commissionTypeIndex);
        log.info("commissionIndex size: {}", commissionIndex.size());

        log.info("build facilityIndex from: {}", facilityFIle);
        final HashMap<String, TermItem<FacilityState>> facilityIndex = createFacilityIndex(commissionIndex);
        log.info("facilityIndex size: {}", facilityIndex.size());

        log.info("build hsaMappingIndex from: {}", mekFile);
        final Map<String, List<TermItem<HSAMappingState>>> hsaIndex = createHSAIndex(facilityIndex);
        log.info("hsaMappingIndex size: {}", hsaIndex.size());

        return hsaIndex;
    }

    //
    protected Map<String, List<TermItem<HSAMappingState>>> createHSAIndex(final HashMap<String, TermItem<FacilityState>> avdIndex) {
        SimpleXMLElementParser elementParser = new SimpleXMLElementParser(this.mekFile);
        final Map<String, List<TermItem<HSAMappingState>>> map = new HashMap<String, List<TermItem<HSAMappingState>>>();

        final Map<String, Integer> elements = new HashMap<String, Integer>();
        elements.put("Kombikakod", 1);
        elements.put("HSAId", 2);
        elements.put("FromDatum", 3);
        elements.put("TillDatum", 4);


        elementParser.parse("mappning", elements, new ElementMatcherCallback() {
            private TermItem<HSAMappingState> mapping = null;
            private HSAMappingState state = null;
            @Override
            public void match(int element, String data) {
                switch (element) {
                case 1:
                    state.setFacility(avdIndex.get(data));
                    break;
                case 2:
                    mapping.setId(data);
                    break;
                case 3:
                    state.setValidFrom(TermState.toDate(data));
                    break;
                case 4:
                    state.setValidTo(TermState.toDate(data));
                    break;
                }
            }

            @Override
            public void end() {
                if (state.isNewerThan(newerThan) && state.getFacility() != null) {
                    List<TermItem<HSAMappingState>> list = map.get(mapping.getId());
                    if (list == null) {
                        list = new ArrayList<TermItem<HSAMappingState>>();
                        map.put(mapping.getId(), list);
                    }
                    mapping.addState(state);
                    list.add(mapping);
                }
            }

            @Override
            public void begin() {
                state = new HSAMappingState();
                mapping = new TermItem<HSAMappingState>();
            }
        });

        return map;
    }


    //
    protected HashMap<String, TermItem<FacilityState>> createFacilityIndex(final HashMap<String, TermItem<CommissionState>> samverksIndex) {
        final HashMap<String, TermItem<FacilityState>> index = new HashMap<String, TermItem<FacilityState>>();

        CodeServiceXMLParser parser = new CodeServiceXMLParser(this.facilityFIle, new CodeServiceEntryCallback() {
            @Override
            public void onCodeServiceEntry(CodeServiceEntry codeServiceEntry) {
                final List<String> codes = codeServiceEntry.getCodes(SAMVERKS);
                if (codes != null) {
                    // filter out non-existing SAMVERKS associations 
                    if (codes.size() == 1 && NO_COMMISSION_ID.equals(codes.get(0))) {
                        return;
                    }
                    TermItem<FacilityState> avd = index.get(codeServiceEntry.getId());
                    if (avd == null) {
                        avd = new TermItem<FacilityState>();
                        avd.setId(codeServiceEntry.getId());
                        index.put(codeServiceEntry.getId(), avd);
                    }
                    final FacilityState state = new FacilityState();
                    state.setName(codeServiceEntry.getAttribute(SHORTNAME));
                    state.setValidFrom(codeServiceEntry.getValidFrom());
                    state.setValidTo(codeServiceEntry.getValidTo());
                    for (final String id : codes) {
                        final TermItem<CommissionState> samverks = samverksIndex.get(id);
                        // don't add the same twice
                        if (samverks != null) {
                            state.getCommissions().add(samverks);
                        }
                    }
                    avd.addState(state);
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
    protected HashMap<String, TermItem<CommissionState>> createCommissionIndex(final HashMap<String, TermItem<CommissionTypeState>> uppdragstypIndex) {
        final HashMap<String, TermItem<CommissionState>> index = new HashMap<String, TermItem<CommissionState>>();

        CodeServiceXMLParser parser = new CodeServiceXMLParser(this.commissionFile, new CodeServiceEntryCallback() {
            @Override
            public void onCodeServiceEntry(CodeServiceEntry codeServiceEntry) {
                TermItem<CommissionTypeState> uppdragstyp = null;
                List<String> ul = codeServiceEntry.getCodes(UPPDRAGSTYP);
                if (ul != null && ul.size() == 1) {
                    uppdragstyp = uppdragstypIndex.get(ul.get(0));
                }
                if (uppdragstyp == null) {
                    return;
                }
                TermItem<CommissionState> commission = index.get(codeServiceEntry.getId());
                if (commission == null) {
                    commission = new TermItem<CommissionState>();
                    commission.setId(codeServiceEntry.getId());
                    index.put(codeServiceEntry.getId(), commission);
                }
                final CommissionState state = new CommissionState();
                state.setName(codeServiceEntry.getAttribute(ABBREVIATION));
                state.setCommissionType(uppdragstyp);
                state.setValidFrom(codeServiceEntry.getValidFrom());
                state.setValidTo(codeServiceEntry.getValidTo());
                commission.addState(state);
            }
        });

        parser.extractAttribute(ABBREVIATION);
        parser.extractCodeSystem(UPPDRAGSTYP);
        parser.setNewerThan(newerThan);

        parser.parse();

        return index;
    }

    //
    protected HashMap<String, TermItem<CommissionTypeState>> createCommissionTypeIndex() {
        final HashMap<String, TermItem<CommissionTypeState>> index = new HashMap<String, TermItem<CommissionTypeState>>();

        CodeServiceXMLParser parser = new CodeServiceXMLParser(this.commissionTypeFile, new CodeServiceEntryCallback() {
            @Override
            public void onCodeServiceEntry(CodeServiceEntry codeServiceEntry) {
                TermItem<CommissionTypeState> commissionType = index.get(codeServiceEntry.getId());
                if (commissionType == null) {
                    commissionType = new TermItem<CommissionTypeState>();
                    commissionType.setId(codeServiceEntry.getId());
                    index.put(codeServiceEntry.getId(), commissionType);
                }
                final CommissionTypeState state = new CommissionTypeState();
                state.setName(codeServiceEntry.getAttribute(SHORTNAME));
                state.setValidFrom(codeServiceEntry.getValidFrom());
                state.setValidTo(codeServiceEntry.getValidTo());
                commissionType.addState(state);
            }
        });

        parser.extractAttribute(SHORTNAME);
        parser.setNewerThan(newerThan);

        parser.parse();

        return index;
    }    
}
