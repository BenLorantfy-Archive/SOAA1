#pragma warning(disable : 4996) 

#ifndef _HTTP_HELPER_C_
#define _HTTP_HELPER_C_

#include "HttpHelper.h"

const char* http_success()
{
	return "HTTP/1.1 200 OK\n\rContent - Length : 0\n\rConnection : Closed";
}

#endif // !_HTTP_HELPER_C_