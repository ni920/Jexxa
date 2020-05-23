package io.jexxa.infrastructure.drivingadapter.rest;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.jexxa.TestConstants;
import io.jexxa.application.applicationservice.IncrementApplicationService;
import io.jexxa.core.JexxaMain;
import io.jexxa.utils.ThrowingConsumer;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(TestConstants.INTEGRATION_TEST)
class MultipleRESTClientsIT
{
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_TYPE = "application/json";
    private static final String METHOD_GET_SIMPLE_VALUE = "increment";
    private static final int MAX_COUNTER = 1000;
    private static final int MAX_THREADS = 5;

    private IncrementApplicationService applicationService;
    private JexxaMain jexxaMain;


    @BeforeEach
    protected void setUp()
    {
        jexxaMain = new JexxaMain(MultipleRESTClientsIT.class.getSimpleName());
        jexxaMain
                .bind(RESTfulRPCAdapter.class).to(IncrementApplicationService.class)
                .start();

        applicationService = jexxaMain.getInstanceOfPort(IncrementApplicationService.class);
    }

    @Test
    protected void synchronizeMultipleClients()
    {
        //Arrange
        applicationService.setMaxCounter(MAX_COUNTER);
        List<Integer> expectedResult = IntStream.rangeClosed(1, MAX_COUNTER)
                .boxed()
                .collect(toList());

        var clientPool = Stream.generate(() -> new Thread(this::incrementService))
                .limit(MAX_THREADS)
                .collect(toList());

        var exceptionList = new ArrayList<Throwable>();
        
        //Act
        clientPool.forEach(Thread::start);

        clientPool.forEach(ThrowingConsumer.exceptionCollector(Thread::join, exceptionList));


        //Assert
        assertEquals(expectedResult, applicationService.getUsedCounter());
        assertTrue(exceptionList.isEmpty());
    }

    protected void incrementService()
    {
        while ( applicationService.getCounter() < MAX_COUNTER )
        {
            //Act
            var restPath = "http://localhost:7000/IncrementApplicationService/";
            var response = Unirest.post(restPath + METHOD_GET_SIMPLE_VALUE)
                    .header(CONTENT_TYPE, APPLICATION_TYPE)
                    .asJson();
            if (!response.isSuccess())
            {
                throw new IllegalArgumentException("HTTP Response Error: " + response.getStatus() + " " + response.getStatusText() );
            }
        }
    }

    @AfterEach
    protected void tearDown()
    {
        jexxaMain.stop();
        Unirest.shutDown();
    }

}
