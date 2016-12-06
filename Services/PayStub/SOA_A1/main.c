#pragma warning(disable : 4996) 
#pragma comment(lib,"ws2_32.lib") //Winsock Library

#ifndef _MAIN_C_
#define _MAIN_C_

#undef UNICODE

#define WIN32_LEAN_AND_MEAN

#include "ProcessHelper.h"
#include "HttpHelper.h"

#include <windows.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <signal.h>
#include <stdlib.h>
#include <stdio.h>
#include <conio.h>
#include <signal.h>  
#include <tchar.h>  

#define DEFAULT_BUFLEN 2048
#define DEFAULT_PORT "555"

int run = TRUE;
SOCKET ClientSocket = INVALID_SOCKET;

/// <summary>
/// Console signal handler
/// </summary>
/// <param name="signal">The signal.</param>
/// <returns>TRUE if successful, anything else otherwise</returns>
BOOL WINAPI consoleHandler(DWORD signal) {
	int res = -1;

	if (signal == CTRL_C_EVENT)
	{
		printf("Ctrl-C handled\n"); // do cleanup
		run = FALSE;

		// shutdown the connection since we're done
		res = shutdown(ClientSocket, SD_SEND);
		if (res == SOCKET_ERROR) {
			printf("shutdown failed with error: %d\n", WSAGetLastError());
			closesocket(ClientSocket);
			WSACleanup();
			return 1;
		}

		// cleanup
		closesocket(ClientSocket);
	}

	return TRUE;
}

//#define _TEST_

#ifdef _TEST_

int main(void)
{
	return 0;
}

#else

int __cdecl main(void)
{
	WSADATA wsaData;
	int iResult;

	SOCKET ListenSocket = INVALID_SOCKET;

	struct addrinfo *result = NULL;
	struct addrinfo hints;

	int iSendResult;
	char recvbuf[DEFAULT_BUFLEN];
	int recvbuflen = DEFAULT_BUFLEN;

	char* success = http_success("\0", 0);
	int success_len = strlen(success);

	char* buffer;

	// Set signal handler
	if (!SetConsoleCtrlHandler(consoleHandler, TRUE)) {
		printf("\nERROR: Could not set control handler");
		return 1;
	}

	// Initialize Winsock
	iResult = WSAStartup(MAKEWORD(2, 2), &wsaData);
	if (iResult != 0) {
		printf("WSAStartup failed with error: %d\n", iResult);
		return 1;
	}

	ZeroMemory(&hints, sizeof(hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_protocol = IPPROTO_TCP;
	hints.ai_flags = AI_PASSIVE;

	do
	{
		// Resolve the server address and port
		printf("Resolving server address and port\n");
		iResult = getaddrinfo(NULL, DEFAULT_PORT, &hints, &result);
		if (iResult != 0) {
			printf("getaddrinfo failed with error: %d\n", iResult);
			WSACleanup();
			return 1;
		}

		// Create a SOCKET for connecting to server
		printf("Creating socket for connecting to server\n");
		ListenSocket = socket(result->ai_family, result->ai_socktype, result->ai_protocol);
		if (ListenSocket == INVALID_SOCKET) {
			printf("socket failed with error: %ld\n", WSAGetLastError());
			freeaddrinfo(result);
			WSACleanup();
			return 1;
		}

		// Setup the TCP listening socket
		printf("Setting up TCP listening socket\n");
		iResult = bind(ListenSocket, result->ai_addr, (int)result->ai_addrlen);
		if (iResult == SOCKET_ERROR) {
			printf("bind failed with error: %d\n", WSAGetLastError());
			freeaddrinfo(result);
			closesocket(ListenSocket);
			WSACleanup();
			return 1;
		}

		freeaddrinfo(result);

		printf("Listening to the socket\n");
		iResult = listen(ListenSocket, SOMAXCONN);
		if (iResult == SOCKET_ERROR) {
			printf("listen failed with error: %d\n", WSAGetLastError());
			closesocket(ListenSocket);
			WSACleanup();
			return 1;
		}

		// Accept a client socket
		printf("Accepting client socket\n");
		ClientSocket = accept(ListenSocket, NULL, NULL);
		if (ClientSocket == INVALID_SOCKET) {
			printf("accept failed with error: %d\n", WSAGetLastError());
			closesocket(ListenSocket);
			WSACleanup();
			return 1;
		}

		// No longer need server socket
		printf("Got client socket\n");
		closesocket(ListenSocket);

		// Receive until the peer shuts down the connection
		for (int i = 0; i < DEFAULT_BUFLEN; ++i)
		{
			recvbuf[i] = 0;
		}

		iResult = recv(ClientSocket, recvbuf, recvbuflen, 0);
		if (iResult > 0) {
			printf("Bytes received: %d\n", iResult);
			printf("Received message: %s\n", recvbuf);

			buffer = process_message(recvbuf, strlen(recvbuf) + 1);

			// Echo the buffer back to the sender
			iSendResult = send(ClientSocket, buffer, strlen(buffer), 0);
			if (iSendResult == SOCKET_ERROR) {
				printf("send failed with error: %d\n", WSAGetLastError());
				closesocket(ClientSocket);
				WSACleanup();
				return 1;
			}

			printf("Bytes sent: %d\n", success_len);
			printf("Message sent: %s\n", buffer);
		}
		else if (iResult == 0)
			printf("Connection closing...\n");
		else {
			printf("recv failed with error: %d\n", WSAGetLastError());
			closesocket(ClientSocket);
			WSACleanup();
			return 1;
		}

		// shutdown the connection since we're done
		iResult = shutdown(ClientSocket, SD_SEND);
		if (iResult == SOCKET_ERROR) {
			printf("shutdown failed with error: %d\n", WSAGetLastError());
			closesocket(ClientSocket);
			WSACleanup();
			return 1;
		}

		// cleanup
		closesocket(ClientSocket);
	} while (run);

	WSACleanup();
	return 0;
}

#endif // _TEST_

#endif