package traductor.errors;

public class ManejadorErrores
{
    public String manejarError(int codigoError)
    {
        switch(codigoError)
        {
            case ErrorCode.ERROR_SQL_EXCEPTION:
                return "";
        }
        return null;
    }
}
