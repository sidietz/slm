package de.oberamsystems.slm.db;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.dialect.function.StandardSQLFunction;

public class MyFunctionContributor implements FunctionContributor {

	@Override
	public void contributeFunctions(FunctionContributions functionContributions) {
		functionContributions.getFunctionRegistry().register("this_years_birthday", new StandardSQLFunction("this_years_birthday"));
		functionContributions.getFunctionRegistry().register("next_annual_occurrence", new StandardSQLFunction("next_annual_occurrence"));
		functionContributions.getFunctionRegistry().register("days_until_annual_occurrence", new StandardSQLFunction("days_until_annual_occurrence"));
	}

}
