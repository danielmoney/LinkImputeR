package VCF.Filters;

import Callers.BinomialCaller;
import VCF.Exceptions.VCFDataException;
import VCF.Genotype;
import VCF.Mappers.DepthMapper;
import VCF.Position;

public class MAFCalculator
{
    public MAFCalculator(double error, int minDepth, int maxDepth)
    {
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        caller = new BinomialCaller(error);
        dm = new DepthMapper();
    }

    public double maf(Position p) throws VCFDataException
    {
        double t = 0.0;
        double c = 0.0;

        for (Genotype g: p.genotypeList())
        {
            int[] r = dm.map(g.getData("AD"));
            int trc = r[0] + r[1];
            if ((trc >= minDepth) && (trc <= maxDepth))
            {
                t += getDosage(r);
                c++;
            }
        }

        double d;
        if (c > 0)
        {
            d = t/c;
        }
        else
        {
            d = 0.0;
        }

        //Convert average dose to allele freq
        double m = d / 2.0;

        return m;
    }

    private double getDosage(int[] r)
    {
        double[] probs = caller.callSingle(r);
        return 2.0 * probs[0] + probs[1];
    }

    private DepthMapper dm;
    private BinomialCaller caller;
    private int minDepth;
    private int maxDepth;
}
