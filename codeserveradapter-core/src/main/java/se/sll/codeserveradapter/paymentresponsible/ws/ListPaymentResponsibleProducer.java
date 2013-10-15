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
        response.setHealthcareServiceType("Språktolk");

        return response;
    }

}
