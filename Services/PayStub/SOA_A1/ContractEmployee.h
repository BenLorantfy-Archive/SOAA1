#pragma once

#include "Employee.h"

typedef struct {
	Employee employee;
	unsigned int contractDuration;
} ContractEmployee;

ContractEmployee NewContractEmployee(float hours, float rate, unsigned int contractDuration);

float CalculateContractPayroll(ContractEmployee employee, unsigned int * error);