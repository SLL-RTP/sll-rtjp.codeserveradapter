package se.sll.codeserveradapter.paymentresponsible;

import static se.sll.codeserveradapter.CodeserveradapterMuleServer.getAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.sll.paymentresponsible._1.ListPaymentResponsibleDataRequest;
import riv.sll.paymentresponsible._1.ListPaymentResponsibleDataResponse;
import riv.sll.paymentresponsible.listpaymentresponsibledata._1.rivtabp21.ListPaymentResponsibleDataResponderInterface;
import se.sll.codeserveradapter.AbstractTestConsumer;

public class PaymentResponsibleTestConsumer extends AbstractTestConsumer<ListPaymentResponsibleDataResponderInterface> {

	private static final Logger log = LoggerFactory.getLogger(PaymentResponsibleTestConsumer.class);

	//private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("codeserveradapter-config");
	    
    public PaymentResponsibleTestConsumer(String serviceAddress) {
        super(ListPaymentResponsibleDataResponderInterface.class, serviceAddress);
    }

    public static void main(String[] args) {
            String serviceAddress = getAddress("PAYMENT-RESPONSIBLE_INBOUND_URL");
            PaymentResponsibleTestConsumer consumer = new PaymentResponsibleTestConsumer(serviceAddress);
            ListPaymentResponsibleDataResponse response = consumer.callService("1234");
            log.info("Returned value = " + response.getHsaId());
    }

    public ListPaymentResponsibleDataResponse callService(String id) {
            log.debug("Calling sample-soap-service with id = {}", id);
            final ListPaymentResponsibleDataRequest request = new ListPaymentResponsibleDataRequest();
            request.setHsaId(id);
            request.setBookingDate(now());
            request.setHealcareServiceType(1);
            final ListPaymentResponsibleDataResponse response = _service.listPaymentResponsibleData("location", request);
            
            return response;
    }	
	
}