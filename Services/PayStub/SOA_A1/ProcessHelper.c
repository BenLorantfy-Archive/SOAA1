#ifndef _PROCESS_HELPER_C_
#define _PROCESS_HELPER_C_

#include "ProcessHelper.h"
#include "Employee.h"
#include "ContractEmployee.h"
#include "SeasonalEmployee.h"
#include "jsmn.h"
#include "HttpHelper.h"

#include <stdio.h>
#include <stdlib.h>
#include <Windows.h>

#define BUF_SIZE 64
#define NULL_STR "null\0"

int index_of(char* string, int string_len, char symbol, int start);
const char* substring(char* string, int string_len, int start, int len);

/// <summary>
/// Checks for specific key for the json
/// </summary>
/// <param name="json">The json.</param>
/// <param name="tok">The token.</param>
/// <param name="s">The string.</param>
/// <returns></returns>
static int jsoneq(const char *json, jsmntok_t *tok, const char *s) {
	if (tok->type == JSMN_STRING && (int)strlen(s) == tok->end - tok->start &&
		strncmp(json + tok->start, s, tok->end - tok->start) == 0) {
		return 0;
	}
	return -1;
}

const char* process_message(char* message, int message_len)
{
	char buffer[BUF_SIZE];
	int start_index = 0;
	int end_index = 0;
	int int_buffer = 0;
	char * json = message;

	int type = -1;
	float hours = -1;
	float rate = -1;
	int pieces = -1;
	int duration = -1;
	float result = 0;
	int r = 0;
	unsigned int error = TRUE_L;

	jsmn_parser parser;
	jsmntok_t tokens[20];

	// Initialize parser
	jsmn_init(&parser);

	start_index = index_of(message, message_len, '{', 0);
	json = substring(message, message_len, start_index, 0);

	// js - pointer to JSON string
	// tokens - an array of tokens available
	// 20 - number of tokens available
	r = jsmn_parse(&parser, json, strlen(json), tokens, 20);

	/* Loop over all keys of the root object */
	for (int i = 1; i < r; i++) {
		if (jsoneq(json, &tokens[i], "type") == 0) {
			/* We may use strndup() to fetch string value */
			sprintf_s(buffer, BUF_SIZE, "%.*s\0", tokens[i + 1].end - tokens[i + 1].start,
				json + tokens[i + 1].start);
			if (sscanf_s(buffer, "%d", &type) != 1)
			{
				type = -1;
			}

			printf("- User: %s\n", buffer);
			i++;
		}
		else if (jsoneq(json, &tokens[i], "hours") == 0) {
			sprintf_s(buffer, BUF_SIZE, "%.*s\0", tokens[i + 1].end - tokens[i + 1].start,
				json + tokens[i + 1].start);
			if (sscanf_s(buffer, "%f", &hours) != 1)
			{
				hours = -1;
			}

			printf("- Hours: %s\n", buffer);
			i++;
		}
		else if (jsoneq(json, &tokens[i], "rate") == 0) {
			sprintf_s(buffer, BUF_SIZE, "%.*s\0", tokens[i + 1].end - tokens[i + 1].start,
				json + tokens[i + 1].start);
			if (sscanf_s(buffer, "%f", &rate) != 1)
			{
				rate = -1;
			}

			printf("- Rate: %s\n", buffer);
			i++;
		}
		else if (jsoneq(json, &tokens[i], "pieces") == 0) {
			sprintf_s(buffer, BUF_SIZE, "%.*s\0", tokens[i + 1].end - tokens[i + 1].start,
				json + tokens[i + 1].start);
			if (sscanf_s(buffer, "%d", &pieces) != 1)
			{
				pieces = 0;
			}

			printf("- Pieces: %s\n", buffer);
			i++;
		}
		else if (jsoneq(json, &tokens[i], "duration") == 0) {
			sprintf_s(buffer, BUF_SIZE, "%.*s\0", tokens[i + 1].end - tokens[i + 1].start,
				json + tokens[i + 1].start);
			if (sscanf_s(buffer, "%d", &duration) != 1)
			{
				duration = 0;
			}

			printf("- Duration: %s\n", buffer);
			i++;
		}
		else {
			printf("Unexpected key: %.*s\n", tokens[i].end - tokens[i].start,
				json + tokens[i].start);
		}
	}

	switch (type)
	{
	case EMPLOYEE_TYPE_PART_TIME:
		result = CalculatePayroll(NewEmployee(type, hours, rate), &error);
		break;
	case EMPLOYEE_TYPE_FULL_TIME:
		result = CalculatePayroll(NewEmployee(type, hours, rate), &error);
		break;
	case EMPLOYEE_TYPE_SEASONAL:
		result = CalculateSeasonalPayroll(NewSeasonalEmployee(type, hours, rate, pieces), &error);
		break;
	case EMPLOYEE_TYPE_CONTRACT:
		result = CalculateContractPayroll(NewContractEmployee(type, hours, rate, duration), &error);
		break;
	default:
		break;
	}

	if (error == TRUE_L)
	{
		return http_fail();
	}

	sprintf_s(buffer, BUF_SIZE, "{\"TotalPayValue\":\"%.2f\"}\0", result);
	return http_success(buffer, strlen(buffer));
}

int index_of(char* string, int string_len, char symbol, int start)
{
	for (int i = start; i < string_len; ++i)
	{
		if (string[i] == symbol)
		{
			return i;
		}
	}

	return -1;
}

const char* substring(char* string, int string_len, int start, int len)
{
	char* result;
	int size;

	if (string_len <= 0
		|| start <= 0
		|| start > string_len
		|| len < 0)
	{
		return "";
	}

	if (len == 0)
	{
		size = string_len - start;
	}
	else
	{
		size = len;
	}

	if (size <= 0
		|| size > string_len - start)
	{
		return "";
	}

	result = (char*)malloc(size);
	for (int i = 0; i < size; ++i)
	{
		result[i] = string[i + start];
	}

	return result;
}

#endif // !_PROCESS_HELPER_C_