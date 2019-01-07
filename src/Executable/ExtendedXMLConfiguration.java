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

package Executable;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * Extends XMLConfiguration so XML output is indented.  Copied from somewhere
 * on the internet.
 * @author Daniel Money
 * @version 0.9
 */
public class ExtendedXMLConfiguration extends XMLConfiguration
{

    /**
     * Copied from internet so who knows (hence placeholders)!
     * @return Placeholder
     * @throws ConfigurationException Placeholder
     */
    @Override
    protected Transformer createTransformer() throws ConfigurationException
    {
        Transformer transformer = super.createTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        return transformer;
    }
}