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

import VCF.Changers.GenotypeChanger;
import VCF.Changers.PositionChanger;
import VCF.Exceptions.VCFDataException;
import VCF.Exceptions.VCFDataLineException;
import VCF.Exceptions.VCFException;
import VCF.Exceptions.VCFHeaderLineException;
import VCF.Exceptions.VCFInputException;
import VCF.Exceptions.VCFMissingFormatException;
import VCF.Filters.PositionFilter;
import VCF.Filters.SampleFilter;
import VCF.Mappers.ByteMapper;
import VCF.Mappers.DoubleMapper;
import VCF.Mappers.IntegerMapper;
import VCF.Mappers.Mapper;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Represents the data from a VCF file
 * @author Daniel Money
 * @version 1.1.3
 */
public class VCF
{

    /**
     * Constructor from a VCF file
     * @param f The VCF file
     * @throws VCF.Exceptions.VCFException If there is a problem with the VCF file
     * or the data in it.
     */
    public VCF(File f) throws VCFException
    {
        this(f, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 
                new ArrayList<>(), new ArrayList<>());
    }
    
    /**
     * Constructor from a file, filtering positions at read time. Ensures
     * filtered positions are not stored in memory reducing memory usage when
     * reading a large VCF file with many positions that will be filtered
     * @param f The file
     * @param filters The position filters to apply
     * @throws VCF.Exceptions.VCFException If there is a problem with the VCF file
     * or the data in it.
     */
    public VCF(File f, List<PositionFilter> filters) throws VCFException
    {
        this(f, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 
                filters,  new ArrayList<>());
    }
    
