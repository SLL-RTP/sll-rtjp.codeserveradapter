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
import se.sll.codeserveradapter.paymentresponsible.model.CareServiceState;
import se.sll.codeserveradapter.paymentresponsible.model.CommissionState;
import se.sll.codeserveradapter.paymentresponsible.model.CommissionTypeState;
import se.sll.codeserveradapter.paymentresponsible.model.CompanyState;
import se.sll.codeserveradapter.paymentresponsible.model.FacilityState;
import se.sll.codeserveradapter.paymentresponsible.model.HSAMappingState;

/**
 * Builds HSA Mapping index. <p>
 * 
 * Uses XML parsing utilities to traverse input files.
 * 
 * @author Peter
 */
public class HSAMappingIndexBuilder {
    // attribute and element names.

    private static final String VARDSERVICE = "Vårdservice";
    private static final String AVTAL = "AVTAL";
    private static final String STYP = "STYP";
    private static final String VARDSERVICE_TYP = "VårdserviceTyp";
    private static final String FORETAG = "FORETAG";
    private static final String TILL_DATUM = "TillDatum";
    private static final String FROM_DATUM = "FromDatum";
    private static final String HSA_ID = "HSAId";
    private static final String KOMBIKAKOD = "Kombikakod";
    private static final String POSTOFFICE = "postoffice";
    private static final String POSTNUMBER = "postnumber";
    private static final String OFFICEPOSTADDRESS = "officepostaddress";
    private static final String ABBREVIATION = "abbreviation";
    private static final String UPPDRAGSTYP = "UPPDRAGSTYP";
    private static final String NO_COMMISSION_ID = "0000";
    private static final String SAMVERKS = "SAMVERKS";
    private static final String SHORTNAME = "shortname";

    private static final Logger log = LoggerFactory.getLogger(HSAMappingIndexBuilder.class);

    private String mekFile;
    private String facilityFile;
    private String commissionFile;
    private String commissionTypeFile;
    private String companyFile;
    private String careServiceFile;
    
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
        this.facilityFile = facilityFile;
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
     * Input file for company (FORETAG) data (mandatory)
     * 
     * @param companyFile the input XML file name.
     * @return the builder.
     */
    public HSAMappingIndexBuilder withCompanyFile(String companyFile) {
        this.companyFile = companyFile;
        return this;
    }
    
