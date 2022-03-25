/**
 *
 */
package de.hybris.platform.multicountry.promotions.translators;

import de.hybris.platform.multicountry.promotions.rao.BaseStoreRAO;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerException;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionValidator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.List;


/**
 * @author i307088
 *
 */
public class RuleBaseStoreConditionTranslator implements RuleConditionTranslator, RuleConditionValidator
{
	private static final String STORE = "store";
	private static final String BASESTORE_CODE = "code";

	public static final String VALUE_PARAM = "value";

	@Override
	public void validate(final RuleCompilerContext context, final RuleConditionData condition,
			final RuleConditionDefinitionData conditionDefinition) throws RuleCompilerException
	{
		final RuleParameterData valueParameter = condition.getParameters().get(VALUE_PARAM);
		final Object value = valueParameter.getValue(); // List of base stores

		if (!(value instanceof List))
		{
			throw new RuleCompilerException("Value should be a list of BaseStore");
		}
	}

	@Override
	public RuleIrCondition translate(final RuleCompilerContext context, final RuleConditionData condition,
			final RuleConditionDefinitionData conditionDefinition) throws RuleCompilerException
	{
		// Read the value specified on the condition
		final RuleParameterData valueParameter = condition.getParameters().get(VALUE_PARAM);

		// List of base stores
		@SuppressWarnings("unchecked")
		final List<String> baseStores = (List<String>) valueParameter.getValue();

		return buildCondition(context, RuleIrAttributeOperator.IN, baseStores);
	}

	protected RuleIrCondition buildCondition(final RuleCompilerContext context, final RuleIrAttributeOperator operator,
			final Object value)
	{
		// declare $v1 := BaseStoreRAO()
		final String baseStoreRaoVariable = context.generateVariable(BaseStoreRAO.class);

		// $v1 := BaseStoreRAO(<attribute> <operator> <value>)
		final RuleIrAttributeCondition irBaseStoreAttribute = new RuleIrAttributeCondition();
		irBaseStoreAttribute.setVariable(baseStoreRaoVariable);
		irBaseStoreAttribute.setAttribute(BASESTORE_CODE);
		irBaseStoreAttribute.setOperator(operator);
		irBaseStoreAttribute.setValue(value);

		// Not required for now, as there is only one base store
		// // $v2 := CartRAO()
		// final String cartRaoVariable = context.generateVariable(CartRAO.class);
		// // $v2 := CartRAO(baseStore == $v1)
		//	final RuleIrAttributeRelCondition irCartBaseStore = new RuleIrAttributeRelCondition();
		//	irCartBaseStore.setVariable(cartRaoVariable);
		//	irCartBaseStore.setAttribute(STORE);
		//	irCartBaseStore.setOperator(RuleIrAttributeOperator.EQUAL);
		//	irCartBaseStore.setTargetVariable(baseStoreRaoVariable);
		//
		//	// ($v1 ... and $v2 )
		//	final List<RuleIrCondition> irConditions = new ArrayList<>();
		//	irConditions.add(irCartBaseStore);
		//	irConditions.add(irBaseStoreAttribute);
		//	final RuleIrGroupCondition irGroupCondition = new RuleIrGroupCondition();
		//	irGroupCondition.setOperator(RuleIrGroupOperator.AND);
		//	irGroupCondition.setChildren(irConditions);
		//
		//	return  irGroupCondition;

		return irBaseStoreAttribute;
	}

}