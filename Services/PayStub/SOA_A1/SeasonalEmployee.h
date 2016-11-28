#pragma once

#include "Employee.h"

typedef struct {
	Employee employee;
	unsigned int piecesMade;
} SeasonalEmployee;

SeasonalEmployee NewSeasonalEmployee(float hours, float rate, unsigned int piecesMade);

float CalculateSeasonalPayroll(SeasonalEmployee employee, unsigned int * error);