    /**
     * Input file for care service (Vårdservice) data (mandatory)
     * @param careServiceFile the input file.
     * @return the builder
     */
    public HSAMappingIndexBuilder withCareServiceFile(String careServiceFile) {
        this.careServiceFile = careServiceFile;
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

        log.info("build hsaMappingIndex from: {}", mekFile);
        final Map<String, List<TermItem<HSAMappingState>>> hsaIndex = createHSAIndex();
        log.info("hsaMappingIndex size: {}", hsaIndex.size());

        return hsaIndex;
    }
    
    
    /**
     * Assumes list is empty or contains one item.
     * 
     * @param list the list.
     * @return null or first item.
     */
    protected static <T> T singleton(List<T> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        if (list.size() > 1) {
            log.warn("Expected singleton list " + list+ " has more than one entry (unsupported and unexpected behavior as result)");
        }
        return list.get(0);
    }

   
    //
    protected Map<String, List<TermItem<HSAMappingState>>> createHSAIndex() {
        log.info("build facilityIndex from: {}", facilityFile);
        final HashMap<String, TermItem<FacilityState>> avdIndex = createFacilityIndex();
        log.info("facilityIndex size: {}", avdIndex.size());

        final SimpleXMLElementParser elementParser = new SimpleXMLElementParser(this.mekFile);
        final Map<String, List<TermItem<HSAMappingState>>> map = new HashMap<String, List<TermItem<HSAMappingState>>>();

        final Map<String, Integer> elements = new HashMap<String, Integer>();
        elements.put(KOMBIKAKOD, 1);
        elements.put(HSA_ID, 2);
        elements.put(FROM_DATUM, 3);
        elements.put(TILL_DATUM, 4);


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
    protected HashMap<String, TermItem<CompanyState>> createCompanyIndex() {
        final HashMap<String, TermItem<CompanyState>> index = new HashMap<String, TermItem<CompanyState>>();
        final CodeServiceXMLParser parser = new CodeServiceXMLParser(this.companyFile, new CodeServiceEntryCallback() {
            @Override
            public void onCodeServiceEntry(CodeServiceEntry codeServiceEntry) {
                TermItem<CompanyState> company = index.get(codeServiceEntry.getId());
                if (company == null) {
                    company = new TermItem<CompanyState>();
                    company.setId(codeServiceEntry.getId());
                    index.put(company.getId(), company);
                }
                final CompanyState companyState = new CompanyState();
                companyState.setName(codeServiceEntry.getAttribute(ABBREVIATION));
                final String postAddress = codeServiceEntry.getAttribute(OFFICEPOSTADDRESS);
                if (postAddress != null) {
                    companyState.setAddressLine1(codeServiceEntry.getAttribute(OFFICEPOSTADDRESS));
                }
                final String postNumber = codeServiceEntry.getAttribute(POSTNUMBER);
                final String postOffice = codeServiceEntry.getAttribute(POSTOFFICE);
                if (postNumber != null && postOffice != null) {
                    companyState.setAddressLine2(postNumber + " " + postOffice);
                }
                companyState.setValidFrom(codeServiceEntry.getValidFrom());
                companyState.setValidTo(codeServiceEntry.getValidTo());
                company.addState(companyState);
            }
        });
        
        parser.extractAttribute(ABBREVIATION);
        parser.extractAttribute(OFFICEPOSTADDRESS);
        parser.extractAttribute(POSTNUMBER);
        parser.extractAttribute(POSTOFFICE);
        
        parser.setNewerThan(newerThan);
        
        parser.parse();

        return index;
    }

    //
    protected HashMap<String, TermItem<CareServiceState>> createCareServiceIndex() {
        log.info("build companyIndex from: {}", companyFile);
        final HashMap<String, TermItem<CompanyState>> companyIndex = createCompanyIndex();
        log.info("companyIndex size: {}", companyIndex.size());

        final HashMap<String, TermItem<CareServiceState>> index = new HashMap<String, TermItem<CareServiceState>>();
        final CodeServiceXMLParser parser = new CodeServiceXMLParser(this.careServiceFile, new CodeServiceEntryCallback() {
            @Override
            public void onCodeServiceEntry(CodeServiceEntry codeServiceEntry) {
                final String typeCode = singleton(codeServiceEntry.getCodes(VARDSERVICE_TYP));
                if (typeCode == null) {
                    return;
                }
                final String companyCode = singleton(codeServiceEntry.getCodes(FORETAG));
                if (companyCode == null) {
                    return;
                }
                final TermItem<CompanyState> company = companyIndex.get(companyCode);
                if (company == null) {
                    return;
                }
                TermItem<CareServiceState> careService = index.get(codeServiceEntry.getId());
                if (careService == null) {
                    careService = new TermItem<CareServiceState>();
                    careService.setId(codeServiceEntry.getId());
                    index.put(careService.getId(), careService);
                }
                final CareServiceState careServiceState = new CareServiceState();
                careServiceState.setName(codeServiceEntry.getAttribute(ABBREVIATION));
                careServiceState.setCompany(company);
                careServiceState.setCareServiceType(typeCode);
                careServiceState.setValidFrom(codeServiceEntry.getValidFrom());
                careServiceState.setValidTo(codeServiceEntry.getValidTo());
                careService.addState(careServiceState);
            }
        });
        
        parser.extractAttribute(ABBREVIATION);
        parser.extractCodeSystem(FORETAG);
        parser.extractCodeSystem(VARDSERVICE_TYP);
        
        parser.parse();
        
        return index;
    }
    
    //
    protected HashMap<String, TermItem<FacilityState>> createFacilityIndex() {
        log.info("build commissionIndex from: {}", commissionFile);
        final HashMap<String, TermItem<CommissionState>> samverksIndex = createCommissionIndex();
        log.info("commissionIndex size: {}", samverksIndex.size());

        final HashMap<String, TermItem<FacilityState>> index = new HashMap<String, TermItem<FacilityState>>();

        final CodeServiceXMLParser parser = new CodeServiceXMLParser(this.facilityFile, new CodeServiceEntryCallback() {
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
    protected HashMap<String, TermItem<CommissionState>> createCommissionIndex() {

        log.info("build careServiceIndex from: {}", careServiceFile);
        final HashMap<String, TermItem<CareServiceState>> careServiceIndex = createCareServiceIndex();
        log.info("careServiceIndex size: {}", careServiceIndex.size());

        final HashMap<String, TermItem<CommissionState>> index = new HashMap<String, TermItem<CommissionState>>();

        log.info("build commissionTypeIndex from: {}", commissionTypeFile);
        final HashMap<String, TermItem<CommissionTypeState>> uppdragstypIndex = createCommissionTypeIndex();
        log.info("commissionTypeIndex size: {}", uppdragstypIndex.size());

        final CodeServiceXMLParser parser = new CodeServiceXMLParser(this.commissionFile, new CodeServiceEntryCallback() {
            @Override
            public void onCodeServiceEntry(CodeServiceEntry codeServiceEntry) {
                final String uCode = singleton(codeServiceEntry.getCodes(UPPDRAGSTYP));
                final TermItem<CommissionTypeState> uppdragstyp = (uCode == null) ? null : uppdragstypIndex.get(uCode);
                if (uppdragstyp == null) {
                    log.trace("No such commission: {}",  uCode);
                    return;
                }
                final String sCode = singleton(codeServiceEntry.getCodes(VARDSERVICE));
                final TermItem<CareServiceState> careItem = (sCode == null) ? null : careServiceIndex.get(sCode);
                if (careItem == null) {
                    log.trace("No such care service code: {}", sCode);
                    return;
                }
                
                TermItem<CommissionState> commission = index.get(codeServiceEntry.getId());
                if (commission == null) {
                    commission = new TermItem<CommissionState>();
                    commission.setId(codeServiceEntry.getId());
                    index.put(codeServiceEntry.getId(), commission);
                }
                final CommissionState state = new CommissionState();
                state.setContractCode(singleton(codeServiceEntry.getCodes(AVTAL)));
                state.setName(codeServiceEntry.getAttribute(ABBREVIATION));
                state.setCommissionType(uppdragstyp);
                state.setValidFrom(codeServiceEntry.getValidFrom());
                state.setValidTo(codeServiceEntry.getValidTo());
                state.setCareService(careItem);
                
                commission.addState(state);
            }
        });

        parser.extractAttribute(ABBREVIATION);
        parser.extractCodeSystem(UPPDRAGSTYP);
        parser.extractCodeSystem(STYP);
        parser.extractCodeSystem(AVTAL);
        parser.extractCodeSystem(VARDSERVICE);
        
        parser.setNewerThan(newerThan);
    
        parser.parse();

        return index;
    }

    //
    protected HashMap<String, TermItem<CommissionTypeState>> createCommissionTypeIndex() {
        final HashMap<String, TermItem<CommissionTypeState>> index = new HashMap<String, TermItem<CommissionTypeState>>();

        final CodeServiceXMLParser parser = new CodeServiceXMLParser(this.commissionTypeFile, new CodeServiceEntryCallback() {
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
