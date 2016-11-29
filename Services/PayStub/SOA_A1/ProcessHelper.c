#ifndef _PROCESS_HELPER_C_
#define _PROCESS_HELPER_C_

#include "ProcessHelper.h"
#include "Employee.h"
#include "ContractEmployee.h"
#include "SeasonalEmployee.h"
#include "jsmn.h"

#include <stdio.h>
#include <stdlib.h>
#include <Windows.h>

int index_of(char* string, int string_len, char symbol, int start);
const char* substring(char* string, int string_len, int start, int len);

static int dump(const char *js, jsmntok_t *t, size_t count, int indent) {
	int i, j, k;
	if (count == 0) {
		return 0;
	}
	if (t->type == JSMN_PRIMITIVE) {
		printf("%.*s", t->end - t->start, js + t->start);
		return 1;
	}
	else if (t->type == JSMN_STRING) {
		printf("'%.*s'", t->end - t->start, js + t->start);
		return 1;
	}
	else if (t->type == JSMN_OBJECT) {
		printf("\n");
		j = 0;
		for (i = 0; i < t->size; i++) {
			for (k = 0; k < indent; k++) printf("  ");
			j += dump(js, t + 1 + j, count - j, indent + 1);
			printf(": ");
			j += dump(js, t + 1 + j, count - j, indent + 1);
			printf("\n");
		}
		return j + 1;
	}
	else if (t->type == JSMN_ARRAY) {
		j = 0;
		printf("\n");
		for (i = 0; i < t->size; i++) {
			for (k = 0; k < indent - 1; k++) printf("  ");
			printf("   - ");
			j += dump(js, t + 1 + j, count - j, indent + 1);
			printf("\n");
		}
		return j + 1;
	}
	return 0;
}

const char* process_message(char* message, int message_len)
{
	char* buffer = "";
	int start_index = 0;
	int end_index = 0;
	int int_buffer = 0;
	char * json = "{\"type\":\"1\",\"hours\":\"40\",\"rate\":\"14.5\",\"pieces\":null,\"duration\":null}";

	int type = -1;
	float hours = -1;
	float rate = -1;
	int pieces = -1;
	int duration = -1;
	jsmn_parser parser;
	jsmntok_t tokens[20];

	/*start_index = index_of(message, message_len, '{', 0);

	buffer = substring(message, message_len, start_index, 0);*/

	jsmn_init(&parser);

	// js - pointer to JSON string
	// tokens - an array of tokens available
	// 10 - number of tokens available
	jsmn_parse(&parser, json, strlen(json), tokens, 20);

	dump(json, tokens, parser.toknext, 0);

	int_buffer = 0;

	do
	{
		int_buffer = index_of(message, message_len, ':', start_index + 1);
		if (int_buffer < 0)
		{
			break;
		}


	} while (start_index > 0);

	return buffer;
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