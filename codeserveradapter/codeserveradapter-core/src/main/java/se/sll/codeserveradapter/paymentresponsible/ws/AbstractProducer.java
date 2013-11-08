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

import javax.annotation.Resource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.binding.soap.SoapFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import riv.sll.paymentresponsible._1.Commission;
import riv.sll.paymentresponsible._1.PaymentResponsible;
import riv.sll.paymentresponsible._1.ResultCode;
import riv.sll.paymentresponsible._1.ResultCodeEnum;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataRequest;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataResponse;
import se.sll.codeserveradapter.jmx.StatusBean;
import se.sll.codeserveradapter.parser.TermItem;
import se.sll.codeserveradapter.paymentresponsible.model.CareServiceState;
import se.sll.codeserveradapter.paymentresponsible.model.CommissionState;
import se.sll.codeserveradapter.paymentresponsible.model.CompanyState;
import se.sll.codeserveradapter.paymentresponsible.model.FacilityState;
import se.sll.codeserveradapter.paymentresponsible.model.HSAMappingState;
import se.sll.codeserveradapter.paymentresponsible.service.HSAMappingService;

public abstract class AbstractProducer {
    private static final String UNKNOWN_RESP = "Okänd";
    private static final String TIO_HUNDRA_RESP = "TioHundra";
    private static final String HSF_RESP = "HSF";
    private static final String SPRAKTOLK_SERVICE_CODE = "01";
    private static final Logger log = LoggerFactory.getLogger("WS-API");
    private static final String SERVICE_CONSUMER_HEADER_NAME = "x-rivta-original-serviceconsumer-hsaid";

    @Autowired
    private StatusBean statusBean;
    
    @Autowired
    private HSAMappingService hsaMappingService;

    @Resource
    private WebServiceContext webServiceContext;

