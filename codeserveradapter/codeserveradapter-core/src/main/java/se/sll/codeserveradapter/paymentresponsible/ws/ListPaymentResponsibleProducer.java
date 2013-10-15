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

import riv.sll.paymentresponsible._1.ListPaymentResponsibleDataRequest;
import riv.sll.paymentresponsible._1.ListPaymentResponsibleDataResponse;
import riv.sll.paymentresponsible.listpaymentresponsibledata._1.rivtabp21.ListPaymentResponsibleDataResponderInterface;

public class ListPaymentResponsibleProducer implements ListPaymentResponsibleDataResponderInterface {

    @Override
    public ListPaymentResponsibleDataResponse listPaymentResponsibleData(
            final String logicalAddress,
            final ListPaymentResponsibleDataRequest request) {
        
        final ListPaymentResponsibleDataResponse response = new ListPaymentResponsibleDataResponse();
        response.setHsaId(request.getHsaId());
        response.setServiceType("Språktolk");

        return response;
    }

}
