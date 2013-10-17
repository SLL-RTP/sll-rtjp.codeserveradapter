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

import static org.junit.Assert.assertEquals;
import static se.sll.codeserveradapter.CodeserveradapterMuleServer.getAddress;

import org.junit.Test;
import org.soitoolkit.commons.mule.test.AbstractJmsTestUtil;
import org.soitoolkit.commons.mule.test.ActiveMqJmsTestUtil;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;

import riv.sll.paymentresponsible.listpaymentresponsibledataresponder._1.ListPaymentResponsibleDataResponseType;

/**
 * 
 * @author Peter
 *
 */
public class PaymentResponsibleIntegrationTest extends AbstractTestCase {


    //private static final Logger log = LoggerFactory.getLogger(PaymentResponsibleIntegrationTest.class);


    private static final String DEFAULT_SERVICE_ADDRESS = getAddress("pr.ws.inboundURL");


    private static final String ERROR_LOG_QUEUE = "SOITOOLKIT.LOG.ERROR";
    
    private AbstractJmsTestUtil jmsUtil = null;


    public PaymentResponsibleIntegrationTest() {


        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

    @Override
    protected String getConfigResources() {
        return "soitoolkit-mule-jms-connector-activemq-embedded.xml,"
                + "mule-config.xml,"
                + "payment-responsible-service.xml";
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
        final String id = "SE2321000016-14L4";
        PaymentResponsibleTestConsumer consumer = new PaymentResponsibleTestConsumer(DEFAULT_SERVICE_ADDRESS);
        ListPaymentResponsibleDataResponseType response = consumer.callService(id);
        assertEquals(id,  response.getPaymentResponsibleData().getHsaId());
    }
}
