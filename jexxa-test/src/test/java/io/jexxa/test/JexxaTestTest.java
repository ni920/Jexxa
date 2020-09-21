package io.jexxa.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import io.jexxa.core.JexxaMain;
import io.jexxa.tutorials.bookstorej.domainservice.IBookRepository;
import io.jexxa.tutorials.bookstorej.domainservice.ReferenceLibrary;
import org.junit.jupiter.api.Test;

class JexxaTestTest
{

    @Test
    public void validateRepository()
    {
        //Arrange
        JexxaMain jexxaMain = new JexxaMain(JexxaTestTest.class.getSimpleName(), new Properties());
        JexxaTest jexxaTest = new JexxaTest(jexxaMain);
        IBookRepository bookRepository = jexxaTest.getRepository(IBookRepository.class);

        //Act
        jexxaMain.bootstrap(ReferenceLibrary.class).with(ReferenceLibrary::addLatestBooks);

        //Assert
        assertTrue(bookRepository.getAll().size() > 0);
    }

}
