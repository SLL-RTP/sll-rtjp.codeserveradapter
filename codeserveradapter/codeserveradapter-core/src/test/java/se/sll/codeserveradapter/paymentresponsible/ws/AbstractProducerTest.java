package se.sll.codeserveradapter.paymentresponsible.ws;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import riv.sll.paymentresponsible._1.Commission;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataRequest;
import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:codeserveradapter-core-spring-context.xml")
public class AbstractProducerTest extends AbstractProducer {
		
	@Test
	public void testListPaymentResponsibleData() {
		getHSAMappingService().revalidate();
		
		List<String> hsaIdList = new ArrayList<String>();
		hsaIdList.add("SE2321000016-14L1");
		
		ListPaymentResponsibleDataRequest listPRDReq = new ListPaymentResponsibleDataRequest();
		listPRDReq.getHsaId().addAll(hsaIdList);
		listPRDReq.setServiceCode("01");
		listPRDReq.setEventTime(toTime(new Date()));
		
		List<ListPaymentResponsibleDataResponse> responseList = getPaymentResponsibleData(listPRDReq);
		for (ListPaymentResponsibleDataResponse responseItem : responseList) {
		
			Assert.assertNotNull(responseItem.getHsaId());
			for (Commission commission : responseItem.getCommissionList()) {
				Assert.assertNotNull(commission);			
			}	
		}
	}
	
	@Test
	public void testListPaymentResponsibleDataWithBatchRequest() {
		getHSAMappingService().revalidate();
		
		List<String> hsaIdList = new ArrayList<String>();
		hsaIdList.add("SE2321000016-14L1");
		hsaIdList.add("SE2321000016-6438");
		hsaIdList.add("SE2321000016-5RPN");
		
		ListPaymentResponsibleDataRequest listPRDReq = new ListPaymentResponsibleDataRequest();
		listPRDReq.getHsaId().addAll(hsaIdList);
		listPRDReq.setServiceCode("01");
		listPRDReq.setEventTime(toTime(new Date()));
		
		List<ListPaymentResponsibleDataResponse> responseList = getPaymentResponsibleData(listPRDReq);
		for (ListPaymentResponsibleDataResponse responseItem : responseList) {
		
			Assert.assertNotNull(responseItem.getHsaId());
			for (Commission commission : responseItem.getCommissionList()) {
				Assert.assertNotNull(commission);			
			}	
		}
	}
	
}
