package Exceptions;

public class InvalidCaseNameException extends Exception
{
    public InvalidCaseNameException(String casename)
    {
        super("'" + casename + "' is not a valid case name.");
        this.casename = casename;
    }

    public String getCasename()
    {
        return casename;
    }

    private String casename;
}
