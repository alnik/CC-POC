/**
 * 
 */
package de.hybris.platform.multicountry.solr.search.strategies;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;

import java.util.Set;


/**
 * Retrieve the list of active product availability groups.
 * 
 * @author marco.rolando
 * 
 */
public interface ActiveProductAvailabilityGroupsStrategy
{
	Set<ProductAvailabilityGroupModel> getAllActiveGroups(ProductModel product);
}
