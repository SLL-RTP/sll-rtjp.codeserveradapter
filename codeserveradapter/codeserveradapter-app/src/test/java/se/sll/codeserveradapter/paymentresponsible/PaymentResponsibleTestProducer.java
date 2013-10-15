package se.sll.codeserveradapter.paymentresponsible;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import riv.sll.paymentresponsible._1.ListPaymentResponsibleDataRequest;
import riv.sll.paymentresponsible._1.ListPaymentResponsibleDataResponse;
import riv.sll.paymentresponsible.listpaymentresponsibledata._1.rivtabp21.ListPaymentResponsibleDataResponderInterface;


@WebService(serviceName = "sampleService", portName = "samplePort", targetNamespace = "urn:org.soitoolkit.refapps.sd.sample.wsdl:v1", name = "sampleService")
public class PaymentResponsibleTestProducer implements ListPaymentResponsibleDataResponderInterface {

	public static final String TEST_ID_OK               = "1234567890";
	public static final String TEST_ID_FAULT_INVALID_ID = "-1";
	public static final String TEST_ID_FAULT_TIMEOUT    = "0";
	
	private static final Logger log = LoggerFactory.getLogger(PaymentResponsibleTestProducer.class);
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("codeserveradapter-config");
	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));


    @Override
    public ListPaymentResponsibleDataResponse listPaymentResponsibleData(
           String logicalAddress,
           ListPaymentResponsibleDataRequest request) {
        log.info("PaymentResponsibleTestProducer received the request: {}", request);

        String id = request.getHsaId();

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
        ListPaymentResponsibleDataResponse response = new ListPaymentResponsibleDataResponse();
        response.setHsaId(id);
        return response;
    }
}


