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
package se.sll.codeserveradapter.paymentresponsible;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import riv.sll.paymentresponsible._1.ResultCode;
import riv.sll.paymentresponsible._1.ResultCodeEnum;
import riv.sll.paymentresponsible.listpaymentresponsibledata._1.rivtabp21.ListPaymentResponsibleDataResponderInterface;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataRequest;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataResponse;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataResponseType;


@WebService(serviceName = "sampleService", portName = "samplePort", targetNamespace = "urn:org.soitoolkit.refapps.sd.sample.wsdl:v1", name = "sampleService")
public class PaymentResponsibleTestProducer implements ListPaymentResponsibleDataResponderInterface {

	public static final String TEST_ID_OK               = "1234567890";
	public static final String TEST_ID_FAULT_INVALID_ID = "-1";
	public static final String TEST_ID_FAULT_TIMEOUT    = "0";
	
	private static final Logger log = LoggerFactory.getLogger(PaymentResponsibleTestProducer.class);
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("codeserveradapter-config");
	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));


    @Override
    public ListPaymentResponsibleDataResponseType listPaymentResponsibleData(
           String logicalAddress,
           ListPaymentResponsibleDataRequest request) {
        log.info("PaymentResponsibleTestProducer received the request: {}", request);

        String id = null;
        
        if (request.getHsaId().size() > 0) {
        	id = request.getHsaId().get(0);
        }

        // Return an error-message if invalid id
        if (TEST_ID_FAULT_INVALID_ID.equals(id)) {
            throw new RuntimeException("Invalid Id: " + id);
        }

        // Force a timeout if zero Id
        if (TEST_ID_FAULT_TIMEOUT.equals(id)) {
            try {
                Thread.sleep(SERVICE_TIMOUT_MS + 1000);
            } catch (InterruptedException e) {}
        }

        // Produce the response
        ListPaymentResponsibleDataResponseType response = new ListPaymentResponsibleDataResponseType();
        ListPaymentResponsibleDataResponse data = new ListPaymentResponsibleDataResponse();
                
        response.getPaymentResponsibleData().add(data);
        data.setHsaId(id);
        
        ResultCode rc = new ResultCode();
        rc.setCode(ResultCodeEnum.OK);
        response.setResultCode(rc);
        return response;
    }
}


