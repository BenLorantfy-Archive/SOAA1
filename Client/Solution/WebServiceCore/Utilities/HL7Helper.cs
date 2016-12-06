using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using WebServiceCore.Models;

namespace WebServiceCore.Utilities
{
    public static class HL7Helper
    {
        #region Constants

        private const char BOM = (char)11;
        private const char EOS = (char)13;
        private const char EOM = (char)28;

        private const string EXEC_SERVICE = "DRC|EXEC-SERVICE|||";
        private const string SRV_HOLD = "SRV||{0}|||||";
        private const string ARG_HOLD = "ARG|{0}|{1}|{2}||{3}|";
        private const string QUERY_SERVICE = "DRC|QUERY-SERVICE|{0}|{1}|";

        private const string PUB_OK = "PUB|OK|";
        private const string RSP = "RSP";

        private const string SRV_QUERY_HOLD = "SRV|{0}||||||";
        private const string SOA_OK = "SOA|OK|";
        private const string MCH = "MCH|";
        private const string SRV = "SRV|";
        private const string ARG = "ARG|";
        private const string MANDATORY = "mandatory";
        private const string OPTIONAL = "optional";

        #endregion

        public static string SendRequest(IWebService service)
        {
            var message = GenerateServiceMessage(service);

            return SendRequest(message, service.IP, service.Port);
        }

        private static string SendRequest(string message, string IP, int port)
        {
            var result = new StringBuilder();
            using (var client = new TcpClient(IP, port))
            {
                using (var stream = client.GetStream())
                using (var writer = new StreamWriter(stream))
                using (var reader = new StreamReader(stream))
                {
                    writer.AutoFlush = true;

                    // Sends request
                    foreach (var line in message.Split('\n'))
                    {
                        writer.WriteLine(line);
                    }

                    // Read the response from server
                    do
                    {
                        var line = reader.ReadLine();
                        if (line != null
                            && line.Contains(EOM))
                        {
                            result.Append(line.Substring(0, line.IndexOf(EOM) + 1) + "\r");
                            break;
                        }

                        result.Append(line + "\r");
                    } while (true);
                }
            }

            return result.ToString();
        }

        public static string GenerateQueryService(string teamName, string teamId, string registryIP, string port, string serviceTag)
        {
            var message = new StringBuilder();

            message.Append(BOM + string.Format(QUERY_SERVICE, teamName, teamId) + EOS);
            message.Append(string.Format(SRV_QUERY_HOLD, serviceTag) + EOS + EOM);

            return SendRequest(message.ToString(), registryIP, Convert.ToInt32(port));
        }

        public static IWebService AnalyzeQueryResponse(string message, out string error)
        {
            var lines = message.Split('\r');
            var parameters = new List<IMethodParameter>();

            string serviceName = null;
            string serviceIP = null;
            int servicePort = 0;

            int count = 0;
            foreach (var line in lines)
            {
                count++;
                if (line.StartsWith(EOM.ToString()))
                {
                    break;
                }

                if (count == 1)
                {
                    if (!line.StartsWith(BOM.ToString())
                        || !line.Contains(SOA_OK))
                    {
                        error = "Wrong header [" + line + "]";
                        return null;
                    }

                    continue;
                }

                if (line.StartsWith(ARG))
                {
                    var blocks = line.Split('|');

                    string parameterName = null;
                    string parameterType = null;
                    bool required = false;

                    int i = 0;
                    foreach (var block in blocks)
                    {
                        switch (i++)
                        {
                            case 2:
                                parameterName = block.Trim();
                                break;
                            case 3:
                                parameterType = block.Trim();
                                break;
                            case 4:
                                var value = block.Trim();
                                if (value.Equals(MANDATORY))
                                {
                                    required = true;
                                    break;
                                }

                                if (value.Equals(OPTIONAL))
                                {
                                    required = false;
                                    break;
                                }

                                try
                                {
                                    required = Convert.ToBoolean(value);
                                }
                                catch (Exception e)
                                {
                                    error = "Unknown mandatory/optional value [" + value + "]";
                                    return null;
                                }
                                break;
                        }
                    }

                    parameters.Add(new MethodParameter(parameterName, parameterType, required));
                }
                else if (line.StartsWith(SRV))
                {
                    var blocks = line.Split('|');

                    int i = 0;
                    foreach (var block in blocks)
                    {
                        switch (i++)
                        {
                            case 2:
                                serviceName = block.Trim();
                                break;
                        }
                    }
                }
                else if (line.StartsWith(RSP))
                {
                    // Do nothing here
                }
                else if (line.StartsWith(MCH))
                {
                    var blocks = line.Split('|');

                    int i = 0;
                    foreach (var block in blocks)
                    {
                        switch (i++)
                        {
                            case 1:
                                serviceIP = block.Trim();
                                break;
                            case 2:
                                var value = block.Trim();
                                if (!Int32.TryParse(value, out servicePort))
                                {
                                    error = "Wrong service port value [" + value + "]";
                                    return null;
                                }

                                break;
                        }
                    }
                }
                else
                {
                    error = "Unknown line start [" + line + "]";
                }
            }

            error = string.Empty;
            return new WebService(serviceIP, servicePort, serviceName, parameters);
        }

        public static string GenerateServiceMessage(IWebService service)
        {
            var message = new StringBuilder();

            message.Append(BOM + EXEC_SERVICE + EOS);
            message.Append(string.Format(SRV_HOLD, service.Name) + EOS);

            int i = 0;
            foreach (var parameter in service.Parameters)
            {
                message.Append(string.Format(ARG_HOLD, i++, parameter.Name, parameter.Type, parameter.Value) + EOS);
            }

            message.Append(EOM);

            return message.ToString();
        }

        public static ResponseHolder AnalyzeResponse(string message, out string error)
        {
            var lines = message.Split('\r');
            var parameters = new List<IMethodParameter>();

            int count = 0;
            foreach (var line in lines)
            {
                if (line.StartsWith(EOM.ToString()))
                {
                    break;
                }

                if (count++ == 0)
                {
                    if (!line.StartsWith(BOM.ToString())
                        || !line.Contains(PUB_OK))
                    {
                        error = "Wrong header [" + line + "]";
                        return null;
                    }

                    continue;
                }
                
                string name = string.Empty;
                string type = string.Empty;
                string value = string.Empty;

                int i = 0;
                var blocks = line.Split('|');
                foreach (var block in blocks)
                {
                    var val = block.Trim();
                    switch (i++)
                    {
                        case 0:
                            if (!val.StartsWith(RSP))
                            {
                                error = "No 'RSP' at the start of [" + val + "]";
                                return null;
                            }

                            break;
                        case 2:
                            name = val;
                            break;
                        case 3:
                            type = val;
                            break;
                        case 4:
                            value = val;
                            break;
                    }
                }

                parameters.Add(new MethodParameter(name, type, value));
            }

            error = string.Empty;
            return new ResponseHolder("Response", parameters);
        }
    }
}
