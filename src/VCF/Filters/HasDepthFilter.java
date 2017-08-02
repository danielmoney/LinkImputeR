/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package VCF.Filters;

import VCF.Position;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 *
 * @author daniel
 */
public class HasDepthFilter extends PositionFilter
{
    public HasDepthFilter()
    {
        
    }
        
    public HasDepthFilter(HierarchicalConfiguration<ImmutableNode> params)
    {
        
    }
    
    public boolean test(Position p)
    {
        return (p.meta().getFormat().contains("AD") && p.meta().getFormat().contains("DP"));
    }
    
    public ImmutableNode getConfig()
    {        
        
        ImmutableNode config = new ImmutableNode.Builder().name("filter")
                .addAttribute("name", "HasDepth")
                .create();
        
        return config;
    }
    
    public String getSummary()
    {
        return "HasDepth";
    }
 
}
