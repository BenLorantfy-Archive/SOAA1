#ifndef _EMPLOYEE_H_
#define _EMPLOYEE_H_

extern const int EMPLOYEE_TYPE_PART_TIME;
extern const int EMPLOYEE_TYPE_FULL_TIME;
extern const int EMPLOYEE_TYPE_SEASONAL;
extern const int EMPLOYEE_TYPE_CONTRACT;

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