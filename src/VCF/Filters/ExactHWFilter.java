package VCF.Filters;

import Callers.BinomialCaller;
import Utils.ProbToCallMinDepth;
import VCF.Exceptions.VCFDataException;
import VCF.Genotype;
import VCF.Mappers.DepthMapper;
import VCF.Mappers.GenoToByte;
import VCF.Position;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.util.Arrays;

public class ExactHWFilter extends PositionFilter
{
    public ExactHWFilter(int minDepth, double significance, double error)
    {
        this.minDepth = minDepth;
        this.significance = significance;
        this.error = error;
        dm = new DepthMapper();
        caller = new BinomialCaller(error);
        p2c = new ProbToCallMinDepth(minDepth);
    }

    /**
     * Constructor from a config (read in from a XML file)
     * @param params The config
     */
    public ExactHWFilter(HierarchicalConfiguration<ImmutableNode> params)
    {
        this.minDepth = params.getInt("minDepth");
        this.significance = params.getDouble("significance");
        this.error = params.getDouble("error");
        dm = new DepthMapper();
        caller = new BinomialCaller(error);
        p2c = new ProbToCallMinDepth(minDepth);
    }

    @Override
    public boolean test(Position p) throws VCFDataException
    {
        int[] gCounts = new int[3];
        for (Genotype g: p.genotypeList())
        {
            int[] r = dm.map(g.getData("AD"));
            double[] prob = caller.callSingle(r);
            byte call = p2c.callSingle(prob, r);
            if (call > -1)
            {
                gCounts[call]++;
            }
        }
        double prob = Filters.HardyWeinbergCalculation.hwCalculate(gCounts[0],gCounts[1],gCounts[2]);
        return (prob > significance);
    }

    @Override
    public String getSummary()
    {
        return "AltHW(" + significance + ")";
    }

    @Override
    public ImmutableNode getConfig()
    {
        ImmutableNode Idepth = new ImmutableNode.Builder().name("minDepth").value(minDepth).create();
        ImmutableNode Isignificance = new ImmutableNode.Builder().name("significance").value(significance).create();
        ImmutableNode Ierror = new ImmutableNode.Builder().name("error").value(error).create();

        ImmutableNode config = new ImmutableNode.Builder().name("filter")
                .addChild(Idepth)
                .addChild(Isignificance)
                .addChild(Ierror)
                .addAttribute("name", "ExactHW")
                .create();

        return config;
    }

    private DepthMapper dm;
    private ProbToCallMinDepth p2c;
    private BinomialCaller caller;
    private int minDepth;
    private double significance;
    private double error;
}
