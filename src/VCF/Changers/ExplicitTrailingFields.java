package VCF.Changers;

import VCF.Genotype;
import org.apache.commons.lang3.StringUtils;

public class ExplicitTrailingFields implements GenotypeChanger
{
    public void change(Genotype g)
    {
        int totalfields = g.getPositionMeta().getFormat().size();
        int fieldspresent = StringUtils.countMatches(g.getData(),":") + 1;
        int addfields = totalfields - fieldspresent;

        for (int i = 0; i < totalfields - fieldspresent; i++)
        {
            g.addData(".");
        }
    }
}
