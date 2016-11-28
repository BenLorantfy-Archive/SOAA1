// --------------------------------------------------------------------------------------------------------------------
// <copyright file="SoapHelper.cs" company="none">
//    Copyright by Grigory Kozyrev
// </copyright>
// <summary>
//    Project: SOA_Assignment2
//    Author: Grigory Kozyrev
//    Date: 27/09/2016
//    Last Updated: 30/09/2016
// </summary>
// --------------------------------------------------------------------------------------------------------------------

#region Using

using System;
using System.Text;
using WebServiceCore.Models;

#endregion

namespace WebServiceCore.Utilities
{
    /// <summary>
    /// Helper class for SOAP messages
    /// </summary>
    public static class SoapHelper
    {
        #region Constants

        private const string HEADER_TEMPLATE =
            "POST {0} HTTP/1.1\nHost: {1}\nContent-Type: text/xml; charset=utf-8\nContent-Length: {2}";

        private const string MESSAGE_TEMPLATE =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<soap:Envelope xmlns:xsi=\"{0}\" xmlns:xsd=\"{1}\" xmlns:soap=\"{2}\">\n<soap:Body>\n{3}\n</soap:Body>\n</soap:Envelope>";

        private const string BODY_TEMPLATE = "<{0} xmlns=\"{1}\">\n{2}\n</{0}>";

        private const string PARAMETER_TEMPLATE = "<{0}>{1}</{0}>";

        private const string XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
        private const string XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
        private const string SOAP_SCHEMA = "http://schemas.xmlsoap.org/soap/envelope/";

        #endregion

        #region External Members

        /// <summary>
        /// Generates the SOAP message.
        /// </summary>
        /// <param name="service">The service.</param>
        /// <param name="method">The method.</param>
        /// <returns><see cref="Tuple"/> with header as a first parameter and SOAP envelop as the second one.</returns>
        public static Tuple<string, string> GenerateSOAP(IWebService service, IWebMethod method)
        {
            return GenerateSOAP(service.Host, service.Url, method);
        }

        /// <summary>
        /// Generates the SOAP message.
        /// </summary>
        /// <param name="host">The host address.</param>
        /// <param name="serviceUrl">The service URL.</param>
        /// <param name="method">The method.</param>
        /// <returns><see cref="Tuple"/> with header as a first parameter and SOAP envelop as the second one.</returns>
        public static Tuple<string, string> GenerateSOAP(string host, string serviceUrl, IWebMethod method)
        {
            var body = GenerateBody(method);

            return new Tuple<string, string>(GenerateHeader(host, serviceUrl, body.Length),
                body.Replace("\r\n", "\n"));
        }

        #endregion

        #region Private Members

        /// <summary>
        /// Generates the HTTP header.
        /// </summary>
        /// <param name="host">The host address.</param>
        /// <param name="serviceUrl">The service URL.</param>
        /// <param name="length">The body length.</param>
        /// <returns>Generates HTTP header for SOAP message.</returns>
        private static string GenerateHeader(string host, string serviceUrl, int length)
        {
            return string.Format(HEADER_TEMPLATE, serviceUrl, host, length);
        }

        /// <summary>
        /// Generates the message body.
        /// </summary>
        /// <param name="method">The web method.</param>
        /// <returns>SOAP message body.</returns>
        private static string GenerateBody(IWebMethod method)
        {
            var body = new StringBuilder();

            foreach (var parameter in method.Parameters)
            {
                body.AppendFormat(PARAMETER_TEMPLATE, parameter.Name, parameter.Value);
            }

            return string.Format(MESSAGE_TEMPLATE,
                    XML_SCHEMA_INSTANCE,
                    XML_SCHEMA,
                    SOAP_SCHEMA,
                    string.Format(BODY_TEMPLATE,
                        method.Name,
                        method.XMLNS,
                        body))
                .Replace("\n", "\r\n");
        }

        #endregion
    }
}