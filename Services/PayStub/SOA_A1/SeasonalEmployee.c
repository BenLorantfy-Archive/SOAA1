#ifndef _SEASONAL_EMPLOYEE_C_
#define _SEASONAL_EMPLOYEE_C_

#include "SeasonalEmployee.h"

const float PAY_BONUS = 150;

SeasonalEmployee NewSeasonalEmployee(float hours, float rate, unsigned int piecesMade)
{
	Employee employee = NewEmployee(EMPLOYEE_TYPE_SEASONAL, hours, rate);

	SeasonalEmployee seasonalEmployee = { employee, piecesMade };

	return seasonalEmployee;
}

float CalculateSeasonalPayroll(SeasonalEmployee employee, unsigned int * error)
{
	*error = FALSE_L;

	if (employee.employee.hours <= 0
		|| employee.piecesMade < 0
		|| employee.employee.rate < 0)
	{
		*error = TRUE_L;
		return -1;
	}

	if (employee.employee.hours < 40)
	{
		return employee.piecesMade * employee.employee.rate;
	}

	return employee.piecesMade * employee.employee.rate + PAY_BONUS;
}

#endif // !_SEASONAL_EMPLOYEE_C_