package se.sll.codeserveradapter.paymentresponsible.ws;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import riv.sll.paymentresponsible._1.Commission;
import riv.sll.paymentresponsible._1.PaymentResponsible;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataRequest;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:codeserveradapter-core-spring-context.xml")
public class AbstractProducerTest extends AbstractProducer {
		
	@Test
	public void testListPaymentResponsibleData() {
		getHSAMappingService().revalidate();
		
		ListPaymentResponsibleDataRequest listPRDReq = new ListPaymentResponsibleDataRequest();
		listPRDReq.setHsaId("SE2321000016-14L1");
		listPRDReq.setServiceCode("01");
		listPRDReq.setEventTime(toTime(new Date()));
		
		ListPaymentResponsibleDataResponse response = getPaymentResponsibleData0(listPRDReq);
		
		Assert.assertNotNull(response.getHsaId());
		for (Commission commission : response.getCommissionList()) {
			Assert.assertNotNull(commission);			
		}	
		
	}
	
}
