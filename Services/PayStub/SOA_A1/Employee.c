#ifndef _EMPLOYEE_C_
#define _EMPLOYEE_C_

#include "Employee.h"

const int EMPLOYEE_TYPE_PART_TIME = 1;
const int EMPLOYEE_TYPE_FULL_TIME = 2;
const int EMPLOYEE_TYPE_SEASONAL = 3;
const int EMPLOYEE_TYPE_CONTRACT = 4;

const int TRUE_L = 1;
const int FALSE_L = 0;

const int WEEKS_PER_YEAR = 52;
const int BONUS_HOURS_REQ = 40;
const double BONUS_MULTIPLIER = 1.5;

/// <summary>
/// Creates new instance of Employee.
/// </summary>
/// <param name="type">The employee type.</param>
/// <param name="hours">The employee work hours.</param>
/// <param name="rate">The employee hourly salary rate.</param>
/// <returns>New Employee object.</returns>
Employee NewEmployee(unsigned int type, float hours, float rate)
{
	Employee employee = { type, hours, rate };
	
	return employee;
}

Employee NewPartTimeEmployee(float hours, float rate)
{
	return NewEmployee(EMPLOYEE_TYPE_PART_TIME, hours, rate);
}

Employee NewFullTimeEmployee(float hours, float rate)
{
	return NewEmployee(EMPLOYEE_TYPE_FULL_TIME, hours, rate);
}

/// <summary>
/// Calculates the payroll.
/// </summary>
/// <param name="employee">The employee.</param>
/// <returns>The payroll amount.</returns>
float CalculatePayroll(Employee employee, unsigned int * error)
{
	*error = FALSE_L;

	if (employee.hours < 0
		|| employee.rate < 0)
	{
		*error = TRUE_L;
		return -1;
	}

	float pay = -1;
	switch (employee.type)
	{
		// Part Time
		case 1:
			if (employee.hours <= BONUS_HOURS_REQ)
			{
				pay = employee.hours * employee.rate;
			}
			else
			{
				pay = BONUS_HOURS_REQ * employee.rate + 
					  (employee.hours - BONUS_HOURS_REQ) * employee.rate * BONUS_MULTIPLIER;
			}

			break;
		//Full Time
		case 2:
			pay = employee.rate / WEEKS_PER_YEAR;
			break;
		default:
			*error = TRUE_L;
			return -2;
	}

	return pay;
};

#endif