    //
    static class NotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * Creates an exception.
         *
         * @param message the user message in plain text.
         */
        protected NotFoundException(String message) {
            super(message);
        }

    }
    
    /**
     * Returns the mapping service.
     * 
     * @return the service instance.
     */
    protected HSAMappingService getHSAMappingService() {
        return hsaMappingService;
    }
    
    /**
     * Creates a soap fault.
     *
     * @param throwable the cause.
     * @return the soap fault object.
     */
    protected SoapFault createSoapFault(Throwable throwable) {
        final String msg = createLogMessage(throwable.toString());
        log.error(msg, throwable);

        final SoapFault soapFault = createSoapFault(msg);

        return soapFault;
    }

    /**
     * Creates a soap fault.
     *
     * @param msg the message.
     * @return the soap fault object.
     */
    protected SoapFault createSoapFault(final String msg) {
        final SoapFault soapFault = new SoapFault(msg, SoapFault.FAULT_CODE_SERVER);
        return soapFault;
    }


    /**
     * Returns status bean.
     *
     * @return the status bean.
     */
    protected StatusBean getStatusBean() {
        return statusBean;
    }
    /**
     * Returns the actual message context.
     *
     *
     * @return the message context.
     */
    protected MessageContext getMessageContext() {
        return webServiceContext.getMessageContext();
    }

    /**
     *
     * Logs message context information.
     *
     * @param messageContext the context.
     */
    private void log(MessageContext messageContext) {
        final Map<?, ?> headers = (Map<?, ?>)messageContext.get(MessageContext.HTTP_REQUEST_HEADERS);
        log.info(createLogMessage(headers.get(SERVICE_CONSUMER_HEADER_NAME)));
        log.debug("HTTP Headers {}", headers);
    }

    /**
     * Creates a log message.
     *
     * @param msg the message.
     * @return the log message.
     */
    protected String createLogMessage(Object msg) {
        return String.format("%s - %s - \"%s\"", statusBean.getName(), statusBean.getGUID(), (msg == null) ? "NA" : msg);
    }

    /**
     * Runs a runnable in an instrumented manner.
     *
     * @param runnable the runnable to run.
     * @return the result code.
     */
    protected ResultCode fulfill(final Runnable runnable) {
        final MessageContext messageContext = getMessageContext();
        final String path = (String)messageContext.get(MessageContext.PATH_INFO);
        statusBean.start(path);
        log(messageContext);
        final ResultCode rc = new ResultCode();
        try {
            runnable.run();
            rc.setCode(ResultCodeEnum.OK);
        } catch (NotFoundException ex) {
            rc.setCode(ResultCodeEnum.ERROR);
            rc.setMessage(ex.getMessage() + " (" + statusBean.getGUID() + ")");
            log.error(createLogMessage(ex.getMessage()));
        } catch (Throwable throwable) {
            throw createSoapFault(throwable);
        } finally {
            statusBean.stop(rc.getCode() == ResultCodeEnum.OK);
        }

        log.debug("stats: {}", statusBean.getPerformanceMetricsAsJSON());

        return rc;
    }
    
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

    
    //
    protected ListPaymentResponsibleDataResponse getPaymentResponsibleData0(
            final ListPaymentResponsibleDataRequest request) {


        final ListPaymentResponsibleDataResponse data = new ListPaymentResponsibleDataResponse();
        data.setHsaId(request.getHsaId());
        data.setServiceCode(request.getServiceCode());


        final Date eventTime = toDate(request.getEventTime());

        final Map<String, List<TermItem<HSAMappingState>>> index = getHSAMappingService().getCurrentIndex();
        final List<TermItem<HSAMappingState>> hsaMappingTerms = index.get(request.getHsaId());

        if (hsaMappingTerms == null) {
            throw new NotFoundException(String.format("No mapping to kombika found for HsaId: %s",
                    request.getHsaId()));
        }

        final Map<String, Commission> map = new HashMap<String, Commission>();

        for (final TermItem<HSAMappingState> hsaMappingTerm : hsaMappingTerms) {
            final HSAMappingState mappingState = hsaMappingTerm.getState(eventTime);
            if (mappingState != null) {
                final FacilityState facilityState = mappingState.getFacility().getState(eventTime);
                if (facilityState != null) {
                    log.debug("process facility: {}", facilityState.getName());
                    processFacility(facilityState, mappingState.getFacility().getId(), eventTime, map);
                }
            }
        }

        data.getCommissionList().addAll(map.values());

        log.info("comissions found {}", data.getCommissionList().size());

        if (data.getCommissionList().size() == 0) {
            throw new NotFoundException(String.format("No valid commisions found for HsaId: %s and ServiceCode: %s at the given time: %tFT%<tRZ",
                    request.getHsaId(),
                    request.getServiceCode(),
                    eventTime));
        }
        
        // sort stuff in time order
        Collections.sort(data.getCommissionList(), new Comparator<Commission>() {
            @Override
            public int compare(Commission left, Commission right) {
                return left.getValidFrom().compare(right.getValidFrom());
            }
        });

        return data;
    }

    //
    protected static String mapCompanyId(String id) {
      if (id.equals(HSAMappingService.getInstance().getHSFCode())) {
          return HSF_RESP;
      }
      if (id.equals(HSAMappingService.getInstance().getTioHundraCode())) {
          return TIO_HUNDRA_RESP;
      }
      return UNKNOWN_RESP;
    }
    
    //
    protected static void processFacility(final FacilityState facilityState, 
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
                    final CommissionState state = commissionTerm.getState(eventTime);
                    final CareServiceState careServiceState = state.getCareService().getState(eventTime);
                    if (careServiceState != null && SPRAKTOLK_SERVICE_CODE.equals(careServiceState.getCareServiceType())) {
                        final PaymentResponsible pr = new PaymentResponsible();
                        pr.setName(careServiceState.getName());
                        final CompanyState companyState = careServiceState.getCompany().getState(eventTime);
                        pr.setId(mapCompanyId(careServiceState.getCompany().getId()));
                        pr.setAddressLine1(companyState.getAddressLine1());
                        pr.setAddressLine2(companyState.getAddressLine2());
                        pr.setName(companyState.getName());
                        commission.setPaymentResponsible(pr);
                        map.put(commission.getId(), commission);
                    }
                }
            }
        }
    }

    //
    protected static Commission createCommission(TermItem<CommissionState> commissionTerm, Date eventTime) {
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
        commission.setContractCode(commissionState.getContractCode());
        
        return commission;
    }
}
