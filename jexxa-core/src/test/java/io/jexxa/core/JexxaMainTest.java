package io.jexxa.core;



import static io.jexxa.TestConstants.JEXXA_APPLICATION_SERVICE;
import static io.jexxa.TestConstants.JEXXA_DRIVEN_ADAPTER;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jexxa.TestConstants;
import io.jexxa.application.annotation.ApplicationService;
import io.jexxa.application.applicationservice.ApplicationServiceWithDrivenAdapters;
import io.jexxa.application.applicationservice.InvalidApplicationService;
import io.jexxa.application.applicationservice.JexxaApplicationService;
import io.jexxa.application.applicationservice.SimpleApplicationService;
import io.jexxa.application.domainservice.IJexxaEntityRepository;
import io.jexxa.application.domainservice.InitializeJexxaEntities;
import io.jexxa.application.infrastructure.drivingadapter.ProxyAdapter;
import io.jexxa.application.infrastructure.drivingadapter.messaging.SimpleApplicationServiceAdapter;
import io.jexxa.core.convention.PortConventionViolation;
import io.jexxa.infrastructure.drivenadapterstrategy.persistence.RepositoryManager;
import io.jexxa.infrastructure.drivenadapterstrategy.persistence.imdb.IMDBRepository;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
@Tag(TestConstants.UNIT_TEST)
class JexxaMainTest
{
    private JexxaMain objectUnderTest;
    private static final String CONTEXT_NAME = "HelloJexxa";

    @BeforeEach
    void initTests()
    {
        objectUnderTest = new JexxaMain(CONTEXT_NAME);
        objectUnderTest.addToInfrastructure(JEXXA_DRIVEN_ADAPTER)
                .addToApplicationCore(JEXXA_APPLICATION_SERVICE);
    }

    @AfterEach
    void tearDownTests()
    {
        if (objectUnderTest != null)
        {
            objectUnderTest.stop();
        }
        Unirest.shutDown();
    }



    @Test
    void bindToPort()
    {
        //Arrange - All done in initTests

        //Act: Bind a concrete type of DrivingAdapter to a concrete type of port
        objectUnderTest
                .bind(ProxyAdapter.class).to(SimpleApplicationService.class)

                .start();

        //Assert
        var result = objectUnderTest.getDrivingAdapter(ProxyAdapter.class)
                .getPortList()
                .stream()
                .filter( element -> SimpleApplicationService.class.equals(element.getClass()) )
                .findFirst();

        assertTrue(result.isPresent());
    }

    @Test
    void conditionalBindToPort()
    {
        //Arrange - All done in initTests

        //Act: Conditional bind (evaluating to false) a concrete type of DrivingAdapter to a concrete type of port
        objectUnderTest
                .conditionalBind(() -> false, ProxyAdapter.class).to(SimpleApplicationService.class)
                .start();

        //Assert that no binding has been performed
        var result = objectUnderTest.getDrivingAdapter(ProxyAdapter.class);

        assertTrue(result.getPortList().isEmpty());
    }


    @Test   // Support of dependency injection
    void bindToPortWithDrivenAdapter()
    {
        //Arrange - All done in initTests

        //Act: Bind a concrete type of DrivingAdapter to a concrete type of port
        objectUnderTest
                .addToInfrastructure(JEXXA_DRIVEN_ADAPTER)
                .bind(ProxyAdapter.class).to(ApplicationServiceWithDrivenAdapters.class)
                .start();


        //Assert
        //Assert
        var result = objectUnderTest.getDrivingAdapter(ProxyAdapter.class)
                .getPortList()
                .stream()
                .filter( element -> ApplicationServiceWithDrivenAdapters.class.equals(element.getClass()) )
                .findFirst();

        assertTrue(result.isPresent());
    }


    @Test
    void bindToAnnotatedPorts()
    {
        //Arrange - All done in initTests

        //Act: Bind all DrivingAdapter to all ApplicationServices
        objectUnderTest
                .bind(ProxyAdapter.class).toAnnotation(ApplicationService.class)
                .start();

        //Assert
        var result = objectUnderTest.getDrivingAdapter(ProxyAdapter.class)
                .getPortList()
                .stream()
                .filter( element -> SimpleApplicationService.class.equals(element.getClass()) )
                .findFirst();

        assertTrue(result.isPresent());
    }

    @Test
    void invalidBindToPortAdapter()
    {
        //Arrange - All done in initTests
        var drivingAdapter = objectUnderTest
                .bind(ProxyAdapter.class);

        //Act /Assert
        assertThrows(PortConventionViolation.class, () -> drivingAdapter.to(SimpleApplicationServiceAdapter.class));
    }


    @Test
    void bootstrapService()
    {
        //Arrange
        RepositoryManager.getInstance().setDefaultStrategy(IMDBRepository.class);
        objectUnderTest = new JexxaMain(CONTEXT_NAME);
        objectUnderTest.addToInfrastructure(JEXXA_DRIVEN_ADAPTER)
                .addToApplicationCore(JEXXA_APPLICATION_SERVICE)

        //Act
                .bootstrap(InitializeJexxaEntities.class).with(InitializeJexxaEntities::initDomainData);

        var jexxaApplicationService = objectUnderTest.getInstanceOfPort(JexxaApplicationService.class);

        //Assert
        assertTrue(jexxaApplicationService.getAggregateCount() > 0);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void getInstanceOfInvalidPort()
    {
        //Arrange --

        //Act/Assert
        assertThrows(PortConventionViolation.class, () -> objectUnderTest.getInstanceOfPort(InvalidApplicationService.class));
    }

    @Test
    void getInstanceOfOutboundPort()
    {
        //Arrange --

        //Act/Assert
        assertNotNull( objectUnderTest.getInstanceOfPort(IJexxaEntityRepository.class));
    }

}