    /**
     * Constructor from a file, filtering positions at read time and changing
     * genotypes read in. By changing genotypes as they are read in any information
     * contained in the genotype field that will not be used can be discarded so
     * saving memory.
     * @param f The file
     * @param preFilters A list of filters to be applied before any changes are
     * applied (e.g. to filter out snps without the required data)
     * @param positionChangers List of changers to apply to the positions
     * @param genotypeChangers List of changers to apply to the genotypes
     * @param filters The position filters to apply (after the changers)
     * @param requiredFormats A list of formats required to be in the VCF
     * @throws VCF.Exceptions.VCFException If there is a problem with the VCF file
     * or the data in it
     */
    public VCF(File f, List<PositionFilter> preFilters, List<PositionChanger> positionChangers,
                List<GenotypeChanger> genotypeChangers, List<PositionFilter> filters, 
                List<String> requiredFormats) throws VCFException
    {
        BufferedReader in;
        try
        {
            if (isGZipped(f))
            {
                in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f))));
            }
            else
            {
                in = new BufferedReader(new FileReader(f));
            }
        }
        catch (FileNotFoundException e)
        {
            throw new VCFInputException("VCF file (" + f.getPath() + ") does not exist", e);
        }
        catch (IOException e)
        {
            throw new VCFInputException("Problem reading VCF file (" + f.getPath() + ")", e);
        }

        int lineNumber = 0;        
        try
        {
            ArrayList<PositionMeta> positionList = new ArrayList<>();
            ArrayList<RawGenotype[]> genotypeList = new ArrayList<>();
            ArrayList<String> metaLines = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null)
            {
                lineNumber ++;
                if (line.startsWith("##"))
                {
                    metaLines.add(line);
                }
                else if (line.startsWith("#"))
                {
                    //Process meta data since we've read it all in
                    meta = new Meta(metaLines);
                    boolean allFormats = true;
                    StringBuilder missingString = new StringBuilder();
                    for (String format: requiredFormats)
                    {
                        if (!meta.hasFormat(format))
                        {
                            allFormats = false;
                            missingString.append(" ");
                            missingString.append(format);
                        }
                    }
                    if (!allFormats)
                    {
                        throw new VCFMissingFormatException(
                                "The VCF is missing the following required formats"
                                + missingString.toString());
                    }
                    
                    
                    //Now deal with the header line
                    String[] parts = line.split("\t");
                    try
                    {
                        samples = Arrays.copyOfRange(parts,9,parts.length);
                    }
                    catch (ArrayIndexOutOfBoundsException | IllegalArgumentException ex)
                    {
                        throw new VCFHeaderLineException("Not enough fields in header line (line number " + lineNumber + ")");
                    }
                }
                else
                {
                    if (samples == null)
                    {
                        throw new VCFHeaderLineException("Data lines occur before header line");
                    }
                    String[] parts = line.split("\t");
                    
                    if (parts.length < samples.length + 9)
                    {
                        throw new VCFDataLineException("Not enough fields in data line (line number " + lineNumber + ")");
                    }
                    if (parts.length > samples.length + 9)
                    {
                        throw new VCFDataLineException("Too many fields in data line (line number " + lineNumber + ")");
                    }
                    
                    String[] metaArray = Arrays.copyOfRange(parts, 0, 9);
                    PositionMeta pm = new PositionMeta(metaArray);
                                        
                    RawGenotype[] data = new RawGenotype[parts.length - 9];
                    for (int i = 0; i < data.length; i++)
                    {
                        data[i] = new RawGenotype(parts[i+9]);
                    }
                    
                    Position p = new Position(pm,samples,data);
                    
                    boolean preallmatch = true;
                    for (PositionFilter filter: filters)
                    {
                        preallmatch = preallmatch && filter.test(p);
                    }
                    if (preallmatch)
                    {
                    
                        for (PositionChanger c: positionChangers)
                        {
                            c.change(p);
                        }

                        for (Genotype g: p.genotypeList())
                        {
                            for (GenotypeChanger c: genotypeChangers)
                            {
                                c.change(g);
                            }
                        }


                        boolean allmatch = true;
                        for (PositionFilter filter: filters)
                        {
                            allmatch = allmatch && filter.test(p);
                        }
                        if (allmatch)
                        {
                            positionList.add(pm);
                            genotypeList.add(data);
                        }
                    }
                }
            }
            
            positions = positionList.toArray(new PositionMeta[positionList.size()]);
            genotypes = genotypeList.toArray(new RawGenotype[genotypeList.size()][]);
            
            pVis = new boolean[positions.length];
            Arrays.fill(pVis, true);

            if (samples == null)
            {
                throw new VCFHeaderLineException("No data line line in VCF");
            }
            sVis = new boolean[samples.length];
            Arrays.fill(sVis, true);
        }
        catch (IOException e)
        {
            if (lineNumber == 0)
            {
                throw new VCFInputException("Problem reading VCF",e);
            }
            else
            {
                throw new VCFInputException("Problem reading VCF (line number " + lineNumber + ")",e);
            }
        }
    }
    
    /**
     * Create a  VCF object from data rather than a file
     * @param meta The meta data for the VCF
     * @param positions The positions for the VCF (which includes information
     * on samples and genotypes).
     * @throws VCFDataException If there is a problem with the passed in list of positions
     */
    public VCF(Meta meta, List<Position> positions) throws VCFDataException
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
                    throw new VCFDataException("A position does not have the same samples as a previous position");
                }
            }
            positionsList.add(p.meta());
            genotypeList.add(p.getRawGenotypes());
        }
        
        this.positions = positionsList.toArray(new PositionMeta[positionsList.size()]);
        genotypes = genotypeList.toArray(new RawGenotype[genotypeList.size()][]);        
                    
        pVis = new boolean[this.positions.length];
        Arrays.fill(pVis, true);

        if (samples == null)
        {
            throw new VCFDataException("No positions provided from which to initalise samples");
        }
        sVis = new boolean[samples.length];
        Arrays.fill(sVis, true);
    }
    
    /**
     * Gets a stream of genotypes.  Genotypes are returned by position
     * i.e. the genotypes for one position are returned before moving onto the
     * next position.
     * @return The stream
     */
    public Stream<Genotype> genotypeStream()
    {
        return genotypesByPositionStream();
    }
    
    /**
     * Gets a stream of genotypes by position.
     * That is the genotypes for one position are returned before moving onto the
     * next position.
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
                Arrays.stream(genotypes).map(genotype -> genotype[i]).toArray((IntFunction<RawGenotype[]>) RawGenotype[]::new));
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
                        Arrays.stream(genotypes).map(genotype -> genotype[i]).toArray((IntFunction<RawGenotype[]>) RawGenotype[]::new)));
    }
    
    /**
     * Get the meta information for this VCF
     * @return The meta information
     */
    public Meta getMeta()
    {
        return meta;
    }
    
    /**
     * Filter the samples based on the given filter.  Samples are merely hidden,
     * not deleted, as they can then be unhidden (see resetVisible) which makes
     * applying different filters to the same VCF much easier.
     * @param filter The sample filter to be applied.
     * @throws VCF.Exceptions.VCFDataException If there is a problem with the
     * data in the VCF
     */
    public void filterSamples(SampleFilter filter) throws VCFDataException
    {
        for (int i = 0; i < samples.length; i++)
        {
            sVis[i] = sVis[i] && filter.test(singleSample(i));
        }
    }
    
    /**
     * Filter the positions based on the given filter.  Positions are merely hidden,
     * not deleted, as they can then be unhidden (see resetVisible) which makes
     * applying different filters to the same VCF much easier.
     * @param filter The position filter to be applied.
     * @throws VCF.Exceptions.VCFDataException If there is a problem with the
     * data in the VCF
     */
    public void filterPositions(PositionFilter filter) throws VCFDataException
    {
        for (int i = 0; i < positions.length; i++)
        {
            pVis[i] = pVis[i] && filter.test(singlePosition(i));
        }
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
        PrintWriter out;
        if (f.getName().endsWith(".gz"))
        {
            out = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(f)))));
        }
        else
        {
            out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        }
        meta.getLinesStream().forEach(out::println);
        
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
     * @throws VCF.Exceptions.VCFDataException If there is a problem with the
     * data in the VCF
     */
    public <V> V[][] asArray(String format,Mapper<V> mapper) throws VCFDataException
    {
        V[][] array = mapper.get2DArray(numberPositions());
        
        int cp = 0;
        for (int i = 0; i < genotypes.length; i++)
        {
            if (pVis[i])
            {
                int cs = 0;
                V[] a = mapper.getArray(numberSamples());
                for (int j = 0; j < genotypes[i].length; j++)
                {
                    if (sVis[i])
                    {
                        a[cs] = mapper.map(new Genotype(genotypes[i][j],positions[i],samples[j]).getData(format));
                        cs++;
                    }
                }
                array[cp] = a;
                cp++;
            }
        }
        
        return array;
    }
    
    /**
     * Gets data from the genotypes in the VCF as a transposed array
     * @param <V> The type of data returned
     * @param format The format in the genotype data to get the data from
     * @param mapper A mapper mapping from the string (in the VCF) to the
     * required type
     * @return The array
     * @throws VCF.Exceptions.VCFDataException If there is a problem with the
     * data in the VCF
     */
    public <V> V[][] asArrayTransposed(String format, Mapper<V> mapper) throws VCFDataException
    {
        V[][] array = mapper.get2DArray(numberSamples());
        
        int cs = 0;
        for (int i = 0; i < genotypes[0].length; i++)
        {
            if (sVis[i])
            {
                int cp = 0;
                V[] a = mapper.getArray(numberPositions());
                for (int j = 0; j < genotypes.length; j++)
                {
                    if (pVis[j])
                    {
                        a[cp] = mapper.map(new Genotype(genotypes[j][i],positions[j],samples[i]).getData(format));
                        cp++;
                    }
                }
                array[cs] = a;
                cs++;
            }
        }
        
        return array;
    }

    /**
     * Gets data from the genotypes in the VCF as an integer array
     * @param format The format in the genotype data to get the data from
     * @param mapper A mapper mapping from the string (in the VCF) to an integer
     * @return The array
     * @throws VCF.Exceptions.VCFDataException If there is a problem with the
     * data in the VCF
     */
    public int[][] asIntegerArray(String format,IntegerMapper mapper) throws VCFDataException
    {
        int[][] array = new int[numberPositions()][];
        
        int cp = 0;
        for (int i = 0; i < genotypes.length; i++)
        {
            if (pVis[i])
            {
                int cs = 0;
                int[] a = new int[numberSamples()];
                for (int j = 0; j < genotypes[i].length; j++)
                {
                    if (sVis[i])
                    {
                        a[cs] = mapper.map(new Genotype(genotypes[i][j],positions[i],samples[j]).getData(format));
                        cs++;
                    }
                }
                array[cp] = a;
                cp++;
            }
        }
        
        return array;
    }
    
    /**
     * Gets data from the genotypes in the VCF as a transposed integer array
     * @param format The format in the genotype data to get the data from
     * @param mapper A mapper mapping from the string (in the VCF) to an integer
     * @return The array
     * @throws VCF.Exceptions.VCFDataException If there is a problem with the
     * data in the VCF
     */
    public int[][] asIntegerArrayTransposed(String format, IntegerMapper mapper) throws VCFDataException
    {
        int[][] array = new int[numberSamples()][];
        
        int cs = 0;
        for (int i = 0; i < genotypes[0].length; i++)
        {
            if (sVis[i])
            {
                int cp = 0;
                int[] a = new int[numberPositions()];
                for (int j = 0; j < genotypes.length; j++)
                {
                    if (pVis[j])
                    {
                        a[cp] = mapper.map(new Genotype(genotypes[j][i],positions[j],samples[i]).getData(format));
                        cp++;
                    }
                }
                array[cs] = a;
                cs++;
            }
        }
        
        return array;
    }
    
    /**
     * Gets data from the genotypes in the VCF as a double array
     * @param format The format in the genotype data to get the data from
     * @param mapper A mapper mapping from the string (in the VCF) to a double
     * @return The array
     * @throws VCF.Exceptions.VCFDataException If there is a problem with the
     * data in the VCF
     */
    public double[][] asDoubleArray(String format,DoubleMapper mapper) throws VCFDataException
    {
        double[][] array = new double[numberPositions()][];
        
        int cp = 0;
        for (int i = 0; i < genotypes.length; i++)
        {
            if (pVis[i])
            {
                int cs = 0;
                double[] a = new double[numberSamples()];
                for (int j = 0; j < genotypes[i].length; j++)
                {
                    if (sVis[i])
                    {
                        a[cs] = mapper.map(new Genotype(genotypes[i][j],positions[i],samples[j]).getData(format));
                        cs++;
                    }
                }
                array[cp] = a;
                cp++;
            }
        }
        
        return array;
    }
    
    /**
     * Gets data from the genotypes in the VCF as a transposed double array
     * @param format The format in the genotype data to get the data from
     * @param mapper A mapper mapping from the string (in the VCF) to a double
     * @return The array
     * @throws VCF.Exceptions.VCFDataException If there is a problem with the
     * data in the VCF
     */
    public double[][] asDoubleArrayTransposed(String format, DoubleMapper mapper) throws VCFDataException
    {
        double[][] array = new double[numberSamples()][];
        
        int cs = 0;
        for (int i = 0; i < genotypes[0].length; i++)
        {
            if (sVis[i])
            {
                int cp = 0;
                double[] a = new double[numberPositions()];
                for (int j = 0; j < genotypes.length; j++)
                {
                    if (pVis[j])
                    {
                        a[cp] = mapper.map(new Genotype(genotypes[j][i],positions[j],samples[i]).getData(format));
                        cp++;
                    }
                }
                array[cs] = a;
                cs++;
            }
        }
        
        return array;
    }
    
    /**
     * Gets data from the genotypes in the VCF as a byte array
     * @param format The format in the genotype data to get the data from
     * @param mapper A mapper mapping from the string (in the VCF) to a byte
     * @return The array
     * @throws VCF.Exceptions.VCFDataException If there is a problem with the
     * data in the VCF
     */
    public byte[][] asByteArray(String format,ByteMapper mapper) throws VCFDataException
    {
        byte[][] array = new byte[numberPositions()][];
        
        int cp = 0;
        for (int i = 0; i < genotypes.length; i++)
        {
            if (pVis[i])
            {
                int cs = 0;
                byte[] a = new byte[numberSamples()];
                for (int j = 0; j < genotypes[i].length; j++)
                {
                    if (sVis[i])
                    {
                        a[cs] = mapper.map(new Genotype(genotypes[i][j],positions[i],samples[j]).getData(format));
                        cs++;
                    }
                }
                array[cp] = a;
                cp++;
            }
        }
        
        return array;
    }
    
    /**
     * Gets data from the genotypes in the VCF as a transposed byte array
     * @param format The format in the genotype data to get the data from
     * @param mapper A mapper mapping from the string (in the VCF) to a byte
     * @return The array
     * @throws VCF.Exceptions.VCFDataException If there is a problem with the
     * data in the VCF
     */
    public byte[][] asByteArrayTransposed(String format,ByteMapper mapper)  throws VCFDataException
    {
        byte[][] array = new byte[numberSamples()][];
        
        int cs = 0;
        for (int i = 0; i < genotypes[0].length; i++)
        {
            if (sVis[i])
            {
                int cp = 0;
                byte[] a = new byte[numberPositions()];
                for (int j = 0; j < genotypes.length; j++)
                {
                    if (pVis[j])
                    {
                        a[cp] = mapper.map(new Genotype(genotypes[j][i],positions[j],samples[i]).getData(format));
                        cp++;
                    }
                }
                array[cs] = a;
                cs++;
            }
        }
        
        return array;
    }
    
    /**
     * Get a stream of sample names
     * @return The stream
     */
    public String[] getSamples()
    {
        //return samples;
        return IntStream.range(0, samples.length).filter(i -> sVis[i]).mapToObj(i -> samples[i]).toArray(String[]::new);
    }
    
    /**
     * Get a stream of position meta data
     * @return The stream
     */
    public PositionMeta[] getPositions()
    {
        //return positions;
        return IntStream.range(0, positions.length).filter(i -> pVis[i]).mapToObj(i -> positions[i]).toArray((IntFunction<PositionMeta[]>) PositionMeta[]::new);
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

    private Meta meta;    
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
     * @throws VCF.Exceptions.VCFInputException If there is a problem with reading
     * the VCF
     */
    public static int numberPositionsFromFile(File f) throws VCFInputException
    {
        BufferedReader in;
        try
        {
            if (isGZipped(f))
            {
                in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f))));
            }
            else
            {
                in = new BufferedReader(new FileReader(f));
            }
            
            int lines = 0;
            String line;
            while ((line = in.readLine()) != null)
            {
                if (!line.startsWith("#"))
                {
                    lines++;
                }
            }
            in.close();
            return lines;
        }
        catch (FileNotFoundException e)
        {
            throw new VCFInputException("VCF file (" + f.getPath() + ") does not exist", e);
        }
        catch (IOException e)
        {
            throw new VCFInputException("Problem reading VCF file (" + f.getPath() + ")", e);
        }
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
    
    // Can't believe there's not a better way to do this but google suggest not
    // From https://stackoverflow.com/questions/30507653
    private static boolean isGZipped(File f) throws IOException
    {
        int magic;
        RandomAccessFile raf = new RandomAccessFile(f, "r");
        magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
        raf.close();
        return magic == GZIPInputStream.GZIP_MAGIC;
 }
}
