#pragma warning(disable : 4996) 

#ifndef _HTTP_HELPER_C_
#define _HTTP_HELPER_C_

//#include "HttpHelper.h"
#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <io.h>
#include <sys/types.h>
#include <WinSock2.h>
#include <stdint.h>

#define MAXMSG  512

int read_from_client1(int filedes)
{
	char buffer[MAXMSG];
	int nbytes;

	nbytes = read(filedes, buffer, MAXMSG);
	if (nbytes < 0)
	{
		/* Read error. */
		perror("read");
		exit(EXIT_FAILURE);
	}
	else if (nbytes == 0)
		/* End-of-file. */
		return -1;
	else
	{
		/* Data read. */
		fprintf(stderr, "Server: got message: `%s'\n", buffer);
		return 0;
	}
}

#endif // !_HTTP_HELPER_C_