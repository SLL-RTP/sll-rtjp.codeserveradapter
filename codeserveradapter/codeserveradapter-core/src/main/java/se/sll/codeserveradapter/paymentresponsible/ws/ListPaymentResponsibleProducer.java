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
package se.sll.codeserveradapter.paymentresponsible.ws;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import riv.sll.paymentresponsible._1.Commission;
import riv.sll.paymentresponsible._1.ResultCode;
import riv.sll.paymentresponsible._1.ResultCodeEnumType;
import riv.sll.paymentresponsible.listpaymentresponsibledata._1.rivtabp21.ListPaymentResponsibleDataResponderInterface;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataRequest;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataResponse;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataResponseType;
import se.sll.codeserveradapter.parser.TermItem;
import se.sll.codeserveradapter.paymentresponsible.model.CommissionState;
import se.sll.codeserveradapter.paymentresponsible.model.FacilityState;
import se.sll.codeserveradapter.paymentresponsible.model.HSAMappingState;
import se.sll.codeserveradapter.paymentresponsible.service.HSAMappingService;

/**
 * Implements the payment responsible Web Service.
 * 
 * @author Peter
 *
 */
public class ListPaymentResponsibleProducer implements ListPaymentResponsibleDataResponderInterface {

    public static XMLGregorianCalendar toTime(Date date) {
        if (date == null) {
            return null;
        }
        try {
            final GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(date);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a {@link Date} date and time representation.
     *
     * @param cal the actual date and time.
     * @return the {@link Date} representation.
     */
    public static Date toDate(XMLGregorianCalendar cal) {
        return (cal == null) ? null : cal.toGregorianCalendar().getTime();
    }

    @Override
    public ListPaymentResponsibleDataResponseType listPaymentResponsibleData(
            final String logicalAddress,
            final ListPaymentResponsibleDataRequest request) {

        final ListPaymentResponsibleDataResponseType response = new ListPaymentResponsibleDataResponseType();

        final ResultCode rc = new ResultCode();
        response.setResultCode(rc);

        final ListPaymentResponsibleDataResponse data = new ListPaymentResponsibleDataResponse();
        data.setHsaId(request.getHsaId());
        data.setServiceCode(request.getServiceCode());


        final Map<String, List<TermItem<HSAMappingState>>> index = HSAMappingService.getInstance().getCurrentIndex();
        final List<TermItem<HSAMappingState>> hsaMappingTerms = index.get(request.getHsaId());

        if (hsaMappingTerms == null) {
            rc.setCode(ResultCodeEnumType.ERROR);
            rc.setComment("No such HSA ID found: " + request.getHsaId());
            return response;
        }

        final Date eventTime = toDate(request.getEventTime());
        final Map<String, Commission> map = new HashMap<String, Commission>();

        for (final TermItem<HSAMappingState> hsaMappingTerm : hsaMappingTerms) {
            final HSAMappingState mappingState = hsaMappingTerm.getState(eventTime);
            if (mappingState != null) {
                final FacilityState facilityState = mappingState.getFacility().getState(eventTime);
                if (facilityState != null) {
                    processFacility(facilityState, mappingState.getFacility().getId(), eventTime, map);
                }
            }
        }

        data.getCommissionList().addAll(map.values());

        // sort stuff in time order
        Collections.sort(data.getCommissionList(), new Comparator<Commission>() {
            @Override
            public int compare(Commission left, Commission right) {
                return left.getValidFrom().compare(right.getValidFrom());
            }
        });

        response.setPaymentResponsibleData(data);
        rc.setCode(ResultCodeEnumType.OK);
        return response;
    }

    //
    static void processFacility(final FacilityState facilityState, 
            final String kombikaId, 
            final Date eventTime,  
            final Map<String, Commission> map) {

        for (final TermItem<CommissionState> commissionTerm : facilityState.getCommissions()) {
            final Commission commission = createCommission(commissionTerm, eventTime);
            if (commission != null) {
                commission.setKombikaId(kombikaId);
                final Commission prev = map.get(commission.getId());
                if (prev != null) {
                    prev.setKombikaId(prev.getKombikaId() + "," + kombikaId);
                } else {
                    map.put(commission.getId(), commission);
                }
            }
        }
    }

    //
    static Commission createCommission(TermItem<CommissionState> commissionTerm, Date eventTime) {
        final CommissionState commissionState = commissionTerm.getState(eventTime);
        if (commissionState == null) {
            return null;
        }
        final Commission commission = new Commission();
        commission.setId(commissionTerm.getId());
        commission.setType(commissionState.getCommissionType().getState(eventTime).getName());
        commission.setValidFrom(toTime(commissionState.getValidFrom()));
        commission.setValidTo(toTime(commissionState.getValidTo()));
        commission.setName(commissionState.getName());
        return commission;
    }
}
