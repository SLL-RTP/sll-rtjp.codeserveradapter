package se.sll.codeserveradapter.paymentresponsible;

import static org.junit.Assert.assertEquals;
import static se.sll.codeserveradapter.CodeserveradapterMuleServer.getAddress;
import static se.sll.codeserveradapter.paymentresponsible.PaymentResponsibleTestProducer.TEST_ID_OK;

import org.junit.Test;
import org.soitoolkit.commons.mule.test.AbstractJmsTestUtil;
import org.soitoolkit.commons.mule.test.ActiveMqJmsTestUtil;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;

import riv.sll.paymentresponsible._1.ListPaymentResponsibleDataResponse;


public class PaymentResponsibleIntegrationTest extends AbstractTestCase {


    //private static final Logger log = LoggerFactory.getLogger(PaymentResponsibleIntegrationTest.class);


    private static final String DEFAULT_SERVICE_ADDRESS = getAddress("PAYMENT-RESPONSIBLE_INBOUND_URL");


    private static final String ERROR_LOG_QUEUE = "SOITOOLKIT.LOG.ERROR";
    private AbstractJmsTestUtil jmsUtil = null;


    public PaymentResponsibleIntegrationTest() {


        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

    protected String getConfigResources() {
        return "soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
                "codeserveradapter-common.xml," +
                "payment-responsible-service.xml";
    }

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        doSetUpJms();

    }

    private void doSetUpJms() {
        // TODO: Fix lazy init of JMS connection et al so that we can create jmsutil in the declaration
        // (The embedded ActiveMQ queue manager is not yet started by Mule when jmsutil is delcared...)
        if (jmsUtil == null) jmsUtil = new ActiveMqJmsTestUtil();


        // Clear queues used for error handling
        jmsUtil.clearQueues(ERROR_LOG_QUEUE);
    }


    @Test
    public void test_ok() {
        String id = TEST_ID_OK;
        PaymentResponsibleTestConsumer consumer = new PaymentResponsibleTestConsumer(DEFAULT_SERVICE_ADDRESS);
        ListPaymentResponsibleDataResponse response = consumer.callService(id);
        assertEquals(id,  response.getHsaId());
    }
}
