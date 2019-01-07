/*
 * This file is part of LinkImputeR.
 * 
 * LinkImputeR is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LinkImputeR is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LinkImpute.  If not, see <http://www.gnu.org/licenses/>.
 */

package VCF.Filters;

import Utils.Optimize.GoldenSection;
import Utils.Optimize.SingleDoubleValue;
import VCF.Exceptions.VCFDataException;
import VCF.Genotype;
import VCF.Mappers.DepthMapper;
import VCF.Position;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

/**
 * Filters a position based on a Hardy-Weinburg test.  See the paper for full
 * details
 * @author Daniel Money
 * @version 0.9
 */
public class ParalogHWFilter extends PositionFilter
{

    /**
     * Constructor
     * @param significance The significance level
     * @param error The error level
     */
    public ParalogHWFilter(double significance, double error)
    {
        this.error = error;
        this.significance = significance;
        cs = new ChiSquaredDistribution(1);
    }
    
    /**
     * Constructor from a config (read in from a XML file)
     * @param params The config
     */ 
    public ParalogHWFilter(HierarchicalConfiguration<ImmutableNode> params)
    {
        this.significance = params.getDouble("significance");
        this.error = params.getDouble("error");
        cs = new ChiSquaredDistribution(1);
    }
    
    public boolean test(Position p) throws VCFDataException
    {
        double maf = maf(p);
        if (maf == 0.0)
        {
            return false;
        }
        HW hw = new HW(p,maf,error);
        GoldenSection gs = new GoldenSection(0.0001,Double.MAX_VALUE);

        double min = Math.max(-maf*maf, -(1-maf)*(1-maf));
        double max = maf*(1-maf);
        double optd = gs.optimize(hw, min, max);
        double l0 = hw.value(0.0);
        double l1 = hw.value(optd);
        double test = 2 * (l1 - l0);
        double pr = 1.0 - cs.cumulativeProbability(test);
        return (pr > significance);
    }

    private double maf(Position p) throws VCFDataException
    {
        DepthMapper dm = new DepthMapper();
        
        double t = 0.0;
        double c = 0.0;
             
        for (Genotype g: p.genotypeList())
        {
            int[] r = dm.map(g.getData("AD"));
            int trc = r[0] + r[1];
            if ((trc >= 8) && (trc <= 100))
            {
                t += r[1]/ (r[0] + r[1]);
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

        return d;
    }
    
    public ImmutableNode getConfig()
    {        
        ImmutableNode Ierror = new ImmutableNode.Builder().name("error").value(error).create();
        ImmutableNode Isignificance = new ImmutableNode.Builder().name("significance").value(significance).create();
        
        ImmutableNode config = new ImmutableNode.Builder().name("filter")
                .addChild(Ierror)
                .addChild(Isignificance)
                .addAttribute("name", "ParalogHW")
                .create();
        
        return config;
    }
    
    public String getSummary()
    {
        return "HW(" + significance + ")";
    }

    private final double significance;
    private final ChiSquaredDistribution cs;
    private final double error;


    private class HW implements SingleDoubleValue
    {
        public HW(Position p, double maf, double error) throws VCFDataException
        {
            DepthMapper dm = new DepthMapper();
            this.maf = maf;
            //partials = p.genotypeStream().map(g -> partial(dm.map(g.getData("AD")),error)).toArray(i -> new double[i][]);
            
            List<Genotype> list = p.genotypeList();
            partials = new double[list.size()][];
            for (int i = 0; i < list.size(); i++)
            {
               partials[i] = partial(dm.map(list.get(i).getData("AD")),error);
            }

        }

        private double[] partial(int[] counts, double e)
        {
            double[] p = new double[3];
            p[0] = Math.pow(1-e,counts[0]) * Math.pow(e,counts[1]);
            p[1] = Math.pow(0.5,counts[0]) * Math.pow(0.5,counts[1]);
            p[2] = Math.pow(e,counts[0]) * Math.pow(e,counts[1]);
            return p;
        }

        public double value(double d)
        {
            double[] f = new double[3];
            f[0] = (1 - maf) * (1 - maf) + d;
            f[1] = 2 * (maf * (1 - maf) - d);
            f[2] = maf * maf + d;

            return Arrays.stream(partials).mapToDouble(partial -> {
                double[] p = partial;
                return Math.log(f[0] * p[0] + f[1] * p[1] + f[2] * p[2]);
            }).sum();
        }

        private final double maf;
        private final double[][] partials;
    }
}
