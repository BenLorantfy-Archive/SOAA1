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
        private const string SRV_HOLD = "SRV|{0}|||";
        private const string ARG_HOLD = "ARG|{0}|{1}|{2}||{3}|";

        #endregion

        public static string SendRequest(IWebService service)
        {
            var message = GenerateServiceMessage(service);

            var result = new StringBuilder();
            using (var client = new TcpClient(service.IP, service.Port))
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
                            result.Append(line.Substring(0, line.IndexOf(EOM)));
                            break;
                        }

                        result.Append(line);
                    } while (true);
                }
            }

            return result.ToString();
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
    }
}
