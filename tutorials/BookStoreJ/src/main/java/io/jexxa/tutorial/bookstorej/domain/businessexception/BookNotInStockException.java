package io.jexxa.tutorial.bookstorej.domain.businessexception;

import io.jexxa.addend.applicationcore.BusinessException;

/**
 * Is thrown in case we try to sell a book that is currently not in stock
 */
@BusinessException
public class BookNotInStockException extends Exception
{

}