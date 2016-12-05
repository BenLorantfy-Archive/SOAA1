#ifndef _EMPLOYEE_H_
#define _EMPLOYEE_H_

#define EMPLOYEE_TYPE_PART_TIME 1
#define EMPLOYEE_TYPE_FULL_TIME 2
#define EMPLOYEE_TYPE_SEASONAL  3
#define EMPLOYEE_TYPE_CONTRACT  4

extern const unsigned int TRUE_L;
extern const unsigned int FALSE_L;

typedef struct
{
	unsigned int type;
	float hours;
	float rate;
} Employee;

Employee NewEmployee(unsigned int type, float hours, float rate);

Employee NewPartTimeEmployee(float hours, float rate);

Employee NewFullTimeEmployee(float hours, float rate);

float CalculatePayroll(Employee employee, unsigned int * error);

#endif