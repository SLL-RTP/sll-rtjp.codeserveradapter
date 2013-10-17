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

import static se.sll.codeserveradapter.CodeserveradapterMuleServer.getAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.sll.paymentresponsible.listpaymentresponsibledata._1.rivtabp21.ListPaymentResponsibleDataResponderInterface;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataRequest;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataResponseType;
import se.sll.codeserveradapter.AbstractTestConsumer;

public class PaymentResponsibleTestConsumer extends AbstractTestConsumer<ListPaymentResponsibleDataResponderInterface> {

	private static final Logger log = LoggerFactory.getLogger(PaymentResponsibleTestConsumer.class);

	//private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("codeserveradapter-config");
	    
    public PaymentResponsibleTestConsumer(String serviceAddress) {
        super(ListPaymentResponsibleDataResponderInterface.class, serviceAddress);
    }

    public static void main(String[] args) {
            String serviceAddress = getAddress("pr.ws.inboundURL");
            PaymentResponsibleTestConsumer consumer = new PaymentResponsibleTestConsumer(serviceAddress);
            ListPaymentResponsibleDataResponseType response = consumer.callService("1234");
            log.info("Returned value = " + response.getPaymentResponsibleData().getHsaId());
    }

    public ListPaymentResponsibleDataResponseType callService(String id) {
            log.debug("Calling sample-soap-service with id = {}", id);
            final ListPaymentResponsibleDataRequest request = new ListPaymentResponsibleDataRequest();
            request.setHsaId(id);
            request.setEventTime(now());
            request.setServiceCode("01");
            final ListPaymentResponsibleDataResponseType response = _service.listPaymentResponsibleData("location", request);
            
            return response;
    }	
	
}