package VCF.Changers;

import VCF.Exceptions.VCFNoDataException;
import VCF.Position;
import java.util.List;

/**
 * Renames a format
 */
public class RenameFormatChanger implements PositionChanger
{

    /**
     * Constructor
     * @param oldName The old name of the format
     * @param newName The new name of the format
     */
    public RenameFormatChanger(String oldName, String newName)
    {
        this.oldName = oldName;
        this.newName = newName;
    }
    
    public void change(Position p) throws VCFNoDataException
    {
        List<String> format = p.meta().getFormat();
        int index = format.indexOf(oldName);
        if (index == -1)
        {
            throw new VCFNoDataException("No data field called " + oldName);
        }
        format.set(index, newName);
    }
    
    private final String oldName;
    private final String newName;
}
