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

import riv.sll.paymentresponsible._1.ResultCode;
import riv.sll.paymentresponsible._1.ResultCodeEnum;
import riv.sll.paymentresponsible.listpaymentresponsibledata._1.rivtabp21.ListPaymentResponsibleDataResponderInterface;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataRequest;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataResponseType;
import se.sll.codeserveradapter.paymentresponsible.service.HSAMappingService;

/**
 * Mule implementation of the responsible Web Service.
 * 
 * @author Peter
 *
 */
public class ListPaymentResponsibleProducer extends AbstractProducer implements ListPaymentResponsibleDataResponderInterface {
    
    @Override
    public ListPaymentResponsibleDataResponseType listPaymentResponsibleData(
            final String logicalAddress,
            final ListPaymentResponsibleDataRequest request) {
        
        final ListPaymentResponsibleDataResponseType response = new ListPaymentResponsibleDataResponseType();
        final ResultCode rc = new ResultCode();
        try {
            response.setPaymentResponsibleData(getPaymentResponsibleData0(request));
            rc.setCode(ResultCodeEnum.OK);
        } catch (NotFoundException ex) {
            rc.setCode(ResultCodeEnum.ERROR);
            rc.setMessage(ex.getMessage());
        }
        
        return response;
    }
    
    // Auto-wiring doesn't work when running as a Mule app
    @Override
    protected HSAMappingService getHSAMappingService() {
        return HSAMappingService.getInstance();
    }

}
