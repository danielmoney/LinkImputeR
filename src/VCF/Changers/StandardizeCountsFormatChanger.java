package VCF.Changers;

import VCF.Exceptions.VCFNoDataException;
import VCF.Genotype;
import VCF.Position;
import java.util.List;

/**
 * Changes readcounts included in two formats to the new single format (AD)
 * specification
 */
public class StandardizeCountsFormatChanger implements PositionChanger
{

    /**
     * Constructor
     * @param ref Format containing the reference allele read count
     * @param alt Format containing the alternate allele read count
     */
    public StandardizeCountsFormatChanger(String ref, String alt)
    {
        this.ref = ref;
        this.alt = alt;
    }
    
    public void change(Position p) throws VCFNoDataException
    {
        List<String> format = p.meta().getFormat();
        format.add("AD");
        
        for (Genotype g: p.genotypeList())
        {
            g.addData(g.getData(ref) + "," + g.getData(alt));
            g.removeData(ref);
            g.removeData(alt);
        }
        
        format.remove(format.indexOf(ref));
        format.remove(format.indexOf(alt));
    }
    
    private final String ref;
    private final String alt;
}
