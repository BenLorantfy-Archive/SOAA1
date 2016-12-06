using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using WebServiceCore.Models;
using WebServiceCore.Utilities;
using Xunit;

namespace Tests.UnitTests
{
    public class HL7Tests : UnitTestBase
    {
        #region Constants

        private static readonly string IP = "192.168.238.1";
        private static readonly IWebService SERVICE_1 = 
            new WebService(IP,
                           2016,
                           "PayStubCalculator",
                           new List<IMethodParameter>()
                           {
                               new MethodParameter("type", "int", "1"),
                               new MethodParameter("hours", "string", "40"),
                               new MethodParameter("rate", "float", "14.5"),
                           });

        #endregion

        [Fact]
        public void Service_1_Message_Success()
        {
            var test = HL7Helper.GenerateServiceMessage(SERVICE_1);

            Assert.Equal(test, "");
        }
    }
}
