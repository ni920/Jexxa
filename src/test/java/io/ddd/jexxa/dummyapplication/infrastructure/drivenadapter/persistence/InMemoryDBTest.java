package io.ddd.jexxa.dummyapplication.infrastructure.drivenadapter.persistence;

import java.util.Optional;

import io.ddd.jexxa.dummyapplication.domain.aggregate.JexxaAggregate;
import io.ddd.jexxa.dummyapplication.domain.valueobject.JexxaValueObject;
import io.ddd.jexxa.infrastructure.drivenadapter.persistence.InMemoryDB;
import org.junit.Assert;
import org.junit.Test;

public class InMemoryDBTest
{

    @Test
    public void addAggregate()
    {
        //Arrange
        var aggregate = JexxaAggregate.create(new JexxaValueObject(42));
        var objectUnderTest = new InMemoryDB<>(JexxaAggregate::getKey);

        //act
        objectUnderTest.add(aggregate);

        //Assert
        Assert.assertEquals(aggregate, objectUnderTest.get(aggregate.getKey()).orElse(null));
        Assert.assertTrue(objectUnderTest.get().size() > 0);
    }


    @Test
    public void removeAggregate()
    {
        //Arrange
        var aggregate = JexxaAggregate.create(new JexxaValueObject(42));
        var objectUnderTest = new InMemoryDB<>(JexxaAggregate::getKey);
        objectUnderTest.add(aggregate);

        //act
        objectUnderTest.remove( aggregate );

        //Assert
        Assert.assertTrue(objectUnderTest.get().isEmpty());
    }

}
