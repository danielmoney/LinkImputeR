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

package VCF;

import Exceptions.ProgrammerException;
import VCF.Changers.GenotypeChanger;
import VCF.Filters.PositionFilter;
import VCF.Filters.SampleFilter;
import VCF.Mappers.ByteMapper;
import VCF.Mappers.DoubleMapper;
import VCF.Mappers.IntegerMapper;
import VCF.Mappers.Mapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Represents the data from a VCF file
 * @author Daniel Money
 * @version 0.9
 */
public class VCF
{

    /**
     * Constructor from a VCF file
     * @param f The VCF file
     * @throws IOException If there is an IO problem
     */
    public VCF(File f) throws IOException
    {
        this(f, new ArrayList<>(), new ArrayList<>());
    }
    
    /**
     * Constructor from a file, filtering positions at read time. Ensures
     * filtered positions are not stored in memory reducing memory usage when
     * reading a large VCF file with many positions that will be filtered
     * @param f The file
     * @param filters The position filters to apply
     * @throws IOException If there is an IO problem
     */
    public VCF(File f, List<PositionFilter> filters) throws IOException
    {
        this(f, new ArrayList<>(), filters);
    }
    
    /**
     * Constructor from a file, filtering positions at read time and changing
     * genotypes read in. By changng genotypes as they are read in any information
     * contained in the genotype field that will not be used can be discared so
     * saving memory.
     * @param f The file
     * @param changers List of changers to apply to the genotypes
     * @param filters The position filters to apply
     * @throws IOException If there is an IO problem
     */
    public VCF(File f, List<GenotypeChanger> changers, List<PositionFilter> filters) throws IOException
    {
        BufferedReader in;
        try
        {
            in = new BufferedReader(new FileReader(f));
        }
        catch (FileNotFoundException e)
        {
            throw e;
            //throw exception or just use that one, who knows!
        }
        
        try
        {
            ArrayList<PositionMeta> positionList = new ArrayList<>();
            ArrayList<RawGenotype[]> genotypeList = new ArrayList<>();
            meta = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null)
            {
                if (line.startsWith("##"))
                {
                    meta.add(line);
                }
                else if (line.startsWith("#"))
                {
                    String[] parts = line.split("\t");
                    samples = Arrays.copyOfRange(parts,9,parts.length);
                }
                else
                {
                    String[] parts = line.split("\t");
                    
                    String[] metaArray = Arrays.copyOfRange(parts, 0, 9);
                    PositionMeta pm = new PositionMeta(metaArray);
                                        
                    RawGenotype[] data = new RawGenotype[parts.length - 9];
                    for (int i = 0; i < data.length; i++)
                    {
                        data[i] = new RawGenotype(parts[i+9]);
                    }
                    
                    Position p = new Position(pm,samples,data);
                    
                    p.genotypeStream().forEach(g -> changers.stream().forEach(c -> c.change(g)));
                    
                    if (filters.stream().allMatch(filter -> filter.test(p)))
                    {
                        positionList.add(pm);
                        genotypeList.add(data);
                    }
                }
            }
            
            positions = positionList.toArray(new PositionMeta[positionList.size()]);
            genotypes = genotypeList.toArray(new RawGenotype[genotypeList.size()][]);
            
            pVis = new boolean[positions.length];
            Arrays.fill(pVis, true);
            sVis = new boolean[samples.length];
            Arrays.fill(sVis, true);
        }
        catch (IOException e)
        {
            //throw exception or just use that one, who knows!
            throw e;
        }
    }
    
    /**
     * Create a  VCF object from data rather than a file
     * @param meta The meta data for the VCF
     * @param positions The positions for the VCF (which includes information
     * on samples and genotypes).
     */
    public VCF(List<String> meta, List<Position> positions)
    {
        this.meta = meta;
        samples = null;
        ArrayList<PositionMeta> positionsList = new ArrayList<>();
        ArrayList<RawGenotype[]> genotypeList = new ArrayList<>();
        for (Position p: positions)
        {
            if (samples == null)
            {
                samples = p.samples();
            }
            else
            {
                if (!Arrays.equals(samples, p.samples()))
                {
                    //NEEDS A PROPER EXCEPTION
                    throw new ProgrammerException();
                }
            }
            positionsList.add(p.meta());
            genotypeList.add(p.getRawGenotypes());
        }
        
        this.positions = positionsList.toArray(new PositionMeta[positionsList.size()]);
        genotypes = genotypeList.toArray(new RawGenotype[genotypeList.size()][]);        
                    
        pVis = new boolean[this.positions.length];
        Arrays.fill(pVis, true);
        sVis = new boolean[samples.length];
        Arrays.fill(sVis, true);
    }
    
    /**
     * Gets a stream of genotypes.  Genotypes are returned by position
     * i.e. the genotypes for one position are returned before moving onto the
     * next positon.
     * @return The stream
     */
    public Stream<Genotype> genotypeStream()
    {
        return genotypesByPositionStream();
    }
    
    /**
     * Gets a stream of genotypes by position.
     * That is the genotypes for one position are returned before moving onto the
     * next positon.
     * @return The stream
     */
    public Stream<Genotype> genotypesByPositionStream()
    {
        return positionStream().flatMap(p -> p.genotypeStream());
    }
    
    /**
     * Gets a stream of genotypes by sample.
     * That is the genotypes for one sample are returned before moving onto the
     * next sample.
     * @return The stream
     */
    public Stream<Genotype> genotypesBySampleStream()
    {
        return sampleStream().flatMap(s -> s.genotypeStream());
    }
    
    /**
     * Returns the data for a single position
     * @param position The position meta data to return the data for
     * @return The position data
     */
    public Position singlePosition(PositionMeta position)
    {
        return singlePosition(ArrayUtils.indexOf(positions, position));
    }
    
    private Position singlePosition(int i)
    {
        return new Position(positions[i],samples,sVis,genotypes[i]);
    }
    
    /**
     * Returns the data for a single sample
     * @param sample The string representing the sample to return the data for
     * @return The position data
     */
    public Sample singleSample(String sample)
    {
        return singleSample(ArrayUtils.indexOf(samples, sample));
    }
    
    private Sample singleSample(int i)
    {
        return new Sample(samples[i], positions, pVis,
                IntStream.range(0, genotypes.length).mapToObj(j -> genotypes[j][i]).toArray(size -> new RawGenotype[size]));
    }
    
    /**
     * Returns a stream of positions in the VCF
     * @return The stream
     */
    public Stream<Position> positionStream()
    {
        return IntStream.range(0, genotypes.length).filter(i -> pVis[i])
                .mapToObj(i -> new Position(positions[i],samples,sVis,genotypes[i]));
    }
    
    /**
     * Returns a stream of samples in the VCF
     * @return The stream
     */
    public Stream<Sample> sampleStream()
    {
        return IntStream.range(0, samples.length).filter(i -> sVis[i])
                .mapToObj(i -> new Sample(samples[i], positions, pVis,
                IntStream.range(0, genotypes.length).mapToObj(j -> genotypes[j][i]).toArray(size -> new RawGenotype[size])));
    }
    
    /**
     * Returns a stream of the strings that make up the VCF meta data
     * @return The stream
     */
    public Stream<String> metaStream()
    {
        return meta.stream();
    }
    
    /**
     * Filter the samples based on the given filter.  Samples are merely hidden,
     * not deleted, as they can then be unhidden (see resetVisible) which makes
     * applying different filters to the same VCF much easier.
     * @param filter The sample filter to be applied.
     */
        public void filterSamples(SampleFilter filter)
    {
        IntStream.range(0, samples.length).forEach(i -> sVis[i] = sVis[i] && filter.test(singleSample(i)));
    }
    
    /**
     * Filter the positions based on the given filter.  Positions are merely hidden,
     * not deleted, as they can then be unhidden (see resetVisible) which makes
     * applying different filters to the same VCF much easier.
     * @param filter The position filter to be applied.
     */
    public void filterPositions(PositionFilter filter)
    {
        IntStream.range(0, positions.length).forEach(i -> pVis[i] = pVis[i] && filter.test(singlePosition(i)));
    }
    
    /**
     * Limits the VCF to the given position.  Again positions are hidden, not
     * deleted.
     * @param keep The positions to keep.
     */
    public void limitToPositions(List<PositionMeta> keep)
    {
        for (int oi = 0; oi < positions.length; oi++)
        {
            if (!keep.contains(positions[oi]))
            {
                pVis[oi] = false;
            }
        }     
    }
    
    /**
     * Limits the VCF to the given samples.  Again samples are hidden, not
     * deleted.
     * @param keep The positions to keep.
     */
    public void limitToSamples(List<String> keep)
    {
        for (int oi = 0; oi < samples.length; oi++)
        {
            if (!keep.contains(samples[oi]))
            {
                sVis[oi] = false;
            }
        }
    }
    

    /**
     * Returns the number of (visible) positions in the VCF
     * @return The number of positions
     */    
    public int numberPositions()
    {
        return (int) IntStream.range(0, pVis.length).filter(i -> pVis[i]).count();
    }
    
    /**
     * Returns the number of (visible) samples in the VCF
     * @return The number of samples
     */
    public int numberSamples()
    {
        //return samples.length;
        return (int) IntStream.range(0, sVis.length).filter(i -> sVis[i]).count();
    }
    
    /**
     * Writes the VCF to a file. Only visible samples / positions are written.
     * @param f The file to write to
     * @throws IOException If there is an IO problem
     */
    public void writeFile(File f) throws IOException
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        metaStream().forEach(m -> out.println(m));
        
        out.print("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT");
        Arrays.stream(getSamples()).forEach(s -> out.print("\t" + s));
        out.println();
        
        positionStream().forEach(p -> out.println(p.toText()));
        out.close();
    }
    
    /**
     * Gets data from the genotypes in the VCF as an array
     * @param <V> The type of data returned
     * @param format The format in the genotype data to get the data from
     * @param mapper A mapper mapping from the string (in the VCF) to the
     * required type
     * @return The array
     */
    public <V> V[][] asArray(String format,Mapper<V> mapper)
    {
        return positionStream().map(p -> 
                p.genotypeStream().map(g -> mapper.map(g.getData(format))).toArray(size -> mapper.getArray(size)))
                .toArray(size -> mapper.get2DArray(size));
    }
    
    /**
     * Gets data from the genotypes in the VCF as a transposed array
     * @param <V> The type of data returned
     * @param format The format in the genotype data to get the data from
     * @param mapper A mapper mapping from the string (in the VCF) to the
     * required type
     * @return The array
     */
    public <V> V[][] asArrayTransposed(String format, Mapper<V> mapper)
    {
        return sampleStream().map(s ->
                s.genotypeStream().map(g -> mapper.map(g.getData(format))).toArray(size -> mapper.getArray(size))).
                toArray(size -> mapper.get2DArray(size));
    }

    /**
     * Gets data from the genotypes in the VCF as an integer array
     * @param format The format in the genotype data to get the data from
     * @param mapper A mapper mapping from the string (in the VCF) to an integer
     * @return The array
     */
    public int[][] asIntegerArray(String format,IntegerMapper mapper)
    {
        return positionStream().map(p -> 
                p.genotypeStream().mapToInt(g -> mapper.map(g.getData(format))).toArray())
                .toArray(size -> new int[size][]);
    }
    
    /**
     * Gets data from the genotypes in the VCF as a transposed integer array
     * @param format The format in the genotype data to get the data from
     * @param mapper A mapper mapping from the string (in the VCF) to an integer
     * @return The array
     */
    public int[][] asIntegerArrayTransposed(String format, IntegerMapper mapper)
    {
        return sampleStream().map(s ->
                s.genotypeStream().mapToInt(g -> mapper.map(g.getData(format))).toArray()).
                toArray(size -> new int[size][]);
    }
    
    /**
     * Gets data from the genotypes in the VCF as a double array
     * @param format The format in the genotype data to get the data from
     * @param mapper A mapper mapping from the string (in the VCF) to a double
     * @return The array
     */
    public double[][] asDoubleArray(String format,DoubleMapper mapper)
    {
        return positionStream().map(p -> 
                p.genotypeStream().mapToDouble(g -> mapper.map(g.getData(format))).toArray())
                .toArray(size -> new double[size][]);
    }
    
    /**
     * Gets data from the genotypes in the VCF as a transposed double array
     * @param format The format in the genotype data to get the data from
     * @param mapper A mapper mapping from the string (in the VCF) to a double
     * @return The array
     */
    public double[][] asDoubleArrayTransposed(String format, DoubleMapper mapper)
    {
        return sampleStream().map(s ->
                s.genotypeStream().mapToDouble(g -> mapper.map(g.getData(format))).toArray()).
                toArray(size -> new double[size][]);
    }
    
    /**
     * Gets data from the genotypes in the VCF as a byte array
     * @param format The format in the genotype data to get the data from
     * @param mapper A mapper mapping from the string (in the VCF) to a byte
     * @return The array
     */
    public byte[][] asByteArray(String format,ByteMapper mapper)
    {
        return positionStream().map(p -> 
                genotypeToByte(p.genotypeStream().toArray(size -> new Genotype[size]),format,mapper))
                .toArray(size -> new byte[size][]);
    }
    
    /**
     * Gets data from the genotypes in the VCF as a transposed byte array
     * @param format The format in the genotype data to get the data from
     * @param mapper A mapper mapping from the string (in the VCF) to a byte
     * @return The array
     */
    public byte[][] asByteArrayTransposed(String format,ByteMapper mapper)
    {
        return sampleStream().map(s -> 
                genotypeToByte(s.genotypeStream().toArray(size -> new Genotype[size]),format,mapper))
                .toArray(size -> new byte[size][]);
    }
    
    private byte[] genotypeToByte(Genotype[] genos, String format, ByteMapper mapper)
    {
        byte[] bytes = new byte[genos.length];
        for (int i = 0; i < bytes.length; i++)
        {
            bytes[i] = mapper.map(genos[i].getData(format));
        }
        return bytes;
    }
    
    /**
     * Get a stream of sample names
     * @return The stream
     */
    public String[] getSamples()
    {
        //return samples;
        return IntStream.range(0, samples.length).filter(i -> sVis[i]).mapToObj(i -> samples[i]).toArray(i -> new String[i]);
    }
    
    /**
     * Get a stream of position meta data
     * @return The stream
     */
    public PositionMeta[] getPositions()
    {
        //return positions;
        return IntStream.range(0, positions.length).filter(i -> pVis[i]).mapToObj(i -> positions[i]).toArray(i -> new PositionMeta[i]);
    }
    
    /**
     * Resets all samples and positions to be visible, that is the state
     * immediately after the VCF was constructed.
     */
    public void resetVisible()
    {
        Arrays.fill(pVis, true);
        Arrays.fill(sVis, true);        
    }

    private List<String> meta;    
    private RawGenotype[][] genotypes;
    private PositionMeta[] positions;
    private String[] samples;
    private boolean[] pVis;
    private boolean[] sVis;
    
    /**
     * Utility function that returns the number of positions in a file
     * without reading in any data
     * @param f The VCF file
     * @return The number of positions
     * @throws IOException If there is an IO problem
     */
    public static int numberPositionsFromFile(File f) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        int lines = 0;
        String line;
        while ((line = reader.readLine()) != null)
        {
            if (!line.startsWith("#"))
            {
                lines++;
            }
        }
        reader.close();
        return lines;
    }

    /**
     * Utility function that returns the number of samples in a file
     * without reading in any data
     * @param f The VCF file
     * @return The number of samples
     * @throws IOException If there is an IO problem
     */
    public static int numberSamplesFromFile(File f) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line;
        while (!(line = reader.readLine()).matches("^#[^#].*"));
        return (line.split("\t").length - 9);
    }
}
