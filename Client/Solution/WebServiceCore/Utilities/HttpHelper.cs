// --------------------------------------------------------------------------------------------------------------------
// <copyright file="HttpHelper.cs" company="none">
//    Copyright by Grigory Kozyrev
// </copyright>
// <summary>
//    Project: SOA_Assignment2
//    Author: Grigory Kozyrev
//    Date: 28/09/2016
//    Last Updated: 30/09/2016
// </summary>
// --------------------------------------------------------------------------------------------------------------------

#region Using

using System;
using System.IO;
using System.Net.Sockets;
using WebServiceCore.Models;

#endregion

namespace WebServiceCore.Utilities
{
    /// <summary>
    /// Helper class for HTTP communication
    /// </summary>
    public static class HttpHelper
    {
        #region Constants

        private const string CONNECTION_CLOSED = "Connection: close";

        private const string CONTENT_LENGTH = "Content-Length:";

        private const string HTTP_200 = "HTTP/1.1 200 OK";

        #endregion

        public static string SendHttpRequest(IWebService service, IWebMethod method, int port = 80)
        {
            var soap = SoapHelper.GenerateSOAP(service, method);

            return SendHttpRequest(service.Host, soap.Item1, soap.Item2, port);
        }

        /// <summary>
        /// Sends the HTTP request and returns response string.
        /// </summary>
        /// <param name="host">The host address.</param>
        /// <param name="header">The request header.</param>
        /// <param name="body">The request body.</param>
        /// <param name="port">The host port.</param>
        /// <returns>Server response or null if no respond received.</returns>
        public static string SendHttpRequest(string host, string header, string body, int port = 80)
        {
            return SendHttpRequest(host, string.Join("\n\n", header, body), port);
        }

        /// <summary>
        /// Sends the HTTP request and returns response string.
        /// </summary>
        /// <param name="host">The host address.</param>
        /// <param name="request">The request header.</param>
        /// <param name="port">The host port.</param>
        /// <returns>Server response or null if no respond received.</returns>
        public static string SendHttpRequest(string host, string request, int port = 80)
        {
            string result = null;
            using (var client = new TcpClient(host, port))
            {
                using (var stream = client.GetStream())
                using (var writer = new StreamWriter(stream))
                using (var reader = new StreamReader(stream))
                {
                    writer.AutoFlush = true;

                    // Sends request
                    foreach (var line in request.Split('\n'))
                    {
                        writer.WriteLine(line);
                    }

                    writer.WriteLine(CONNECTION_CLOSED);
                    writer.WriteLine();

                    // Read the response from server
                    result = reader.ReadToEnd();
                }
            }

            return result;
        }

        public static bool ValidateHeader(string message, out string output)
        {
            output = string.Empty;

            if (!message.StartsWith(HTTP_200))
            {
                int indexOfNewLine = message.IndexOf("\n");
                if (indexOfNewLine == -1)
                {
                    output = message;
                }
                else
                {
                    output = message.Substring(0, indexOfNewLine);
                }

                return false;
            }

            return true;
        }

        public static string ExtractSOAP(string message)
        {
            int contentLengthIndex = message.IndexOf(CONTENT_LENGTH, StringComparison.OrdinalIgnoreCase);
            if (contentLengthIndex == -1)
            {
                return null;
            }

            message = message.Substring(contentLengthIndex + CONTENT_LENGTH.Length);

            int newLineIndex = message.IndexOf('\n');
            if (newLineIndex == -1 ||
                newLineIndex > 20)
            {
                return null;
            }

            int contentLength = 0;
            if (!int.TryParse(message.Substring(0, newLineIndex), out contentLength))
            {
                return null;
            }

            var xmlMessage = message.Substring(newLineIndex, contentLength + 5)
                .Replace("&lt;", "<")
                .Replace("&gt;", ">");
            int lastTagCloseIndex = xmlMessage.LastIndexOf('>');
            if (lastTagCloseIndex == -1)
            {
                return null;
            }

            xmlMessage = xmlMessage.Substring(0, lastTagCloseIndex + 1).TrimStart(new char[] {' ', '\n', '\r', '\t'});

            return xmlMessage;
        }
    }
}