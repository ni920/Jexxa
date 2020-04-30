package io.jexxa.application.domain.valueobject;


import io.jexxa.application.annotation.ValueObject;

@ValueObject
public class JexxaValueObject
{
    private final int value;
    final double valueInPercent;

    public JexxaValueObject(int value) {
        this.value = value;
        this.valueInPercent = value / 100.0;
    }

    public int getValue()
    {
        return value;
    }

    @SuppressWarnings("unused")
    public double getValueInPercent()
    {
        return valueInPercent;
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (getClass() != other.getClass()) {
            return false;
        }
        JexxaValueObject otherObject = (JexxaValueObject) other;


        return (this.value == otherObject.getValue() &&
                this.valueInPercent == getValueInPercent());
    }

    public int hashCode()
    {
        return value; 
    }
}