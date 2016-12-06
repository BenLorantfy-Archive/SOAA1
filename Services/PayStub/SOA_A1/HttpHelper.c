#pragma warning(disable : 4996) 

#ifndef _HTTP_HELPER_C_
#define _HTTP_HELPER_C_

#include<Windows.h>

#define HTTP_200 "HTTP/1.1 200 OK\n\r\n\r"
#define HTTP_500 "HTTP/1.1 500 Internal Server Error\n\r\n\r"
#define CONNECTION_CLOSED "Connection : Closed"

#define BUFFER_SIZE 512

#include "HttpHelper.h"

const char* http_success(char* body, int length)
{
	char buffer[BUFFER_SIZE];

	sprintf_s(buffer, BUFFER_SIZE, "%s%.*s\0", HTTP_200, length, body);
	return buffer;
}

const char* http_fail(char* body, int length)
{
	char buffer[BUFFER_SIZE];

	sprintf_s(buffer, BUFFER_SIZE, "%s%.*s\0", HTTP_200, length, body);
	return buffer;
}

#endif // !_HTTP_HELPER_C_